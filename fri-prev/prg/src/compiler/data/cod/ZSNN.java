package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Created by pr3mar on 5/21/16.
 */
public class ZSNN extends Expression {

    public ZSNN(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        print = new Print("\tZSNN %s,%s,%s\n", result, op1, op2);
        this.def.add(result);
        this.use.add(op1);
        this.use.add(op2);
    }

    public ZSNN(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("\tZSNN %s,%s," + op2_const + "\n", result, op1);
        this.def.add(result);
        this.use.add(op1);
    }
}
