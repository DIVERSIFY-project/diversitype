
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

    /**
     * Failed test of the main project
     */
    private static List<String> testSuiteFail=new ArrayList<>();

    /**
     * faild test of the mutates project
     */
    private static List<String> testSuiteFailCurrentT=new ArrayList<>();

    /**
     * add failed test for the main project
     * @param testSuiteCurrent
     * @param testCaseCurrent
     * @param failure
     * @param data
     */
    public static void addTestFail(String testSuiteCurrent, String testCaseCurrent, String failure, String data) {
        if(!testSuiteFail.contains(testSuiteCurrent)){
            //TODO bien récuperer les informations necessaire
            testSuiteFail.add(testSuiteCurrent+":"+testCaseCurrent);
        }
        //System.out.println(testSuiteCurrent+" testCase: "+testCaseCurrent+" failure: "+failure+" data: "+data);
    }

    /**
     * return failed test of the main project
     * @return
     */
    public static List<String> getTestSuiteFail() {
        return testSuiteFail;
    }

    /**
     * clean failed test of the main project
     */
    public static void clean() {
        testSuiteFail=new ArrayList<>();

    }

    /**
     * run tests in the given project repository
     * maven goal "clean test" executed
     * @param repo
     */
    public static void runTest(String repo){
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

    /**
     *add failed test for the mutated project
     * @param testSuiteCurrent
     * @param testCaseCurrent
     * @param failure
     * @param data
     */
    public static void addTestFailMutation(String testSuiteCurrent, String testCaseCurrent, String failure, String data) {
        if(!testSuiteFailCurrentT.contains(testSuiteCurrent)){
            testSuiteFailCurrentT.add(testSuiteCurrent+":"+testCaseCurrent);
        }
    }

    /**
     * return failed test for the mutated project
     * @return
     */
    public static List<String> getTestSuiteFailCurrentT() {
        return testSuiteFailCurrentT;
    }

    /**
     * clean failed test for the mutated project
     */
    public static void cleanTestFailMutation(){
        testSuiteFailCurrentT=new ArrayList<>();
    }
}
