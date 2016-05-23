package compiler.data.cod.graph;

import compiler.data.imc.TEMP;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Interference graph node
 * Created by pr3mar on 5/23/16.
 */
public class RegNode {
    private TEMP node;

    private Set<RegEdge> in;
    private Set<RegEdge> out;

    public RegNode(TEMP node) {
        this.node = node;
        this.in = new HashSet<>();
        this.out = new HashSet<>();
    }

    public void addIn(RegNode node) {
        this.in.add(new RegEdge(this, node));
        node.addOut(this);
    }

    public void addOut(RegNode node) {
        this.out.add(new RegEdge(this, node));
        node.addIn(this);
    }

    public TEMP getNode() {
        return node;
    }

    public Set<RegEdge> getIn() {
        return in;
    }

    public Set<RegEdge> getOut() {
        return out;
    }
}
