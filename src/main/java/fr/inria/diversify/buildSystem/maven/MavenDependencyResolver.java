package fr.inria.diversify.buildSystem.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * User: Simon
 * Date: 6/12/13
 * Time: 3:47 PM
 */
public class MavenDependencyResolver {
    Set<URL> jarURL = new HashSet<>();
    Set<String> dependencyResolve = new HashSet<>();
    Properties properties;
    String baseDir;
    MavenResolver resolver;

    ArrayList<String> repositoriesUrls;


    public void DependencyResolver(String pomFile) throws Exception {

        String[] split = pomFile.split("/");
        baseDir = "";
        for(int i = 0;  i <  split.length - 1; i++) {
            baseDir += split[i] + "/";
        }
        MavenProject project = loadProject(new File(pomFile));
        initMavenResolver();
        resolveAllDependencies(project);
        addApplicationClasses(new File(pomFile));
    }

    public MavenProject loadProject(File pomFile) throws IOException, XmlPullParserException {
        MavenProject ret;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        //Removed null and file exists protections that mask errors
        FileReader reader = null;
        reader = new FileReader(pomFile);
        Model model = mavenReader.read(reader);
        model.setPomFile(pomFile);
        ret = new MavenProject(model);
        reader.close();

        return ret;
    }

    protected void addApplicationClasses(File pomFile) throws MalformedURLException {
        jarURL.add((new File( pomFile.getParent() + "/target/classes/").toURL()));
        URLClassLoader child = new URLClassLoader(jarURL.toArray(new URL[jarURL.size()]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(child);
    }


    protected void initMavenResolver() {
        resolver = new MavenResolver();
        resolver.setBasePath(System.getProperty("user.home") + File.separator + ".m2/repository");

        repositoriesUrls = new ArrayList<>();
        repositoriesUrls.add("http://repo1.maven.org/maven2/");
    }

    protected void updateRepositoriesUrl(MavenProject project) {
        for(int i=0;i<project.getRepositories().size();i++) {
            repositoriesUrls.add(((Repository)project.getRepositories().get(i)).getUrl());
        }
    }

    public void resolveAllDependencies(MavenProject project) throws MalformedURLException {
        updateRepositoriesUrl(project);
        updateProperties(project.getProperties());

        for (int i=0;i<project.getDependencies().size();i++) {
            Dependency dependency= (Dependency) project.getDependencies().get(i);
            try {
                String artifactId = "mvn:" + resolveName(dependency.getGroupId(), properties) +
                        ":" + resolveName(dependency.getArtifactId(), properties) +
                        ":" + resolveName(dependency.getVersion(), properties);

                File cachedFile;
                if(dependency.getScope() != null && dependency.getScope().equals("system")) {
                    cachedFile = new File(resolveName(dependency.getSystemPath(), properties));
                } else {
                    cachedFile = resolver.resolve(artifactId + ":" + resolveName(dependency.getType(), properties), repositoriesUrls);
                }
                jarURL.add(cachedFile.toURI().toURL());


                File pomD = resolver.resolve(artifactId + ":pom", repositoriesUrls);
                if(!dependencyResolve.contains(pomD.getAbsolutePath())) {
                    dependencyResolve.add(pomD.getAbsolutePath());
                    resolveAllDependencies(loadProject(pomD));
                }

            } catch (Exception e) {}

        }
        for(int i=0;i<project.getModules().size();i++) {
            String module= (String) project.getModules().get(i);
            try {
                resolveModuleDependencies(module);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        URLClassLoader child = new URLClassLoader(jarURL.toArray(new URL[jarURL.size()]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(child);
    }

    protected void updateProperties(Properties properties) {
        if(this.properties == null) {
            this.properties = new Properties(properties);
            properties.setProperty("basedir", baseDir);
        } else {
            for (Object key : properties.keySet()) {
                this.properties.put(key, properties.get(key));
            }
        }
    }

    protected void resolveModuleDependencies(String moduleName) throws IOException, XmlPullParserException {
        MavenProject project = loadProject(new File(baseDir + moduleName + "/pom.xml"));
        resolveAllDependencies(project);
    }



    protected String resolveName(String string, Properties properties) {
        char[] chars = string.toCharArray();
        int replaceBegin = -1;
        String id = "";
        for (int i = 0; i < chars.length; i++) {
            if (replaceBegin != -1 && chars[i] != '{' && chars[i] != '}') {
                id += chars[i];
            }
            if (replaceBegin != -1 && chars[i] == '}') {
                string = string.substring(0, replaceBegin) + properties.getProperty(id) + string.substring(i + 1, string.length());
                replaceBegin = -1;
                id = "";
            }
            if (chars[i] == '$' && i + 1 < chars.length && chars[i + 1] == '{') {
                replaceBegin = i;
            }
        }
        return string;
    }


}
