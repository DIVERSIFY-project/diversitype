package fr.inria.diversify.processor;

import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.logger.ShutDownHookLogClassLoader;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import org.reflections.Reflections;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StatisticsListProcessor class
 * This processor analyse source code and create a report.
 * The report contains the number of possibles changes and where the diversity can be inject.
 *
 * Created by lguerin on 24/06/15.
 */
public class StatisticsListProcessor extends spoon.processing.AbstractProcessor<CtClass> {
    private List<String> interfaces=new ArrayList<>();
    private Class staticType=null;
    private List<String> allreadyTreat=new ArrayList();

    public StatisticsListProcessor(String interfaces){

        String[] tab=interfaces.split(";");
        for(int i=0;i<tab.length;i++){
            this.interfaces.add(tab[i]);

        }
        LogWriter.initialize(this.interfaces);
        ShutDownHookLogClassLoader shutdownHook = new ShutDownHookLogClassLoader();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        checkSubType();

    }

    /**
     * Check if interfaces given in parameter can be diversify
     */
    private void checkSubType() {
        Reflections reflections = new Reflections(".*");
        for(int i=0;i<interfaces.size();i++){
            try {
                Class current=Class.forName(interfaces.get(i));
                Set<Class<?>> subtypes= reflections.getSubTypesOf(current);

                if(subtypes.size()<2){
                    LogWriter.isNotAPossibility(current);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(CtClass ctClass) {

        List<CtConstructorCall> ctConstructorCalls=ctClass.getElements(new TypeFilter<CtConstructorCall>(CtConstructorCall.class));

        LogWriter.addClass(ctClass);

        for(int i=0;i<ctConstructorCalls.size();i++){
            if((!allreadyTreat.contains(ctConstructorCalls.get(i).getPosition().toString())) && isCandidate(ctConstructorCalls.get(i))){
                LogWriter.addCandidateList(ctClass.getSimpleName(),staticType, ctConstructorCalls.get(i).getType().getActualClass().getSimpleName());
                UtilsProcessorImpl.addCandidate(ctConstructorCalls.get(i));
                allreadyTreat.add(ctConstructorCalls.get(i).getPosition().toString());

            }
        }
    }

    /**
     * Method that indicate if constructorCall can be changed
     * @param candidate
     * @return
     */
    private boolean isCandidate(CtConstructorCall candidate) {
        staticType= UtilsProcessorImpl.getStaticType(candidate);
        return (staticType!=null && interfaces.contains(staticType.getName()));
    }
}
