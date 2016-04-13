package fr.inria.diversify.mojo.search;



import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.processor.StatisticsListProcessor;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import spoon.Launcher;
import spoon.SpoonAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which create the analyse report
 *
 * @goal search
 * 
 * @phase compile
 *
 * @requiresProject True
 *
 *
 * Created by guerin on 04/02/16.
 */
public class SearchMojo extends AbstractMojo {




    /**
     * @parameter
     *  expression="${search.project}"
     *  default-value="/home/guerin/Documents/INRIA/ExProj/ProjA/"
     *  @throws MojoExecutionException
     */
    private String projectDirectory;

    /**
     *@parameter
     *  expression="${search.interfaces}"
     *  default-value="java.util.List"
     * @throws MojoExecutionException
     */
    private String interfaces;



    public void execute()
        throws MojoExecutionException
    {

        try {
            getLog().info(" * Search mojo - Execute with: " + projectDirectory);

            //initialization of tmpDir
            InitUtils.init(projectDirectory);

            //analyse source code
            UtilsProcessorImpl.spoonLauncher(projectDirectory, InitUtils.getTmpDirectory()+InitUtils.getSourceDirectory(), new StatisticsListProcessor(interfaces),false);
            LogWriter.printStatisticList();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public void spoonLauncher(){

        InitUtils.setProject(projectDirectory);
        final SpoonAPI spoon = new Launcher();
        spoon.addInputResource(projectDirectory + "/src/main/java/");
        spoon.setSourceOutputDirectory(projectDirectory + outputDirectory);
        spoon.addProcessor(new StatisticsListProcessor(interfaces));
        spoon.run();

        try {
            LogWriter.printStatisticList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }*/

}
