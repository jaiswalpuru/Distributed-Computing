package pxj200018.server;

import javafx.util.Pair;
import pxj200018.algorithm.SynchGHS;
import pxj200018.config.Config;
import pxj200018.message.GHSMessage;

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

    /*-------------------------------------------Synch GHS variables-----------------------------------------------------*/
    int UID; // My UID.
    int leader; // who is the leader.
    Boolean isActive;
    Boolean isFirstMessage;
    public CopyOnWriteArrayList<GHSMessage> ghsMessages; // messages which will be received from neighbors.
    /*-------------------------------------------Synch GHS variables-----------------------------------------------------*/

    /*-------------------------------------------Node variables-----------------------------------------------------*/
    Config config;
    int portNumber;
    String hostName;
    List<Pair<Integer, Integer>> neighbors;

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
        System.out.println(hostName);

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

        //ghs
        isActive = true;
        isFirstMessage = true;
        leader = UID;
        ghsMessages = new CopyOnWriteArrayList<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Node n = new Node();

        n.handleGHSClient();
        n.startSynchGHS();
        // sleeping assuming all nodes to be active in that time.
        Thread.sleep(5000);
        log.log(Level.INFO, "SynchGHS Execution over\n");
    }

    public void startSynchGHS() throws InterruptedException {
        Thread t = new Thread(new SynchGHS(this));
        t.start();
        t.join();
    }

    /**
     * Spawns a thread to start the socket server to handle neighbors messages
     */
    private void handleGHSClient() { new Thread(new ClientHandler(this)).start(); }

    // getters and setters
    public CopyOnWriteArrayList<GHSMessage> getSynchGHSMessages() { return ghsMessages; }

    public Config getConfig() { return config; }

    public Boolean getActive() {
        return isActive;
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

    public String getHostName() { return hostName; }

    public int getUID() { return UID; }

    public int getPortNumber() { return portNumber; }

    public List<Pair<Integer, Integer>> getNeighbors() { return neighbors; }

}
