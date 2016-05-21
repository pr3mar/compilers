package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Compare 2 registers
 * -1 - op1 < op2
 *  0 - op1 == op2
 * +1 - op1 > op2
 * Created by pr3mar on 5/21/16.
 */
public class CMP extends Expression {

    public CMP(TEMP result, TEMP op1, TEMP op2) {
        super(result, op1, op2);
        print = new Print("CMP %s,%s,%s", result, op1, op2);
    }

    public CMP(TEMP result, TEMP op1, long op2_const) {
        super(result, op1, op2_const);
        print = new Print("CMP %s,%s," + op2_const, result, op1);
    }

    public CMP(TEMP result, long op1_const, long op2_const) {
        super(result, op1_const, op2_const);
        print = new Print("CMP %s," + op1_const + "," + op2_const, result);
    }
}
