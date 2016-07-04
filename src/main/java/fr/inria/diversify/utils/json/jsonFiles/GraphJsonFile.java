package fr.inria.diversify.utils.json.jsonFiles;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by guerin on 04/07/16.
 */
public class GraphJsonFile implements JsonStrategy {


    private final List<String> abstractClass;
    private final HashMap<String, List<String>> classChildren;
    private final HashMap<String, List<String>> interfaceChildren;
    private final HashMap<String, Integer> classToNumber;
    private final HashMap<Integer, String> numberToClass;
    private final JsonGenerator generator;

    public GraphJsonFile(String output,List<String> abstractClasses, HashMap<String,List<String>> interfaceChildren, HashMap<String,List<String>> classChildren, HashMap<Integer,String> numberToClass, HashMap<String,Integer> classToNumber) throws FileNotFoundException {
        this.abstractClass=abstractClasses;
        this.classChildren=classChildren;
        this.interfaceChildren=interfaceChildren;
        this.classToNumber=classToNumber;
        this.numberToClass=numberToClass;
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        this.generator = factory.createGenerator(new FileOutputStream(output+"graph.json"));
    }

    /**
     * Print json graph which represent the hierarchy of the project
     */
    @Override
    public void printJsonFile() {
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

    }

    private void addClassesIntoNodes(JsonGenerator generator) {


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
    }
}
