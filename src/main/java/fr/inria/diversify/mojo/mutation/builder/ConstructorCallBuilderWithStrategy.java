package fr.inria.diversify.mojo.mutation.builder;

import fr.inria.diversify.exceptions.NoAlternativesException;
import fr.inria.diversify.mojo.mutation.strategy.MutationStrategy;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsReport;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Implementation of the constructor call builder
 * Created by guerin on 10/05/16.
 */
public class ConstructorCallBuilderWithStrategy extends ConstructorCallBuilder{
    private CandidatesStrategy candidatesStrategy=CandidatesStrategy.internal;
    private MutationStrategy mutationStrategy=MutationStrategy.one;
    private List<Constructor> mutants=new ArrayList<>();
    private String staticType;

    @Override
    public CtConstructorCall findCtConstructorCall() throws NoAlternativesException {
        ctConstructorCall=elementToTransplant;

        if(mutants.size()==0){
            throw new NoAlternativesException();
        }
        //create from factory
        Factory factory=elementToTransplant.getFactory();


        //CtConstructorCall newConstructorCall=factory.Code().createConstructorCall(factory.Type().createReference(concreteClass));

        if(mutationStrategy.equals(MutationStrategy.one)){
            //TODO
            throw new NotImplementedException();
        }else if(mutationStrategy.equals(MutationStrategy.random)){
            Constructor chosenConstructor=getRandomConstructor();
            CtConstructorCall newConstructorCall=factory.Code().createConstructorCall(factory.Type().createReference(chosenConstructor.getDeclaringClass()));
            newConstructorCall.setArguments(elementToTransplant.getArguments());
            ctConstructorCall=newConstructorCall;
        }else{
            //TODO
        }
        //check strategy

        return ctConstructorCall;
    }

    /**
     * internal or external
     * @param candidatesStrategy
     */
    @Override
    public void setSelectionStrategy(CandidatesStrategy candidatesStrategy) {
        this.candidatesStrategy=candidatesStrategy;
        List<String> alltypes=new ArrayList<>();

        //get all candidates possible
        if(candidatesStrategy.equals(CandidatesStrategy.external)){
            //TODO
            throw new NotImplementedException();

        }else if(candidatesStrategy.equals(CandidatesStrategy.internal)){
            alltypes= UtilsReport.getHierarchy().get(UtilsProcessorImpl.getStaticType(elementToTransplant).getName());
            alltypes.remove(elementToTransplant.getType().toString());
        }else{
            //TODO throws exception?
        }

        //select candidates which have a good constructor
        for(int i=0;i<alltypes.size();i++){
            Constructor current=getConstructor(alltypes.get(i));
            if(current!=null){
                mutants.add(current);
            }
        }
    }

    @Override
    public void setStaticType(String staticType){
        this.staticType=staticType;
    }

    private Constructor getConstructor(String s) {
        try {

            Class concreteClass=Class.forName(s);

            List<CtExpression<?>> arguments=elementToTransplant.getArguments();
            Class typeArgs[]=new Class[arguments.size()];
            for(int i=0;i<arguments.size();i++){typeArgs[i]=arguments.get(i).getType().getActualClass();}

            Constructor[] constructors=concreteClass.getConstructors();
            Constructor newCons=selectConstructor(constructors, typeArgs,elementToTransplant);
            return newCons;
        } catch (ClassNotFoundException e) {
           // e.printStackTrace();
            return null;
        }

    }

    /**
     * one or random
     * @param strategy
     */
    @Override
    public void setMutationStrategy(MutationStrategy strategy) {
        this.mutationStrategy=strategy;
    }



    /**
     * Choose a constructor in the potentials constructors list which have compatibles parameters with typeArgs.
     * The constructor chosen should be different to the constructor of ctConstructorCall.
     * If there are not a compatible constructor, this function return null.
     * @param potentialsConstructors
     * @param typeArgs
     * @param ctConstructorCall
     * @return Constructor
     */
    private Constructor selectConstructor(Constructor[] potentialsConstructors, Class[] typeArgs, CtConstructorCall ctConstructorCall) {
        for(int i=0;i<potentialsConstructors.length;i++){
            Class params[]=potentialsConstructors[i].getParameterTypes();
            if(compareConstructorsArguments(params,typeArgs) && (!potentialsConstructors[i].getName().equals(ctConstructorCall.getType().getActualClass().getName()))){
                return potentialsConstructors[i];
            }
        }

        return null;
    }

    /**
     * This function compare the arguments of a constructor and the arguments of a constructorCall
     * for example:
     * compareConstructorsArguments([Collection.class],[ArrayList.class]) return True because an ArrayList can instantiate a Collection.
     * compareConstructorsArguments([ArrayList.class],[integer.class]) return False because an integer can't instantiate an ArrayList.
     * compareConstructorsArguments([Collection.class],[ArrayList.class, Integer.class]) return False because the sizes are different.
     * @param constructorArguments
     * @param constructorCallArguments
     * @return boolean
     */
    private boolean compareConstructorsArguments(Class[] constructorArguments, Class[] constructorCallArguments){
        //Reflections reflection=new Reflections(".*");

        if(constructorArguments.length!= constructorCallArguments.length){
            return false;
        }

        for(int i=0;i<constructorArguments.length;i++){
            if( !matchClass(constructorArguments[i], constructorCallArguments[i] )){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if potentialSubType is a subType of potentialSuperType
     * @param potentialSuperType
     * @param potentialSubType
     * @return boolean
     */
    private boolean matchClass(Class potentialSuperType, Class potentialSubType){
        if(potentialSubType==null){
            return false;
        }else if(potentialSuperType.getName().equals(potentialSubType.getName())){
            return true;
        }else if(potentialSubType.getName().equals(Object.class.getName())){
            return false;
        }else{
            List<Class> superClassList=new ArrayList<>(Arrays.asList(potentialSubType.getInterfaces()));
            superClassList.add(potentialSubType.getSuperclass());
            if(superClassList.isEmpty()){
                return false;
            }else{
                for(int i=0;i<superClassList.size();i++){
                    if(matchClass(potentialSuperType, superClassList.get(i))){
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private Constructor getRandomConstructor() {
        Random r=new Random();
        int random = (int)(Math.random() * mutants.size());
        return mutants.get(random);

    }
}
