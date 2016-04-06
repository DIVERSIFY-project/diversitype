package fr.inria.diversify.mojo.executedTests;

import fr.inria.diversify.mojo.executedTests.xmlParser.XmlParserInstru;

import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsTestProcessorImpl;
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

    private String projectDirectory;
    private String testResultDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        initializeMojo();
        getLog().info(" *** ExecutesTestMojo: " + projectDirectory + " " + testResultDirectory);

        //analyser chaque fichier TEST-project.classTest.xml
        XmlParserInstru.start(testResultDirectory);

    }

    private void initializeMojo() {
        projectDirectory= InitUtils.getProjectDirectory();
        testResultDirectory=projectDirectory+"/target/surefire-reports/";
    }
}
