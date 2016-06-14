package fr.inria.diversify.learning;

import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.logger.ShutdownHookLog;
import fr.inria.diversify.utils.InitUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manage the learning file for improve the algorithm of selection
 * Created by guerin on 10/06/16.
 */
public class UtilsLearning {

    private static String interfaceLearning="interfacesLearning.txt";

    private static String constructorCallLearning="constructorCallLearning.txt";

    private static PrintWriter interfacefileWriter;

    private static PrintWriter constructorfileWriter;

    private static List<String> interfaces;

    private static List<String> constructorCall;



    public static void addInterface(String interfaceLearning){
        try {
            PrintWriter writer=getInterfaceFileWriter();
            writer.write(interfaceLearning+"\n");

        } catch (FileNotFoundException e) {

        }
    }

    public static void addConstructorCall(String constructoCallLearning){
        try {
            PrintWriter writer=getConstructorFileWriter();
            writer.write(constructoCallLearning+"\n");

        } catch (FileNotFoundException e) {

        }

    }

    public static List<String> readInterfacesLearning() throws IOException {
        if(interfaces==null){
            interfaces=new ArrayList<>();
            FileReader file=new FileReader(InitUtils.getLearningDirectory()+interfaceLearning);
            BufferedReader bufferedReader=new BufferedReader(file);

            String current=bufferedReader.readLine();
            while (current!=null){
                interfaces.add(current);
                current=bufferedReader.readLine();
            }


        }
        return interfaces;
    }

    public static List<String> readConstructorCallLearning() throws IOException {
        if(constructorCall==null){
            constructorCall=new ArrayList<>();
            FileReader file=new FileReader(InitUtils.getLearningDirectory()+constructorCallLearning);
            BufferedReader bufferedReader=new BufferedReader(file);
            String current=bufferedReader.readLine();
            while (current!=null){
                constructorCall.add(current);
                current=bufferedReader.readLine();
            }


        }
        return constructorCall;
    }

    private static PrintWriter getInterfaceFileWriter() throws FileNotFoundException {
        if(interfacefileWriter==null){
            InterfaceShutdownHookLog shutdownHook = new InterfaceShutdownHookLog();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            interfacefileWriter=new PrintWriter(new File(InitUtils.getLearningDirectory()+interfaceLearning));
        }
        return interfacefileWriter;
    }

    private static PrintWriter getConstructorFileWriter() throws FileNotFoundException {
        if(constructorfileWriter==null){
            ConstructorShutdownHookLog shutdownHook = new ConstructorShutdownHookLog();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            constructorfileWriter=new PrintWriter(new File(InitUtils.getLearningDirectory()+constructorCallLearning));
        }
        return constructorfileWriter;
    }

    public static void addInterface(List<String> interfaces) {
        for (int i =0; i<interfaces.size();i++){
            addInterface(interfaces.get(i));
        }
    }

    private static class ConstructorShutdownHookLog extends Thread {

        public void run() {
            constructorfileWriter.close();
        }
    }

    private static class InterfaceShutdownHookLog extends Thread {

        public void run() {
            interfacefileWriter.close();
        }
    }


}
