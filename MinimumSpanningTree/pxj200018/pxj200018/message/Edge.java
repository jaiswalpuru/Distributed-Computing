package pxj200018.message;

public class Edge {
    int fromVertex;
    int toVertex;
    int edgeWt;
    EdgeType type;

    public Edge(int from, int to, int wt, EdgeType t) {
        fromVertex = from;
        toVertex = to;
        edgeWt = wt;
        type = t;
    }

    //returns true if edge1 is greater than edge2.
    public boolean compare(Edge edge) {
        if (getEdgeWt() == edge.getEdgeWt()) {
            int maxUID1 = Math.max(getFromVertex(), edge.getToVertex());
            int minUID1 = Math.min(getFromVertex(), edge.getToVertex());
            int maxUID2 = Math.max(getFromVertex(), edge.getToVertex());
            int minUID2 = Math.min(getFromVertex(), edge.getToVertex());
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
