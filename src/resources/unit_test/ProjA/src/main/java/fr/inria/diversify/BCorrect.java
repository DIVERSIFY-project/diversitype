package fr.inria.diversify;

import java.util.ArrayList;
import java.util.List;

/**
 * The values list shouldn't have doubloon and negative value
 * This class shouldn't contains errors
 * Created by guerin on 02/05/16.
 */
public class BCorrect implements InterfaceB<Integer>, InterfaceC {

    private List<Integer> values=new ArrayList<>();

    @Override
    public void addElement(Integer element) {
        if((!values.contains(element)) && (element>=0)){
            values.add(element);
        }

    }

    @Override
    public void removeElement(Integer element) {
        values.remove(element);

    }

    @Override
    public List<Integer> getElements() {
        return values;
    }

    @Override
    public void init(List<Integer> elements) {
        for(int i=0;i<values.size();i++){
            addElement(elements.get(i));
        }
    }

    @Override
    public boolean contains(Integer element) {

        return values.contains(element);
    }

    @Override
    public int sum() {
        int sum=0;
        for(int i=0;i<values.size();i++){
            sum=sum+values.get(i);
        }
        return sum;
    }

    @Override
    public int mutiply() {
        int mul=1;
        for(int i=0;i<values.size();i++){
            mul=mul*values.get(i);
        }
        return mul;
    }

    @Override
    public int numberOf(Integer i) {
        int nb=0;
        for (int j=0;i<values.size();i++){
            if(values.get(i)==i){
                nb++;
            }
        }
        return nb;
    }
}
