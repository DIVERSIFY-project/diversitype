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
 * Print hierarchy lvl1
 * Created by guerin on 04/07/16.
 */
public class InterfacesJsonFile implements JsonStrategy {

    private HashMap<String, List<String>> interfaces;
    private String outpuDirectory;
    private JsonGenerator generator;


    public InterfacesJsonFile(String output, HashMap<String,List<String>> interfaceChildren) throws FileNotFoundException {
        this.outpuDirectory=output;
        this.interfaces=interfaceChildren;
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        this.generator = factory.createGenerator(new FileOutputStream(output+"interfaces.json"));
    }

    @Override
    public void printJsonFile() {
        generator.writeStartObject().writeStartArray("interfaces");
        addClassesIntoNodes(generator);
        generator.writeEnd().writeEnd();
        generator.close();
    }

    private void addClassesIntoNodes(JsonGenerator generator) {
        Set<String> set=interfaces.keySet();


        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String current = it.next();
            List<String> list = interfaces.get(current);

            generator.writeStartObject().writeStartArray(current);
            for(int i=0;i<list.size();i++){
                generator.write(list.get(i));
            }
            generator.writeEnd().writeEnd();
        }

    }
}
