package compiler.data.cod.graph;

import compiler.common.report.CompilerError;
import compiler.data.imc.TEMP;
import compiler.data.cod.wrapper.FragmentCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Interference graph
 * Created by pr3mar on 5/23/16.
 */
public class RegGraph {

    public HashMap<TEMP, Set<TEMP>> nodes;
    private FragmentCode fragmentCode;

    public RegGraph(FragmentCode fragmentCode) {
        this.fragmentCode = fragmentCode;
        this.nodes = new HashMap<>();
        for(TEMP temp : this.fragmentCode.temps.keySet()) {
            if(this.fragmentCode.temps.get(temp) == null)
                throw new CompilerError("FUCK U!");
//            if(this.fragmentCode.temps.get(temp).equals("SP")) continue;
            this.nodes.put(temp, new HashSet<TEMP>());
        }
        buildGraph();
    }

    public RegGraph(FragmentCode fragmentCode, Set<TEMP> spilled) {
        this.fragmentCode = fragmentCode;
        this.nodes = new HashMap<>();
        for(TEMP temp : this.fragmentCode.temps.keySet()) {
            if(spilled.contains(temp)) {
                throw new CompilerError("GOTCHA!");
            }
//            if(this.fragmentCode.temps.get(temp).equals("SP")) continue;
            this.nodes.put(temp, new HashSet<TEMP>());
        }
        buildGraph();
    }

    private void buildGraph() {
        /*for(GraphNode node : this.fragmentCode.codeGraph.getNodes()) {
            if(!node.getInstruction().getMove()) {
                for (TEMP def : node.def) {
                    Set<TEMP> update = this.nodes.get(def);
                    for(TEMP out : node.out) {
                        if(out.equals(def)) continue;
                        update.add(out);
                        Set<TEMP> outUpdate = this.nodes.get(out);
                        this.nodes.put(out, outUpdate);
                        outUpdate.add(def);
                    }
                    this.nodes.put(def, update);
                }
            } else {
                TEMP dont = node.getInstruction().getOp1();
                for (TEMP def : node.def) {
                    Set<TEMP> update = this.nodes.get(def);
                    for(TEMP out : node.out) {
                        if(out.equals(def)) continue;
                        update.add(out);
                        Set<TEMP> outUpdate = this.nodes.get(out);
                        outUpdate.add(def);
                        this.nodes.put(out, outUpdate);
                    }
                    update.remove(dont);
                    this.nodes.put(def, update);
                }
            }
        }*/
        for(GraphNode node : this.fragmentCode.codeGraph.getNodes()) {
            for (TEMP in : node.in) {
                Set<TEMP> inUpdate = this.nodes.get(in);
                if(inUpdate == null) continue;
                for (TEMP add : node.in) {
                    if(add.equals(in)) continue;
                    Set<TEMP> addUpdate = this.nodes.get(add);
                    if(addUpdate == null) continue;
                    inUpdate.add(add);
                    addUpdate.add(in);
                    this.nodes.put(add, addUpdate);
                }
                this.nodes.put(in, inUpdate);
            }
            for (TEMP out : node.out) {
                Set<TEMP> outUpdate = this.nodes.get(out);
                if(outUpdate == null) continue;
                for (TEMP add : node.out) {
                    if(add.equals(out)) continue;
                    Set<TEMP> addUpdate = this.nodes.get(add);
                    if(addUpdate == null) continue;
                    outUpdate.add(add);
                    addUpdate.add(out);
                    this.nodes.put(add, addUpdate);
                }
                this.nodes.put(out, outUpdate);
            }
        }
    }

    public HashMap<TEMP, Set<TEMP>> getNodes() {
        return this.nodes;
    }

    public void setNodes(HashMap<TEMP, Set<TEMP>> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        String ret = "";
        for(TEMP reg : this.nodes.keySet()) {
            ret += this.fragmentCode.temps.get(reg) + ": ";
            for(TEMP ints : this.nodes.get(reg)) {
                ret += this.fragmentCode.temps.get(ints) + ", ";
            }
            ret += "\n";
        }
        return ret;
    }

    public void print() {
        for(TEMP reg : this.nodes.keySet()) {
            System.out.print(this.fragmentCode.temps.get(reg) + ": ");
            for(TEMP ints : this.nodes.get(reg)) {
                System.out.print(this.fragmentCode.temps.get(ints) + ", ");
            }
            System.out.println();
        }
    }
}
