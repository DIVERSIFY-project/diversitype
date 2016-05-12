package fr.inria.diversify.utils;


import fr.inria.diversify.buildSystem.AbstractBuilder;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class which contains methods for treat test suite
 * Created by guerin on 30/03/16.
 *
 */
public class UtilsTestProcessorImpl {

    private static List<String> testSuiteFail=new ArrayList<>();

    private static List<String> testSuiteFailCurrentT=new ArrayList<>();


    public static void addTestFail(String testSuiteCurrent, String testCaseCurrent, String failure, String data) {
        if(!testSuiteFail.contains(testSuiteCurrent)){
            //TODO bien récuperer les informations necessaire
            testSuiteFail.add(testSuiteCurrent+":"+testCaseCurrent);
        }
        //System.out.println(testSuiteCurrent+" testCase: "+testCaseCurrent+" failure: "+failure+" data: "+data);
    }

    public static List<String> getTestSuiteFail() {
        return testSuiteFail;
    }

    public static void clean() {
        testSuiteFail=new ArrayList<>();

    }


    public static void runTest(String repo){
        //lancement des tests
        AbstractBuilder builder= null;
        try {
            builder = new MavenBuilder(repo);
            builder.setGoals(new String[]{"clean test"});
            builder.initTimeOut();
            builder.runBuilder();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addTestFailMutation(String testSuiteCurrent, String testCaseCurrent, String failure, String data) {
        if(!testSuiteFailCurrentT.contains(testSuiteCurrent)){
            testSuiteFailCurrentT.add(testSuiteCurrent+":"+testCaseCurrent);
        }
    }

    public static List<String> getTestSuiteFailCurrentT() {
        return testSuiteFailCurrentT;
    }

    public static void cleanTestFailMutation(){
        testSuiteFailCurrentT=new ArrayList<>();
    }
}
