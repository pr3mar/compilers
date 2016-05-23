package compiler.data.cod.graph;

/**
 * Interference graph edge
 * Created by pr3mar on 5/23/16.
 */
public class RegEdge {
    private RegNode from;
    private RegNode to;

    public RegEdge(RegNode from, RegNode to) {
        this.from = from;
        this.to = to;
    }

    public RegNode getFrom() {
        return from;
    }

    public RegNode getTo() {
        return to;
    }
}
