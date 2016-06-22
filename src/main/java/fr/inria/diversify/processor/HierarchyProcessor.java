package fr.inria.diversify.processor;

import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This Processor analyse source code and foreach class,
 * it add link between this class and its super class.
 * Super class corresponding to interface implemented
 * Created by guerin on 02/05/16.
 *
 */
public class HierarchyProcessor extends AbstractProcessor<CtType> {

    @Override
    public boolean isToBeProcessed(CtType candidate) {
        if(candidate instanceof CtInterface || candidate instanceof CtClass){
            return true;
        }
        return false;
    }

    @Override
    public void process(CtType element) {


        if(element instanceof CtClass){
            CtClass ctClass=(CtClass) element;

            if(ctClass.getModifiers().contains(ModifierKind.ABSTRACT)){
                treatAbstractClass(ctClass);
            }else{
                treatClass(ctClass);
            }
        }else if(element instanceof CtInterface){
            CtInterface ctInterface=(CtInterface)element;
            treatInterface(ctInterface);

        }


       /* element.getSuperInterfaces();

        Set<CtTypeReference<?>> interfaces=element.getSuperInterfaces();
        Iterator<CtTypeReference<?>> iterator=interfaces.iterator();

        while(iterator.hasNext()){
            CtTypeReference current=iterator.next();
            String pack;
            if(current.getPackage()==null){
                pack=current.getDeclaringType().getPackage().getSimpleName();
            }else{
                pack=current.getPackage().getSimpleName();
            }

            if(pack.contains(InitUtils.getGroupId())){
                UtilsProcessorImpl.addHierarchyLink(current.getQualifiedName(),element.getQualifiedName());
            }
        }*/
    }

    private void treatAbstractClass(CtType element) {
        Set<CtTypeReference<?>> superInterfaces=element.getSuperInterfaces();
        CtTypeReference<?> superClass=element.getSuperclass();
        UtilsProcessorImpl.addAbstractClass(element.getQualifiedName(), superInterfaces, superClass);

    }

    private void treatClass(CtClass element) {
        Set<CtTypeReference<?>> superInterfaces=element.getSuperInterfaces();
        CtTypeReference<?> superClass=element.getSuperclass();
        UtilsProcessorImpl.addClass(element.getQualifiedName(),superInterfaces,superClass);

    }

    private void treatInterface(CtInterface ctInterface) {
        UtilsProcessorImpl.addInterface(ctInterface.getQualifiedName(), ctInterface.getSuperInterfaces());
    }

    /*private List<String> ctTypeReferenceSetToStringList(Set<CtTypeReference<?>> set){
        List<String> list=new ArrayList<>();

        Iterator<CtTypeReference<?>> iterator=set.iterator();
        while (iterator.hasNext()){
            CtTypeReference ctTypeReference=iterator.next();
            list.add(ctTypeReference.getQualifiedName());
        }
        return list;
    }*/

}