package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.exceptions.NoAlternativesException;
import fr.inria.diversify.learning.UtilsLearning;
import fr.inria.diversify.mojo.executedTests.xmlParser.XmlParserInstru;
import fr.inria.diversify.mojo.mutation.builder.ConstructorCallBuilder;
import fr.inria.diversify.mojo.mutation.builder.ConstructorCallBuilderWithStrategy;
import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.transformation.DiversiTypeTransformation;
import fr.inria.diversify.processor.TestWatcherProcessor;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsReport;
import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import spoon.compiler.Environment;
import spoon.processing.ProcessingManager;
import spoon.processing.Processor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * /**
 *
 *
 * This goal create diversity on the project.
 * It instrumentalize test suite, change the mutation point,
 * execute test suite and analyse results.
 *
 * @goal mutation
 *
 * @phase test
 *
 * @requiresProject True
 *
 * @execute  lifecycle="analysetest" phase="test"
 *
 * Created by guerin on 04/02/16.
 */
public class MutationMojo extends AbstractMojo{

    /**
     * @parameter
     *  property="nchange"
     * defaut-value=1
     * @throws MojoExecutionException
     */
    private int nChange;

    /**
     * @parameter
     *  property="mutation.project"
     *  default-value="/home/guerin/Documents/INRIA/ExProj/unit_test.ProjA/"
     *  @throws MojoExecutionException
     */
    private String projectDirectory;

    /**
     *@parameter
     *   property="mutation.interfaces"
     *  default-value="java.util.List"
     * @throws MojoExecutionException
     */
    private String interfaces;

    /**
     * @parameter
     *  property="jarLocation"
     * @throws MojoExecutionException
     */
    private String jarLocation;

    /**
     * @parameter
     *  property="mutationStrategy"
     *  default-value="random"
     *  @throws MojoExecutionException
     */
    private String mutationStrategy;

    /**
     * @parameter
     *  property="candidateStrategy"
     *  default-value="internal"
     *  @throws MojoExecutionException
     */
    private String candidateStratregy;

    /**
     * this attribute contains failed test before mutation
     */
    private List<String> testFailMainProject;

    private String tmpDir;

    private List<CtConstructorCall> selectedCandidates;

    private PrintWriter printWriter;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        testFailMainProject=UtilsTestProcessorImpl.getTestSuiteFail();
        getLog().info("for principal project: "+testFailMainProject+" failed !");

        getLog().info("instrumentalize test suite");
        addWatcherClass();

        instrumentalizeTestSuite();

        getLog().info("mutation...");
        doMutation();

        deleteTmpDirectory();

