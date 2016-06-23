package fr.inria.diversify.utils;

import fr.inria.diversify.exceptions.NotInterfacesUsefullException;
import fr.inria.diversify.learning.UtilsLearning;
import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.processor.StatisticsListProcessor;
import fr.inria.diversify.utils.selectionStrategy.CandidatesSelectStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.ExternalLibraryStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.InternalTypeStrategy;
import org.reflections.Reflections;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.compiler.SpoonCompiler;
import spoon.processing.ProcessingManager;
import spoon.processing.Processor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.DefaultCoreFactory;
import spoon.support.QueueProcessingManager;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.io.*;
import java.util.*;

/**
 *
 *Class which contains methods for treat specific node in the source code
 *(Exclude test suite)
 *
 * Created by guerin on 29/03/16.
 */
public class UtilsProcessorImpl {

    /**
     * possible ConstructorCall for the mutation
     */
    private static List<CtConstructorCall> candidates=new ArrayList<>();

    /**
     * selected constructor call for the mutation
     */
    private static List<CtConstructorCall> selected=new ArrayList<>();

    /**
     * represent link between interfaces and all possibility
     */
    private static HashMap<String,List<String>> hierarchy=new HashMap<>();



    /**
     * represent link between interface and concrete type (lvl 1)
     */
    private static HashMap<String,List<String>> interfaceChildren=new HashMap<>();

    /**
     *represent link between abstract class and concrete type (lvl 1)
     */
    private static List<String> abstractClass=new ArrayList<>();

    /**
     * represent link between class and his children (lvl1)
     */
    private static HashMap<String,List<String>> classChildren=new HashMap<>();




    /**
     * List of static type for the mutation
     */
    private static List<String> interfaces=new ArrayList<>();
    private static boolean hierarchyIsAlreadyLearning=false;


    /**
     * Add possible point for the mutation
     * @param ctConstructorCall
     */
    public static void addCandidate(CtConstructorCall ctConstructorCall){
        candidates.add(ctConstructorCall);
    }

    /**
     * Select mutation point
     * @param n
     * @return
     */
    public static List<CtConstructorCall> getSelectedCandidates(int n){

        compareWithLearning();


        if(n>=candidates.size()) {
            selected = candidates;
            return candidates;
        }

        Random r = new Random();

        for(int i=0;i<n;i++){
            int valeur =r.nextInt(candidates.size());
            selected.add(candidates.get(valeur));
        }
        return selected;
    }

    private static void compareWithLearning() {
        List<String> learning=new ArrayList<>();
        try {
            learning =UtilsLearning.readConstructorCallLearning();
        } catch (IOException e) {

        }

        for(int i=0;i<candidates.size();i++){
            if(learning.contains(candidates.get(i).getPosition().toString())){
                candidates.remove(candidates.get(i));
            }
        }
    }

    /**
     * Launch a processor to the given project Directory
     * if onlyTest equals true, processor is launch only on test source code
     * Else, the processor is launch on source code only
     * @param projectDirectory
     * @param output
     * @param processor
     * @param onlyTest
     * @return
     */
    public static Factory spoonLauncher(String projectDirectory,String output,Processor processor,boolean onlyTest){
        final SpoonAPI spoon = new Launcher();
        if(onlyTest) {
            spoon.addInputResource(projectDirectory + InitUtils.getTestDirectory());
        }else{
            spoon.addInputResource(projectDirectory + InitUtils.getSourceDirectory());
        }
        spoon.setSourceOutputDirectory(output);
        spoon.addProcessor(processor);
        spoon.run();
        return spoon.getFactory();
    }

