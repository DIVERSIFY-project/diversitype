package fr.inria.diversify.utils;

import fr.inria.diversify.mojo.mutation.strategy.MutationStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by guerin on 09/06/16.
 */
public class InitUtilsTest {

    private final String outputDirectory="src/resources/unit_test/ProjAForInitUtils/";

    @Before
    public void initalize(){
        InitUtils.setAlreadyInit(false);
    }

    @Test
    public void testInitMethod() throws IOException, InterruptedException {

        InitUtils.init(outputDirectory, "internal", "random", outputDirectory + "target/ProjA-1.0-SNAPSHOT.jar");

        assertTrue(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal));
        assertTrue(InitUtils.getMutationStrategy().equals(MutationStrategy.random));

        assertTrue(InitUtils.getOutput().contains(outputDirectory + "target/diversiType/"));
        assertTrue(InitUtils.getProjectDirectory().contains(outputDirectory));
        assertTrue(InitUtils.getSourceDirectory().equals("src/main/java/"));
        assertTrue(InitUtils.getTestDirectory().equals("src/test/java/"));
        assertTrue(InitUtils.getTmpDirectory().contains(outputDirectory + "target/diversiType/tmp_mutation/"));
    }

}