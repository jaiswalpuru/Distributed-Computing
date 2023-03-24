package ghs.message;

import java.io.Serializable;

public class Edge implements Serializable {
    int edgeWt;
    int toVertex;
    int fromVertex;
    EdgeType type;

    public Edge(int from, int to, int wt, EdgeType t) {
        fromVertex = from;
        toVertex = to;
        edgeWt = wt;
        type = t;
    }

    //returns true if edge1 is greater than edge2.
    public boolean compare(Edge edge) {
        System.out.println(this + " compared to edge : " + edge);
        if (getEdgeWt() == edge.getEdgeWt()) {
            int maxUID1 = Math.max(getFromVertex(), getToVertex());
            int minUID1 = Math.min(getFromVertex(), getToVertex());
            int maxUID2 = Math.max(edge.getFromVertex(), edge.getToVertex());
            int minUID2 = Math.min(edge.getFromVertex(), edge.getToVertex());
            if (maxUID1 == maxUID2) {
                return minUID1 > minUID2;
            }
            return maxUID1 > maxUID2;
        }
        return getEdgeWt() < edge.getEdgeWt();
    }

    public void setType(EdgeType type) {
        this.type = type;
    }

    public int getEdgeWt() {
        return edgeWt;
    }

    public int getFromVertex() {
        return fromVertex;
    }

    public EdgeType getType() {
        return type;
    }

    public int getToVertex() {
        return toVertex;
    }

    public String toString() {
        return "From Vertex : " + fromVertex + ", to Vertex : " + toVertex + ", wt : " + edgeWt + ", edge type : " +
                type;
    }
}
