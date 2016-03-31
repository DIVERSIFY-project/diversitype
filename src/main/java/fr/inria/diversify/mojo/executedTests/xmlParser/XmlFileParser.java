package fr.inria.diversify.mojo.executedTests.xmlParser;


import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.util.jar.Attributes;


/**
 * Created by guerin on 30/03/16.
 */
public class XmlFileParser {

    public static void treat(File file) {
        try{
            //TODO

            /*XMLReader reader = new org.apache.xerces.parsers.SAXParser();
            TestFileHandler handler = new TestFileHandler();
            reader.setContentHandler(handler);
            reader.parse(file.getAbsolutePath());*/
        }catch(Exception e){
            System.out.println(e);
        }
    }


    private static class TestFileHandler extends DefaultHandler{
        private String tagCourant;

        private String testCaseCurrent;
        private String testSuiteCurrent;

        /**
         * Action on new xml element
         * @param nameSpace
         * @param localName
         * @param qName
         * @param attr
         * @throws SAXException
         */
        public void startElement(String nameSpace, String localName, String qName, Attributes attr) throws SAXException {
            switch (localName){
                case "testsuite": break;
                case "testcase": break;
                case "failure": break;
            }

            tagCourant = localName;
            System.out.println("debut tag : " + localName);
        }

        /**
         * action at the xml element's ending
         * @param nameSpace
         * @param localName
         * @param qName
         * @throws SAXException
         */
        public void endElement(String nameSpace, String localName,String qName) throws SAXException {
            tagCourant = "";
            System.out.println("Fin tag " + localName);
        }

        /**
         *
         * Action for document's beginning
         *
         */
        public void startDocument() {
            //nothing to do
        }

        /**
        * Action for document's ending
        */
        public void endDocument() {

        }

        /**
         * Action on datas
         * @param caracteres
         * @param debut
         * @param longueur
         * @throws SAXException
         */
        public void characters(char[] caracteres, int debut,int longueur) throws SAXException {
            String donnees = new String(caracteres, debut, longueur);

            if (!tagCourant.equals("")) {
                if(!Character.isISOControl(caracteres[debut])) {
                    System.out.println("   Element " + tagCourant +", valeur = *" + donnees + "*");
                }
            }
        }
    }

}
