package fr.inria.diversify.mojo.mutation.strategy;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by lguerin on 10/08/15.
 */
public class OneConcreteTypeStrategy extends ChangeConcreteTypeStrategy {
    private String newConcreteType;

    public OneConcreteTypeStrategy(String s) {
        super();
        newConcreteType =s;
    }

    @Override
    public CtElement getElementToTransplant() {
        if(getElementToModify()!=null){
            try {
                Class concreteClass=Class.forName(newConcreteType);
                Factory factory=getElementToModify().getFactory();

                CtConstructorCall newConstructorCall=factory.Code().createConstructorCall(factory.Type().createReference(concreteClass));
                //FIX: CtConstructorCall newConstructorCall=factory.Core().createConstructorCall();

                List<CtExpression<?>> arguments=getElementToModify().getArguments();
                Class typeArgs[]=new Class[arguments.size()];
                for(int i=0;i<arguments.size();i++){typeArgs[i]=arguments.get(i).getType().getActualClass();}

                Constructor[] constructors=concreteClass.getConstructors();
                Constructor newCons=selectConstructor(constructors,typeArgs,getElementToModify());

                //FIX: newConstructorCall.setType(factory.Type().createReference(concreteClass));
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

    public String getNewConcreteType(){
        return newConcreteType;
    }


}
