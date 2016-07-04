package fr.inria.diversify.mojo.mutation;

import fr.inria.diversify.mojo.search.SearchMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by guerin on 08/06/16.
 */
public class MutationMojoTest {

    private static File pom;

    private final String outputDirectory="src/resources/unit_test/ProjA/target/diversiType/";

    private final String tmpDirectory="tmp_mutation/";

    private final String outputFilesearch="listStatistics.txt";

    private final String outputFilemutation="diversiType.txt";

    @Rule
    public MojoRule rule = new MojoRule();



    @BeforeClass
    public static void initPom() throws Exception {
        pom =  new File("src/resources/unit_test/ProjA/pom.xml");
    }


    @Before
    public void launchSeachMojo() throws Exception {
        SearchMojo myMojo = (SearchMojo) rule.lookupMojo( "search", pom );
        assertNotNull(myMojo);
        myMojo.execute();
    }

    @Test
    public void testMutationMojoInPom() throws MojoExecutionException,Exception{
        MutationMojo myMojo = (MutationMojo) rule.lookupMojo( "mutation", pom );
        assertNotNull(myMojo);
    }

    @Test
    public void testFileOutput() throws MojoExecutionException,Exception{

        MutationMojo myMojo = (MutationMojo) rule.lookupMojo( "mutation", pom );
        assertNotNull(myMojo);

        myMojo.execute();

        File file=new File(outputDirectory+tmpDirectory);
        assertNotNull(file);
        assertTrue(!file.exists());

       /* File fileoutmut=new File(outputDirectory+outputFilemutation);
        assertNotNull(fileoutmut);
        assertTrue(fileoutmut.exists());
        assertTrue(fileoutmut.isFile());*/

        File fileoutsea=new File(outputDirectory+outputFilesearch);
        assertNotNull(fileoutsea);
        assertTrue(fileoutsea.exists());
        assertTrue(fileoutsea.isFile());
    }


}