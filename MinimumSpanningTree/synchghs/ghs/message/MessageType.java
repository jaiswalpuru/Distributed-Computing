package ghs.message;

public enum MessageType {
    EMPTY,
    MERGE,
    TERMINATE,
    BROADCAST,
    GET_LEADER, //testing,
    START_MERGE,
    SEND_LEADER,
    INFORM_LEADER, // termination,
    CONVERGE_CAST,
    LEADER_BROADCAST;
}
