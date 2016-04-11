package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.mojo.executedTests.xmlParser.XmlParserInstru;
import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.transformation.DiversiTypeTransformation;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import spoon.processing.Processor;
import spoon.reflect.code.CtConstructorCall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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


            //stockage des données.



            //restoration
            transformation.restore();
        }

    }

    private void instrumentalizeTestSuite() {
        //TODO instrumentalizeTestSUite
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

    public String getWatcherBody() {
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
                +"fileWriter=new FileWriter("+InitUtils.getTmpDirectory()+"/resultTestCaseTransfo.txt);\n"
                +"}\n"
                +"fileWriter.write(currentTest+\":\"+position,true);\n"
                +"fileWriter.flush();\n"
                +"} catch (IOException e) {\n"
                +"e.printStackTrace();\n"
                +"}}}";


        return body;
    }
}
