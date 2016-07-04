package fr.inria.diversify.utils;

import fr.inria.diversify.utils.json.ReportJsonManager;
import spoon.reflect.reference.CtTypeReference;

import java.io.*;
import java.util.*;

/**
 * Created by guerin on 24/06/16.
 *
 * This class manage all the report file
 * txt or hmtl
 */
public class UtilsReport {

    private static String repoJSON="infos/";

    /**
     * represent link between interfaces and all possibility
     */
    private static HashMap<String,List<String>> hierarchy=new HashMap<>();



    /**
     *
     */
    private static boolean hierarchyIsAlreadyLearning=false;

    /**
     * represent link between interface and concrete type (lvl 1)
     */
    private static HashMap<String,List<String>> interfaceChildren=new HashMap<>();

    /**
     *represent link between abstract class and concrete type (lvl 1)
     */
    private static List<String> abstractClass=new ArrayList<>();

    /**
     * represent link between class and his children (lvl1)
     */
    private static HashMap<String,List<String>> classChildren=new HashMap<>();

    private static HashMap<Integer,String> numberToClasses =new HashMap<>();

    private static HashMap<String,Integer> classesToNumber =new HashMap<>();

    private static Integer classesNmber=0;


    /**
     * Get hashmap with Interfaces and all their children
     * This HashMap allow to find possibility mutation
     * @return
     */
    public static HashMap<String, List<String>> getHierarchy() {
        return hierarchy;
    }



    private static void printHierarchyFile() {
        try {
            PrintWriter printWriter=new PrintWriter(InitUtils.getReportDirectory()+"hierarchy.txt");
            Set<String> set=hierarchy.keySet();
            Iterator<String> it=set.iterator();
            while (it.hasNext()){
                String current=it.next();
                List<String> list=hierarchy.get(current);
                printWriter.write(current+" : "+list+"\n");
            }
            printWriter.close();
        } catch (FileNotFoundException e) {

        }

    }

