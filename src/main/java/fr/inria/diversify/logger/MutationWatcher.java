package fr.inria.diversify.logger;

import java.util.List;

/**
 * Created by guerin on 06/04/16.
 */
public class MutationWatcher {

    public static List<String> coverageTests;

    public static String currentTest;


    public static void setCurrentTest(String s){
        currentTest=s;
    }

    public static void coverage(String position){
        System.out.print("");
    }
}
