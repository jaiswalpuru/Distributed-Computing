package ghs.server;

import ghs.algorithm.SynchGHS;
import ghs.config.Config;
import ghs.message.Edge;
import ghs.message.EdgeType;
import ghs.message.GHSMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    /*------------Logger-------------*/
    private static final Logger log = Logger.getLogger(String.valueOf(Node.class));

    /*-------------------------------------------Synch GHS variables-----------------------------------------------------*/
    int UID; // My UID.
    int srcUID;
    int leader; // who is the leader.
    int round;
    int mstEdgeCount;
    Boolean isActive;
    int normalEdgeCount;
    boolean isConvergeDone;
    Boolean isFirstMessage;

    Boolean isFirstConverge;
    public HashMap<Integer, Edge> neighborsMinEdge; // <srcUID, minEdge>
    public HashMap<Integer, Integer> neighborLeader; // <srcUID,leaderUID>
    public HashSet<Integer> neighborRepliedRound;
    public CopyOnWriteArrayList<GHSMessage> ghsMessages; // messages which will be received from neighbors.
    /*-------------------------------------------Synch GHS variables-----------------------------------------------------*/

    /*-------------------------------------------Node variables-----------------------------------------------------*/
    Config config;
    int portNumber;
    String hostName;
    List<Edge> neighbors;

    /**
     * Constructor
     */
    public Node() {
        neighbors = new ArrayList<>();
        config = new Config();
        config.readFile();

        //get the host name
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostName = ip.getHostName();

        // node details
        portNumber = config.getMyPortNumber(hostName);
        UID = config.getMyUID(hostName);
        neighbors = config.getNeighbors(UID);

        //ghs
        srcUID = -1;
        round = 0;
        leader = UID;
        isActive = true;
        mstEdgeCount = 0;
        isFirstMessage = true;
        isFirstConverge = true;
        isConvergeDone = false;
        neighborRepliedRound = new HashSet<>();
        neighborLeader = new HashMap<>();
        normalEdgeCount = neighbors.size();
        neighborsMinEdge = new HashMap<>();
        ghsMessages = new CopyOnWriteArrayList<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Node n = new Node();

        n.handleGHSClient();
        //sleeping for some time to wait for all the nodes to be active by that time.
        Thread.sleep(10000);
        log.log(Level.INFO, "\nSocket client listening at port : " + n.portNumber+"\n");

        n.startSynchGHS();
        log.log(Level.INFO, "\nSynchGHS Execution over\n");
    }

    /**
     * Spawn a thread to start the Synch GHS algorithm.
     */
    public void startSynchGHS() throws InterruptedException {
        Thread t = new Thread(new SynchGHS(this));
        t.start();
        t.join();
    }

    /**
     * Spawns a thread to start the socket server to handle neighbors messages
     */
    private void handleGHSClient() { new Thread(new ClientHandler(this)).start(); }

    /**
     * To update my neighbors edge type.
     * @param srcUID uid of the source vertex whose graph need to be updated.
     */
    public void updateMyGraph(int srcUID, EdgeType type) {
        if (type == EdgeType.MST_EDGE) setMstEdgeCount(getMstEdgeCount()+1);

        for (Edge e : neighbors) {
            if (e.getToVertex() == srcUID) {
                setNormalEdgeCount(getNormalEdgeCount()-1);
                e.setType(type);
                if (neighborLeader.getOrDefault(srcUID, 0) != 0) {
                    neighborLeader.remove(srcUID);
                }
            }
        }
    }

    // getters and setters
    /**
     * To get the edge which matches the To vertex.
     * @param srcUID uid for which
     * @return Edge from which the current node is connected.
     */
    public Edge getEdge(int srcUID){
        for (Edge e: neighbors){
            if(e.getToVertex() == srcUID)   return e;
        }
        return null;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public CopyOnWriteArrayList<GHSMessage> getSynchGHSMessages() { return ghsMessages; }

    public void setConvergeDone(boolean convergeDone) {
        isConvergeDone = convergeDone;
    }

    public boolean isConvergeDone() {
        return isConvergeDone;
    }

    public HashMap<Integer, Integer> getNeighborLeader() {
        return neighborLeader;
    }

    public void setNeighborLeader(HashMap<Integer, Integer> neighborLeader) {
        this.neighborLeader = neighborLeader;
    }

    public Boolean getFirstConverge() {
        return isFirstConverge;
    }

    public void setFirstConverge(Boolean firstConverge) {
        isFirstConverge = firstConverge;
    }

    public Config getConfig() { return config; }

    public Boolean getActive() {
        return isActive;
    }

    public int getNormalEdgeCount() {
        return normalEdgeCount;
    }

    public void setNormalEdgeCount(int normalEdgeCount) {
        this.normalEdgeCount = normalEdgeCount;
    }

    public void setMstEdgeCount(int mstEdgeCount) {
        this.mstEdgeCount = mstEdgeCount;
    }

    public int getMstEdgeCount() {
        return mstEdgeCount;
    }

    public HashMap<Integer, Edge> getNeighborsMinEdge() {
        return neighborsMinEdge;
    }

    public void updateMinEdge(int srcUID, Edge minEdge) {
        neighborsMinEdge.putIfAbsent(srcUID, minEdge);
    }

    public void updateNeighborLeader(int vertex, int leader) {
        neighborLeader.putIfAbsent(vertex, leader);
    }

    public int getSrcUID() {
        return srcUID;
    }

    public void setSrcUID(int srcUID) {
        this.srcUID = srcUID;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getFirstMessage() {
        return isFirstMessage;
    }

    public void setFirstMessage(Boolean firstMessage) {
        isFirstMessage = firstMessage;
    }

    public int getLeader() { return leader; }

    public void setLeader(int leader) { this.leader = leader; }

    public int getUID() { return UID; }

    public int getPortNumber() { return portNumber; }

    public List<Edge> getNeighbors() { return neighbors; }

}
