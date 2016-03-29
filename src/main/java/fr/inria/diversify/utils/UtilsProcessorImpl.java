package fr.inria.diversify.utils;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.util.List;

/**
 *
 *Class which contains methods for treat specific node
 *
 *
 * Created by guerin on 29/03/16.
 */
public class UtilsProcessorImpl {

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
