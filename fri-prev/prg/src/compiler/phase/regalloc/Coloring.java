package compiler.phase.regalloc;

import compiler.data.cod.graph.RegGraph;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.data.imc.TEMP;

import java.util.HashMap;

/**
 * Color the provided interference graph
 * Created by pr3mar on 5/27/16.
 */
public class Coloring {

    private FragmentCode fragment;
    private RegGraph regGraph;
    private HashMap<TEMP, String> colored;

    public Coloring(FragmentCode fragment) {
        this.fragment = fragment;
        this.regGraph = fragment.regGraph;
        this.colored = new HashMap<>(this.fragment.temps);
    }

    public FragmentCode getResult() {
        return this.fragment;
    }

    public void color() {
        // TODO
    }

}
