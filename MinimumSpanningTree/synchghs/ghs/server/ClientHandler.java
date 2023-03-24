package ghs.server;

// References : https://www.baeldung.com/a-guide-to-java-sockets

import ghs.message.GHSMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger log = Logger.getLogger(String.valueOf(ClientHandler.class));
    boolean DEBUG = true;
    Node node;
    ServerSocket serverSocket;
    public ClientHandler(Node n) { this.node = n; }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(node.getPortNumber(), node.config.getNumberOfNodes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(node.getActive()) {
            Socket socket;

            try {
                socket = serverSocket.accept();
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                GHSMessage ghsMsg = (GHSMessage) inputStream.readObject();

                if (DEBUG)
                    log.log(Level.INFO, "\nSynch GHS message received : " + ghsMsg + "\n");

                node.getSynchGHSMessages().add(ghsMsg);
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                log.log(Level.SEVERE, "SynchGHS : error in accepting request from machine " + node.hostName + "\n");
                throw new RuntimeException(e);
            }

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Synch : error in closing the socket " + node.hostName + "\n");
            throw new RuntimeException(e);
        }
    }
}
