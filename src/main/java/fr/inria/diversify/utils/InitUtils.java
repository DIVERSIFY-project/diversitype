package fr.inria.diversify.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
     * tmpDirectory contains sosies during plugin's process
     */
    private static String tmpDirectory;

    /**
     * OutputDirectory is directory that contains plugin's result
     */
    private static String outputDirectory;
    private static String ext="/target/diversiType/";


    public static String init(String dirProject) throws IOException, InterruptedException {
        projectDirectory=dirProject;
        outputDirectory=projectDirectory+ext;
        tmpDirectory = outputDirectory + "tmp_mutation/";

        File dir = new File(tmpDirectory);
        dir.mkdirs();
        FileUtils.copyDirectory(new File(dirProject), dir,new TargetFileFilter() );

        return tmpDirectory;
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



    public static String getSourceDirectory(){
        return srcDir;
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
}
