package pxj200018.server;

// References : https://www.baeldung.com/a-guide-to-java-sockets

import pxj200018.message.GHSMessage;
import pxj200018.message.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    Node node;
    ServerSocket serverSocket;
    private static final Logger log = Logger.getLogger(String.valueOf(ClientHandler.class));
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
                log.log(Level.INFO, "Synch GHS message received\n" + ghsMsg + "\n");
                node.getSynchGHSMessages().add(ghsMsg);

                if (ghsMsg.getMessageType() == MessageType.TERMINATE) node.setActive(false);

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
