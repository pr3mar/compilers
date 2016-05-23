package compiler.data.cod.graph;

import compiler.data.cod.Expression;
import compiler.data.imc.TEMP;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This is the node of the graph
 * Created by pr3mar on 5/21/16.
 */
public class GraphNode {
    private Expression instruction;
    private LinkedList<GraphEdge> inEdges;
    private LinkedList<GraphEdge> outEdges;

    public Set<TEMP> in;
    public Set<TEMP> out;
    public Set<TEMP> def;
    public Set<TEMP> use;

    public GraphNode(Expression instruction) {
        this.instruction = instruction;
        this.inEdges = new LinkedList<>();
        this.outEdges = new LinkedList<>();
        this.in = new HashSet<>();
        this.out = new HashSet<>();
        this.def = instruction.getDef();
        this.use = instruction.getUse();
    }

    public void addInEdge(GraphNode from) {
        this.inEdges.add(new GraphEdge(from, this));
//        from.addOutEdge(this);
    }

    public void addOutEdge(GraphNode to) {
        this.outEdges.add(new GraphEdge(this, to));
//        to.addInEdge(this);
    }

    public LinkedList<GraphEdge> getInEdges() {
        return this.inEdges;
    }

    public LinkedList<GraphEdge> getOutEdges() {
        return this.outEdges;
    }

    public Expression getInstruction() { return this.instruction; }
}
