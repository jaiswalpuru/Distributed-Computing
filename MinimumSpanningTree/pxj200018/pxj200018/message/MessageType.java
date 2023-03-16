package pxj200018.message;

public enum MessageType {
    NULL,
    MERGE,
    END_MERGE,
    TERMINATE,
    BROADCAST,
    GET_LEADER, //testing,
    START_MERGE,
    SEND_LEADER,
    INFORM_LEADER, // termination,
    UPDATE_LEADER,
    CONVERGE_CAST,
    LEADER_BROADCAST;
}
