package fr.inria.diversify.mojo.mutation.strategy;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtElement;

/**
 * Created by lguerin on 10/08/15.
 */
@Deprecated
public abstract class ChangeConcreteTypeStrategy {

    private  CtConstructorCall elementToModify;

    public ChangeConcreteTypeStrategy(){
        this.elementToModify =null;
    }

    public void setElementToModify(CtConstructorCall element){
        elementToModify=element;
    }

    public abstract CtElement getElementToTransplant();

    public CtConstructorCall getElementToModify(){
        return elementToModify;
    }


}
