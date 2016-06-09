package fr.inria.diversify.mojo.executedTests.xmlParser;

import fr.inria.diversify.utils.UtilsProcessorImpl;
import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by guerin on 09/06/16.
 */
public class XmlFileParserTest {


    private final String directory="src/resources/unit_test/filesForXmlParser/";

    @Before
    public void cleanList(){
        UtilsTestProcessorImpl.cleanTestFailMutation();
        UtilsTestProcessorImpl.clean();
    }

    @Test
    public void testWithoutFailedTestForMainProject(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.AppTest.xml"),true);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
    }

    @Test
    public void testWithoutFailedTestForMutation(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.AppTest.xml"),false);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
    }

    @Test
    public void testWithFailedTestForMainProject(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.ATest.xml"),true);
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().size()==1);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
    }

    @Test
    public void testWithFailedTestForMutation(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.ATest.xml"),false);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().size()==1);
    }

    @Test
    public void testWith3FailedTestForMainProject(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.ClassUseBTest.xml"),true);
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().size()==3);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
    }

    @Test
    public void testWith3FailedTestForMutation(){
        XmlFileParser.treat(new File(directory+"/withFail/TEST-fr.inria.diversify.ClassUseBTest.xml"),false);
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().size()==3);
    }

    @Test
    public void testWithCorrectTestForMainProject(){
        XmlFileParser.treat(new File(directory + "/withFail/TEST-fr.inria.diversify.ClassUseBTest.xml"), true);
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFail().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().contains("fr.inria.diversify.ClassUseBTest:testAdd3"));
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().contains("fr.inria.diversify.ClassUseBTest:testAdd4"));
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFail().contains("fr.inria.diversify.ClassUseBTest:testMul"));
     ;
    }

    @Test
    public void testWithCorrectTestForForMutation(){
        XmlFileParser.treat(new File(directory + "/withFail/TEST-fr.inria.diversify.ClassUseBTest.xml"), false);
        assertTrue(!UtilsTestProcessorImpl.getTestSuiteFailCurrentT().isEmpty());
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().contains("fr.inria.diversify.ClassUseBTest:testAdd3"));
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().contains("fr.inria.diversify.ClassUseBTest:testAdd4"));
        assertTrue(UtilsTestProcessorImpl.getTestSuiteFailCurrentT().contains("fr.inria.diversify.ClassUseBTest:testMul"));
    }

}