    /**
     * Print all files which describe the hierarchy of the project
     * This files are print in diversitfy repository
     */
    public static void printHierarchy() {
        if(!hierarchyIsAlreadyLearning) {
            try {
                InitUtils.createDirectory(InitUtils.getReportDirectory() + repoJSON);
                /*printClassChildren();
                printAbstractClass();
                printInterfaceChildren();*/
                printHierarchyFile();
                //generateGraphJSON();
                generateJSON();
                //generateClassesJSON();
                copyIndexHtml();
            } catch (FileNotFoundException e) {
                System.out.print(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateJSON() throws FileNotFoundException {
        ReportJsonManager jsonManager=new ReportJsonManager(InitUtils.getReportDirectory() + repoJSON);
        jsonManager.printJsonFiles();
    }


    /**
     * Print HTML code to the report directory
     */
    private static void copyIndexHtml() throws IOException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory() + "index.html");
        printWriter.write(getIndexHTMLcode());
        printWriter.close();
    }


   /* private static void printClassChildren() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory() + "Classes.txt");
        Set<String> set = classChildren.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = classChildren.get(current);
            printWriter.write(current + " : " + list + "\n");
        }
        printWriter.close();
    }

    private static void printAbstractClass() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory() + "AbstractClasses.txt");

        for(int i=0;i<abstractClass.size();i++) {
            printWriter.write(abstractClass.get(i)+ "\n");
        }

        printWriter.close();
    }

    private static void printInterfaceChildren() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory() + "Interfaces.txt");
        Set<String> set = interfaceChildren.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = interfaceChildren.get(current);
            printWriter.write(current + " : " + list + "\n");
        }
        printWriter.close();
    }*/

    /**
     * Record the current abstract class, its superClass and superInterface
     * @param qualifiedName
     * @param superInterfaces
     * @param superClass
     */
    public static void addAbstractClass(String qualifiedName, Set<CtTypeReference<?>> superInterfaces, CtTypeReference<?> superClass) {
        if(!abstractClass.contains(qualifiedName)) {
            abstractClass.add(qualifiedName);

        }
        addSuperInterfaces(qualifiedName, superInterfaces);
        addSuperClass(qualifiedName, superClass);

    }




    /**
     * Record superClass and superInterface for the current class, which not interface or abstract class
     * @param qualifiedName
     * @param superInterfaces
     * @param superClass
     */
    public static void addClass(String qualifiedName, Set<CtTypeReference<?>> superInterfaces, CtTypeReference<?> superClass) {
        addSuperInterfaces(qualifiedName, superInterfaces);
        addSuperClass(qualifiedName, superClass);
    }

    /**
     * Record the current interface and its superClass
     * @param qualifiedName
     * @param strings: superClasses
     */
    public static void addInterface(String qualifiedName, Set<CtTypeReference<?>> strings) {
        addSuperInterfaces(qualifiedName, strings);
    }

    private static void addToAllClasses(String qualifiedName) {
        if(!classesToNumber.containsKey(qualifiedName)){
            numberToClasses.put(classesNmber, qualifiedName);
            classesToNumber.put(qualifiedName,classesNmber);
            classesNmber++;
        }

    }


    private static void addSuperClass(String subClass, CtTypeReference<?> superClass) {
        addToAllClasses(subClass);
        if(isClassProject(superClass)){
            String superClassName=superClass.getQualifiedName();
            addToAllClasses(superClassName);
            if(classChildren.containsKey(superClassName)){
                if(!classChildren.get(superClassName).contains(subClass)) {
                    classChildren.get(superClassName).add(subClass);
                }
            }else{
                List<String> list=new ArrayList<>();
                list.add(subClass);
                classChildren.put(superClassName,list);
            }
        }
    }

    private static void addSuperInterfaces(String qualifiedName, Set<CtTypeReference<?>> superInterfaces) {
        Iterator<CtTypeReference<?>> iterator=superInterfaces.iterator();
        addToAllClasses(qualifiedName);
        while(iterator.hasNext()){
            CtTypeReference current=iterator.next();

            if(isClassProject(current)){
                addInterfaceLink(current.getQualifiedName(), qualifiedName);
                addToAllClasses(current.getQualifiedName());
            }
        }
    }

    private static boolean isClassProject(CtTypeReference<?> type){
        String pack;
        if(type==null){
            return false;
        }
        if(type.getPackage()==null){
            pack=type.getDeclaringType().getPackage().getSimpleName();
        }else{
            pack=type.getPackage().getSimpleName();
        }

        if(pack.contains(InitUtils.getGroupId())){
            return true;
        }
        return false;
    }

    private static void addInterfaceLink(String superClass, String subClass) {
        if(interfaceChildren.containsKey(superClass)){
            if(!interfaceChildren.get(superClass).contains(subClass)) {
                interfaceChildren.get(superClass).add(subClass);
            }
        }else{
            List<String> list=new ArrayList<>();
            list.add(subClass);
            interfaceChildren.put(superClass,list);
        }
    }

    /**
     * Analyse interfacesChildren, abstractClass and classChildren
     * and deduct the hierarchy of the project
     */
    public static void createHierarchy() {
        Set<String> allInterfaces =interfaceChildren.keySet();
        Iterator<String> iterator=allInterfaces.iterator();
        while (iterator.hasNext()){
            String current=iterator.next();
            List<String> childrenLvlOne=interfaceChildren.get(current);
            for (int i=0;i<childrenLvlOne.size(); i++) {
                addChildrenToHierarchy(current, childrenLvlOne.get(i));
            }

        }
    }

    private static void addChildrenToHierarchy(String current, String childrenLvlOne) {
        if( !interfaceChildren.keySet().contains(childrenLvlOne) && !abstractClass.contains(childrenLvlOne)){
            addHierarchyLink(current,childrenLvlOne);
        }
        if(interfaceChildren.containsKey(childrenLvlOne)) {
            List<String> list=interfaceChildren.get(childrenLvlOne);
            for(int i=0;i<list.size();i++){
                addChildrenToHierarchy(current,list.get(i));
            }
        }
        if(classChildren.containsKey(childrenLvlOne)){
            List<String> list=classChildren.get(childrenLvlOne);
            for(int i=0;i<list.size();i++){
                addChildrenToHierarchy(current,list.get(i));
            }
        }
    }

    /**
     * Add link between Interface and its subclass
     * @param superClass
     * @param subClass
     */
    private static void addHierarchyLink(String superClass, String subClass) {
        if(hierarchy.containsKey(superClass)){
            if(!hierarchy.get(superClass).contains(subClass)) {
                hierarchy.get(superClass).add(subClass);
            }
        }else{
            List<String> list=new ArrayList<>();
            list.add(subClass);
            hierarchy.put(superClass, list);
        }
    }

    /**
     * read the project's hierarchy in the learning txt file.
     * @param hierarchy: txt File
     */
    public static void readHierarchyFile(File hierarchy) throws IOException {
        hierarchyIsAlreadyLearning=true;
        FileReader fileReader=new FileReader(hierarchy);

        BufferedReader bufferedReader=new BufferedReader(fileReader);
        String current=bufferedReader.readLine();
        while (current!=null){
            splitAndRecordCurrentHierarchyLine(current);
            current=bufferedReader.readLine();
        }
    }

    private static void splitAndRecordCurrentHierarchyLine(String current) {
        String[] split=current.split(" : ");
        List<String> list=new ArrayList<>(Arrays.asList(split[1].split(", ")));
        String first=list.get(0).substring(1);
        list.set(0, first);
        String last=list.get(list.size()-1).substring(0, list.get(list.size() - 1).length() - 1);
        list.set(list.size() - 1, last);
        hierarchy.put(split[0], list);
    }

    public static Set<String> getClasseSet(){
        return classesToNumber.keySet();
    }

    public static HashMap<String, Integer> getClassesToNumber() {
        return classesToNumber;
    }

    public static HashMap<Integer, String> getNumberToClasses() {
        return numberToClasses;
    }

    public static HashMap<String, List<String>> getClassChildren() {
        return classChildren;
    }

    public static List<String> getAbstractClass() {
        return abstractClass;
    }

    public static HashMap<String, List<String>> getInterfaceChildren() {
        return interfaceChildren;
    }

    /*private static void generateGraphJSON() throws FileNotFoundException {

        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory()+repoJSON + "graph.json");
        printWriter.write("{");
        printWriter.write("\"nodes\":[");
        addClassesIntoNodes(printWriter);
        printWriter.write("],");
        printWriter.write("\"links\":[");
        String links=addLinksIntoLinks();
        if(links.length()>0){
            links=links.substring(0,links.length()-2);
        }
        printWriter.write(links);
        printWriter.write("]}");
        printWriter.close();
    }



    private static String addLinksIntoLinks() {
        String interfaces=addLinksFromHashMap(interfaceChildren);
        String classes=addLinksFromHashMap(classChildren);
        return interfaces+classes;
    }

    private static String addLinksFromHashMap( HashMap<String, List<String>> h){
        Set<String> interfaces=h.keySet();
        Iterator<String> iterator=interfaces.iterator();
        String result="";
        while (iterator.hasNext()){
            String current=iterator.next();
            List<String> list=h.get(current);
            int nb=classesToNumber.get(current);
            for (int j=0;j<list.size();j++){
                result=result+addLinktoJSON( classesToNumber.get(list.get(j)), nb);
            }
        }
        return result;
    }

    private static String addLinktoJSON( int source, int target) {
        return "{\"source\":"+source+",\"target\":"+target+"},\n";
    }

    private static void addClassesIntoNodes(PrintWriter printWriter) {
        for(int i=0;i<numberToClasses.size();i++) {
            String current=numberToClasses.get(i);
            int value=getGroupNumber(current);
            printWriter.write("{\"name\":\"" +current +"\",\"group\":" +value+"}");
            if(i<numberToClasses.size()-1){
                printWriter.write(",\n");
            }
        }
    }

    private static int getGroupNumber(String current) {
        if(interfaceChildren.containsKey(current)){
            return 1;
        }else if(abstractClass.contains(current)){
            return 2;
        }
        return 3;
    }*/

    /*private static void generateClassesJSON() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(InitUtils.getReportDirectory()+repoJSON + "classes.json");
        Set<String> classes=classesToNumber.keySet();
        Iterator<String> it=classes.iterator();
        String json="{\"classes\":[";
        while (it.hasNext()){
            String current=it.next();
            json=json+"\""+current+"\",";
        }
        String sub=json.substring(0,json.length()-1);
        printWriter.write( sub+"]}");
        printWriter.close();

    }*/


    public static String getIndexHTMLcode(){
        return "<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<title>DiversiType</title>\n" +
                "<style>\n" +
                "\n" +
                "    .node {\n" +
                "    stroke: #fff;\n" +
                "    stroke-width: 1.5px;\n" +
                "    }\n" +
                "\n" +
                "    .link {\n" +
                "    fill: none;\n" +
                "    stroke: #bbb;\n" +
                "    }\n" +
                "\n" +
                "</style>\n" +
                "<body>\n" +
                "<script src=\"//d3js.org/d3.v3.min.js\"></script>\n" +
                "<script>\n" +
                "\n" +
                "var endmarkers = [\n" +
                "    { id: 0, name: 'circle', path: 'M 0, 0  m -5, 0  a 5,5 0 1,0 10,0  a 5,5 0 1,0 -10,0', viewbox: '-6 -6 12 12' }\n" +
                "  , { id: 1, name: 'square', path: 'M 0,0 m -5,-5 L 5,-5 L 5,5 L -5,5 Z', viewbox: '-5 -5 10 10' }\n" +
                "  , { id: 2, name: 'arrow', path: 'M 0,0 m -5,-5 L 5,0 L -5,5 Z', viewbox: '-5 -5 10 10' }\n" +
                "  , { id: 2, name: 'stub', path: 'M 0,0 m -1,-5 L 1,-5 L 1,5 L -1,5 Z', viewbox: '-1 -5 2 10' }\n" +
                "  ]\n" +
                "\n" +
                "var width = 960,\n" +
                "    height = 500;\n" +
                "\n" +
                "var color = d3.scale.category20();\n" +
                "\n" +
                "var force = d3.layout.force()\n" +
                "    .linkDistance(10)\n" +
                "    .linkStrength(2)\n" +
                "    .size([width, height]);\n" +
                "\n" +
                "var svg = d3.select(\"body\").append(\"svg\")\n" +
                "    .attr(\"width\", width)\n" +
                "    .attr(\"height\", height);\n" +
                "\n" +
                "var defs = svg.append('svg:defs')\n" +
                "\n" +
                "var paths = svg.append('svg:g')\n" +
                "    .attr('id', 'markers');\n" +
                "\n" +
                "\n" +
                "d3.json(\"infos/graph.json\", function(error, graph) {\n" +
                "  if (error) throw error;\n" +
                "\n" +
                "  var nodes = graph.nodes.slice(),\n" +
                "      links = [],\n" +
                "      bilinks = [];\n" +
                "\n" +
                "  graph.links.forEach(function(link) {\n" +
                "    var s = nodes[link.source],\n" +
                "        t = nodes[link.target],\n" +
                "        i = {}; // intermediate node\n" +
                "    nodes.push(i);\n" +
                "    links.push({source: s, target: i}, {source: i, target: t});\n" +
                "    bilinks.push([s, i, t]);\n" +
                "  });\n" +
                "\n" +
                "  force\n" +
                "      .nodes(nodes)\n" +
                "      .links(links)\n" +
                "      .start();\n" +
                "\n" +
                "      var marker = defs.selectAll('marker')\n" +
                "    .data(endmarkers)\n" +
                "    .enter()\n" +
                "    .append('svg:marker')\n" +
                "      .attr('id', function(d){ return 'marker_' + d.name})\n" +
                "      .attr('markerHeight', 6)\n" +
                "      .attr('markerWidth', 6)\n" +
                "      .attr('markerUnits', 'strokeWidth')\n" +
                "      .attr('orient', 'auto')\n" +
                "      .attr('refX', 15)\n" +
                "      .attr('refY', 0)\n" +
                "      .attr('viewBox', function(d){ return d.viewbox })\n" +
                "      .append('svg:path')\n" +
                "        .attr('d', function(d){ return d.path })\n" +
                "        .attr('fill', function(d,i) { return color(i)});\n" +
                "\n" +
                "\n" +
                "  var link = svg.selectAll(\".link\")\n" +
                "      .data(bilinks)\n" +
                "    .enter().append(\"path\")\n" +
                "      .attr(\"class\", \"link\")\n" +
                "\n" +
                "      .attr('marker-end', function(d,i){ return 'url(#marker_' + \"arrow\" + ')' });\n" +
                "\n" +
                "\n" +
                "  var node = svg.selectAll(\".node\")\n" +
                "      .data(graph.nodes)\n" +
                "    .enter().append(\"circle\")\n" +
                "      .attr(\"class\", \"node\")\n" +
                "      .attr(\"r\", 5)\n" +
                "      .style(\"fill\", function(d) { return color(d.group); })\n" +
                "      .call(force.drag);\n" +
                "\n" +
                "  node.append(\"title\")\n" +
                "      .text(function(d) { return d.name; });\n" +
                "\n" +
                "  force.on(\"tick\", function() {\n" +
                "    link.attr(\"d\", function(d) {\n" +
                "      return \"M\" + d[0].x + \",\" + d[0].y\n" +
                "          + \"S\" + d[1].x + \",\" + d[1].y\n" +
                "          + \" \" + d[2].x + \",\" + d[2].y;\n" +
                "    });\n" +
                "    node.attr(\"transform\", function(d) {\n" +
                "      return \"translate(\" + d.x + \",\" + d.y + \")\";\n" +
                "    });\n" +
                "  });\n" +
                "});\n" +
                "\n" +
                "</script>";
    }
}
