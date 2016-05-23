package compiler.data.cod.graph;

import compiler.data.imc.TEMP;
import compiler.phase.codegen.FragmentCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Interference graph
 * Created by pr3mar on 5/23/16.
 */
public class RegGraph {

    HashMap<TEMP, Set<TEMP>> nodes;
    FragmentCode fragmentCode;

    public RegGraph(FragmentCode fragmentCode) {
        this.fragmentCode = fragmentCode;
        this.nodes = new HashMap<>();
        for(TEMP temp : this.fragmentCode.temps.keySet()) {
            this.nodes.put(temp, new HashSet<TEMP>());
        }
        buildGraph();
    }

    private void buildGraph() {
        for(GraphNode node : this.fragmentCode.codeGraph.getNodes()) {
            if(node.getInstruction().getMove()) {
                for (TEMP def : node.def) {
                    Set<TEMP> update = this.nodes.get(def);
                    for(TEMP out : node.out) {
                        update.add(out);
                    }
                    this.nodes.put(def, update);
                }
            } else {
                TEMP dont = node.getInstruction().getOp1();
                for (TEMP def : node.def) {
                    Set<TEMP> update = this.nodes.get(def);
                    for(TEMP out : node.out) {
//                        if(out.equals(dont)) continue;
                        update.add(out);
                    }
                    update.remove(dont);
                    this.nodes.put(def, update);
                }
            }
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for(TEMP reg : this.nodes.keySet()) {
            ret += reg.toString() + ": ";
            for(TEMP ints : this.nodes.get(reg)) {
                ret += ints.toString() + ", ";
            }
            ret += "\n";
        }
        return ret;
    }

    public void print() {
        System.out.println(this.toString());
    }
}
