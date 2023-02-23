package pxj200018.server;

import pxj200018.message.BFSMessage;
import pxj200018.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
        while(node.isBFSNodeActive()) {
            Socket socket;

            try {
                socket = serverSocket.accept();
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                BFSMessage bfsMessage = (BFSMessage) inputStream.readObject();
                log.log(Level.INFO, "UID : " +  node.getUID() + ", synch BFS Message received\n" + bfsMessage + "\n\n");
                node.bfsMessages.add(bfsMessage);
                if (bfsMessage.getMessageType() == MessageType.NACK) node.nackMessages.add(bfsMessage);
                if (bfsMessage.getMessageType() == MessageType.ACK) node.ackMessages.add(bfsMessage);
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                log.log(Level.SEVERE, "Synch BFS : error in accepting request from machine " + node.hostName + "\n\n");
                throw new RuntimeException(e);
            }

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Synch BFS : error in closing the socket " + node.hostName + "\n");
            throw new RuntimeException(e);
        }
    }
}
