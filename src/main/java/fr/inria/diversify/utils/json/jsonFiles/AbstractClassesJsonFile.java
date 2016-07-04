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
public class AbstractClassesJsonFile implements JsonStrategy {

    private List<String> abstractClasses;
    private String outpuDirectory;
    private JsonGenerator generator;


    public AbstractClassesJsonFile(String output, List<String> interfaceChildren) throws FileNotFoundException {
        this.outpuDirectory=output;
        this.abstractClasses =interfaceChildren;
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        this.generator = factory.createGenerator(new FileOutputStream(output+"abstractClasses.json"));
    }

    @Override
    public void printJsonFile() {
        generator.writeStartObject().writeStartArray("abstractClasses");
        addClassesIntoNodes(generator);
        generator.writeEnd().writeEnd();
        generator.close();
    }

    private void addClassesIntoNodes(JsonGenerator generator) {
       for(int i=0;i<abstractClasses.size();i++){
           generator.write(abstractClasses.get(i));
       }
    }
}
