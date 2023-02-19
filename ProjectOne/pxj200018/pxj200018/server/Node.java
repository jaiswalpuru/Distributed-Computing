package pxj200018.server;

import pxj200018.algorithm.Peleg;
import pxj200018.algorithm.SynchBFS;
import pxj200018.config.Config;
import pxj200018.message.BFSMessage;
import pxj200018.message.PelegMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    /*------------Logger-------------*/
    private static final Logger log = Logger.getLogger(String.valueOf(Node.class));

    /*-------------------------------------------Peleg's variables-----------------------------------------------------*/
    int d; // distance.
    int UID; // My UID.
    int UID_X; // max UID seen so far.
    int leader; // who is the leader.
    int round; // which round is currently being executed.
    int count; // this is to keep track if for more than two rounds the d doesn't change, then terminate.
    boolean isLeaderFound; // if the leader is found.
    boolean isPelegNodeActive; // is peleg still running.
    public CopyOnWriteArrayList<PelegMessage> pelegMessages; // messages which will be received from neighbors.

    /*-------------------------------------------Synch BFS variables--------------------------------------------------*/
    int parent; // this nodes parent.
    int ackCount; // number of acknowledgement received.
    int childCount; // children count.
    boolean isMarked; // node visited or not.
    boolean isBFSActive; // if synch bfs is still running.
    List<Integer> children; // UID's of the children.
    public CopyOnWriteArrayList<BFSMessage> bfsMessages; // message buffer for "search" messages

    /*-------------------------------------------Node variables-----------------------------------------------------*/
    Config config;
    int portNumber;
    String hostName;
    List<Integer> neighbors;

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

        String[] host = ip.getHostName().split("\\.");
        hostName = host[0];

        // this is just for testing
        if (hostName.equals("Purus-MacBook-Pro") || hostName.equals("Purus-MBP")) {
            portNumber = 8000;
            UID = -1;
            neighbors = null;
            return;
        } // to deleted in the future.

        // node details
        portNumber = config.getMyPortNumber(hostName);
        UID = config.getMyUID(hostName);
        neighbors = config.getNeighbors(UID);

        // pelegs
        this.round = 0;
        this.UID_X = UID;
        this.d = 0;
        this.isPelegNodeActive = true;
        this.count = 0;
        this.isLeaderFound = false;
        this.pelegMessages = new CopyOnWriteArrayList<>();

        // bfs
        this.parent = -1;
        this.childCount = 0;
        this.children = new ArrayList<>();
        this.ackCount = 0;
        this.isBFSActive = true;
        this.isMarked = false;
        this.bfsMessages = new CopyOnWriteArrayList<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Node n = new Node();

        n.handlePelegClient();
        // sleeping assuming all nodes to be active in that time.
        Thread.sleep(10000);
        n.startLeaderElection();
        log.log(Level.INFO, "Peleg Execution over\n");

        Thread.sleep(10000);

        //sleeping after peleg to ensure all the nodes have returned.
        n.handleBFSClient();
        n.startSynchBFS();
        log.log(Level.INFO, "Synch BFS execution over\n");
    }

    /**
     * Spawns a thread to run synch bfs algorithm.
     */
    private void startSynchBFS() throws InterruptedException {
        Thread bfsThread = new Thread(new SynchBFS(this));
        bfsThread.start();
        bfsThread.join();
    }

    /**
     * Spawns a thread to start the socket server to handles neighbors messages
     */
    private void handleBFSClient() { new Thread(new ClientHandlerSynchBFS(this)).start(); }

    /**
     * Spawns a thread to run leader election algorithm.
     */
    private void startLeaderElection() throws InterruptedException {
        Thread pelegThread = new Thread(new Peleg(this));
        pelegThread.start();
        pelegThread.join();
    }

    /**
     * Spawns a thread to start the socket server to handle neighbors messages
     */
    private void handlePelegClient() { new Thread(new ClientHandlerPeleg(this)).start(); }

    // getters and setters
    public CopyOnWriteArrayList<PelegMessage> getPelegMessages() { return pelegMessages; }

    public int getRound() { return this.round; }

    public void setRound(int round) { this.round = round; }

    public Config getConfig() { return config; }

    public int getD() {
        return d;
    }

    public void setD(int d) { this.d = d; }

    public int getLeader() { return leader; }

    public void setLeader(int leader) { this.leader = leader; }

    public String getHostName() { return hostName; }

    public boolean isLeaderFound() { return isLeaderFound; }

    public void setLeader(boolean flag) { this.isLeaderFound = flag; }

    public int getUID_X() { return UID_X; }

    public void setUID_X(int uid_x) { this.UID_X = uid_x; }

    public int getUID() { return UID; }

    public int getPortNumber() { return portNumber; }

    public boolean isNodeActive() { return this.isPelegNodeActive; }

    public void setIsNodeActive(boolean flag) { this.isPelegNodeActive = flag; }

    public int getCount() { return this.count; }

    public void setCount(int count) { this.count = count; }

    public List<Integer> getNeighbors() { return neighbors; }

    public String toString() { return "Round : " + this.round + "\nUID : " +
            this.UID + "\nMaxUID : " + this.UID_X + "\nDis : " + d;  }
}
