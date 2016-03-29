package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.utils.UtilsProcessorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.swing.*;

/**
 * /**
 * Goal which inject diversity
 *
 * @goal mutation
 *
 * @phase test
 *
 * @requiresProject True
 *
 * Created by guerin on 04/02/16.
 */
public class MutationMojo extends AbstractMojo{

    /**
     * @parameter
     * expression=${mutation.nchange}}
     * defaut-value=1
     * @throws MojoExecutionException
     */
    private int nChange;

    /**
     * @parameter
     *  expression="${mutation.project}"
     *  default-value="/home/guerin/Documents/INRIA/ExProj/ProjA/"
     *  @throws MojoExecutionException
     */
    private String projectDirectory;

    /**
     *@parameter
     *  expression="${mutation.interfaces}"
     *  default-value="java.util.List"
     * @throws MojoExecutionException
     */
    private String interfaces;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("MutationMojo launch "+nChange);
        getLog().info("constructorCall: "+ UtilsProcessorImpl.getSelectedCandidates(nChange));


    }
}
