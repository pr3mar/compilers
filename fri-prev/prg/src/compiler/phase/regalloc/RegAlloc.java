package compiler.phase.regalloc;

import compiler.Task;
import compiler.common.report.CompilerError;
import compiler.data.cod.print.PrintCode;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.data.frm.Frame;
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

    private final long MAX_STACK_SIZE = 10240;

    public RegAlloc(Task task, int maxNumReg) {
        super(task, "regalloc");
        this.task = task;
        this.fragCode = task.generatedCode;
        /*if(4 > maxNumReg || maxNumReg > 64)
            throw new CompilerError("[RegAlloc] Invalid number of max registers");*/
        this.maxNumReg = maxNumReg;
        color();
        check();
        PrintCode print = new PrintCode(task.generatedCode);
        if(task.phase.equals("regalloc")) {
//            print.print();
//            print.printColored();
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


    void check() { // check stack size
        long stackSize = 0;
        for (int i = 0; i < this.fragCode.size(); i++) {
            FragmentCode currentCode = this.fragCode.get(i);
            Frame currentFrame = currentCode.fragment.frame;
//            stackSize += currentFrame.size + currentFrame.locVarsSize + currentFrame.tmpVarsSize;
            stackSize += currentFrame.locVarsSize + 16 + currentFrame.tmpVarsSize + currentFrame.hidRegsSize + currentFrame.outCallSize;
            if(stackSize > MAX_STACK_SIZE) {
                throw new CompilerError("Stack is too big! ("  + stackSize + " bytes)");
            }
        }
        System.out.println("Stack size = " + stackSize);
    }
}
