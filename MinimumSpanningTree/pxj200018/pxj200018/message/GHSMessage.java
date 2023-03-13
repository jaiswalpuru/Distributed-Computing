package pxj200018.message;

import java.io.Serializable;

public class GHSMessage implements Serializable {
    //messages variables related to pelegs
    int sourceUID;
    int destinationUID;
    int leaderUID;
    MessageType messageType;

    public GHSMessage() {
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return "";
    }
}
