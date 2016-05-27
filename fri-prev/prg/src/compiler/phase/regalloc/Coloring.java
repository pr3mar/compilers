package compiler.phase.regalloc;

import compiler.data.cod.Expression;
import compiler.data.cod.graph.RegGraph;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Color the provided interference graph
 * Created by pr3mar on 5/27/16.
 */
public class Coloring {

    private FragmentCode fragment;
    private RegGraph regGraph;
    private HashMap<TEMP, String> colored;
    private Set<TEMP> spilled;
    private int maxNumReg;
    private boolean canReplace;

    public Coloring(FragmentCode fragment, int maxNumReg) {
        this.fragment = fragment;
        this.regGraph = fragment.regGraph;
        this.colored = new HashMap<>();
        for(TEMP curr : fragment.temps.keySet()) {
            this.colored.put(curr, "-1");
        }
        this.spilled = new HashSet<>();
        this.maxNumReg = maxNumReg;
        this.canReplace = true;
    }

    public FragmentCode getResult() {
        this.fragment.coloredMap = this.colored;
        return this.fragment;
    }

    public void color() {
        if(this.regGraph.nodes.isEmpty())
            return;
        while(this.canReplace) {
            // simplify
            boolean removed = false;
            Set<TEMP> edges;
            TEMP selected = null;
            for (TEMP current : this.regGraph.nodes.keySet()) {
                Set<TEMP> currEdges = this.regGraph.nodes.get(current);
                if (currEdges.size() < this.maxNumReg) {
                    edges = new HashSet<>(currEdges);
                    remove(current);
                    color();
                    add(current, edges);
                    selected = current;
                    removed = true;
                    break;
                }
            }

            // spill
            boolean spilled = false;
            if (!removed) {
                for (TEMP spill : this.regGraph.nodes.keySet()) {
                    selected = spill;
                    spilled = true;
                    break;
                }
                color();
            }

            // select
            int clr = getColor(selected);
            if (clr < 0) this.spilled.add(selected);
            else this.colored.put(selected, "$" + clr);

            if(removed && !spilled) this.canReplace = false;
            // start over
            // TODO: insert new registers
            insert();
        }
    }

    private void remove(TEMP remove) {
        if(this.regGraph.nodes.keySet() == null) return;
        for(TEMP current : this.regGraph.nodes.keySet()) {
            this.regGraph.nodes.get(current).remove(remove);
        }
        this.regGraph.nodes.remove(remove);
    }

    private void add(TEMP add, Set<TEMP> edges) {
        this.regGraph.nodes.put(add, edges);
        for(TEMP current : edges) {
            this.regGraph.nodes.get(current).add(add);
        }
    }

    private int getColor(TEMP selected) {
        Set<TEMP> edges = this.regGraph.nodes.get(selected);
        for(int color = 0; color < this.maxNumReg; color++) {
            boolean taken = false;
            for(TEMP edge : edges) {
                if(this.colored.get(edge).equals("$" + color)) {
                    taken = true;
                    break;
                }
            }
            if(!taken) return color;
        }
        return -1;
    }

    private void insert() {
        for(TEMP t : this.spilled) {
            for(Expression exp : this.fragment.code) {
                if(exp.getDef().contains(t)) {
                    // insert code
                }
                if(exp.getDef().contains(t)) {
                    // insert code
                }
            }
        }
    }

}
