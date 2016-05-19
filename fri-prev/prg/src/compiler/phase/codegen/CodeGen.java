package compiler.phase.codegen;

import compiler.Task;
import compiler.data.cod.*;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.phase.Phase;


import java.util.HashMap;
import java.util.LinkedList;


/**
 * Generation of asm code
 *
 * @author pr3mar
 */
public class CodeGen extends Phase{
    private Task task;

    private final HashMap<String, Fragment> fragments;

    private LinkedList<Holder> allCode;

    public CodeGen(Task task) {
        super(task, "codegen");
        this.task = task;
        this.fragments = task.fragments;
        this.allCode = new LinkedList<>();
        iterate();
        PrintCode print = new PrintCode(allCode);
        print.print();
    }

    void iterate() {
        for(Fragment fragment : this.fragments.values()) {
            if(fragment instanceof CodeFragment) {
                GenerateCode generate = new GenerateCode((CodeFragment) fragment);
                generate.generate();
                Holder codeNow = generate.get();
                // TODO: generate interference graph here, or make it a new phase
                allCode.add(codeNow);
            }
        }
    }

//    void generate(CodeFragment fragment) {
//        for(int i = 0; i < fragment.linCode.numStmts(); i++) {
//            fragment.linCode.stmts(i);
//        }
//    }
}
