package fr.inria.diversify.logger;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

/**
 * Created by lguerin on 17/08/15.
 */
public class ShutDownHookLogClassLoader extends Thread {
    public void run() {

        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        Reflections reflections = new Reflections(".*",new SubTypesScanner(false));

    }

}
