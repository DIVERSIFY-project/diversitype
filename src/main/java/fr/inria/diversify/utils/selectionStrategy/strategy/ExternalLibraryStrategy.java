package fr.inria.diversify.utils.selectionStrategy.strategy;

import fr.inria.diversify.utils.selectionStrategy.CandidatesSelectStrategy;
import spoon.reflect.code.CtConstructorCall;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guerin on 02/05/16.
 */
@Deprecated
public class ExternalLibraryStrategy implements CandidatesSelectStrategy {
    private int nb=0;
    private List<CtConstructorCall> candidates=new ArrayList<>();

    @Override
    public void init(int nbCandidates, List<CtConstructorCall> candidates) {
        nb=nbCandidates;
        this.candidates=candidates;

    }

    @Override
    public List<CtConstructorCall> getCandidates() {



        return null;
    }


}
