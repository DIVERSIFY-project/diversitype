package fr.inria.diversify.mojo.search;



import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.processor.HierarchyProcessor;
import fr.inria.diversify.processor.StatisticsListProcessor;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * @parameter
     * exression="${search.jarLocation}"
     * @throws MojoExecutionException
     */
    private String jarLocation;

    /**
     * @parameter
     * expression="${search.mutationStrategy}"
     *  default-value="random"
     *  @throws MojoExecutionException
     */
    private String mutationStrategy;

    /**
     * @parameter
     * expression="${search.candidatesStrategy}"
     *  default-value="internal"
     *  @throws MojoExecutionException
     */
    private String selectedCandidatesStratregy;

    /**
     *@parameter
     *  expression="${search.interfaces}"
     * @throws MojoExecutionException
     */
    private String interfaces;

    /**
     * chosen interfaces for the mutation after parameters treating
     */
    private List<String> finalInterfaces;


    public void execute()
        throws MojoExecutionException
    {

        try {
            if(!InitUtils.getAlreadyAnalyse()) {
                getLog().info(" * Search mojo - Execute with: " + projectDirectory);

                //initialization of tmpDir
                InitUtils.init(projectDirectory, mutationStrategy, selectedCandidatesStratregy,jarLocation);

                //create the project's hierarchy
                if (InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)) {
                    getLog().info("inspect source code and generate the hierarchy");
                    UtilsProcessorImpl.spoonLauncher(projectDirectory, InitUtils.getTmpDirectory() + InitUtils.getSourceDirectory(), new HierarchyProcessor(), false);
                }

                //treat given parameters and deduct the static type use during the mutation
                finalInterfaces = getInterfaces();

                //analyse source code
                getLog().info("analyse the source code");
                UtilsProcessorImpl.spoonLauncher(projectDirectory, InitUtils.getTmpDirectory() + InitUtils.getSourceDirectory(), new StatisticsListProcessor(finalInterfaces), false);
                LogWriter.printStatisticList();
                InitUtils.setAlreadyAnalyse(true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> getInterfaces() {

        return UtilsProcessorImpl.getInterfacesFromStrategy(splitInterfaces());

        /*if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)){
            return getInterfacesForInternalStrategy();
        }else if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.external)){
            return getInterfacesForExternalStrategy();
        }else{
            //TODO
            return new ArrayList<>();
        }*/
    }

    /*public List<String> getInterfacesForInternalStrategy() {

        //return UtilsProcessorImpl.getInterfacesForInternalStrategy(splitInterfaces()) ;
    }

    public List<String> getInterfacesForExternalStrategy() {
        //return UtilsProcessorImpl.getInterfacesForExternalStrategy(splitInterfaces());

    }*/

    private List<String> splitInterfaces() {
        if(interfaces==null || interfaces.equals("")){
            return new ArrayList<>();
        }else{
            List<String> result= new ArrayList<>();
            String[] tab=interfaces.split(";");
            for(int i=0;i<tab.length;i++){
                result.add(tab[i]);

            }
            return result;
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
