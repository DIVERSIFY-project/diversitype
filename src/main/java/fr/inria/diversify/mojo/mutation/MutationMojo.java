package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.mojo.executedTests.xmlParser.XmlParserInstru;
import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.transformation.DiversiTypeTransformation;
import fr.inria.diversify.processor.TestWatcherProcessor;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
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
 * Goal which inject diversity
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
     * expression=${mutation.nchange}}
     * defaut-value=1
     * @throws MojoExecutionException
     */
    private int nChange;

    /**
     * @parameter
     *  expression="${mutation.project}"
     *  default-value="/home/guerin/Documents/INRIA/ExProj/ProjA/"
     *  @throws MojoExecutionException
     */
    private String projectDirectory;

    /**
     *@parameter
     *  expression="${mutation.interfaces}"
     *  default-value="java.util.List"
     * @throws MojoExecutionException
     */
    private String interfaces;

    /**
     * this attribute contains failed test before mutation
     */
    private List<String> testFailMainProject;

    private String tmpDir;

    private List<CtConstructorCall> selectedCandidates;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        testFailMainProject=UtilsTestProcessorImpl.getTestSuiteFail();
        getLog().info("for principal project: "+testFailMainProject+" failed !");

        addWatcherClass();

        instrumentalizeTestSuite();

        doMutation();
    }


    private void doMutation() {

        selectedCandidates=UtilsProcessorImpl.getSelectedCandidates(nChange);


        for(int i=0;i<selectedCandidates.size();i++){


            getLog().info("Treat the candadiate!; "+selectedCandidates.get(i));
            ChangeConcreteTypeStrategy strategy=getStrategy(selectedCandidates.get(i));
            getLog().info("Strategy Selected: " + strategy);

            DiversiTypeTransformation transformation=new DiversiTypeTransformation(selectedCandidates.get(i),InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory(),getStrategy(selectedCandidates.get(i)));
            transformation.apply();
            getLog().info("write transformation " + InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory());

            getLog().info("run test to " + InitUtils.getTmpDirectory());
            UtilsTestProcessorImpl.runTest(InitUtils.getTmpDirectory());

            //recuperation des résultats
            UtilsTestProcessorImpl.cleanTestFailMutation();
            getLog().info("analyse test result");
            XmlParserInstru.start(InitUtils.getTmpDirectory(),false);
            getLog().info("for mutation project: "+UtilsTestProcessorImpl.getTestSuiteFailCurrentT()+" failed !");


            //recupération de la couverture
            HashMap<String,List<String>> hashMap=analyseCoverageResult(InitUtils.getOutput());

            //compare coverage and testFailed
            List<String> list=compareResults(hashMap,UtilsTestProcessorImpl.getTestSuiteFailCurrentT(),selectedCandidates.get(i));


            //restoration
            transformation.restore();
        }

    }

    private List<String> compareResults(HashMap<String, List<String>> hashMap, List<String> testSuiteFailCurrentT, CtConstructorCall ctConstructorCall) {
        List<String> coverageTests=hashMap.get(ctConstructorCall.getPosition().toString());

        for(int i=0;i<coverageTests.size();i++){
            if(!testSuiteFailCurrentT.contains(coverageTests.get(i))){
                coverageTests.remove(coverageTests.get(i));
            }
        }

        return coverageTests;
    }

    private HashMap<String,List<String>> analyseCoverageResult(String output) {
        HashMap<String,List<String>> results=new HashMap<>();
        try {


            FileReader file=new FileReader(""+InitUtils.getOutput()+"resultTestCaseTransfo.txt");
            BufferedReader bufferedReader=new BufferedReader(file);

           String current= bufferedReader.readLine();
            while(current!=null){
                String[] tab=current.split("::::");
                if(results.containsKey(tab[1])){
                    List<String> tests=results.get(tab[1]);
                    tests.add(tab[0]);
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
            e.printStackTrace();
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
     * @param ctConstructorCall
     * @return
     */
    public ChangeConcreteTypeStrategy getStrategy(CtConstructorCall ctConstructorCall) {

        return new OneConcreteTypeStrategy("java.util.LinkedList");
    }


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
                +"fileWriter.write(currentTest+\"::::\"+position);\n"
                +"fileWriter.flush();\n"
                +"} catch (IOException e) {\n"
                +"e.printStackTrace();\n"
                +"}}}";


        return body;
    }
}
