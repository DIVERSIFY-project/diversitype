package fr.inria.diversify.processor;

import fr.inria.diversify.utils.InitUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Created by guerin on 12/04/16.
 */
public class TestWatcherProcessor extends AbstractProcessor<CtClass>{

    @Override
    public void process(CtClass element) {

            List<CtMethod> methodList = element.getElements(new TypeFilter<CtMethod>(CtMethod.class));
            for (int i = 0; i < methodList.size(); i++) {
                CtBlock body = methodList.get(i).getBody();
                if(body!=null) {
                    CtStatement statement = element.getFactory().Code().createCodeSnippetStatement("fr.inria.diversify.diversitype.MutationWatcher.setCurrentTest(\"" + element.getQualifiedName() + ":" + methodList.get(i).getSimpleName() + "\")");
                    body.insertBegin(statement);
                }
            }

    }


    @Override
    public boolean isToBeProcessed(CtClass candidate) {

        if(candidate.getPosition().toString().contains(InitUtils.getProjectDirectory()+InitUtils.getTestDirectory())){
            return true;
        }
        return false;
    }
}
