package fr.inria.diversify;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by guerin on 02/05/16.
 */
public class ClassUseBTest {

    private InterfaceB b;
    private ClassUseB classUseB;

    @Before
    public void initTest(){
        classUseB=new ClassUseB();
        b=classUseB.getB();

    }

    @Test
    public void testAdd1(){
        b.addElement(2);
        assertTrue(b.contains(2));
    }

    @Test
    public void testAdd2(){
        b.addElement(2);
        b.addElement(6);
        assertTrue(b.contains(6));
    }

    @Test
    public void testAdd3(){
        b.addElement(9);
        b.addElement(-5);
        assertTrue(!b.contains(-5));
    }

    @Test
    public void testAdd4(){
        b.addElement(9);
        b.addElement(9);
        assertTrue(b.numberOf(9)==1);

    }

    @Test
    public void testRemove(){
        b.addElement(5);
        b.removeElement(5);
        assertTrue(!b.contains(5));
    }

    @Test
    public void testSum(){
        b.addElement(8);
        b.addElement(9);
        b.addElement(10);
        InterfaceC c=classUseB.bToC(b);
        assertTrue(c.sum()==27);
    }

    @Test
    public void testMul(){
        b.addElement(9);
        b.addElement(2);
        InterfaceC c=classUseB.bToC(b);
        assertTrue(c.mutiply()==18);
    }

}