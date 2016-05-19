package compiler.data.cod;

import compiler.data.imc.TEMP;

/**
 * Created by pr3mar on 5/19/16.
 */
public class DIV extends BIN {
    public NodeType type;

    public DIV(TEMP op1, TEMP op2) {
        super(op1, op2);
        this.type = NodeType.DIV;
    }
}
