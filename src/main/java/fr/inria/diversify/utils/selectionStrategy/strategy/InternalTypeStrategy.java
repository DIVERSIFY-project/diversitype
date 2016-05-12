package fr.inria.diversify.utils.selectionStrategy.strategy;

import fr.inria.diversify.utils.selectionStrategy.CandidatesSelectStrategy;
import spoon.reflect.code.CtConstructorCall;

import java.util.List;

/**
 * Created by guerin on 02/05/16.
 */
public class InternalTypeStrategy implements CandidatesSelectStrategy {
    @Override
    public void init(int nbCandidates, List<CtConstructorCall> candidates) {

    }

    @Override
    public List<CtConstructorCall> getCandidates() {
        return null;
    }
}
