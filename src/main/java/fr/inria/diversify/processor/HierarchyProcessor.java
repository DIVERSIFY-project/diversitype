package fr.inria.diversify.processor;

import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

import java.util.Iterator;
import java.util.Set;

/**
 * This Processor analyse source code and foreach class,
 * it add link between this class and its super class.
 * Super class corresponding to interface implemented
 * Created by guerin on 02/05/16.
 *
 */
public class HierarchyProcessor extends AbstractProcessor<CtClass> {


    @Override
    public void process(CtClass element) {
        element.getSuperInterfaces();

        Set<CtTypeReference<?>> interfaces=element.getSuperInterfaces();
        Iterator<CtTypeReference<?>> iterator=interfaces.iterator();

        while(iterator.hasNext()){
            CtTypeReference current=iterator.next();
            if(current.getPosition().toString().contains(InitUtils.getProjectDirectory())){
                UtilsProcessorImpl.addHierarchyLink(current.getQualifiedName(),element.getQualifiedName());
            }
        }
    }
}