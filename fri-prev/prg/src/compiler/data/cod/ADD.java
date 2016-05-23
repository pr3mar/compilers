package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.*;

/**
 * Addition instruction
 * Created by pr3mar on 5/19/16.
 */
public class ADD extends Expression {

    public ADD(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        print = new Print("\tADD %s,%s,%s\n", result, op1, op2);
        this.def.add(result);
        this.use.add(op1);
        this.use.add(op2);
    }

    public ADD(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("\tADD %s,%s," + op2_const + "\n", result, op1);
        this.def.add(result);
        this.use.add(op1);
        if(op2_const == 0) {
            this.move = true;
        }
    }
}
