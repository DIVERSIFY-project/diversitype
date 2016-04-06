package fr.inria.diversify.mojo.mutation.transformation;

import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.RandomConcreteTypeStrategy;
import org.apache.maven.plugin.logging.Log;
import spoon.compiler.Environment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
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


    public DiversiTypeTransformation(CtConstructorCall ctConstructorCall, String outputDir,ChangeConcreteTypeStrategy strategy){

        elementsToChange=ctConstructorCall;
        output=outputDir;
        this.strategy=strategy;
    }

    @Override
    public void apply() {

        strategy.setElementToModify(elementsToChange);

        CtExpression newElem=(CtConstructorCall)strategy.getElementToTransplant();

        //Voir pour la nouvelle version de spoon
        //elementsToChange.replace(newElem);
        elementsToChange.replace(newElem);

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
