package fr.inria.diversify.mojo.search;

import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsReport;
import fr.inria.diversify.utils.selectionStrategy.strategy.CandidatesStrategy;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.util.ReaderFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by guerin on 06/06/16.
 */
public class SearchMojoTest {

    private static File pom;

    private final String outputDirectory="src/resources/unit_test/ProjA/target/diversiType/";

    private final String tmpDirectory="tmp_mutation/";

    private final String outputFile="listStatistics.txt";

    @Rule
    public MojoRule rule = new MojoRule();


    @BeforeClass
    public static void initPom(){
        pom =  new File("src/resources/unit_test/ProjA/pom.xml");
    }


    @Test
    public void testPomValidation(){
        assertNotNull( pom );
        assertTrue(pom.exists());
    }

    @Test
    public void testSearchMojoInPom() throws MojoExecutionException,Exception{


        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);

    }

    @Test
    public void testDiversitypeRepositoryExist() throws MojoExecutionException, Exception{


        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        File file=new File(outputDirectory);
        assertNotNull(file );
        assertTrue(file.exists());
        assertTrue(file.isDirectory());


    }

    @Test
    public void testTmpRepositoryExist() throws MojoExecutionException, Exception{

        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        File file=new File(outputDirectory+tmpDirectory);
        assertNotNull(file );
        assertTrue(file.exists());
        assertTrue(file.isDirectory());

    }

    @Test
    public void testTxtFileResultNotEmpty() throws MojoExecutionException,Exception{
        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        File file=new File(outputDirectory+outputFile);
        assertNotNull(file );
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertTrue(file.length()>0);
    }

    @Test
    public void testCheckPomDependencies() throws Exception {
        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        File file=new File(outputDirectory+tmpDirectory+"/pom.xml");
        assertNotNull(file );
        assertTrue(file.exists());
        assertTrue(file.isFile());

        Reader reader = ReaderFactory.newXmlReader(file);
        MavenXpp3Reader xpp3 = new MavenXpp3Reader();

        Model model = xpp3.read(reader);
        List<Plugin> pluginList= model.getBuild().getPlugins();
        for(int i=0;i<pluginList.size();i++){
            if(pluginList.get(i).getGroupId().contains("fr.inria.diversify")&&pluginList.get(i).getArtifactId().contains("DiversiType")){
               fail("DiversiType dependencies is not removed");
            }
        }
    }

    @Test
    public void testInitIsCall() throws Exception {
        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        assertTrue(InitUtils.getAlreadyAnalyse());
    }

    @Test
    public void testHierarchyIsInitialize() throws Exception {
        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();

        HashMap<String,List<String>> hashMap= UtilsReport.getHierarchy();

        if(InitUtils.getCandidatesStrategy().equals(CandidatesStrategy.internal)){
            assertTrue(!hashMap.isEmpty());
        }else{
            assertTrue(hashMap.isEmpty());
        }
    }




}