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
    public boolean compare(Edge edge1, Edge edge2) {
        if (edge1.getEdgeWt() == edge2.getEdgeWt()) {
            int maxUID1 = Math.max(edge1.getFromVertex(), edge1.getToVertex());
            int minUID1 = Math.min(edge1.getFromVertex(), edge1.getToVertex());
            int maxUID2 = Math.max(edge2.getFromVertex(), edge2.getToVertex());
            int minUID2 = Math.min(edge2.getFromVertex(), edge2.getToVertex());
            if (maxUID1 == maxUID2) {
                return minUID1 > minUID2;
            }
            return maxUID1 > maxUID2;
        }
        return edge1.getEdgeWt() < edge2.getEdgeWt();
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
