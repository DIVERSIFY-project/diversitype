package fr.inria.diversify.mojo.mutation.strategy;

import org.reflections.Reflections;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lguerin on 12/08/15.
 */
public class RandomConcreteTypeStrategy extends ChangeConcreteTypeStrategy {
    private List<String> concreteTypes=new ArrayList<>();

    public RandomConcreteTypeStrategy(String s) {
        super();
        String[] tab=s.split(",");
        for(int i=0; i<tab.length;i++){
            concreteTypes.add(tab[i]);
        }
    }

    @Override
    public CtElement getElementToTransplant() {
        if(getElementToModify()!=null){
            try {


                Class concreteClass=Class.forName(chooseConcreteType());
                Factory factory=getElementToModify().getFactory();
                CtConstructorCall newConstructorCall=factory.Core().createConstructorCall();



                List<CtExpression<?>> arguments=getElementToModify().getArguments();
                Class typeArgs[]=new Class[arguments.size()];
                for(int i=0;i<arguments.size();i++){typeArgs[i]=arguments.get(i).getType().getActualClass();}


                Constructor[] constructors=concreteClass.getConstructors();
                Constructor newCons=selectConstructor(constructors,typeArgs,getElementToModify());

                newConstructorCall.setType(factory.Type().createReference(concreteClass));
                if(newCons!=null){

                    newConstructorCall.setArguments(arguments);
                }


                return newConstructorCall;

            } catch (ClassNotFoundException e) {
                return null;
            }

        }

        return null;
    }

    private String chooseConcreteType() {
        Random r=new Random();
        int random = (int)(Math.random() * concreteTypes.size());
        return concreteTypes.get(random);

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
        Reflections reflection=new Reflections(".*");

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

    public List<String> getConcreteType(){
        return concreteTypes;
    }




}