        //printOutResult
    }

    private void printHierarchy() {
        UtilsReport.printHierarchy();
    }

    private void deleteTmpDirectory() {
        InitUtils.deleteTmpDirectory();
    }


    private void doMutation() {

        selectedCandidates=UtilsProcessorImpl.getSelectedCandidates(nChange);
        ConstructorCallBuilder constructorCallBuilder=new ConstructorCallBuilderWithStrategy();

        if(selectedCandidates.size()==0){
            getLog().info("For selected interfaces, there aren't mutation point: Interface i= new Impl()");
            addLearningInterfaces(UtilsProcessorImpl.getInterfaces());
        }

        for(int i=0;i<selectedCandidates.size();i++){

            try {
                getLog().info("Treat the candadiate!; " + selectedCandidates.get(i));
                //ChangeConcreteTypeStrategy strategy=getStrategy(selectedCandidates.get(i));
                getLog().info("MutationStrategy Selected: " + InitUtils.getMutationStrategy());

                String staticType = UtilsProcessorImpl.getStaticType(selectedCandidates.get(i)).getName();

                constructorCallBuilder.selElementToTransplant(selectedCandidates.get(i));
                constructorCallBuilder.setStaticType(staticType);
                constructorCallBuilder.setMutationStrategy(InitUtils.getMutationStrategy());
                constructorCallBuilder.setSelectionStrategy(InitUtils.getCandidatesStrategy());

                CtConstructorCall newCtConstructorCall = constructorCallBuilder.findCtConstructorCall();
                getLog().info("new ctConstructorCall: "+newCtConstructorCall);

                DiversiTypeTransformation transformation = new DiversiTypeTransformation(selectedCandidates.get(i), InitUtils.getTmpDirectory() + InitUtils.getSourceDirectory(), newCtConstructorCall);
                transformation.apply();
                getLog().info("write transformation " + InitUtils.getTmpDirectory() + InitUtils.getSourceDirectory());

                getLog().info("run test to " + InitUtils.getTmpDirectory());
                UtilsTestProcessorImpl.runTest(InitUtils.getTmpDirectory());

                //recuperation des résultats de l'execution de tests pour la transfo courante
                UtilsTestProcessorImpl.cleanTestFailMutation();
                getLog().info("analyse test result");
                XmlParserInstru.start(InitUtils.getTmpDirectory(), false);
                getLog().info("for mutation project: " + UtilsTestProcessorImpl.getTestSuiteFailCurrentT() + " failed !");

                getLog().info("analyse results and print report");
                //recupération de la couverture (resutlTestCaseTransfo.txt)
                HashMap<String, List<String>> hashMap = analyseCoverageResult(InitUtils.getOutput());

                //compare coverage and testFailed
                List<String> list = compareResults(hashMap, UtilsTestProcessorImpl.getTestSuiteFailCurrentT(), selectedCandidates.get(i));

                printResultTorCurrentTransfo(staticType, selectedCandidates.get(i), newCtConstructorCall, (hashMap.isEmpty()), list, testFailMainProject);


                //restoration
                getLog().info("project restoration");
                transformation.restore();
            }catch (NoAlternativesException e){
                getLog().info("there are not alternative for the current candidates");
                getLog().info("add "+selectedCandidates.get(i)+" to learning files");
                UtilsLearning.addConstructorCall(selectedCandidates.get(i).getPosition().toString());
            }
        }

        getPrintWriter().close();

    }

    private void addLearningInterfaces(List<String> interfaces) {
        UtilsLearning.addInterface(interfaces);
    }

    private void printResultTorCurrentTransfo(String staticType, CtConstructorCall ctConstructorCall, CtConstructorCall newCtConstructorCall, boolean coverageIsNull, List<String> listTestCurrentT, List<String> testMainProject) {
        PrintWriter out=getPrintWriter();
        String result="For the mutation: static type: "+staticType+", concrete type: "+ctConstructorCall.getType()+", mutation: "+newCtConstructorCall.getType()+"\n"
                +"mutation position: "+ctConstructorCall.getPosition().toString()+"\n" ;
        if(coverageIsNull){
            result=result+"the code coverage is not assured";
        }

        result=result+"\n\n"+getCommonTest(listTestCurrentT,testMainProject)+getImprovement(listTestCurrentT,testMainProject)+getRegression(listTestCurrentT,testMainProject);
        out.write(result);

    }



    private List<String> compareResults(HashMap<String, List<String>> hashMap, List<String> testSuiteFailCurrentT, CtConstructorCall ctConstructorCall) {
        if(hashMap.isEmpty()){//if the constructorCall is a field
            return testSuiteFailCurrentT;
        }else {

            List<String> coverageTests = hashMap.get(ctConstructorCall.getPosition().toString());
            List<String> results=new ArrayList<>();

            for (int i = 0; i < coverageTests.size(); i++) {
                if (testSuiteFailCurrentT.contains(coverageTests.get(i))) {
                    results.add(coverageTests.get(i));
                }
            }

            return results;
        }
    }

    private HashMap<String,List<String>> analyseCoverageResult(String output) {
        HashMap<String,List<String>> results=new HashMap<>();
        try {


            FileReader file=new FileReader(InitUtils.getOutput()+"resultTestCaseTransfo.txt");
            BufferedReader bufferedReader=new BufferedReader(file);

           String current= bufferedReader.readLine();
            while(current!=null){
                String[] tab=current.split("::::");
                if(results.containsKey(tab[1])){
                    List<String> tests=results.get(tab[1]);
                    if(!tests.contains(tab[0])) {
                        tests.add(tab[0]);
                    }
                }else{
                    List<String> list=new ArrayList<>();
                    list.add(tab[0]);
                    results.put(tab[1],list);
                }

                current=bufferedReader.readLine();
            }

            bufferedReader.close();
            file.close();

        } catch (FileNotFoundException e) {
           //Do something here -> the result is not print because the mutation is a field

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return results;
        }

    }

    private void instrumentalizeTestSuite() {
        InitUtils.resolveDepedencies(InitUtils.getTmpDirectory());
        UtilsProcessorImpl.spoonLauncher(InitUtils.getProjectDirectory(),InitUtils.getTmpDirectory()+InitUtils.getTestDirectory(),new TestWatcherProcessor(),true);
    }



    /**
     * TODO treat parameters and switch strategy
     * @param
     * @return
     */
    /*public ChangeConcreteTypeStrategy getStrategy(CtConstructorCall ctConstructorCall) {


        return new OneConcreteTypeStrategy("java.util.LinkedList");
    }*/


    private void addWatcherClass() {
        try {
            createPackage();
            PrintWriter file=new PrintWriter(InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory()+"fr/inria/diversify/diversitype/MutationWatcher.java");
            file.write(getWatcherBody());
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createPackage() {
        if(! new File(InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory()+"fr/inria/diversify/diversitype/").exists()){
            new File(InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory()+"fr/inria/diversify/diversitype/").mkdirs();
        }
    }

    private String getWatcherBody() {
        String body=" package fr.inria.diversify.diversitype;\n\n"
                +"import java.io.FileWriter;\n"
                +"import java.io.IOException;\n\n"
                +"public class MutationWatcher {\n"
                +"public static String currentTest;\n"
                +"public static FileWriter fileWriter;\n\n"
                +"public static void setCurrentTest(String s){\n"
                +"\tcurrentTest=s;"
                +"}\n\n"
                +"public static void setCurrentTransfo(String position){\n"
                +"try {\n"
                +"if(fileWriter==null){\n"
                +"fileWriter=new FileWriter(\""+InitUtils.getOutput()+"resultTestCaseTransfo.txt\");\n"
                +"}\n"
                +"fileWriter.write(currentTest+\"::::\"+position+\"\\n\");\n"
                +"fileWriter.flush();\n"
                +"} catch (IOException e) {\n"
                +"e.printStackTrace();\n"
                +"}}}";


        return body;
    }

    private PrintWriter getPrintWriter() {
        if(printWriter==null){
            try {
                printWriter=new PrintWriter(InitUtils.getOutput()+"diversiType.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return printWriter;
    }

    private String getCommonTest(List<String> listTestCurrentT, List<String> testMainProject) {
        String commonTest="";
        for(int i=0;i<listTestCurrentT.size();i++){
            if(testMainProject.contains(listTestCurrentT.get(i))){
                commonTest=commonTest+listTestCurrentT.get(i)+"\n";
            }
        }
        if(commonTest.equals("")){
            commonTest="there are not common failed test for this mutation";
        }
        commonTest="** Common failed test **\n"+commonTest+"\n";
        return commonTest;
    }

    private String getRegression(List<String> listTestCurrentT, List<String> testMainProject) {
        String regression="";
        for(int i=0;i<listTestCurrentT.size();i++){
            if(!testMainProject.contains(listTestCurrentT.get(i))){
                regression=regression+listTestCurrentT.get(i)+"\n";
            }
        }
        if(regression.equals("")){
            regression="there are not new failed test for this mutation !!";
        }
        regression="** Failed test added by the mutation **\n"+regression+"\n";
        return regression;
    }

    private String getImprovement(List<String> listTestCurrentT, List<String> testMainProject) {
        String improvement="";
        for(int i=0;i<testMainProject.size();i++){
            if(!listTestCurrentT.contains(testMainProject.get(i))){
                improvement=improvement+testMainProject.get(i)+"\n";
            }
        }
        if(improvement.equals("")){
            improvement="there are not improvement with this mutation :(\n";
        }
        improvement="** Failed test resolve by the mutation **\n"+improvement+"\n";
        return improvement;
    }
}
