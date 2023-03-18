package pxj200018.message;

import java.io.Serializable;

public class GHSMessage implements Serializable {
    //messages variables related to GHS
    int sourceUID;
    int destinationUID;
    int leaderUID;
    MessageType messageType;
    Edge minEdge;
    boolean isMessageProcessed;

    public GHSMessage(int src, int dst, int leader, MessageType msgType, Edge e) {
        sourceUID = src;
        destinationUID = dst;
        leaderUID = leader;
        messageType = msgType;
        minEdge = e;
        isMessageProcessed = false;
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

    @Override
    public String toString() {
        return "source UID : " + sourceUID + ", destination UID : " + destinationUID + ", leader UID : " + leaderUID +
                ", message type : " + messageType + ", min edges " + minEdge;
    }
}
