package fr.inria.diversify.mojo.executedTests;

import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by guerin on 09/06/16.
 */
public class ExecutedTestsMojoTest {


    private final String outputDirectory="src/resources/unit_test/ProjForExecuteTestMojo/target/diversiType/";

    @Test
    public void testOutputIsNotNull() throws MojoFailureException, MojoExecutionException {
        ExecutedTestsMojo executedTestsMojo=new ExecutedTestsMojo();
        executedTestsMojo.setProjectDirectory(outputDirectory);

        executedTestsMojo.execute();

        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
    }



}