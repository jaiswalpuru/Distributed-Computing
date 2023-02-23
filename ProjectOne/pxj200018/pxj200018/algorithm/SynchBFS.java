package pxj200018.algorithm;

import pxj200018.config.Config;
import pxj200018.message.BFSMessage;
import pxj200018.message.MessageType;
import pxj200018.server.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchBFS implements Runnable {
    Logger log = Logger.getLogger(String.valueOf(SynchBFS.class));
    Node node;
    Config config;

    public SynchBFS(Node node) {
        this.node = node;
        config = node.getConfig();
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
        while(node.isBFSNodeActive()) {
            if (node.getUID() == node.getLeader() && !node.isSearchSent()) { // this means that the leader is found
                node.setParent(-1); // as this node is the parent.
                node.setSearchSent(true);
                node.setVisited(true);
                sendSearch(); // send search message to all its neighbour.
            }else {
                List<BFSMessage> bfsMessages = Collections.synchronizedList(node.getBfsMessages());

                synchronized (bfsMessages) {
                    for (BFSMessage bfm : bfsMessages) {
                        check(bfm);
                        node.bfsMessages.remove(bfm);
                    }
                }

                // if received responses from all its neighbors
                if (node.ackMessages.size() + node.nackMessages.size() == node.getNeighbors().size()) {
                    int parentUID = node.getParent();
                    node.setBFSNodeActive(false);

                    log.log(Level.INFO, "\n BFS terminated at node : " + node.getUID() + ", parent : " + node.getParent());
                    log.log(Level.INFO, "\n Total Children : " + node.getChildren());
                    int totalDegree = 1+node.getChildren().size();

                    if (parentUID != -1) {
                        log.log(Level.INFO, "UID : " + node.getUID() + " Degree : " + totalDegree + "\n\n");
                        sendAck();
                    } else {
                        log.log(Level.INFO, "UID : " + node.getUID() + " Degree : " + node.getChildren().size() + "\n\n");
                    }
                    break;
                }
            }
        }
    }

    /**
     * Checks the type of message and takes action based on that.
     * @param bfm message to be evaluated
     */
    public void check(BFSMessage bfm) {

        //if node is unvisited and message type is search
        if (!node.isVisited() && bfm.getMessageType() == MessageType.SEARCH) {
            node.setVisited(true);
            node.setParent(bfm.getUID());
            node.setSearchSent(true);
            log.log(Level.INFO, "UID : " + node.getUID() + "\nParent : " + node.getParent() + "\n\n");
            sendSearch(); // send search message to all its neighbors.
        } else {
            //if node is already visited and message type is SEARCH, then we need to send nack.
            if (node.isVisited() && bfm.getMessageType() == MessageType.SEARCH) {
                sendNack(bfm);
            }

            //Ack received.
            if (node.getUID() != bfm.getUID() && bfm.getMessageType() == MessageType.ACK) {
                // add as a child
                log.log(Level.INFO, "Adding " + bfm.getUID() + " as a child to node " + node.getUID());
                node.updateChildren(bfm.getUID());
            }
        }
    }

    /**
     * Sends acknowledgement to the parent.
     */
    public void sendAck() {
        int parentUID = node.getParent();
        try  {
            String hostName = config.getHostName(parentUID);
            int portNumber = config.getMyPortNumber(hostName);
            Socket socket = new Socket(hostName, portNumber);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new BFSMessage(node.getUID(), MessageType.ACK));

            log.log(Level.SEVERE, "UID : " + node.getUID() + ", ack sent to UID : " + parentUID+"\n\n");
            socket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "UID : " + node.getUID() + ", error in sending ack to " + parentUID + "\n\n");
        }

    }

    /**
     * Sends search to all its neighbor.
     */
    public void sendSearch() {
        List<Integer> neighbors = node.getNeighbors();
        for (Integer neighbor : neighbors) {
            try {
                String hostName = config.getHostName(neighbor);
                int portNumber = config.getMyPortNumber(hostName);
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                BFSMessage bfm = new BFSMessage(node.getUID(), MessageType.SEARCH);

                objectOutputStream.writeObject(bfm);
                socket.close();

                log.log(Level.INFO, "UID : " + node.getUID() + ", search sent to " + neighbor + "\n\n");

            } catch (IOException e) {
                e.printStackTrace();
                log.log(Level.SEVERE, "UID : " + node.getUID() + ", failed in sending search to " + neighbor + "\n\n");
            }
        }
    }

    /**
     * Sends nack to the specified UID given the message.
     * @param bfm message which contains the UID to which nack needs to be sent.
     */
    public void sendNack(BFSMessage bfm) {
        try {
            String hostName = config.getHostName(bfm.getUID());
            int portNumber = config.getMyPortNumber(hostName);

            Socket socket = new Socket(hostName, portNumber);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new BFSMessage(node.getUID(), MessageType.NACK));
            socket.close();
            log.log(Level.INFO, "UID : " + node.getUID() + ", nack sent to UID : " + bfm.getUID() + "\n\n");

        }catch (IOException e) {
            log.log(Level.SEVERE, "UID : " + node.getUID() + ", error in sending Nack to " + bfm.getUID() + "\n\n");
        }
    }
}
