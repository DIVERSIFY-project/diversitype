package unit_test.ProjAForInitUtils.src.main.java.fr.inria.diversify;

import fr.inria.diversify.*;

/**
 * Created by guerin on 02/05/16.
 */
public class ClassUseB {

    private InterfaceB b=new fr.inria.diversify.B();


    public InterfaceB getB(){
        return b;
    }

    public InterfaceC bToC(InterfaceB b1){
        return ((InterfaceC) b);
    }
}
