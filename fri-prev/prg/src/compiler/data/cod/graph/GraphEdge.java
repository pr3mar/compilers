package compiler.data.cod.graph;

/**
 * This class shows how the registers are connected to each other
 * Created by pr3mar on 5/21/16.
 */
public class GraphEdge {

    GraphNode to;
    GraphNode from;

    public GraphEdge(GraphNode from, GraphNode to) {
        this.from = from;
        this.to = to;
    }

    public GraphNode getTo() {
        return to;
    }

    public GraphNode getFrom() {
        return from;
    }
}
