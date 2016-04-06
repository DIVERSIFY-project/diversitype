package fr.inria.diversify.utils;

import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.processor.StatisticsListProcessor;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

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

   private static String project="";
    private static Factory factory;


    public static void addCandidate(CtConstructorCall ctConstructorCall){
        candidates.add(ctConstructorCall);
    }

    public static List<CtConstructorCall> getSelectedCandidates(int n){



        if(n>=candidates.size()){
            return candidates;
        }
        Random r = new Random();

        for(int i=0;i<n;i++){
            int valeur =r.nextInt(candidates.size());
            selected.add(candidates.get(valeur));
        }

        return selected;
    }

    public static void spoonLauncher(String projectDirectory,String output,Processor processor){


        //InitUtils.setProject(projectDirectory);
        final SpoonAPI spoon = new Launcher();
        spoon.addInputResource(projectDirectory + "/src/main/java/");
        spoon.setSourceOutputDirectory(projectDirectory + output);
        spoon.addProcessor(processor);
        spoon.run();

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




}
