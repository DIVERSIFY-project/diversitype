package fr.inria.diversify.mojo.executedTests.xmlParser;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by guerin on 31/03/16.
 */
public class XmlParserInstru {

    public static void main(String[] args){
        start("/home/guerin/Documents/INRIA/ExProj/ProjA/target/surefire-reports/");

    }

    public static void start(String testResultDirectory) {
        File root=new File(testResultDirectory);
        Pattern name= Pattern.compile("(TEST-)(.)*(xml)");

        findByFilter(root,name);

    }

    public static void findByFilter(File parent, Pattern filename) {
        String path = null;
        if (parent.isDirectory()) {
            TestFileFilter filter = new TestFileFilter(filename);
            List<File> children = Arrays.asList(parent.listFiles(filter));
            if (children.size() > 0) {
                for(int i=0;i<children.size();i++){
                    XmlFileParser.treat(children.get(i));
                }
            }
            List<File> files = Arrays.asList(parent.listFiles());
            Iterator<File> fileIterator = files.iterator();
            while(fileIterator.hasNext()) {
                findByFilter(fileIterator.next(), filename);
            }
        }
    }

    private static class TestFileFilter implements FileFilter {
        private final Pattern pattern;

        /**
         * Construct a new regular expression filter.
         *
         * @param p regular string expression to match
         * @throws IllegalArgumentException if the pattern is null
         */
        public TestFileFilter(Pattern p) {
            if (p == null) {
                throw new IllegalArgumentException("Pattern is missing");
            }

            this.pattern = p;
        }

        @Override
        public boolean accept(File pathname) {
            return (pattern.matcher(pathname.getName()).matches());
        }

    }
}
