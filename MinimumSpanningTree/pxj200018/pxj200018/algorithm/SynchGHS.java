package pxj200018.algorithm;

import pxj200018.config.Config;
import pxj200018.message.Edge;
import pxj200018.message.EdgeType;
import pxj200018.message.GHSMessage;
import pxj200018.message.MessageType;
import pxj200018.server.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
            processMessages();
        }
    }

    public synchronized void processMessages() {
        List<GHSMessage> ghsMessages = Collections.synchronizedList(node.getSynchGHSMessages());

        for (GHSMessage ghsMessage : ghsMessages) {
            if (ghsMessage.isMessageProcessed()) continue;
            ghsMessage.setMessageProcessed(true);
            switch (ghsMessage.getMessageType()) {
                case BROADCAST: // broadcast all neighbors except the source.
                    node.setSrcUID(ghsMessage.getSourceUID());
                    broadcast();
                    node.setFirstConverge(true);
                    node.neighborsMinEdge.clear();
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
                default:
                    break;
            }
        }
    }

    //broadcast to MST edges only
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
            node.setFirstConverge(false);
            node.neighborLeader.clear();
            sendMessageOnNormalEdge();
            return;
        }
        HashMap<Integer, Edge> neighborsMinEdge = node.getNeighborsMinEdge();
        HashMap<Integer, Integer> neighborsLeader = node.neighborLeader;
        if (node.getMstEdgeCount() != neighborsMinEdge.size() || node.getNormalEdgeCount() != neighborsLeader.size()) {
            return;
        }

        Edge currMinEdge = null;

        //else process all converge messages
        for (Integer srcUID : neighborsLeader.keySet()) {
            int neighLeader = neighborsLeader.get(srcUID);
            if (neighLeader == node.getLeader()) {
                node.updateMyGraph(srcUID); // update the vertex which belong to the same component.
            } else {
                Edge neiEdge = node.getEdge(srcUID);
                if(currMinEdge == null) currMinEdge = neiEdge;
                else{
                    if(neiEdge.compare(neiEdge, currMinEdge)){
                        currMinEdge = neiEdge;
                    }
                }
            }
        }

        // update the min edge from the neighbors
        for (Integer srcUID : neighborsMinEdge.keySet()) {
            Edge e = neighborsMinEdge.get(srcUID);
            if (currMinEdge == null) currMinEdge = e;
            else {
                if (e.compare(e, currMinEdge)) {
                    currMinEdge = e;
                }
            }
        }

        log.log(Level.INFO, "Current minimum edge on UID : " + node.getUID() + " = " + currMinEdge + "\n");

        // send message of converge cast only if I am not the leader else need to broadcast
        // the minimum edge
        if(node.getSrcUID() == -1) {
            //Send Merge Message
            log.log(Level.INFO, " I am the leader UID : " + node.getUID() + "\n");
        } else {
            sendMessage(node.getUID(), node.getSrcUID(), node.getLeader(), MessageType.CONVERGE_CAST, currMinEdge);
        }
    }

    /**
     * To send message on the normal edges.
     * Where Normal edge is a message type.
     */
    public void sendMessageOnNormalEdge(){
        List<Edge> neighbors = node.getNeighbors();
        for(Edge e: neighbors){
            if(e.getType() == EdgeType.NORMAL_EDGE){
                sendMessage(node.getUID(), e.getToVertex(), node.getLeader(), MessageType.GET_LEADER, null);
            }
        }
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