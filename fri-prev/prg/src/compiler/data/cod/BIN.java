package compiler.data.cod;

import compiler.data.imc.TEMP;

/**
 * Template for all binary operators
 * Created by pr3mar on 5/19/16.
 */
public class BIN extends Expression {
    public TEMP result;
    public TEMP op1;
    public TEMP op2;

    public BIN(TEMP op1, TEMP op2) {
        this.result = new TEMP(TEMP.newTempName());
        this.op1 = op1;
        this.op2 = op2;
    }
}
