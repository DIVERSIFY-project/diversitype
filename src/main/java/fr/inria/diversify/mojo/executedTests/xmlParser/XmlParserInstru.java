package fr.inria.diversify.mojo.executedTests.xmlParser;

import fr.inria.diversify.utils.UtilsTestProcessorImpl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class scan the given repository and search test result of the plugin surefire.
 * Created by guerin on 31/03/16.
 */
public class XmlParserInstru {

    private static boolean isMainProg;
    private static List<File> children;

    /**
     * Launch the analyse on the given repository
     * @param testResultDirectory
     * @param isPrincipalProject
     */
    public static void start(String testResultDirectory, boolean isPrincipalProject) {
        File root=new File(testResultDirectory);
        Pattern name= Pattern.compile("(TEST-)(.)*(xml)");
        isMainProg=isPrincipalProject;
        children=new ArrayList<>();

        findByFilter(root, name);

    }

    private static void findByFilter(File parent, Pattern filename) {

        if (parent.isDirectory()) {
            TestFileFilter filter = new TestFileFilter(filename);
            children = Arrays.asList(parent.listFiles(filter));
            if (children.size() > 0) {
                for(int i=0;i<children.size();i++){
                    if(children.get(i).length()!=0) {
                        XmlFileParser.treat(children.get(i),isMainProg);
                    }
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

    public static boolean isMainProg() {
        return isMainProg;
    }

    public static List<File> getChildren() {
        return children;
    }
}
