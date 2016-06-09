package fr.inria.diversify;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by guerin on 03/02/16.
 */
public class ATest {

    @Test
    public void atest(){
        assertTrue(false);
    }

    @Test
    public void atest1(){
        assertTrue(true);
    }

    @Test
    public void atest2(){
        assertTrue(true);
    }


    @Test
    public void atest3(){
        A a=new A();
        assertTrue(a.getListA().isEmpty());
    }

    @Test
    public void atest4(){
        A a=new A(1);
        assertTrue(a.getListA().isEmpty());
    }

    @Test
    public void atest5(){
        A a=new A("essai");
        assertTrue(!a.getListA().isEmpty());
    }

    @Test
    public void atest6(){
        A a=new A("essai");
        a.cleanList();
        assertTrue(a.getListA().isEmpty());
    }


}