package pxj200018.message;

public enum MessageType {
    BROADCAST,
    CONVERGE_CAST,
    MERGE,
    GET_LEADER, //testing
    SEND_LEADER,
    LEADER_BROADCAST, //
    TERMINATE,
    NULL; // termination
}
