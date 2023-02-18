package pxj200018.algorithm;

import pxj200018.config.Config;
import pxj200018.message.PelegMessage;
import pxj200018.server.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peleg implements Runnable {
    Node node;
    int maxDis = Integer.MIN_VALUE;
    Config config;
    private final static Logger log = Logger.getLogger(String.valueOf(Peleg.class));
    public Peleg(Node n) {
        node = n;
        config = n.getConfig();
    }

    /**
     * If round is 0 then evey neighbor needs to broadcast the message to its neighbors
     * else check if all the messages have been received from the previous round
     */
    @Override
    public void run() {
        log.log(Level.INFO, "Starting Peleg's execution from " + node.getHostName());
        while(node.isNodeActive()) {
            if (node.getRound() == 0) {
                log.log(Level.INFO, ""+node);
                broadcast(false);
                node.setRound(node.getRound()+1);
                log.log(Level.INFO, "Broadcast done for round 0");
            } else {
                if (check()) { // this is basically a synch technique
                    log.log(Level.INFO, "Round : " + node.getRound());

                    PelegMessage maxMessage = getMaxUIDMessage();
                    log.log(Level.INFO, "Max UID seen so far : "  + maxMessage);
                    if (maxMessage.getUID_X() > node.getUID_X()) {
                        node.setUID_X(maxMessage.getUID_X());
                        node.setD(maxMessage.getdMax()+1);
                        broadcast(false);
                    } else if (maxMessage.getUID_X() < node.getUID_X()) {
                        node.setCount(1);
                        broadcast(false);
                    } else {
                        if (maxDis > node.getD()) {
                            node.setD(maxDis);
                            node.setCount(0);
                            broadcast(false);
                        } else {
                            if (maxDis == node.getD()) {
                                node.setCount(node.getCount() + 1);

                                //terminate if the count is 2 that is the distance has not changed for two rounds
                                if (node.getUID_X() == node.getUID() && node.getCount() == 2) {
                                    log.log(Level.INFO, "Leader found : " + node + "\n");
                                    node.setLeader(true);
                                    node.setIsNodeActive(false);
                                    node.setLeader(node.getUID());
                                    broadcast(true);
                                } else {
                                    broadcast(false);
                                }
                            } else {
                                broadcast(false);
                            }
                        }
                    }
                    node.setRound(node.getRound()+1);
                }
            }
        }

        // broadcast termination
        broadcast(true);
        log.log(Level.INFO, "Leader found : " + node.getLeader());
    }

    /**
     * Broadcast message to all its neighbors.
     */
    public void broadcast(boolean terminate) {

        for (Integer neighbor : node.getNeighbors()) {

            String hostName = config.getHostName(neighbor);
            int portNumber = config.getMyPortNumber(hostName);
            hostName += ".utdallas.edu";

            try {
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                PelegMessage pm;

                if (terminate)
                    pm = new PelegMessage(node.getUID_X(), node.getD(), node.getRound()+1, node.getUID(), true);
                else
                    pm = new PelegMessage(node.getUID_X(), node.getD(), node.getRound(), node.getUID(), node.isLeaderFound());

                outputStream.writeObject(pm);
                log.log(Level.INFO, "Message sent to " + neighbor + ", " + pm + "\n");
                socket.close();
            } catch (IOException e) {
                log.log(Level.SEVERE,
                        "UID : " + node.getUID() +
                        ", broadcast, socket closed for neighbor : " + neighbor +
                        " hostName : " + hostName + "\n");
            }
        }
    }

    // get the maximum UID from its neighbors
    PelegMessage getMaxUIDMessage() {
        List<PelegMessage> messageBuffer = Collections.synchronizedList(node.pelegMessages);
        synchronized (messageBuffer) {
            PelegMessage maxUIDMessage = null;
            //get the max UID message from the buffer
            for (PelegMessage curr : messageBuffer) {
                if (maxUIDMessage == null) {
                    maxUIDMessage = curr;
                    continue;
                }
                if (curr.getRound() == node.getRound()-1) {
                    if (maxUIDMessage.getUID_X() < curr.getUID_X()) {
                        maxUIDMessage = curr;
                    }
                    if (curr.getUID_X() == maxUIDMessage.getUID_X()) {
                        if (maxDis < curr.getdMax()) maxDis = curr.getdMax();
                    }
                }
            }

            assert maxUIDMessage != null;
            log.log(Level.INFO, maxUIDMessage.toString());

            //remove all the messages which have UID < maxUID
            for (PelegMessage curr : messageBuffer) {
                if (curr.getRound() == node.getRound()-1) {
                    if (curr.getUID_X() < maxUIDMessage.getUID_X()) { // if curr UID is smaller remove
                        log.log(Level.INFO, curr + " removed from list\n");
                        node.pelegMessages.remove(curr);
                    }
                }
            }
            return maxUIDMessage;
        }
    }

    /**
     * This is a synch technique to check if the node has received all the message from its
     * neighbor. (refer Nancy lynch : Distributed Algorithms)
     * @return true if all the message from the previous rounds have been received.
     */
    private boolean check() {
        List<PelegMessage> pm = Collections.synchronizedList(node.pelegMessages);
        HashMap<Integer, Boolean> visited = new HashMap<>();
        synchronized (pm) {
            for (PelegMessage curr : pm) {
                if (curr.getRound() == node.getRound()-1) {
                    visited.put(curr.getParentUID(), true);
                    if (curr.isTerminate()) {
                        log.log(Level.INFO, "Leader found : " + curr.getUID_X()+"\n");
                        node.setLeader(true);
                        node.setIsNodeActive(false);
                    }
                }
            }
        }
        return visited.size() == node.getNeighbors().size();
    }
}
