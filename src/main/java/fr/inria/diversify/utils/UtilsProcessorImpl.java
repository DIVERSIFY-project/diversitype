package fr.inria.diversify.utils;

import fr.inria.diversify.exceptions.NotInterfacesUsefullException;
import fr.inria.diversify.learning.UtilsLearning;
import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.utils.selectionStrategy.CandidatesSelectStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.ExternalLibraryStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.InternalTypeStrategy;
import org.reflections.Reflections;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.processing.Processor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.io.*;
import java.util.*;

/**
 *
 *This class manage the mutation
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
     * List of static type for the mutation
     */
    private static List<String> interfaces=new ArrayList<>();



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




        if(n>=candidates.size() || n==-1) {
            selected = candidates;
            return candidates;
        }

        Random r = new Random();
        List<CtConstructorCall> list=candidates;
        for(int i=0;i<n;i++){
            int valeur =r.nextInt(list.size());
            selected.add(list.get(valeur));
            list.remove(valeur);
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

        List<String> learning=new ArrayList<>();
        try {
            learning= UtilsLearning.readInterfacesLearning();
        } catch (IOException e) {

        }
        List<String> result=new ArrayList<>();
        HashMap<String,List<String>> hierarchy=UtilsReport.getHierarchy();

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
        HashMap<String,List<String>> hierarchy=UtilsReport.getHierarchy();
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

    public static List<String> getInterfaces() {
        return interfaces;
    }
}
