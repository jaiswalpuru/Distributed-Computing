package pxj200018.message;

public enum MessageType {
    MERGE,
    TERMINATE,
    BROADCAST,
    GET_LEADER, //testing,
    SEND_LEADER,
    CONVERGE_CAST,
    LEADER_BROADCAST,
    START_MERGE,
    END_MERGE,
    UPDATE_LEADER,
    NULL, INFORM_LEADER; // termination
}
