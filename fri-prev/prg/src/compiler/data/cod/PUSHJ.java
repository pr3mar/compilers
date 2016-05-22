package compiler.data.cod;

import compiler.data.cod.print.Print;

/**
 * Created by pr3mar on 5/21/16.
 */
public class PUSHJ extends Expression {
    public PUSHJ(long allRegs, String label) {
        super(allRegs);
        this.label = label;
        this.print = new Print("PUSHJ " + allRegs + "," + label);
    }
}
