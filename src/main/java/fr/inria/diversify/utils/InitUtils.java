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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
    private static String testDir="src/test/java/";

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

    private static String learningExt="target/diversiType_learning/";

    /**
     * directory contain the learned information during plugin execution
     */
    private static String learningDirectory;

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

    /**
     * Check if the project has already analyse by the statisticProcessor
     */
    private static boolean alreadyAnalyse;

    /**
     * represent the path of the project's jar
     */
    private static String jarLocation;

    /**
     * ArtefactId of the project
     */
    private static String groupId;




    /**
     * Initialization of project information
     * @param dirProject
     * @param mutationStrat
     * @param selectedCandidatesStratregy
     * @param jarLocat
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String init(String dirProject, String mutationStrat, String selectedCandidatesStratregy, String jarLocat) throws IOException, InterruptedException {
        resolveDepedencies(dirProject);

        if(alreadyInit){

            return tmpDirectory;
        }

        alreadyAnalyse=false;
        mutationStrategy=getMutationStrategy(mutationStrat);
        candidatesStrategy=getCandidatesStrategy(selectedCandidatesStratregy);

        projectDirectory=dirProject;
        jarLocation=jarLocat;
        outputDirectory=projectDirectory+ext;
        tmpDirectory = outputDirectory + "tmp_mutation/";
        learningDirectory=projectDirectory+learningExt;

        cleanDiversitypeRepository();
        addLearningRepository();

        File dir = new File(tmpDirectory);
        dir.mkdirs();
        FileUtils.copyDirectory(new File(dirProject), dir, new TargetFileFilter());

        initPomTmp();

        loadJarProject();

        alreadyInit=true;

        return tmpDirectory;
    }

    private static void addLearningRepository() {
        File dir = new File(learningDirectory);
        if(dir !=null && !dir.exists()) {
            dir.mkdirs();
        }

    }

    private static void cleanDiversitypeRepository() {

        File dir=new File(outputDirectory);

        if(dir!=null && dir.exists()) {
            deleteRepository(new File(outputDirectory));
        }
    }

    /**
     * Add project's jar to the classPath
     */
    private static void loadJarProject() {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        try{
            //TODO get parameter for jar name
            URL url = new File(jarLocation).toURI().toURL();
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class<?>[]{URL.class});
            addURL.setAccessible(true);
            addURL.invoke(systemClassLoader, new Object[]{url});
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Resolve project's dependencies
     * @param dirProject
     */
    public static void resolveDepedencies(String dirProject){
        MavenDependencyResolver mavenDependencyResolver=new MavenDependencyResolver();
        try {
            mavenDependencyResolver.DependencyResolver(dirProject + "pom.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * Modification of the temporary pom.xml: delete the dependence to diversiType plugin
     * avoid recursive calls to diversitype plugin
     */
    private static void initPomTmp() {

        File pomFile=new File(tmpDirectory+"/pom.xml");
        Reader reader= null;
        try {
            reader = ReaderFactory.newXmlReader(pomFile);
            MavenXpp3Reader xpp3=new MavenXpp3Reader();

            Model model=xpp3.read(reader);
            groupId=model.getGroupId();
            checkPlugins(model);

            MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
            Writer fileWriter= WriterFactory.newXmlWriter(pomFile);
            mavenXpp3Writer.write(fileWriter, model);
            IOUtil.close(fileWriter);


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

    public static String getLearningDirectory() {
        return learningDirectory;
    }

    public static boolean getAlreadyAnalyse(){
        return alreadyAnalyse;
    }

    public static void setAlreadyInit(boolean alreadyInit) {
        InitUtils.alreadyInit = alreadyInit;
    }

    public static String getGroupId() {
        return groupId;
    }

    public static void deleteTmpDirectory() {
        File dir = new File(tmpDirectory);
        deleteRepository(dir);
    }

    private static void deleteRepository(File r){
        File [] fileList = r.listFiles();
        for(int i = 0;i<fileList.length;i++){
            if(fileList[i].isDirectory() ){
                deleteRepository(fileList[i]);
                fileList[i].delete();
            }else{
                fileList[i].delete();
            }
        }
        r.delete();
    }

    public static void setAlreadyAnalyse(boolean alreadyAnalyse) {
        InitUtils.alreadyAnalyse = alreadyAnalyse;
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
