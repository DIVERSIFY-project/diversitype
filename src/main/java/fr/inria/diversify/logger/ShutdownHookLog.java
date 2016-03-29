package fr.inria.diversify.logger;

public class ShutdownHookLog extends Thread {

    public void run() {
        LogWriter.writeLog();
    }
}
