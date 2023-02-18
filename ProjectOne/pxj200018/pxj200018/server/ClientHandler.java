package pxj200018.server;

// References : https://www.baeldung.com/a-guide-to-java-sockets

import pxj200018.message.PelegMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    Node node;
    private static final Logger log = Logger.getLogger(String.valueOf(ClientHandler.class));
    public ClientHandler(Node n) { this.node = n; }
    ServerSocket serverSocket;
    @Override
    public void run() {
        try {
            // passing numberOfNodes as backlog as there might be at most numberOfNodes-1 connected to a node.
            serverSocket = new ServerSocket(node.getPortNumber(), node.config.getNumberOfNodes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(node.isActive) {
            Socket s = null;
            try {
                s = serverSocket.accept();
                ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
                PelegMessage m = (PelegMessage) inputStream.readObject();
                log.log(Level.INFO, "Received : " + m);
                node.getPelegMessages().add(m);

                // if is terminated true then send across all the neighbors
                if (m.isTerminate()) {
                    node.setIsNodeActive(false);
                    node.setLeader(m.getUID_X());
                }

            } catch (Exception e) {
                assert  s != null;
                try {
                    s.close();
                }catch (IOException ex) {
                    throw new RuntimeException();
                }
                log.log(Level.SEVERE, "Error in accepting request from machine " + node.hostName);
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error in closing the socket " + node.hostName);
            throw new RuntimeException(e);
        }
    }
}
