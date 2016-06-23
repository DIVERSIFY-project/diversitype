package fr.inria.diversify.mojo.search;



import fr.inria.diversify.exceptions.NotInterfacesUsefullException;
import fr.inria.diversify.logger.LogWriter;
import fr.inria.diversify.processor.HierarchyProcessor;
import fr.inria.diversify.processor.StatisticsListProcessor;
import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This goal create the analyse report and initialize project information.
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
     * property="nchange"
     * defaut-value=1
     * @throws MojoExecutionException
     */
    private int nChange;


    /**
     * @parameter
     *  property="project"
     *  default-value="/home/guerin/Documents/INRIA/ExProj/unit_test.ProjA/"
     *  @throws MojoExecutionException
     */
    private String projectDirectory;

    /**
     * @parameter
     *  property="jarLocation"
     * @throws MojoExecutionException
     */
    private String jarLocation;

    /**
     * @parameter
     *  property="mutationStrategy"
     *  default-value="random"
     *  @throws MojoExecutionException
     */
    private String mutationStrategy;

    /**
     * @parameter
     *  property="candidateStrategy"
     *  default-value="internal"
     *  @throws MojoExecutionException
     */
    private String candidateStratregy;

    /**
     *@parameter
     *   property="interfaces"
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
                getLog().info(" * Search mojo - Execute with: " + projectDirectory+mutationStrategy+candidateStratregy+jarLocation);

                //initialization of tmpDir
                InitUtils.init(projectDirectory, mutationStrategy, candidateStratregy,jarLocation);

                //create the project's hierarchy
                if (InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)) {
                    getLog().info("inspect source code and generate the hierarchy");
                    try {
                        File hierarchy=new File(InitUtils.getLearningDirectory()+"hierarchy.txt");
                        if(hierarchy.exists()){
                            UtilsProcessorImpl.readHierarchyFile(hierarchy);
                        }else {
                            UtilsProcessorImpl.spoonLauncher(projectDirectory, InitUtils.getTmpDirectory() + InitUtils.getSourceDirectory(), new HierarchyProcessor(), false);
                            UtilsProcessorImpl.createHierarchy();
                            UtilsProcessorImpl.printHierarchy();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                //treat given parameters and deduct the static type use during the mutation
                finalInterfaces = getInterfaces();

                //analyse source code
                getLog().info("analyse the source code for "+finalInterfaces);
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
        } catch (NotInterfacesUsefullException e) {
            getLog().info("There aren't interface with possibility of mutation");
            try {
                LogWriter.printStatisticList();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            InitUtils.setAlreadyAnalyse(true);
        }

    }

    private List<String> getInterfaces() throws NotInterfacesUsefullException {
        return UtilsProcessorImpl.getInterfacesFromStrategy(splitInterfaces());
    }


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


}
