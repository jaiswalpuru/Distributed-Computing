package pxj200018.server;

// References : https://www.baeldung.com/a-guide-to-java-sockets

import pxj200018.message.PelegMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandlerPeleg implements Runnable {
    Node node;
    ServerSocket serverSocket;
    private static final Logger log = Logger.getLogger(String.valueOf(ClientHandlerPeleg.class));
    public ClientHandlerPeleg(Node n) { this.node = n; }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(node.getPortNumber(), node.config.getNumberOfNodes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(node.isPelegNodeActive) {
            Socket socket;

            try {
                socket = serverSocket.accept();
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                PelegMessage pelegMessage = (PelegMessage) inputStream.readObject();
                log.log(Level.INFO, "Peleg message received\n" + pelegMessage + "\n");
                node.getPelegMessages().add(pelegMessage);

                // if is terminated true then send across all the neighbors
                if (pelegMessage.isTerminate()) {
                    node.setIsNodeActive(false);
                    node.setLeader(pelegMessage.getUID_X());
                }

                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                log.log(Level.SEVERE, "Peleg : error in accepting request from machine " + node.hostName + "\n");
                throw new RuntimeException(e);
            }

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Pelegs : error in closing the socket " + node.hostName + "\n");
            throw new RuntimeException(e);
        }
    }
}
