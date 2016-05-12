package fr.inria.diversify.utils.selectionStrategy;

import spoon.reflect.code.CtConstructorCall;

import java.util.List;

/**
 * Created by guerin on 02/05/16.
 */
public interface CandidatesSelectStrategy {

    public void init(int nbCandidates, List<CtConstructorCall> candidates);
    public List<CtConstructorCall> getCandidates();

}
