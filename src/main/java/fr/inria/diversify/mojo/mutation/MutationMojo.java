package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.mojo.mutation.strategy.ChangeConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.OneConcreteTypeStrategy;
import fr.inria.diversify.mojo.mutation.strategy.Strategy;
import fr.inria.diversify.mojo.mutation.transformation.DiversiTypeTransformation;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import spoon.reflect.code.CtConstructorCall;

import java.util.List;

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
 * @execute  lifecycle="analysetest" phase="test"
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

    /**
     * this attribute contains failed test before mutation
     */
    private List<String> testFailMainProject;

    private String tmpDir;

    private List<CtConstructorCall> selectedCandidates;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {


        testFailMainProject=UtilsTestProcessorImpl.getTestSuiteFail();

        doMutation();



    }



    private void doMutation() {

        selectedCandidates=UtilsProcessorImpl.getSelectedCandidates(nChange);

        //creation de la transformation pour l'unique point de transformation
        //récupération du dossier source dans initUtils
        for(int i=0;i<selectedCandidates.size();i++){
            getLog().info("Treat the candadiate!; "+selectedCandidates.get(i));
            ChangeConcreteTypeStrategy strategy=getStrategy(selectedCandidates.get(i));

            getLog().info("Strategy Selected: "+strategy);
            DiversiTypeTransformation transformation=new DiversiTypeTransformation(selectedCandidates.get(i),InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory(),getStrategy(selectedCandidates.get(i)));
            transformation.apply();
            getLog().info("write transformation "+InitUtils.getSourceDirectory());
            //lancement des tests

            //recuperation des résultats

            //stockage des données.

            //restoration
            transformation.restore();
        }

    }

    /**
     * TODO treat parameters and switch strategy
     * @param ctConstructorCall
     * @return
     */
    public ChangeConcreteTypeStrategy getStrategy(CtConstructorCall ctConstructorCall) {

        return new OneConcreteTypeStrategy("java.util.LinkedList");
    }
}
