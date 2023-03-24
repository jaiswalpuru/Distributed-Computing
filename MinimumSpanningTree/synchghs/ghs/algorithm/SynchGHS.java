package ghs.algorithm;

import ghs.message.*;
import javafx.util.Pair;
import ghs.config.Config;
import ghs.server.Node;
import org.omg.CORBA.INTERNAL;

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
    boolean DEBUG = true;
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

    /**
     * Processes all the messages in the buffer.
     */
    public synchronized void processMessages() throws InterruptedException {
        int i=0;
        List<GHSMessage> ghsMessages = Collections.synchronizedList(node.getSynchGHSMessages());
        for (GHSMessage ghsMessage : ghsMessages) {
            i++;
            if (ghsMessage.isMessageProcessed()) continue;
            ghsMessage.setMessageProcessed(true);
            switch (ghsMessage.getMessageType()) {

                case BROADCAST: // broadcast all neighbors except the source.
                    log.log(Level.INFO, "\nBROADCAST : "  +ghsMessage + "\n");
                    if(node.isConvergeDone() && ghsMessage.getRound() >= node.getRound()){
                        log.log(Level.INFO, "Condition Satisfied and doing broadcast!");
                        node.setSrcUID(ghsMessage.getSourceUID());
                        node.setFirstConverge(true);
                        node.setConvergeDone(false);
                        node.setRound(ghsMessage.getRound());
                        node.setLeader(ghsMessage.getLeaderUID());
                        node.neighborsMinEdge.clear();
                        node.neighborRepliedRound.clear();
                        broadcast();
                    } else{
                        log.log(Level.INFO," broadcast condition not satisfied");
                        ghsMessage.setMessageProcessed(false);
                    }
                    break;

                case CONVERGE_CAST:
                    log.log(Level.INFO, "\nCONVERGE_CAST : " + ghsMessage + "\n");
                    node.updateMinEdge(ghsMessage.getSourceUID(), ghsMessage.getMinEdge());
                    convergeCast();
                    break;

                case GET_LEADER:
                    //need to wait
                    if (node.getUID() < ghsMessage.getRound()) {
                        ghsMessage.setMessageProcessed(false);
                        continue;
                    }

                    log.log(Level.INFO, "\nGET_LEADER : " + ghsMessage + "\n");
                    sendMessage(node.getUID(), ghsMessage.getSourceUID(), node.getLeader(), MessageType.SEND_LEADER, null);
                    break;

                case SEND_LEADER:
                    log.log(Level.INFO, "\nSEND_LEADER : " + ghsMessage + "\n");
                    node.updateNeighborLeader(ghsMessage.getSourceUID(), ghsMessage.getLeaderUID());
                    convergeCast();
                    break;

                case MERGE:
                    log.log(Level.INFO, "Merge : " + ghsMessage);
                    if (ghsMessage.getMinEdge() != null)
                        merge(ghsMessage.getMinEdge());
                    break;

                case EMPTY:
                    log.log(Level.INFO, "Empty : " + ghsMessage);
                    node.neighborRepliedRound.add(ghsMessage.getSourceUID());
                    log.log(Level.INFO, node.neighborRepliedRound + " EMPTY neighbor ");
                    break;

                case START_MERGE:
                    node.neighborRepliedRound.add(ghsMessage.getSourceUID());
                    if (!node.isConvergeDone() || ghsMessage.getRound() > node.getRound()) {
                        ghsMessage.setMessageProcessed(false);
                        continue;
                    }
                    log.log(Level.INFO, "\nSTART_MERGE : " + ghsMessage + "\n");
                    startMerge(ghsMessage, i);
                    break;

                case LEADER_BROADCAST:
                    if(!node.isConvergeDone() || ghsMessage.getRound() > node.getRound()) {
                        ghsMessage.setMessageProcessed(false);
                        continue;
                    }
                    log.log(Level.INFO , "\nLEADER_BROADCAST : " + ghsMessage + "\n");
                    node.setType(NodeType.LEAF);
                    node.setLeader(ghsMessage.getLeaderUID());
                    node.setSrcUID(ghsMessage.getSourceUID());
                    broadcastLeader();
                    break;

                case TERMINATE:
                    log.log(Level.INFO, "\nTERMINATE : " + ghsMessage + "\n");
                    processTerminate();
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * Broadcasts who is the leader.
     */
    public void broadcastLeader() {
        List<Edge> neighbors = node.getNeighbors();
        for (Edge nei : neighbors) {
            if (nei.getToVertex() == node.getSrcUID() || nei.getType() != EdgeType.MST_EDGE) continue;
            if(node.getSrcUID() != -1)  node.setType(NodeType.INTERNAL);
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
     * Tells whether it is leaf node or not.
     * @return leaf node or not.
     */
    public boolean isLeafNode() {
        return (node.getType() == NodeType.LEAF
                && node.getNormalEdgeCount() == node.getNeighborLeader().size()
        );
    }

    /**
     * Tells whether it is internal node or not.
     * @return internal node or not.
     */
    public boolean isInternalNode() {
        return (node.getType() == NodeType.INTERNAL
                && node.getMstEdgeCount()-1 == node.getNeighborsMinEdge().size()
                && node.getNeighborLeader().size() == node.getNormalEdgeCount()
        );
    }

    /**
     * Tells whether it is root node or not.
     * @return root node or not.
     */
    public boolean isRootNode() {
        return (node.getType() == NodeType.ROOT
                && node.getNormalEdgeCount() == node.getNeighborLeader().size()
                && node.getNeighborsMinEdge().size() == node.getMstEdgeCount()
        );
    }

    public void print(String msg) {
        log.log(Level.INFO, msg);
    }

    /**
     * Converge cast when message is received from all its neighbors, and compute the MWOE
     */
    public void convergeCast() {
        if(node.getFirstConverge()) {
            node.setFirstConverge(false);
            node.setConvergeDone(false);
            node.neighborLeader.clear();
            sendMessageOnNormalEdge();
            return;
        }

        HashMap<Integer, Edge> neighborsMinEdge = new HashMap<>(node.getNeighborsMinEdge());
        HashMap<Integer, Integer> neighborsLeader = new HashMap<>(node.getNeighborLeader());

        boolean leafNode = isLeafNode();
        boolean internalNode = isInternalNode();
        boolean rootNode = isRootNode();

        if (DEBUG)
            log.log(Level.INFO, "\nLeaf Node : " + leafNode + "\nInternal Node : " + internalNode + "\nRoot Node " + rootNode + "\n");

        if (DEBUG)
            log.log(Level.INFO, "Source UID : " + node.getSrcUID() + "\nMSTEdgeCount : " + node.getMstEdgeCount() + "\nNormalEdgeCount" + node.getNormalEdgeCount() + "\nNeighbor Leader" + node.getNeighborLeader() +
                    "\nNeighborsMinEdge : "+ node.getNeighborsMinEdge() + "\nNode Type : " + node.getType() + "\n");
        if (leafNode || internalNode || rootNode){


            Edge currMinEdge = null;
            //else process all converge messages
            for (Integer srcUID : neighborsLeader.keySet()) {
                if (neighborsLeader.getOrDefault(srcUID, -1) == -1) continue;
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

            // send message of converge cast only if I am not the leader else need to broadcast
            // the minimum edge
            if(node.getSrcUID() == -1) {
                //Send Merge Message
                if(currMinEdge == null) processTerminate();
                else merge(currMinEdge);
            } else {
                sendMessage(node.getUID(), node.getSrcUID(), node.getLeader(), MessageType.CONVERGE_CAST, currMinEdge);
            }
            node.setConvergeDone(true);
        }
    }

    /**
     * Sends termination signal to MST edges.
     */
    public void processTerminate() {
        List<Edge> neighbor = node.getNeighbors();
        log.log(Level.INFO, "\nCurrent UID: " + node.getUID()+"\n");
        for (Edge nei : neighbor) {
            if(nei.getType() == EdgeType.MST_EDGE)
                log.log(Level.INFO,  "\nMST edge : " + nei + "\n");
            if (nei.getToVertex() != node.getSrcUID() && nei.getType() == EdgeType.MST_EDGE) {
                sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.TERMINATE, null);
            }
        }
        node.setActive(false);
    }

    public int countSameComponent() {
        int cnt = 0;
        for (Edge e : node.getNeighbors()) if (e.getType() == EdgeType.SAME_COMPONENT) cnt++;
        return cnt;
    }

    public void updateNodeType() {
        //check for internal node
        if (node.getSrcUID() == -1) {
            node.setType(NodeType.ROOT);
        } else if (node.getType() == NodeType.ROOT && node.getMstEdgeCount() > 0) {
            node.setType(NodeType.INTERNAL);
        } else if (node.getType() == NodeType.ROOT ) {
            node.setType(NodeType.LEAF);
        } else if (node.getType() == NodeType.LEAF){
            node.setType(NodeType.INTERNAL);
        }
    }

    /**
     * Starts the merge operation with the edge given in message
     * @param msg contains the min edge vertex
     */
    public void startMerge(GHSMessage msg, int cnt) throws InterruptedException {
        Edge mstCheck = node.getEdge(msg.getSourceUID()); // here source UID refers from where we received the message
        if (mstCheck.getType() == EdgeType.MST_EDGE){
            // special edge case
            // check for who is greater to announce the leader
            if (node.getUID() > msg.getSourceUID()){
                log.log(Level.INFO, "Special Edge Identified!\n" + mstCheck);
                while(node.getNeighbors().size()-countSameComponent() != node.neighborRepliedRound.size()) {
                    log.log(Level.INFO, "Neighbor Size: "+ node.getNeighbors().size() + "\ncountSameComponent: " + countSameComponent() + "\n NeighborReplied: " + node.neighborRepliedRound.size());
                    log.log(Level.INFO, "Neighbors replied : " + node.neighborRepliedRound);
                    if (node.getSynchGHSMessages().size() > cnt) {
                        msg.setMessageProcessed(false);
                        return;
                    }
//                    cnt = 0;
                }
                node.setSrcUID(-1);
                node.setLeader(node.getUID());
                node.setType(NodeType.ROOT);
                log.log(Level.INFO, "Leader update UID : " + node.getUID() + " message leader : " +  msg.getLeaderUID());
                broadcastLeader();
                Thread.sleep(2000);
                node.setFirstConverge(true);
                node.setConvergeDone(false);
                node.setRound(node.getRound()+1);
                node.neighborLeader.clear();
                node.neighborsMinEdge.clear();
                node.neighborRepliedRound.clear();
                broadcast();
            }
        } else {
            log.log(Level.INFO,"Updating MST graph");
//            node.setSrcUID(msg.getSourceUID());
            node.updateMyGraph(msg.getSourceUID(), EdgeType.MST_EDGE);
        }

    }

    public boolean compareEdge(Edge e1, Edge e2) {
        log.log(Level.INFO, "Curr min Edge : " + e1 + "\n" + " compared to \n" + e2);
        return e1.getToVertex() == e2.getToVertex() &&
                e1.getEdgeWt() == e2.getEdgeWt();
    }

    /**
     * Merges with the minimum edge.
     * Update source min edge from two hashmaps, neighborsMinEdge and srcMinEdge
     * @param currMinEdge the edge which needs to be merged
     */
    public void merge(Edge currMinEdge) {
        log.log(Level.INFO, "CurrMinEdge : " + currMinEdge);
        if (currMinEdge.getFromVertex() == node.getUID()) {
            //merge
            Edge minEdge = null;
            for (Edge nei : node.getNeighbors()) {
                if (compareEdge(currMinEdge, nei)) {
                        minEdge = nei;
                } else {
                    //send Empty msg to that edge
                    if(nei.getType() == EdgeType.MST_EDGE && nei.getToVertex() != node.getSrcUID())
                        sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.MERGE, currMinEdge);
                    else if(nei.getType() == EdgeType.NORMAL_EDGE || node.getSrcUID() == nei.getToVertex()){
                        sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.EMPTY, null);
                    }
                }
            }

            //start merge with min edge
            if (minEdge != null) {
                node.updateMyGraph(minEdge.getToVertex(), EdgeType.MST_EDGE);
                sendMessage(minEdge.getFromVertex(), minEdge.getToVertex(), node.getLeader(), MessageType.START_MERGE, null);
            }
        } else {
            // forward to mst edges
            for (Edge nei : node.getNeighbors()) {
                if (nei.getType() == EdgeType.MST_EDGE && nei.getToVertex() != node.getSrcUID())
                    sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.MERGE, currMinEdge);
                else if (nei.getType() == EdgeType.NORMAL_EDGE || node.getSrcUID() == nei.getToVertex())
                    sendMessage(node.getUID(), nei.getToVertex(), node.getLeader(), MessageType.EMPTY, null);
            }
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
            GHSMessage ghsM = new GHSMessage(src, dst, leader, messageType, minEdge, node.getRound());
            outputStream.writeObject(ghsM);

            if (DEBUG)
                log.log(Level.INFO, "\nMessage sent to : " + dst + ", " + ghsM + "\n");

            socket.close();
        } catch (IOException e) {
            log.log(Level.SEVERE,
                    "UID : " + node.getUID() +
                            ", broadcast, socket closed for neighbor : " + dst +
                            " hostName : " + hostName + "\n");
        }
    }

}