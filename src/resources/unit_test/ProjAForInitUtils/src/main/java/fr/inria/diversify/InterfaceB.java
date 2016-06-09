package unit_test.ProjAForInitUtils.src.main.java.fr.inria.diversify;

import java.util.List;

/**
 * Created by guerin on 02/05/16.
 */
public interface InterfaceB<E> {
    public void addElement(E element);
    public void removeElement(E element);
    public List<E> getElements();
    public void init(List<E> elements);
    public boolean contains(E element);
    public int numberOf(Integer i);
}
