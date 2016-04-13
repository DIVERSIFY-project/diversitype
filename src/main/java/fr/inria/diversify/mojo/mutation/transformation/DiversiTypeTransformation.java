package fr.inria.diversify.mojo.mutation.transformation;


import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.RandomConcreteTypeStrategy;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import org.apache.maven.plugin.logging.Log;
import spoon.compiler.Environment;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.Parent;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * This class apply the transformation with a given strategy for an unique point
 * Created by guerin on 04/04/16.
 */
public class DiversiTypeTransformation implements Transformation{


    private CtConstructorCall elementsToChange;
    private String output;
    private ChangeConcreteTypeStrategy strategy;
    private CtExpression newElement;
    private CtCodeSnippetStatement watcher;

    public DiversiTypeTransformation(CtConstructorCall ctConstructorCall, String outputDir,ChangeConcreteTypeStrategy strategy){

        elementsToChange=ctConstructorCall;
        output=outputDir;
        this.strategy=strategy;
    }

    @Override
    public void apply() {

        strategy.setElementToModify(elementsToChange);

        newElement=(CtConstructorCall)strategy.getElementToTransplant();

        //Voir pour la nouvelle version de spoon
        //elementsToChange.replace(newElem);
        addWatcher(elementsToChange);
        elementsToChange.replace(newElement);

        try {
            printJavaFile(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addWatcher(CtConstructorCall elementsToChange) {
        CtElement parent=elementsToChange.getParent();
        while(!(parent instanceof CtStatement)){
            parent=parent.getParent();
        }

        //TODO add createCodeSnippet
        watcher=parent.getFactory().Code().createCodeSnippetStatement("fr.inria.diversify.diversitype.MutationWatcher.setCurrentTransfo(\""+elementsToChange.getPosition().toString()+"\")");
        ((CtStatement)parent).insertBefore(watcher);
    }

    public void restore(){
        watcher.replace(null);
        newElement.replace(elementsToChange);

        try {
            printJavaFile(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printJavaFile(String directory) throws IOException {
        CtType<?> type = getOriginalClass(elementsToChange);
        Factory factory = type.getFactory();
        Environment env = factory.getEnvironment();
        JavaOutputProcessor processor = new JavaOutputProcessor(new File(directory), new DefaultJavaPrettyPrinter(env));
        processor.setFactory(factory);
        processor.createJavaFile(type);

    }

    public CtType<?> getOriginalClass(CtElement element) {
        return (CtType<?>) element.getPosition().getCompilationUnit().getMainType();
    }


}
