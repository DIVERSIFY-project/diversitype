package fr.inria.diversify.utils;

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


    public static void addTestFail(String testSuiteCurrent, String testCaseCurrent, String failure, String data) {
        if(!testSuiteFail.contains(testSuiteCurrent)){
            testSuiteFail.add(testSuiteCurrent);
        }
        //System.out.println(testSuiteCurrent+" testCase: "+testCaseCurrent+" failure: "+failure+" data: "+data);
    }

    public static List<String> getTestSuiteFail() {
        return testSuiteFail;
    }

    public static void clean() {
        testSuiteFail=new ArrayList<>();

    }
}
