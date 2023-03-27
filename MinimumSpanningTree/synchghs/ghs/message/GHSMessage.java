package ghs.message;

import java.io.Serializable;

public class GHSMessage implements Serializable {
    //messages variables related to GHS
    int sourceUID;
    int destinationUID;
    int leaderUID;
    MessageType messageType;
    int round;
    Edge minEdge;
    boolean isMessageProcessed;

    public GHSMessage(int src, int dst, int leader, MessageType msgType, Edge e, int r) {
        sourceUID = src;
        destinationUID = dst;
        leaderUID = leader;
        messageType = msgType;
        minEdge = e;
        isMessageProcessed = false;
        round = r;
    }

    public void setMinEdge(Edge minEdge) {
        this.minEdge = minEdge;
    }

    public boolean isMessageProcessed() {
        return isMessageProcessed;
    }

    public void setMessageProcessed(boolean messageProcessed) {
        isMessageProcessed = messageProcessed;
    }

    public Edge getMinEdge() {
        return minEdge;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getSourceUID() {
        return sourceUID;
    }

    public int getLeaderUID() {
        return leaderUID;
    }

    public int getRound(){
        return round;
    }

    public void setRound(int r){
        this.round = r;
    }

    @Override
    public String toString() {
        return "\tROUND: " + round + "\tMESSAGE_TYPE: " + messageType + "\tSRC_UID: " + sourceUID + "\tDST_UID: " + destinationUID + "\tLEADER_UID: " + leaderUID + "\tMIN_EDGE: " + minEdge;  //"source UID : " + sourceUID + ", destination UID : " + destinationUID + ", leader UID : " + leaderUID +
          //      ", message type : " + messageType + ", min edges " + minEdge + ", round : " + round;
    }
}