    /**
     *For un constructor call, given in parameter, return its static type
     * @param candidate
     * @return
     */
    public static Class getStaticType(CtConstructorCall candidate){

        CtTypeReference staticTypeTmp=null;
        CtElement parent=candidate.getParent();
        try{
            candidate.getType().getActualClass();

            if(parent instanceof CtLocalVariableImpl) {
                staticTypeTmp= ((CtLocalVariableImpl) parent).getType();
            }else if(parent instanceof CtAssignment){
                staticTypeTmp= ((CtAssignment) parent).getType();
            }else if(parent instanceof CtField){
                staticTypeTmp= ((CtField) parent).getType();
            }else if(parent instanceof CtInvocationImpl){
                parent=(CtInvocationImpl) parent;
                List<CtExpression> argumentsInvocation=((CtInvocationImpl) parent).getArguments();
                CtExecutableReference executable=((CtInvocationImpl) parent).getExecutable();
                List<CtTypeReference> argumentsExecutable=executable.getParameters();
                for(int i=0; i<argumentsInvocation.size();i++){
                    if(argumentsInvocation.get(i).equals(candidate)){
                        staticTypeTmp=argumentsExecutable.get(i);
                    }
                }
            }else if(parent instanceof CtReturn){
                while (! (parent instanceof CtMethod) ){parent=parent.getParent();}
                staticTypeTmp=((CtMethod) parent).getType();
            }

            if(staticTypeTmp!=null){
                return staticTypeTmp.getActualClass();
            }
        }catch(Exception e){
            return null;
        }catch (NoClassDefFoundError e){
            return null;
        }
        return null;
    }




    /**
     * Internal strategy for choose interfaces with parameter
     * @param interf
     * @return
     */
    private static List<String> getInterfacesForInternalStrategy(List<String> interf) throws NotInterfacesUsefullException {
        List<String> internalInterfaces= checkGivenInterf(interf);
        if(internalInterfaces.isEmpty()){
            //choose in the hierarchy, an interresting interface

                List<String> selectInterfaceInHierachy=selectInterfaceInHierarchy();
                Random r = new Random();
                int valeur =r.nextInt(selectInterfaceInHierachy.size());
                internalInterfaces.add(selectInterfaceInHierarchy().get(valeur));
                return internalInterfaces;

        }else{
           return internalInterfaces;
        }

    }

    private static List<String> selectInterfaceInHierarchy() throws NotInterfacesUsefullException {

        //TODO LEARN: comparer avec le fichier d'apprentissage
        List<String> learning=new ArrayList<>();
        try {
            learning= UtilsLearning.readInterfacesLearning();
        } catch (IOException e) {

        }
        List<String> result=new ArrayList<>();

        Set<String> keys=hierarchy.keySet();
        Iterator<String> it=keys.iterator();
        while(it.hasNext()){
            String current=it.next();
            if(hierarchy.get(current).size()>1 && !learning.contains(current)){
                result.add(current);
            }
        }

        if(result.isEmpty()){
            throw  new NotInterfacesUsefullException();
        }

        return result;
    }

    private static List<String> checkGivenInterf(List<String> interf) {
        for(int i=0;i<interf.size();i++){
            if(!hierarchy.containsKey(interf.get(i))){
                interf.remove(interf.get(i));
            }
        }
        return interf;
    }

    /**
     * External strategy for choose interfaces with parameter
     * @param interf
     * @return
     */
    private static List<String> getInterfacesForExternalStrategy(List<String> interf) {
        List<String> result= checkSubType(interf);
        if(result.isEmpty()) {
            //return java.util.List ?
            result.add("java.util.List");
        }
        return result;
    }

