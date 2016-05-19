package compiler.data.cod;

import compiler.data.imc.TEMP;

/**
 * Multiplication
 * Created by pr3mar on 5/19/16.
 */
public class MUL extends BIN {
    public NodeType type;

    public MUL(TEMP op1, TEMP op2) {
        super(op1, op2);
        this.type = NodeType.MUL;
    }
}
