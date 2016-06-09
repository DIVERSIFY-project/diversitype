package fr.inria.diversify;


import org.apache.commons.math3.geometry.spherical.twod.Circle;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guerin on 01/02/16.
 */
public class A {


    private List<String> listA;
    private Circle circle;

    public A(List<String> listA) {
        this.listA = listA;
        Pair pair=new Pair("sqsdf",1);
    }

    public A(){
        this.listA=new ArrayList<String>();
    }

    public A(int i){
        this.listA=new ArrayList<String>();
    }

    public A(String s){
        this.listA=new ArrayList<String>(new ArrayList<>());
        listA.add(s);
    }

    public void add(String s){
        if(!listA.contains(s)){
            listA.add(s);
        }
    }

    public void remove(String s){
        if(listA.contains(s)){
            listA.remove(s);
        }
    }

    public List<String> getListA() {
        return listA;
    }

    public void cleanList(){
        setListA(new ArrayList<>());
    }




    public void setListA(List<String> listA) {

        this.listA = listA;
    }
}
