package fr.inria.diversify.utils.json.jsonFiles;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by guerin on 04/07/16.
 */
public class ClassesJsonFile implements JsonStrategy {
    private Set<String> classeslist;
    private JsonGenerator generator;



    public ClassesJsonFile(Set<String> classes,String outputDirectory) throws FileNotFoundException {
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        this.generator = factory.createGenerator(new FileOutputStream(outputDirectory+"classes.json"));
        this.classeslist=classes;
    }

    /**
     * Print the list whiwh contains classes in to json file in the ${outputdirectory}/classes.json
     */
    @Override
    public void printJsonFile() {
        generator.writeStartObject().writeStartArray("classes");
        Iterator<String> it=classeslist.iterator();
        while (it.hasNext()){
            generator.write(it.next());
        }
        generator.writeEnd().writeEnd().close();
    }
}
