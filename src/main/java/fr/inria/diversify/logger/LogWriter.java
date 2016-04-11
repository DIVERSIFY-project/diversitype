package fr.inria.diversify.logger;

import fr.inria.diversify.utils.InitUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


public class LogWriter {

    private static PrintWriter fileWriter;
    private static List<CtClass> classList = new ArrayList();
    private static HashMap<String, Integer> newList = new HashMap<String, Integer>();
    //private static HashMap<String,HashMap<String,Integer>> statisticList=new HashMap<String,HashMap<String,Integer>>();
    private static HashMap<String, HashMap<String, HashMap<String, Integer>>> statisticList = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
    private static List<String> notPossibilities = new ArrayList<>();
    private static List<String> testClasses = new ArrayList<>();
    private static int nbtestCases = 0;
    private static HashMap<String, Integer> typeArgumentList = new HashMap<>();
    private static int nbConstructorCall = 0;


    public static void writeLog() {

        fileWriter.close();
    }

    public static void out(String string, boolean error) {
        try {
            PrintWriter writer = getWriter();

            if (error) {
                writer.write("ERROR: ");
            } else {
                writer.write("INFO: ");
            }
            writer.write(string + "\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void out(String string) {
        try {
            PrintWriter writer = getWriter();


            writer.write(string + "\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected static PrintWriter getWriter() throws FileNotFoundException {
        if (fileWriter == null) {
            ShutdownHookLog shutdownHook = new ShutdownHookLog();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            fileWriter = new PrintWriter("logTransformation.csv");

        }
        return fileWriter;
    }


    public static void addClass(CtClass simpleName) {

        if (!classList.contains(simpleName)) {
            classList.add(simpleName);
        }
    }

    /**************
     * For NbClassProcessor
     **********/

    public static List<CtClass> getClassList() {
        return classList;
    }

    public static void addCandidateList(String simpleName) {
        if (newList.containsKey(simpleName)) {
            newList.put(simpleName, newList.get(simpleName) + 1);
        } else {
            newList.put(simpleName, 1);
        }
    }

    public static void printCandidateList() throws FileNotFoundException {
        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        fileWriter = new PrintWriter("candidateList.txt");

        Set<String> keys = newList.keySet();
        Iterator<String> it = keys.iterator();

        while (it.hasNext()) {
            String current = it.next();
            fileWriter.write(current + ": " + newList.get(current) + "\n");
        }


    }

    /***************
     * For statisticsListProcessor
     ********************/

    public static void printStatisticList() throws FileNotFoundException {
        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        fileWriter = new PrintWriter(InitUtils.getOutput()+"listStatistics.txt");

        Set<String> keys = statisticList.keySet();
        Iterator<String> it = keys.iterator();


        int nbpossibilities = 0;
        List<String> completelistClasses = new ArrayList<>();
        HashMap<String, Integer> completelistConstructor = new HashMap<>();

        List<String> currentlistClasses = new ArrayList<>();
        HashMap<String, Integer> currentlistConstructor = new HashMap<>();

        while (it.hasNext()) {
            String currentStaticType = it.next();
            fileWriter.write("\n-------- For " + currentStaticType + " -------\n");
            currentlistClasses.clear();
            currentlistConstructor.clear();

            if (notPossibilities.contains(currentStaticType)) {
                fileWriter.write("\nthe type " + currentStaticType + " have only one subtype. The transformation is not possible.\n\n");
            }

            HashMap<String, HashMap<String, Integer>> hashclasses = statisticList.get(currentStaticType);

            Iterator<String> itClasses = hashclasses.keySet().iterator();
            while (itClasses.hasNext()) {
                String currentClass = itClasses.next();

                if (!currentlistClasses.contains(currentClass)) {
                    currentlistClasses.add(currentClass);
                }

                if (!notPossibilities.contains(currentStaticType)) {
                    if (!completelistClasses.contains(currentClass)) {
                        completelistClasses.add(currentClass);
                    }
                }
                fileWriter.write(currentClass + ": ");

                HashMap<String, Integer> hashConstructors = hashclasses.get(currentClass);

                Iterator<String> itConstructor = hashConstructors.keySet().iterator();
                while (itConstructor.hasNext()) {
                    String currentConstructor = itConstructor.next();
                    fileWriter.write(hashConstructors.get(currentConstructor) + " " + currentConstructor + ", ");

                    if (!notPossibilities.contains(currentStaticType)) {
                        nbpossibilities += hashConstructors.get(currentConstructor);

                        if (completelistConstructor.containsKey(currentConstructor)) {
                            completelistConstructor.put(currentConstructor, completelistConstructor.get(currentConstructor) + hashConstructors.get(currentConstructor));
                        } else {
                            completelistConstructor.put(currentConstructor, hashConstructors.get(currentConstructor));
                        }
                    }

                    if (currentlistConstructor.containsKey(currentConstructor)) {
                        currentlistConstructor.put(currentConstructor, currentlistConstructor.get(currentConstructor) + hashConstructors.get(currentConstructor));
                    } else {
                        currentlistConstructor.put(currentConstructor, hashConstructors.get(currentConstructor));
                    }


                }
                fileWriter.write("\n");

            }


            fileWriter.write("\n *** stat for " + currentStaticType + " ***\n");
            Iterator<String> itCurrConst = currentlistConstructor.keySet().iterator();
            int nb = 0;
            while (itCurrConst.hasNext()) {
                String current = itCurrConst.next();
                fileWriter.write(current + ": " + currentlistConstructor.get(current) + "\n");
                nb = nb + currentlistConstructor.get(current);
            }
            fileWriter.write("Total: " + nb + "\n");
            fileWriter.write("\nclasses: " + currentlistClasses.size() + "/" + classList.size() + "\n\n");
        }

        fileWriter.write("---- General ----\n");
        fileWriter.write("possibilities: " + nbpossibilities);
        fileWriter.write("\n classes: " + completelistClasses.size() + "/" + classList.size());

        //fileWriter.write("\n\n typeArguements:\n");
        Iterator<String> itTypeA = typeArgumentList.keySet().iterator();
        while (itTypeA.hasNext()) {
            String current = itTypeA.next();
            fileWriter.write(current + ": " + typeArgumentList.get(current) + "\n");
        }

        fileWriter.close();

    }

    public static void addCandidateList(String simpleName, Class staticType, String constructorName) {
        if (statisticList.containsKey(staticType.getName())) {
            HashMap<String, HashMap<String, Integer>> hash1 = statisticList.get(staticType.getName());

            if (hash1.containsKey(simpleName)) {
                HashMap<String, Integer> hash2 = hash1.get(simpleName);

                if (hash2.containsKey(constructorName)) {
                    hash2.put(constructorName, hash2.get(constructorName) + 1);
                } else {
                    hash2.put(constructorName, 1);
                }

                hash1.put(simpleName, hash2);

                System.out.println("ajout: " + staticType.getName());
                statisticList.put(staticType.getName(), hash1);

            } else {


                HashMap<String, Integer> hash = new HashMap<>();
                hash.put(constructorName, 1);

                HashMap<String, HashMap<String, Integer>> hash2 = statisticList.get(staticType.getName());
                hash2.put(simpleName, hash);

                System.out.println("ajout: " + staticType.getName());
                statisticList.put(staticType.getName(), hash2);
            }

        } else {
            HashMap<String, Integer> hash = new HashMap<>();
            hash.put(constructorName, 1);

            HashMap<String, HashMap<String, Integer>> hash2 = new HashMap<>();
            hash2.put(simpleName, hash);

            System.out.println("ajout: " + staticType.getName());
            statisticList.put(staticType.getName(), hash2);
        }
    }

    /**
     * Return the list of classes which contains the staticType instantiate by the concrete type give in parameters
     *
     * @param staticName:  the name of static type
     * @param concretName: the name of concrete type
     * @return
     */
    public static List<String> getStatisticsList(String staticName, String concretName) {
        List<String> stringArrayList = new ArrayList<String>();

        Set<String> keys = statisticList.get(staticName).keySet();
        Iterator<String> it = keys.iterator();

        while (it.hasNext()) {
            String currentClass = it.next();

            HashMap<String, Integer> hashList = statisticList.get(staticName).get(currentClass);
            Set<String> constructors = hashList.keySet();

            if (constructors.contains(concretName)) {
                stringArrayList.add(currentClass);
            }

        }

        return stringArrayList;
    }


    public static List<String> getAllStatisticsList(String s) {
        List<String> stringArrayList = new ArrayList<String>();

        Set<String> keys = statisticList.get(s).keySet();
        Iterator<String> it = keys.iterator();

        while (it.hasNext()) {
            String current = it.next();
            stringArrayList.add(current);
        }
        return stringArrayList;

    }

    public static void initialize(List<String> interfaces) {
        for (int i = 0; i < interfaces.size(); i++) {
            statisticList.put(interfaces.get(i), new HashMap<String, HashMap<String, Integer>>());
        }
    }

    public static void isNotAPossibility(Class current) {


        notPossibilities.add(current.getName());
    }


}