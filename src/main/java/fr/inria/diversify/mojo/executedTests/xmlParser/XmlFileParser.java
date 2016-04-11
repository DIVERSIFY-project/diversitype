package fr.inria.diversify.mojo.executedTests.xmlParser;


import fr.inria.diversify.utils.UtilsTestProcessorImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.jar.Attributes;


/**
 * Created by guerin on 30/03/16.
 */
public class XmlFileParser {

    private static boolean isPrinicipalProg;

    public static void treat(File file, boolean isMainProg) {
        XMLReader reader;
        TestFileHandler handler = new TestFileHandler();
        isPrinicipalProg=isMainProg;
        try{
            reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            reader.setContentHandler(handler);
            reader.parse(file.getAbsolutePath());

        }catch(Exception e) {
            try {
                // If unable to create an instance
                // use the XMLReader from JAXP
                SAXParserFactory m_parserFactory = SAXParserFactory.newInstance();
                m_parserFactory.setNamespaceAware(true);
                reader = m_parserFactory.newSAXParser().getXMLReader();
                reader.setContentHandler(handler);
                reader.parse(file.getAbsolutePath());
            }catch (Exception e2){
                System.out.println("exception: " + e2);
            }
        }


    }


    private static class TestFileHandler implements ContentHandler {
        private Locator locator;
        private String tagCourant;

        private String testCaseCurrent;
        private String testSuiteCurrent;
        private String data;
        private String failure;

        public TestFileHandler(){
            super();
            this.locator=new LocatorImpl();
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator=locator;
        }

        @Override
        public void startDocument() throws SAXException {
            tagCourant=null;
            testCaseCurrent=null;
            testSuiteCurrent=null;
            data="";
        }

        @Override
        public void endDocument() throws SAXException {

        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {

        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {

        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException {
            switch(localName){
                case "testsuite": testSuiteCurrent=findSpecificAtt(atts, "name");break;
                case "testcase": testCaseCurrent=findSpecificAtt(atts,"classname"); break;
                case "failure": failure=findSpecificAtt(atts,"type");data="";break;
            }
        }

        private String findSpecificAtt(org.xml.sax.Attributes atts, String name) {
            for (int index = 0; index < atts.getLength(); index++) {
                if (atts.getLocalName(index).equals(name)){
                    return  atts.getValue(index);
                }
            }
            return null;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(localName.equals("failure")){
                if(isPrinicipalProg){
                    UtilsTestProcessorImpl.addTestFail(testSuiteCurrent,testCaseCurrent,failure,data);
                }else{
                    UtilsTestProcessorImpl.addTestFailMutation(testSuiteCurrent,testCaseCurrent,failure,data);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            data=data+(new String(ch, start, length));

        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {

        }

        @Override
        public void skippedEntity(String name) throws SAXException {

        }
    }

}
