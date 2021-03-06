package fr.inria.diversify.processor;

import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.logger.ShutDownHookLogClassLoader;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.reflections.Reflections;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StatisticsListProcessor class
 * This processor analyse source code and create a report.
 * The report contains the number of possibles changes and where the diversity can be inject.
 * This processor initialize candidates into UtilsProcessor too.
 * Created by lguerin on 24/06/15.
 */
public class StatisticsListProcessor extends spoon.processing.AbstractProcessor<CtClass> {
    private List<String> interfaces=new ArrayList<>();
    private Class staticType=null;
    private List<String> allreadyTreat=new ArrayList();

    public StatisticsListProcessor(List<String> interfaces){

        this.interfaces=interfaces;
        LogWriter.initialize(this.interfaces);
        ShutDownHookLogClassLoader shutdownHook = new ShutDownHookLogClassLoader();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

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
