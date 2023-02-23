package pxj200018.message;

import java.io.Serializable;

public class BFSMessage implements Serializable {

    int UID;
    MessageType messageType;

    public BFSMessage(int UID, MessageType messageType) {
        this.UID = UID;
        this.messageType = messageType;
    }

    public int getUID() { return UID; }

    public MessageType getMessageType() { return messageType; }

    @Override
    public String toString() {
        return "UID : " + UID + "\nMessageType : " + messageType.toString() + "\n" ;
    }
}
