package pxj200018.message;

import java.io.Serializable;

public class PelegMessage implements Serializable {
    //messages variables related to pelegs
    int round = -1;
    int UID_X;
    int dMax;
    int parentUID;
    boolean terminate;

    public PelegMessage(int UID_X, int dMax, int round, int parentUID, boolean terminate) {
        this.round = round;
        this.UID_X = UID_X;
        this.dMax = dMax;
        this.parentUID = parentUID;
        this.terminate = terminate;
    }

    public String toString() {
        return "Round : " + round + "\nUID_X : "  + UID_X + "\nd : " + dMax + "\nParentUID : " +
                parentUID + "\nisTerminated : " + terminate + "\n";
    }

    // getters and setters
    public int getParentUID() { return parentUID; }

    public int getRound() {
        return round;
    }

    public int getUID_X() {
        return UID_X;
    }

    public int getdMax() { return dMax; }

    public boolean isTerminate() { return terminate; }
}
