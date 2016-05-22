package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Created by pr3mar on 5/21/16.
 */
public class ZSN extends Expression {

    public ZSN(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        print = new Print("ZSN %s,%s,%s", result, op1, op2);
        this.def.add(result);
        this.use.add(op1);
        this.use.add(op2);
    }

    public ZSN(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("ZSN %s,%s," + op2_const, result, op1);
        this.def.add(result);
        this.use.add(op1);
    }
}
