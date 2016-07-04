package fr.inria.diversify.utils.json;

import fr.inria.diversify.utils.InitUtils;
import fr.inria.diversify.utils.UtilsReport;
import fr.inria.diversify.utils.json.jsonFiles.*;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * The class print different json file into the given directory
 * Created by guerin on 30/06/16.
 */
public class ReportJsonManager implements JsonManager{
    private String outputDirectory;


    /**
     *
     * @param s: the outputDirectory where json file will printed
     */
    public ReportJsonManager(String s) {
        this.outputDirectory=s;
        InitUtils.createDirectory(outputDirectory);
    }

    public void printJsonFiles() throws FileNotFoundException {
        List<JsonStrategy> jsonToPrint=getJsonFile();
        for(int i=0;i<jsonToPrint.size();i++){
            jsonToPrint.get(i).printJsonFile();
        }
    }

    public List<JsonStrategy> getJsonFile() throws FileNotFoundException {
        List<JsonStrategy> list=new ArrayList<>();
        list.add(new ClassesJsonFile(UtilsReport.getClasseSet(),outputDirectory));
        list.add(new GraphJsonFile(outputDirectory,UtilsReport.getAbstractClass(),UtilsReport.getInterfaceChildren(),UtilsReport.getClassChildren(),UtilsReport.getNumberToClasses(),UtilsReport.getClassesToNumber()));
        list.add(new InterfacesJsonFile(outputDirectory,UtilsReport.getInterfaceChildren()));
        list.add(new HierarchyJsonFile(outputDirectory,UtilsReport.getHierarchy()));
        list.add(new AbstractClassesJsonFile(outputDirectory,UtilsReport.getAbstractClass()));
        return list;
    }


    /**
     * Print the list whiwh contains classes in to json file in the ${outputdirectory}/classes.json
     * @param classes: List<String>
     * @throws FileNotFoundException
     */
   /* public void printClassesJson(Set<String> classes) throws FileNotFoundException {
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        JsonGenerator generator = factory.createGenerator(new FileOutputStream(outputDirectory+"classes.json"));
        generator.writeStartObject().writeStartArray("classes");
        Iterator<String> it=classes.iterator();
        while (it.hasNext()){
            generator.write(it.next());
        }
        generator.writeEnd().writeEnd().close();
    }*/


    /**
     * Print json graph which represent the hierarchy od the project
     * parameters should be not null
     * @param abstractClasses: list of abstract class in the graph
     * @param interfaceChildren: For each interface, its children
     * @param classChildren: For each concret class, its children
     * @param numberToClass: give a single number to a class (for indentify class in the graph)
     * @param classToNumber: the invert of numberToClass
     * @throws FileNotFoundException
     */
    /*public void printGraphJSON(List<String> abstractClasses, HashMap<String,List<String>> interfaceChildren, HashMap<String,List<String>> classChildren, HashMap<Integer,String> numberToClass, HashMap<String,Integer> classToNumber) throws FileNotFoundException {
        this.abstractClass=abstractClasses;
        this.classChildren=classChildren;
        this.interfaceChildren=interfaceChildren;
        this.classToNumber=classToNumber;
        this.numberToClass=numberToClass;

        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        JsonGenerator generator = factory.createGenerator(new FileOutputStream(outputDirectory+"graph.json"));

        generator.writeStartObject().writeStartArray("nodes");
        addClassesIntoNodes(generator);
        generator.writeEnd();

        generator.writeStartArray("links");
        addLinksIntoLinks(generator);
        generator.writeEnd().writeEnd().close();
    }



    private void addLinksIntoLinks(JsonGenerator generator) {
        addLinksFromHashMap(interfaceChildren,generator);
        addLinksFromHashMap(classChildren,generator);
    }

    private  void addLinksFromHashMap(HashMap<String, List<String>> h, JsonGenerator generator){
        Set<String> interfaces=h.keySet();
        Iterator<String> iterator=interfaces.iterator();
        while (iterator.hasNext()){
            String current=iterator.next();
            List<String> list=h.get(current);
            int nb=classToNumber.get(current);
            for (int j=0;j<list.size();j++){
                generator.writeStartObject().write("source",classToNumber.get(list.get(j))).write("target",nb).writeEnd();
            }
        }

    }*/

   /* private  String addLinktoJSON( int source, int target) {
        return "{\"source\":"+source+",\"target\":"+target+"},\n";
    }*/

    /*private void addClassesIntoNodes(JsonGenerator generator) {


        for(int i=0;i<numberToClass.size();i++) {
            String current=numberToClass.get(i);
            int value=getGroupNumber(current);
            generator.writeStartObject().write("name",current).write("group",value).writeEnd();
        }
    }

    private int getGroupNumber(String current) {
        if(interfaceChildren.containsKey(current)){
            return 1;
        }else if(abstractClass.contains(current)){
            return 2;
        }
        return 3;
    }*/
}
