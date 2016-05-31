package compiler.phase.regalloc;

import compiler.Task;
import compiler.common.report.CompilerError;
import compiler.data.cod.print.PrintCode;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.phase.Phase;

import java.util.LinkedList;

/**
 * Register allocator
 * Created by pr3mar on 5/27/16.
 */
public class RegAlloc extends Phase {
    private Task task;
    private LinkedList<FragmentCode> fragCode;
    private int maxNumReg;

    public RegAlloc(Task task, int maxNumReg) {
        super(task, "regalloc");
        this.task = task;
        this.fragCode = task.generatedCode;
        /*if(4 > maxNumReg || maxNumReg > 64)
            throw new CompilerError("[RegAlloc] Invalid number of max registers");*/
        this.maxNumReg = maxNumReg;
        color();
        PrintCode print = new PrintCode(task.generatedCode);
        if(task.phase.equals("regalloc")) {
//            print.print();
            print.printColored();
        }
    }

    void color() {
        for(int i = 0; i < this.fragCode.size(); i++) {
            FragmentCode code = this.fragCode.get(i);
            Coloring color = new Coloring(code, this.maxNumReg);
            color.assign();
            code = color.getResult();
//            System.out.println(color.maxUsed);
            this.fragCode.set(i, code);
        }
    }
}
