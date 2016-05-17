package fr.inria.diversify.mojo.mutation.builder;

import fr.inria.diversify.exceptions.NoAlternativesException;
import fr.inria.diversify.mojo.mutation.strategy.MutationStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import spoon.reflect.code.CtConstructorCall;

/**
 * This abstract class describe the functioning of constructor call builder
 * * Created by guerin on 10/05/16.
 */
public abstract class ConstructorCallBuilder {
    CtConstructorCall ctConstructorCall;
    CtConstructorCall elementToTransplant;

    /**
     * set the point where the mutation will take place
     * @param elementToTransplant
     */
    public void selElementToTransplant(CtConstructorCall elementToTransplant){
        this.elementToTransplant=elementToTransplant;
    }

    /**
     * Find new constructorCall corresponding to the elementToTransplant.
     * there can be have no interesting constructorCall
     * @return
     * @throws NoAlternativesException
     */
    public CtConstructorCall findCtConstructorCall() throws NoAlternativesException {
        return elementToTransplant;
    }

    /**
     * Set selection strategy.
     * This strategy correspond to the mean to select mutation point in the source code
     * It can be internal or external
     * @see CandidatesStrategy
     * @param candidatesStrategy
     */
    public abstract void setSelectionStrategy(CandidatesStrategy candidatesStrategy);

    /**
     * set Mutation strategy
     * This strategy correspond to the mean to select new Constructor call
     * it can be one or random
     * @see MutationStrategy
     * @param strategy
     */
    public abstract void setMutationStrategy(MutationStrategy strategy);

    /**
     * Set the staticType treat during current building
     * @param staticType
     */
    public abstract void setStaticType(String staticType);

}
