package compiler.data.cod;

import compiler.data.imc.TEMP;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by pr3mar on 5/19/16.
 */
public class Expression extends Code {

    public int constructorUsed = -1;

    private TEMP result;
    private TEMP op1; long op1_const;
    private TEMP op2; long op2_const;
    protected String label;
    protected boolean move;

    protected Set<TEMP> def;
    protected Set<TEMP> use;

    public Expression() { // 0 ops
        this.constructorUsed = 0;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result) { // 1 ops: ret
        this.constructorUsed = 1;
        this.result = result;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(long op1_const) { // 1 ops: ret
        this.constructorUsed = 2;
        this.op1_const = op1_const;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result, TEMP op1) { // 2 ops: ret tmp
        this.constructorUsed = 3;
        this.result = result;
        this.op1 = op1;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result, long op1_const) { // 2 ops: ret int
        this.constructorUsed = 4;
        this.result = result;
        this.op1_const = op1_const;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result, long op1_const, TEMP op2) { // 3 ops: ret int temp
        this.constructorUsed = 5;
        this.result = result;
        this.op1_const = op1_const;
        this.op2 = op2;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result, TEMP op1, long op2_const) { // 3 ops: ret temp int
        this.constructorUsed = 6;
        this.result = result;
        this.op1 = op1;
        this.op2_const = op2_const;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }
    public Expression(TEMP result, long op1_const, long op2_const) { // 3 ops: ret int int
        this.constructorUsed = 7;
        this.result = result;
        this.op1_const = op1_const;
        this.op2_const = op2_const;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public Expression(TEMP result, TEMP op1, TEMP op2) { // 3 ops: ret temp temp
        this.constructorUsed = 8;
        this.result = result;
        this.op1 = op1;
        this.op2 = op2;
        def = new HashSet<>();
        use = new HashSet<>();
        this.move = false;
    }

    public String getLabel() { return this.label; }

    public Set<TEMP> getUse() { return this.use; }

    public Set<TEMP> getDef() { return this.def; }

    public boolean getMove() { return this.move; }

    public TEMP getResult() { return this.result; }

    public TEMP getOp1() { return this.op1; }

    public TEMP getOp2() { return this.op2; }
}
