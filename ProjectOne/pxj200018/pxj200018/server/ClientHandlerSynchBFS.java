package pxj200018.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandlerSynchBFS implements Runnable {
    private final static Logger log = Logger.getLogger(String.valueOf(ClientHandlerSynchBFS.class));
    Node node;
    ServerSocket serverSocket;
    public ClientHandlerSynchBFS(Node node) {
        this.node = node;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(node.getPortNumber(), node.config.getNumberOfNodes());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.log(Level.INFO, "\nStarting Synch BFS\n");
        while(node.isBFSActive) {
            return;
        }
    }
}
