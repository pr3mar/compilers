package compiler.phase.regalloc;

import compiler.common.report.CompilerError;
import compiler.data.ast.Expr;
import compiler.data.cod.*;
import compiler.data.cod.graph.Graph;
import compiler.data.cod.graph.RegGraph;
import compiler.data.cod.print.Print;
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
    public int maxUsed;
    private boolean canReplace;

    public Coloring(FragmentCode fragment, int maxNumReg) {
        this.fragment = fragment;
        this.regGraph = fragment.regGraph;
        this.colored = new HashMap<>();
        for (TEMP curr : fragment.temps.keySet()) {
            this.colored.put(curr, "-1");
        }
        this.spilled = new HashSet<>();
        this.maxNumReg = maxNumReg;
        this.maxUsed = 0;
        this.canReplace = true;
    }

    public FragmentCode getResult() {
        this.fragment.coloredMap = this.colored;
        return this.fragment;
    }

    public void assign() {
        boolean can = true;
        int iters = 1;
        while(can) {
            this.spilled = new HashSet<>();
            color();
//            this.fragment.codeGraph.print();
//            this.regGraph.print();
//            System.out.println();
            insert();
            can = check();
            iters++;
        }
        System.out.println("Iterations = " + iters);
    }

    private void color() {
        if (this.regGraph.nodes.isEmpty())
            return;
//        while(this.canReplace) {
        // simplify
        boolean removed = false;
        Set<TEMP> edges = null;
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
//                Set<TEMP> edges = null;
            for (TEMP spill : this.regGraph.nodes.keySet()) {
                selected = spill;
                edges = new HashSet<>(this.regGraph.nodes.get(selected));
                remove(selected);
                spilled = true;
                break;
            }
            color();
            if (edges != null)
                add(selected, edges);
        }

        // select
        if (selected == null) {
            throw new CompilerError("null?");
        }
        int clr = getColor(selected);
        if (clr < 0) {
            this.spilled.add(selected);
//                this.colored.put(selected, "$-1");
        } else {
            this.colored.put(selected, "$" + clr);
        }
    }

    private boolean check() {
        for (TEMP t : this.regGraph.nodes.keySet()) {
            if (this.colored.get(t).equals("-1"))
                return true;
        }
        return false;
    }

    private void remove(TEMP remove) {
        if (this.regGraph.nodes.keySet().size() == 0) return;
        for (TEMP current : this.regGraph.nodes.keySet()) {
            this.regGraph.nodes.get(current).remove(remove);
        }
        this.regGraph.nodes.remove(remove);
    }

    private void add(TEMP add, Set<TEMP> edges) {
        this.regGraph.nodes.put(add, edges);
        for (TEMP current : edges) {
            this.regGraph.nodes.get(current).add(add);
        }
    }

    private int getColor(TEMP selected) {
        if (selected == null) return -1;
        Set<TEMP> edges = this.regGraph.nodes.get(selected);
        if (edges == null)
            throw new CompilerError("wat??");
        for (int color = 0; color < this.maxNumReg; color++) {
            if (color > this.maxUsed) this.maxUsed = color;
            boolean taken = false;
            for (TEMP edge : edges) {
                if (this.colored.get(edge).equals("$" + color)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) return color;
        }
        return -1;
    }

    private void insert() {
        if (this.spilled.size() == 0)
            return;
//        System.out.print("spilled: ");
//        for(TEMP t : this.spilled) System.out.print(t + ", ");
//        System.out.println();
        for (TEMP t : this.spilled) {
//            for(Expression exp : this.fragment.code) {
            for (int i = 0; i < this.fragment.code.size(); i++) {
//                System.out.println(i + ", " + this.fragment.code.size());
                Expression exp = this.fragment.code.get(i);
                long offset = Long.MAX_VALUE;
                TEMP tmp = null;
                if (exp.getResult() == null) continue;
                if (t.name == exp.getResult().name) {
                    tmp = new TEMP(TEMP.newTempName());
                    this.fragment.temps.put(tmp, tmp.toString());
                    this.colored.put(tmp, "-1");
                    offset = -(this.fragment.fragment.frame.locVarsSize + 2 * 8 + this.fragment.fragment.frame.tmpVarsSize + 8);
                    this.fragment.fragment.frame.tmpVarsSize += 8;
                    exp = update(exp, tmp, 1);
                    this.fragment.code.add(i + 1, new STO(tmp, "FP", offset));
                    i++;
                }
                if (exp.getOp1() != null && t.name == exp.getOp1().name) {
                    if (tmp == null) {
                        tmp = new TEMP(TEMP.newTempName());
                        this.fragment.temps.put(tmp, tmp.toString());
                        this.colored.put(tmp, "-1");
                        offset = -(this.fragment.fragment.frame.locVarsSize + 2 * 8 + this.fragment.fragment.frame.tmpVarsSize + 8);
                        this.fragment.fragment.frame.tmpVarsSize += 8;
                    }
                    exp = update(exp, tmp, 2);
                    this.fragment.code.set(i, exp);
                    this.fragment.code.add(i, new LDO(tmp, "FP", offset));
                    i++;
                }
                if (exp.getOp2() != null && t.name == exp.getOp2().name) {
                    if (tmp == null) {
                        tmp = new TEMP(TEMP.newTempName());
                        this.fragment.temps.put(tmp, tmp.toString());
                        this.colored.put(tmp, "-1");
                        offset = -(this.fragment.fragment.frame.locVarsSize + 2 * 8 + this.fragment.fragment.frame.tmpVarsSize + 8);
                        this.fragment.fragment.frame.tmpVarsSize += 8;
                    }
                    exp = update(exp, tmp, 3);
                    this.fragment.code.set(i, exp);
                    this.fragment.code.add(i, new LDO(tmp, "FP", offset));
                    i++;
                }
            }
            this.fragment.temps.remove(t);
        }
        this.fragment.coloredMap = this.colored;
        this.fragment.codeGraph = new Graph(this.fragment);
        this.fragment.codeGraph.setNodes(this.fragment.codeGraph.getNodes());
        this.fragment.regGraph = new RegGraph(this.fragment, this.spilled);
        this.regGraph = this.fragment.regGraph;
        this.colored = new HashMap<>();
        for (TEMP curr : fragment.temps.keySet()) {
            this.colored.put(curr, "-1");
        }
//        System.out.println("End of spill");
    }

    private Expression update(Expression exp, TEMP newReg, int arg) {
//        String type = exp.getPrint().printString.split(" ")[0];
        Print prt = null;
        switch (arg) {
            case 1:
                exp.getDef().remove(exp.getResult());
                exp.getDef().add(newReg);
                exp.setResult(newReg);
                prt = exp.getPrint();
                prt.op1 = newReg;
                exp.setPrint(prt);
                break;
            case 2:
                exp.getUse().remove(exp.getOp1());
                exp.getUse().add(newReg);
                exp.setOp1(newReg);
                prt = exp.getPrint();
                prt.op2 = newReg;
                exp.setPrint(prt);
                break;
            case 3:
                exp.getUse().remove(exp.getOp2());
                exp.getUse().add(newReg);
                exp.setOp2(newReg);
                prt = exp.getPrint();
                prt.op3 = newReg;
                exp.setPrint(prt);
                break;
        }
        return exp;
    }

}
