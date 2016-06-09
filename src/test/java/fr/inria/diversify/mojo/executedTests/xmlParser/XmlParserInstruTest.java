package fr.inria.diversify.mojo.executedTests.xmlParser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by guerin on 09/06/16.
 */
public class XmlParserInstruTest {

    private final String directory="src/resources/unit_test/filesForXmlParser/";

    @Test
    public void testIsMainProject(){
        XmlParserInstru.start(directory + "empty", true);
        assertTrue(XmlParserInstru.isMainProg());
    }

    @Test
    public void testIsNotMainProject(){
        XmlParserInstru.start(directory+"empty",false);
        assertTrue( !XmlParserInstru.isMainProg());
    }

    @Test
    public void testNoFileMatch(){
        XmlParserInstru.start(directory + "empty", true);
        assertTrue(XmlParserInstru.getChildren().isEmpty());
    }

    @Test
    public void testFileMatch(){
        XmlParserInstru.start(directory + "withFail", true);
        assertTrue(!XmlParserInstru.getChildren().isEmpty());
    }

    @Test
    public void testFileMatchLvl2(){
        XmlParserInstru.start(directory + "directoryTree", true);
        assertTrue(XmlParserInstru.getChildren().size() == 1);
    }

}