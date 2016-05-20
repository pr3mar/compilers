package compiler.data.cod;

import compiler.data.imc.TEMP;

/**
 *
 * Created by pr3mar on 5/19/16.
 */
public class Expression extends Code {

    public int constructorUsed = -1;

    private TEMP result;
    private TEMP op1; long op1_const;
    private TEMP op2; long op2_const;

    public Expression() { // 0 ops
        this.constructorUsed = 0;
    }

    public Expression(TEMP result, TEMP op1) { // 2 ops: ret tmp
        this.constructorUsed = 1;
        this.result = result;
        this.op1 = op1;
    }

    public Expression(TEMP result, long op1_const) { // 2 ops: ret int
        this.constructorUsed = 2;
        this.result = result;
        this.op1_const = op1_const;
    }

    public Expression(TEMP result, long op1_const, TEMP op2) { // 3 ops: ret int temp
        this.constructorUsed = 3;
        this.result = result;
        this.op1_const = op1_const;
        this.op2 = op2;
    }

    public Expression(TEMP result, TEMP op1, long op2_const) { // 3 ops: ret temp int
        this.constructorUsed = 4;
        this.result = result;
        this.op1 = op1;
        this.op2_const = op2_const;
    }

    public Expression(TEMP result, TEMP op1, TEMP op2) { // 3 ops: ret temp temp
        this.constructorUsed = 5;
        this.result = result;
        this.op1 = op1;
        this.op2 = op2;
    }

    public TEMP getResult() {
        return this.result;
    }

}