    /**
     * Check if interfaces given in parameter can be diversify
     */
    private static List<String> checkSubType(List<String> interf) {
        Reflections reflections = new Reflections(".*");
        for(int i=0;i<interf.size();i++){
            try {
                Class current=Class.forName(interf.get(i));
                Set<Class<?>> subtypes= reflections.getSubTypesOf(current);

                if(subtypes.size()<2){
                    LogWriter.isNotAPossibility(current);
                    interf.remove(current);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return interf;
    }

    @Deprecated
    public static CandidatesSelectStrategy getStrategy() {
        switch(InitUtils.getCandidatesStrategy()){
            case internal:return new InternalTypeStrategy();
            case external:return new ExternalLibraryStrategy();
            default:return new InternalTypeStrategy();
        }

    }

    /**
     * Check interfaces, given in parameter, et return selected interface according to selection strategy
     * @param strings
     * @return
     */
    public static List<String> getInterfacesFromStrategy(List<String> strings) throws NotInterfacesUsefullException {
        interfaces=new ArrayList<>();
         if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)){
            interfaces= getInterfacesForInternalStrategy(strings);
        }else if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.external)){
            interfaces= getInterfacesForExternalStrategy(strings);
        }
        return interfaces;
    }

    /**
     * Get hashmap with Interfaces and all their children
     * This HashMap allow to find possibility mutation
     * @return
     */
    public static HashMap<String, List<String>> getHierarchy() {
        return hierarchy;
    }


    public static List<String> getInterfaces() {
        return interfaces;
    }


    private static void printHierarchyFile() {
        try {
            PrintWriter printWriter=new PrintWriter(InitUtils.getLearningDirectory()+"hierarchy.txt");
            Set<String> set=hierarchy.keySet();
            Iterator<String> it=set.iterator();
            while (it.hasNext()){
                String current=it.next();
                List<String> list=hierarchy.get(current);
                printWriter.write(current+" : "+list+"\n");
            }
            printWriter.close();
        } catch (FileNotFoundException e) {

        }

    }

    /**
     * Print all files which describe the hierarchy of the project
     * This files are print in diversitfy repository
     */
    public static void printHierarchy() {
        if(!hierarchyIsAlreadyLearning) {
            try {
                printClassChildren();
                printAbstractClass();
                printInterfaceChildren();
                printHierarchyFile();
            } catch (FileNotFoundException e) {

            }
        }
    }

    private static void printClassChildren() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getLearningDirectory() + "Classes.txt");
        Set<String> set = classChildren.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = classChildren.get(current);
            printWriter.write(current + " : " + list + "\n");
        }
        printWriter.close();
    }

    private static void printAbstractClass() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getLearningDirectory() + "AbstractClasses.txt");

        for(int i=0;i<abstractClass.size();i++) {
            printWriter.write(abstractClass.get(i)+ "\n");
        }

        printWriter.close();
    }

    private static void printInterfaceChildren() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getLearningDirectory() + "Interfaces.txt");
        Set<String> set = interfaceChildren.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = interfaceChildren.get(current);
            printWriter.write(current + " : " + list + "\n");
        }
        printWriter.close();
    }

    /**
     * Record the current abstract class, its superClass and superInterface
     * @param qualifiedName
     * @param superInterfaces
     * @param superClass
     */
    public static void addAbstractClass(String qualifiedName, Set<CtTypeReference<?>> superInterfaces, CtTypeReference<?> superClass) {
        if(!abstractClass.contains(qualifiedName)) {
            abstractClass.add(qualifiedName);
        }
        addSuperInterfaces(qualifiedName,superInterfaces);
        addSuperClass(qualifiedName, superClass);
    }


    /**
     * Record superClass and superInterface for the current class, which not interface or abstract class
     * @param qualifiedName
     * @param superInterfaces
     * @param superClass
     */
    public static void addClass(String qualifiedName, Set<CtTypeReference<?>> superInterfaces, CtTypeReference<?> superClass) {
        addSuperInterfaces(qualifiedName,superInterfaces);
        addSuperClass(qualifiedName, superClass);
    }

    /**
     * Record the current interface and its superClass
     * @param qualifiedName
     * @param strings: superClasses
     */
    public static void addInterface(String qualifiedName, Set<CtTypeReference<?>> strings) {
        addSuperInterfaces(qualifiedName, strings);
    }



    private static void addSuperClass(String subClass, CtTypeReference<?> superClass) {

        if(isClassProject(superClass)){
            String superClassName=superClass.getQualifiedName();
            if(classChildren.containsKey(superClassName)){
                if(!classChildren.get(superClassName).contains(subClass)) {
                    classChildren.get(superClassName).add(subClass);
                }
            }else{
                List<String> list=new ArrayList<>();
                list.add(subClass);
                classChildren.put(superClassName,list);
            }
        }
    }

    private static void addSuperInterfaces(String qualifiedName, Set<CtTypeReference<?>> superInterfaces) {
        Iterator<CtTypeReference<?>> iterator=superInterfaces.iterator();

        while(iterator.hasNext()){
            CtTypeReference current=iterator.next();

            if(isClassProject(current)){
                UtilsProcessorImpl.addInterfaceLink(current.getQualifiedName(), qualifiedName);
            }
        }
    }

    private static boolean isClassProject(CtTypeReference<?> type){
        String pack;
        if(type==null){
            return false;
        }
        if(type.getPackage()==null){
            pack=type.getDeclaringType().getPackage().getSimpleName();
        }else{
            pack=type.getPackage().getSimpleName();
        }

        if(pack.contains(InitUtils.getGroupId())){
            return true;
        }
        return false;
    }

    private static void addInterfaceLink(String superClass, String subClass) {
        if(interfaceChildren.containsKey(superClass)){
            if(!interfaceChildren.get(superClass).contains(subClass)) {
                interfaceChildren.get(superClass).add(subClass);
            }
        }else{
            List<String> list=new ArrayList<>();
            list.add(subClass);
            interfaceChildren.put(superClass,list);
        }
    }

    /**
     * Analyse interfacesChildren, abstractClass and classChildren
     * and deduct the hierarchy of the project
     */
    public static void createHierarchy() {
        Set<String> allInterfaces =interfaceChildren.keySet();
        Iterator<String> iterator=allInterfaces.iterator();
        while (iterator.hasNext()){
            String current=iterator.next();
            List<String> childrenLvlOne=interfaceChildren.get(current);
            for (int i=0;i<childrenLvlOne.size(); i++) {
                addChildrenToHierarchy(current, childrenLvlOne.get(i));
            }

        }
    }

    private static void addChildrenToHierarchy(String current, String childrenLvlOne) {
        if( !interfaceChildren.keySet().contains(childrenLvlOne) && !abstractClass.contains(childrenLvlOne)){
            addHierarchyLink(current,childrenLvlOne);
        }
        if(interfaceChildren.containsKey(childrenLvlOne)) {
            List<String> list=interfaceChildren.get(childrenLvlOne);
            for(int i=0;i<list.size();i++){
                addChildrenToHierarchy(current,list.get(i));
            }
        }
        if(classChildren.containsKey(childrenLvlOne)){
            List<String> list=classChildren.get(childrenLvlOne);
            for(int i=0;i<list.size();i++){
                addChildrenToHierarchy(current,list.get(i));
            }
        }
    }

    /**
     * Add link between Interface and its subclass
     * @param superClass
     * @param subClass
     */
    private static void addHierarchyLink(String superClass, String subClass) {
        if(hierarchy.containsKey(superClass)){
            if(!hierarchy.get(superClass).contains(subClass)) {
                hierarchy.get(superClass).add(subClass);
            }
        }else{
            List<String> list=new ArrayList<>();
            list.add(subClass);
            hierarchy.put(superClass,list);
        }
    }

    /**
     * read the project's hierarchy in the learning txt file.
     * @param hierarchy: txt File
     */
    public static void readHierarchyFile(File hierarchy) throws IOException {
        hierarchyIsAlreadyLearning=true;
        FileReader fileReader=new FileReader(hierarchy);

        BufferedReader bufferedReader=new BufferedReader(fileReader);
        String current=bufferedReader.readLine();
        while (current!=null){
            splitAndRecordCurrentHierarchyLine(current);
            current=bufferedReader.readLine();
        }
    }

    private static void splitAndRecordCurrentHierarchyLine(String current) {
        String[] split=current.split(" : ");
        String[] classes=split[1].split(", ");
        List<String> list=new ArrayList<>(Arrays.asList(split[1].split(", ")));
        String first=list.get(0).substring(1);
        list.set(0, first);
        String last=list.get(list.size()-1).substring(0, list.get(list.size() - 1).length() - 1);
        list.set(list.size() - 1, last);
        hierarchy.put(split[0],list);
    }


}
