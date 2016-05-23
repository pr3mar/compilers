package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Multiplication
 * Created by pr3mar on 5/19/16.
 */
public class MUL extends Expression {
    public MUL(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        print = new Print("MUL %s,%s,%s", result, op1, op2);
        this.def.add(result);
        this.use.add(op1);
        this.use.add(op2);
    }

    public MUL(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("MUL %s,%s," + op2_const, result, op1);
        this.def.add(result);
        this.use.add(op1);
        if(op2_const == 1) {
            this.move = true;
        }
    }
}
