package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Get a negative value of a number
 * Created by pr3mar on 5/20/16.
 */
public class NEG extends Expression {

    public NEG(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        this.print = new Print("NEG %s,%s,%s", result, op1, op2);
    }

    public NEG(TEMP result, long op1_const, TEMP op2) {
        super(result, op1_const, op2);
        this.print = new Print("NEG %s," + op1_const + ",%s", result, op2);
    }
}
