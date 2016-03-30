package fr.inria.diversify.mojo.executedTests;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Created by guerin on 30/03/16.
 *
 *
 *
 * @goal analysetests
 *
 */
public class ExecutedTestsMojo extends AbstractMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(" *** ExecutesTestMojo: ");
    }
}
