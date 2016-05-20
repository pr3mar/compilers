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
        print = new Print("ADD %s, %s, %s", result, op1, op2);
    }

    public ADD(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("ADD %s, %s, " + op2_const, result, op1);
    }
}
