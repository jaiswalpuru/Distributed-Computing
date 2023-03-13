package pxj200018.algorithm;

import javafx.util.Pair;
import pxj200018.message.GHSMessage;
import pxj200018.server.Node;

import java.util.Collections;
import java.util.List;

public class SynchGHS implements Runnable {
    Node node;
    public SynchGHS(Node n) {
        node = n;
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
            }else {

                //check();
            }
        }
    }

    public void broadcast() {
        List<Pair<Integer, Integer>> neighbors = node.getNeighbors();
        for (Pair<Integer, Integer> p : neighbors) {
            Integer v = p.getKey();

        }
    }

    public void broadcast(int vertex) {

    }

    public void convergeCast() {

    }

    public void merge(GHSMessage ghsMsg) {

    }


    public void check() {
        List<GHSMessage> messages = Collections.synchronizedList(node.getSynchGHSMessages());

        for (GHSMessage ghsMsg : messages) {
            switch(ghsMsg.getMessageType()) {
                case BROADCAST:

                    break;
            }
        }
    }

}

// broadcast
// converge cast
// merge message (parent child relationship)