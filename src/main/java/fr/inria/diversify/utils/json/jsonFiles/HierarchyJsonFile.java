package fr.inria.diversify.utils.json.jsonFiles;

import fr.inria.diversify.utils.InitUtils;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by guerin on 04/07/16.
 */
public class HierarchyJsonFile implements JsonStrategy {


    private JsonGenerator generator;
    private HashMap<String, List<String>> hierarchy;
    private String output;

    public HierarchyJsonFile(String output,HashMap<String,List<String>> hierarchy) throws FileNotFoundException {
        this.output=output;
        this.hierarchy=hierarchy;
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        this.generator = factory.createGenerator(new FileOutputStream(output+"hierarchy.json"));
    }

    @Override
    public void printJsonFile() {
        generator.writeStartObject().writeStartArray("hierarchy");
        addClassesIntoNodes(generator);
        generator.writeEnd().writeEnd();
        generator.close();


    }

    private void addClassesIntoNodes(JsonGenerator generator) {
        Set<String> set=hierarchy.keySet();


        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = hierarchy.get(current);

            generator.writeStartObject().writeStartArray(current);
            for(int i=0;i<list.size();i++){
                generator.write(list.get(i));
            }
            generator.writeEnd().writeEnd();
        }
    }
}
