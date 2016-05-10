package fr.inria.diversify.utils;


import fr.inria.diversify.buildSystem.maven.MavenDependencyResolver;
import fr.inria.diversify.mojo.mutation.strategy.MutationStrategy;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.List;

/**
 *
 * Class manage project initialization
 * Created by guerin on 04/04/16.
 */
public class InitUtils {

    /**
     * the project to mutate
     */
    private static String projectDirectory;


    /**
     * source directory
     */
    private static String srcDir="src/main/java/";

    /**
     * Test directory
     */
    private static String testDir="src/test/java";

    /**
     * tmpDirectory contains sosies during plugin's process
     */
    private static String tmpDirectory;

    /**
     * OutputDirectory is directory that contains plugin's result
     */
    private static String outputDirectory;

    /**
     * directory for diversitype plugin
     */
    private static String ext="target/diversiType/";

    /**
     * boolean for init the static class only once
     */
    private static boolean alreadyInit=false;

    /**
     *Strategy use for select the new ConstructorCall
     */
    private static MutationStrategy mutationStrategy;

    /**
     * Strategy for select candidates which can be mutate
     */
    private static CandidatesStrategy candidatesStrategy;


    public static String init(String dirProject, String mutationStrat, String selectedCandidatesStratregy) throws IOException, InterruptedException {
        resolveDepedencies(dirProject);

        if(alreadyInit){

            return tmpDirectory;
        }

        mutationStrategy=getMutationStrategy(mutationStrat);
        candidatesStrategy=getCandidatesStrategy(selectedCandidatesStratregy);

        projectDirectory=dirProject;
        outputDirectory=projectDirectory+ext;
        tmpDirectory = outputDirectory + "tmp_mutation/";

        File dir = new File(tmpDirectory);
        dir.mkdirs();
        FileUtils.copyDirectory(new File(dirProject), dir, new TargetFileFilter());


        initPomTmp();

        alreadyInit=true;
        return tmpDirectory;
    }





    public static void resolveDepedencies(String dirProject){
        MavenDependencyResolver mavenDependencyResolver=new MavenDependencyResolver();
        try {
            mavenDependencyResolver.DependencyResolver(dirProject+"pom.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initPomTmp() {

        File pomFile=new File(tmpDirectory+"/pom.xml");
        Reader reader= null;
        try {
            reader = ReaderFactory.newXmlReader(pomFile);
            MavenXpp3Reader xpp3=new MavenXpp3Reader();

            Model model=xpp3.read(reader);
            checkPlugins(model);

            MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
            Writer fileWriter= WriterFactory.newXmlWriter(pomFile);
            mavenXpp3Writer.write(fileWriter, model);
            IOUtil.close(fileWriter);

            System.out.print("");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private static void checkPlugins(Model model) {
        List<Plugin> pluginList= model.getBuild().getPlugins();
        for(int i=0;i<pluginList.size();i++){
            if(pluginList.get(i).getGroupId().contains("fr.inria.diversify")&&pluginList.get(i).getArtifactId().contains("DiversiType")){
                pluginList.remove(pluginList.get(i));
            }
        }
        model.getBuild().setPlugins(pluginList);
    }

    public static void setOutputDirectory(String outputDirectory) {
        InitUtils.outputDirectory = outputDirectory+ext;
    }

    public static String getOutput() {
        return outputDirectory;
    }

    public static void setProject(String project) {
        InitUtils.projectDirectory = project;
    }

    public static String getProjectDirectory() {
        return projectDirectory;
    }

    public static void setProjectDirectory(String projectDirectory) {
        InitUtils.projectDirectory = projectDirectory;
    }

    public static String getTmpDirectory() {
        return tmpDirectory;
    }

    public static void setTmpDirectory(String tmpDirectory) {
        InitUtils.tmpDirectory = tmpDirectory;
    }

    public static String getTestDirectory(){
        return testDir;
    }

    public static String getSourceDirectory(){
        return srcDir;
    }

    public static MutationStrategy getMutationStrategy(){
        return mutationStrategy;
    }

    public static CandidatesStrategy getCandidatesStrategy(){
        return candidatesStrategy;
    }

    private static class TargetFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            if (pathname.getAbsolutePath().contains(projectDirectory+"/target")) {
                return false;
            }
            return true;
        }
    }

    private static CandidatesStrategy getCandidatesStrategy(String selectedCandidatesStratregy) {
        switch (selectedCandidatesStratregy){
            case "internal": return CandidatesStrategy.internal;
            case "external":return CandidatesStrategy.external;
            default:return CandidatesStrategy.internal;
        }
    }

    private static MutationStrategy getMutationStrategy(String mutationStrategy) {
        switch (mutationStrategy){
            case "one": return MutationStrategy.one;
            case "random":return MutationStrategy.random;
            default:return MutationStrategy.random;
        }
    }
}
