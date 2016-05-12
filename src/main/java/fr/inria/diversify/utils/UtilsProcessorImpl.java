package fr.inria.diversify.utils;

import fr.inria.diversify.exceptions.NotInterfacesUsefullException;
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

import java.io.File;
import java.io.FileNotFoundException;
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
    private static List<CtConstructorCall> selected=new ArrayList<>();

    private static HashMap<String,List<String>> hierarchy=new HashMap<>();

    private static String project="";
    private static Factory factory;

    /**
     * List of static type for the mutation
     */
    private static List<String> interfaces=new ArrayList<>();
    private static CandidatesSelectStrategy strategy;


    public static void addCandidate(CtConstructorCall ctConstructorCall){
        candidates.add(ctConstructorCall);
    }

    /**
     * Select mutation point
     * @param n
     * @return
     */
    public static List<CtConstructorCall> getSelectedCandidates(int n){



        if(n>=candidates.size()){
            selected=candidates;
            return candidates;
        }

        /*strategy=getStrategy();
        strategy.init(n,candidates);
        selected=strategy.getCandidates();*/


        Random r = new Random();

        for(int i=0;i<n;i++){
            int valeur =r.nextInt(candidates.size());
            selected.add(candidates.get(valeur));
        }

        return selected;
    }

    public static Factory spoonLauncher(String projectDirectory,String output,Processor processor,boolean onlyTest){


        //InitUtils.setProject(projectDirectory);
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


    public static void addHierarchyLink(String superClass, String subClass) {
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
     * Internal strategy for choose interfaces with parameter
     * @param interf
     * @return
     */
    private static List<String> getInterfacesForInternalStrategy(List<String> interf) {
        List<String> internalInterfaces= checkGivenInterf(interf);

        if(internalInterfaces.isEmpty()){
            //choose in the hierarchy, an interresting interface
            try {

                internalInterfaces.add(selectInterfaceInHierarchy().get(0));
                return internalInterfaces;
            } catch (NotInterfacesUsefullException e) {
                //TODO better processing for this exception
                e.printStackTrace();
                System.exit(0);
                return null;
            }
        }else{
           return internalInterfaces;
        }

    }

    private static List<String> selectInterfaceInHierarchy() throws NotInterfacesUsefullException {
        List<String> result=new ArrayList<>();

        Set<String> keys=hierarchy.keySet();
        Iterator<String> it=keys.iterator();
        while(it.hasNext()){
            String current=it.next();
            if(hierarchy.get(current).size()>1){
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

    public static CandidatesSelectStrategy getStrategy() {
        switch(InitUtils.getCandidatesStrategy()){
            case internal:return new InternalTypeStrategy();
            case external:return new ExternalLibraryStrategy();
            default:return new InternalTypeStrategy();
        }

    }

    public static List<String> getInterfacesFromStrategy(List<String> strings) {
        interfaces=new ArrayList<>();
         if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)){
            interfaces= getInterfacesForInternalStrategy(strings);
        }else if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.external)){
            interfaces= getInterfacesForExternalStrategy(strings);
        }
        return interfaces;
    }

    public static HashMap<String, List<String>> getHierarchy() {
        return hierarchy;
    }


}
