package pxj200018.algorithm;

import javafx.util.Pair;
import pxj200018.config.Config;
import pxj200018.message.Edge;
import pxj200018.message.EdgeType;
import pxj200018.message.GHSMessage;
import pxj200018.message.MessageType;
import pxj200018.server.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchGHS implements Runnable {
    private final static Logger log = Logger.getLogger(String.valueOf(SynchGHS.class));
    Node node;
    Config c;

    public SynchGHS(Node n) {
        node = n;
        c = node.getConfig();
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
        while (node.getActive()) {
            if (node.getFirstMessage()) {
                node.setFirstMessage(false);
                broadcast();
            }
            try {
                processMessages();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void processMessages() throws InterruptedException {
        List<GHSMessage> ghsMessages = Collections.synchronizedList(node.getSynchGHSMessages());
        for (GHSMessage ghsMessage : ghsMessages) {
            if (ghsMessage.isMessageProcessed()) continue;
            ghsMessage.setMessageProcessed(true);
            switch (ghsMessage.getMessageType()) {
                case BROADCAST: // broadcast all neighbors except the source.
                    if (node.getLeader() <= ghsMessage.getLeaderUID()) {
                        node.setSrcUID(ghsMessage.getSourceUID());
                        node.setFirstConverge(true);
                        node.setConvergeDone(false);
                        node.setLeader(ghsMessage.getLeaderUID());
                        node.neighborsMinEdge.clear();
                        broadcast();
                    }
                    break;
                case CONVERGE_CAST:
                    // MST edges.
                    node.updateMinEdge(ghsMessage.getSourceUID(), ghsMessage.getMinEdge());
                    convergeCast();
                    break;
                case GET_LEADER:
                    sendMessage(node.getUID(), ghsMessage.getSourceUID(), node.getLeader(), MessageType.SEND_LEADER, null);
                    break;
                case SEND_LEADER:
                    node.updateNeighborLeader(ghsMessage.getSourceUID(), ghsMessage.getLeaderUID());
                    convergeCast();
                    break;
                case START_MERGE:
                    if (!node.isConvergeDone()) {
                        ghsMessage.setMessageProcessed(false);
                        continue;
                    }
                    startMerge(ghsMessage);
                    break;
                case INFORM_LEADER:
                    if (node.getSrcUID() != -1) {
                        if (node.getLeader() <= ghsMessage.getLeaderUID())
                            sendMessage(node.getUID(), node.getSrcUID(), node.getLeader(), MessageType.INFORM_LEADER, null);
                    } else{
                        broadcastLeader();
                        //iterate through all edges and check if all the messages in array are marked then don't do anything.
                        Thread.sleep(10000);
                        if (checkAllMessages(ghsMessages)) {
//                            processTerminate();
                            initBroadcast();
                        }
                    }
                    break;
                case LEADER_BROADCAST:
                    if (node.getLeader() <= ghsMessage.getLeaderUID()) {
                        node.setLeader(ghsMessage.getLeaderUID());
                        node.setSrcUID(ghsMessage.getSourceUID());
                        broadcastLeader();
                    }
                    break;
                case TERMINATE:
                    processTerminate();
                    break;
                default:
                    break;
            }
        }
    }

    public void initBroadcast(){
        node.setFirstConverge(true);
        node.neighborsMinEdge.clear();
        log.log(Level.INFO, "calling broadcast from init broadcast");
        broadcast();
    }

    /**
     * To check whether all the messages have been processed or not
     * @param m list of all the messages in buffer
     * @return true if all the messages have been processed.
     */
    public boolean checkAllMessages(List<GHSMessage> m) {
        for (GHSMessage msg : m) {
            if (!msg.isMessageProcessed()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Broadcasts who is the leader.
     */
    public void broadcastLeader() {
        List<Edge> neighbors = node.getNeighbors();
        for (Edge nei : neighbors) {
            if (nei.getToVertex() == node.getSrcUID() || nei.getType() != EdgeType.MST_EDGE) continue;
            sendMessage(nei.getFromVertex(), nei.getToVertex(), node.getLeader(), MessageType.LEADER_BROADCAST, null);
        }
    }

    /**
     * Function to broadcast message to everyone.
     */
    public void broadcast() {
        boolean isLeafNode = true;
        List<Edge> neighbors = Collections.synchronizedList(node.getNeighbors());
        for (Edge edge : neighbors) {
            int from = edge.getFromVertex();
            int to = edge.getToVertex();
            EdgeType type = edge.getType();
            if (type == EdgeType.MST_EDGE && to != node.getSrcUID()) {
                isLeafNode = false;
                sendMessage(from, to, node.getLeader(), MessageType.BROADCAST, null);
            }
        }
        //if lead node then converge cast.
        if (isLeafNode) convergeCast();

    }

    /**
     * Converge cast when message is received from all its neighbors, and compute the MWOE
     */
    public void convergeCast() {
        if(node.getFirstConverge()) {
            log.log(Level.INFO, " UID " + node.getUID() + " in node.getFirstConverge");
            node.setFirstConverge(false);
            node.neighborLeader.clear();
            sendMessageOnNormalEdge();
            return;
        }

        HashMap<Integer, Edge> neighborsMinEdge = node.getNeighborsMinEdge();
        HashMap<Integer, Integer> neighborsLeader = node.neighborLeader;

        log.log(Level.INFO, " Neighbors min edge : " + neighborsMinEdge + " Neighbors leader"  +neighborsLeader);
        log.log(Level.INFO, "Normal edge count : " + node.getNormalEdgeCount());
        log.log(Level.INFO, "MST Edge Count : " + node.getMstEdgeCount());

        //neighbors min edge size == mst edge count (leader)
        //for internal nodes neighbors min edge size = mstEdge count-1
        //for leaf mst edge count = 0
        if (
                (node.getMstEdgeCount() != 0 && //leaf nodes
                        (node.getSrcUID() == -1 && node.getMstEdgeCount() != neighborsMinEdge.size()) //root node
                || (node.getSrcUID() != -1 && node.getMstEdgeCount()-1 != neighborsMinEdge.size())) //internal nodes
                || node.getNormalEdgeCount() != neighborsLeader.size()
        ) {
            return;
        }

        Edge currMinEdge = null;

        //else process all converge messages
        for (Integer srcUID : neighborsLeader.keySet()) {
            int neighLeader = neighborsLeader.get(srcUID);
            if (neighLeader == node.getLeader()) {
                node.updateMyGraph(srcUID, EdgeType.SAME_COMPONENT); // update the vertex which belong to the same component.
            } else {
                Edge neiEdge = node.getEdge(srcUID);
                if(currMinEdge == null) currMinEdge = neiEdge;
                else{
                    if(neiEdge.compare(currMinEdge)){
                        currMinEdge = neiEdge;
                    }
                }
            }
        }

        // update the min edge from the neighbors
        for (Integer srcUID : neighborsMinEdge.keySet()) {
            Edge e = neighborsMinEdge.get(srcUID);
            if (e == null) continue;
            if (currMinEdge == null) currMinEdge = e;
            else {
                if (e.compare(currMinEdge)) {
                    currMinEdge = e;
                }
            }
        }

        log.log(Level.INFO, "Current minimum edge on UID : " + node.getUID() + " = " + currMinEdge + "\n");
        log.log(Level.INFO, "Neighbors min edges map <neighbor, minEdge> : " + node.getNeighborsMinEdge()+"\n");
        log.log(Level.INFO, "Neighbors leader <neighbor, leader> : "  + node.neighborLeader);
        // send message of converge cast only if I am not the leader else need to broadcast
        // the minimum edge
        node.setConvergeDone(true);
        if(node.getSrcUID() == -1) {
            //Send Merge Message
            log.log(Level.INFO, " I am the leader UID : " + node.getUID() + "\n");
            if(currMinEdge == null) processTerminate();
            else merge(currMinEdge);
        } else {
            sendMessage(node.getUID(), node.getSrcUID(), node.getLeader(), MessageType.CONVERGE_CAST, currMinEdge);
        }
    }

    /**
     * Sends termination signal to MST edges.
     */
    public void processTerminate() {
        log.log(Level.INFO,  "Terminating : "+ node.getUID() + "\n");
        List<Edge> neighbor = node.getNeighbors();
        log.log(Level.INFO, "\nCurrent UID: " + node.getUID());
        for (Edge nei : neighbor) {
            if (nei.getType() == EdgeType.MST_EDGE) {
                log.log(Level.INFO,  "MST edge : " + nei + "\n");
                sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.TERMINATE, null);
            }
        }
        node.setActive(false);
    }

    /**
     * Starts the merge operation with the edge given in message
     * @param msg contains the min edge vertex
     */
    public void startMerge(GHSMessage msg) {
        Edge mstCheck = node.getEdge(msg.getSourceUID());
        if (mstCheck.getType() != EdgeType.MST_EDGE) node.updateMyGraph(msg.getSourceUID(), EdgeType.MST_EDGE);

        //check whose leader wins
        if (node.getLeader() < msg.getLeaderUID()) {
            //Current node lost
            node.setLeader(msg.getLeaderUID());
            sendMessage(node.getUID(), msg.getSourceUID(), node.getLeader(), MessageType.INFORM_LEADER, null);
        } else {
            //Current node winner
            if (node.getSrcUID() != -1)
                sendMessage(node.getUID(), node.getSrcUID(), node.getLeader(), MessageType.INFORM_LEADER, null);
            else {
                if (msg.getLeaderUID() == node.getLeader()) {
                    broadcastLeader();
                }
            }
        }
    }

    /**
     * Merges with the minimum edge.
     * Update source min edge from two hashmaps, neighborsMinEdge and srcMinEdge
     * @param currMinEdge the edge which needs to be merged
     */
    public void merge(Edge currMinEdge) {
        List<Pair<Integer, Edge>> srcMinEdge = new ArrayList<>();
        HashMap<Integer, Edge> neighborsMinEdge = node.getNeighborsMinEdge();
        for (Integer nei : neighborsMinEdge.keySet()) {
            Edge edge = neighborsMinEdge.get(nei);
            if (edge.getEdgeWt() == currMinEdge.getEdgeWt()) {
                //propagate the message to from vertex
                srcMinEdge.add(new Pair<>(nei, edge));
            }
        }

        for (Edge nei : node.getNeighbors()) {
            if (nei.getType() == EdgeType.NORMAL_EDGE && nei.getEdgeWt() == currMinEdge.getEdgeWt()) {
                srcMinEdge.add(new Pair<>(nei.getToVertex(), nei));
            }
        }

        // lexicographical comparison
        Pair<Integer, Edge>  minEdge = srcMinEdge.get(0);
        for (int i = 1; i<srcMinEdge.size(); i++) {
            if (!minEdge.getValue().compare(srcMinEdge.get(i).getValue())) {
                minEdge = srcMinEdge.get(i);
            }
        }

        Edge e = node.getEdge(minEdge.getKey());
        if (e.getType() == EdgeType.MST_EDGE) {
            sendMessage(e.getFromVertex(), e.getToVertex(), node.getLeader(), MessageType.MERGE, null);
        } else {
            node.updateMyGraph(e.getToVertex(), EdgeType.MST_EDGE);
            sendMessage(e.getFromVertex(), e.getToVertex(), node.getLeader(), MessageType.START_MERGE, null);
        }
    }

    /**
     * To send message on the normal edges.
     * Where Normal edge is a message type.
     */
    public void sendMessageOnNormalEdge(){
        List<Edge> neighbors = node.getNeighbors();
        boolean isNormalEdge = false;
        for(Edge e: neighbors){
            if(e.getType() == EdgeType.NORMAL_EDGE){
                isNormalEdge = true;
                sendMessage(node.getUID(), e.getToVertex(), node.getLeader(), MessageType.GET_LEADER, null);
            }
        }
        if (!isNormalEdge) convergeCast();
    }

    /**
     * Generic function to send message to dst.
     * @param src source UID.
     * @param dst destination UID.
     * @param leader leader UID.
     * @param messageType type of the message which needs to be communicated.
     * @param minEdge current minimum edge which is calculated.
     */
    public void sendMessage(int src, int dst, int leader, MessageType messageType, Edge minEdge) {
        String hostName = c.getHostName(dst);
        try {
            Socket socket = new Socket(hostName, c.getMyPortNumber(hostName));
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            GHSMessage ghsM = new GHSMessage(src, dst, leader, messageType, minEdge);

            outputStream.writeObject(ghsM);
            log.log(Level.INFO, "Message sent to " + dst + ", " + ghsM + "\n");
            socket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE,
                    "UID : " + node.getUID() +
                            ", broadcast, socket closed for neighbor : " + dst +
                            " hostName : " + hostName + "\n");
        }
    }

}