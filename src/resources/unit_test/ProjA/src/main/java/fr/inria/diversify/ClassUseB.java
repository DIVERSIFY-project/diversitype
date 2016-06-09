package fr.inria.diversify;

/**
 * Created by guerin on 02/05/16.
 */
public class ClassUseB {

    private InterfaceB b=new B();


    public InterfaceB getB(){
        return b;
    }

    public InterfaceC bToC(InterfaceB b1){
        return ((InterfaceC) b);
    }
}
