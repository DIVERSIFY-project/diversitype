package fr.inria.diversify.mojo.mutation.builder;

import fr.inria.diversify.exceptions.NoAlternativesException;
import fr.inria.diversify.mojo.mutation.strategy.MutationStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import spoon.reflect.code.CtConstructorCall;

/**
 * Created by guerin on 10/05/16.
 */
public abstract class ConstructorCallBuilder {
    CtConstructorCall ctConstructorCall;
    CtConstructorCall elementToTransplant;

    public void selElementToTransplant(CtConstructorCall elementToTransplant){
        this.elementToTransplant=elementToTransplant;
    }

    public CtConstructorCall findCtConstructorCall() throws NoAlternativesException {
        return elementToTransplant;
    }

    public abstract void setSelectionStrategy(CandidatesStrategy candidatesStrategy);
    public abstract void setMutationStrategy(MutationStrategy strategy);
    public abstract void setStaticType(String staticType);

}
