package compiler.phase.codegen;

import compiler.Task;
import compiler.data.cod.*;
import compiler.data.cod.graph.Graph;
import compiler.data.cod.graph.RegGraph;
import compiler.data.cod.print.Print;
import compiler.data.cod.print.PrintCode;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.frm.Frame;
import compiler.data.imc.TEMP;
import compiler.phase.Phase;
import compiler.data.cod.graph.GraphNode;

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

    private final long MAX_STACK_SIZE = 10240; // bytes

    public CodeGen(Task task) {
        super(task, "codegen");
        this.task = task;
        this.fragments = task.fragments;
        iterate();
        PrintCode print = new PrintCode(task.generatedCode);
        if(task.phase.equals("codegen"))
            print.print();
    }

    void iterate() {
        for(Fragment fragment : this.fragments.values()) {

            if(fragment instanceof CodeFragment) {
                GenerateCode generate = new GenerateCode((CodeFragment) fragment, task.fragments);
                generate.generate();
                FragmentCode codeNow = generate.get();
                codeNow.codeGraph = new Graph(codeNow);
                codeNow.regGraph = new RegGraph(codeNow);
                this.task.generatedCode.add(codeNow);
            }
        }

        /* test starts here */
        /*CodeFragment test_frag = new CodeFragment(new Frame(0,"_test", 0,0,0,0,0), 253, 255, null);
        LinkedList<Expression> test_code = new LinkedList<>();
        TEMP t1 = new TEMP(1);
        TEMP t2 = new TEMP(2);
        TEMP t3 = new TEMP(3);
        test_code.add(new ADD(t1, t1, 0));
        test_code.add(new LAB("L"));
        test_code.add(new ADD(t2, t1, 1));
        test_code.add(new ADD(t3, t3, t2));
        test_code.add(new MUL(t1, t2, 2));
        test_code.add(new BP(t1, "L"));
        test_code.add(new ADD(t3, t3, 1));
        HashMap<TEMP, String> test_hash = new HashMap<>();
        test_hash.put(t1, "a");
        test_hash.put(t2, "b");
        test_hash.put(t3, "c");
        FragmentCode test = new FragmentCode(test_frag, test_code, test_hash);
        test.codeGraph = new Graph(test);
        test.regGraph = new RegGraph(test);
        this.task.generatedCode.add(test);*/

        /*CodeFragment test_frag = new CodeFragment(new Frame(0,"_test", 0,0,0,0,0), 253, 255, null);
        LinkedList<Expression> test_code = new LinkedList<>();
        TEMP t1 = new TEMP(1);
        TEMP t2 = new TEMP(2);
        TEMP t3 = new TEMP(3);
        TEMP t4 = new TEMP(4);
        TEMP t5 = new TEMP(5);
        TEMP t6 = new TEMP(6);
        test_code.add(new ADD(t1, t2, 0));
        test_code.add(new ADD(t3, t4, 0));
        test_code.add(new ADD(t5, t6, 0));
        HashMap<TEMP, String> test_hash = new HashMap<>();
        test_hash.put(t1, "1");
        test_hash.put(t2, "2");
        test_hash.put(t3, "3");
        test_hash.put(t4, "4");
        test_hash.put(t5, "5");
        test_hash.put(t6, "6");
        FragmentCode test = new FragmentCode(test_frag, test_code, test_hash);
        test.codeGraph = new Graph(test);
        /*for(GraphNode n : test.codeGraph.getNodes()) {
            System.out.printString(n.getInstruction().getPrint().toString(test_hash));
            System.out.println("def: " + n.def);
            System.out.println("use: " + n.use);
            System.out.println("in: " + n.in);
            System.out.println("out: " + n.out);
        }
        test.regGraph = new RegGraph(test);
        this.task.generatedCode.add(test);*/
    }

//    void generate(CodeFragment fragment) {
//        for(int i = 0; i < fragment.linCode.numStmts(); i++) {
//            fragment.linCode.stmts(i);
//        }
//    }
}
