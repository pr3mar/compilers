package compiler.data.cod.graph;

import compiler.data.ast.Expr;
import compiler.data.cod.*;
import compiler.data.frg.Fragment;
import compiler.data.imc.TEMP;

import compiler.phase.codegen.FragmentCode;
import compiler.phase.lexan.Symbol;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This class represents a graph data structure
 * Created by pr3mar on 5/21/16.
 */
public class Graph {
    private FragmentCode fragmentCode;
    private LinkedList<GraphNode> nodes;

    /**
     * every register used in the
     * @param fragmentCode
     */
    public Graph(FragmentCode fragmentCode) {
        this.fragmentCode = fragmentCode;
        this.nodes = new LinkedList<>();
        for(int i = 0; i < this.fragmentCode.code.size(); i++) {
            Expression instruction = this.fragmentCode.code.get(i);
            GraphNode currentNode = new GraphNode(instruction);
            this.nodes.add(currentNode);
        }
        buildGraph();
        interfere();
    }

    public LinkedList<GraphNode> getNodes() {
        return this.nodes;
    }

    void interfere() {
        boolean equal = true;
        int st = 0;
        do {
            equal = true;
//            for (GraphNode node : this.nodes) {
            for (int n = (this.nodes.size() - 1); n >= 0; n--) {
                GraphNode node = this.nodes.get(n);
                Set<TEMP> oldIn = new HashSet<>(node.in);
                Set<TEMP> oldOut = new HashSet<>(node.out);

                node.out = new HashSet<>();
                for (int i = 0; i < node.getOutEdges().size(); i++) {
                    node.out.addAll(node.getOutEdges().get(i).to.in);
                }

                Expression instruction = node.getInstruction();
                Set<TEMP> diff = new HashSet<>(node.out);
                diff.removeAll(instruction.getDef());

                Set<TEMP> tmp2 = new HashSet<>(instruction.getUse());
                tmp2.addAll(diff);

                node.in = new HashSet<>(tmp2);
                equal = equal && equalSets(node.in, oldIn);
                equal = equal && equalSets(node.out, oldOut);
            }
            st++;
        } while(!equal);
        System.out.println("iterations = " +  st);
        print();
    }

    boolean equalSets(Set<TEMP> first, Set<TEMP> second) {
        return first.size() == second.size() && first.containsAll(second);
    }

    void print() {
        for (GraphNode node : this.nodes) {
            if(node.getInstruction() instanceof LAB) continue;
            System.out.println(node.getInstruction().getPrint().toString(fragmentCode.temps));
            System.out.print("INS: ");
            for(TEMP t : node.in){
                System.out.print(t + ", ");
            }
            System.out.println();
            System.out.print("OUTS: ");
            for(TEMP t : node.out){
                System.out.print(t + ", ");
            }
            System.out.println();
            System.out.println();
        }
        System.out.println();
    }


    private void buildGraph() {
        for(int i = 0; i < this.nodes.size(); i++) {
            findSuccessors(i);
        }
    }

    private void findSuccessors(int index) {
        GraphNode currentNode = this.nodes.get(index);
        Expression ex = currentNode.getInstruction();
        if(ex instanceof JMP) {
            currentNode.addOutEdge(getAfterLabel(ex.getLabel()));
            return;
        }
        if(ex instanceof BP) {
            currentNode.addOutEdge(getAfterLabel(ex.getLabel()));
        }
        if(index + 1 < this.nodes.size())
            currentNode.addOutEdge(this.nodes.get(index + 1));
    }

    private GraphNode getAfterLabel(String label) {
        GraphNode ret = null;
        for(int i = 0; i < this.nodes.size(); i++) {
            GraphNode current = this.nodes.get(i);
            Code instr = current.getInstruction();
            if(instr instanceof LAB
                    && ((LAB) instr).getLabel().equals(label)
                        && ((i + 1) < this.nodes.size()) ) {
                ret = this.nodes.get(i + 1);
                break;
            }
        }
        return ret;
    }
}