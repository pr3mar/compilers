package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * SETL, sets lower 2 bytes of an 8 byte register
 * Created by pr3mar on 5/20/16.
 */
public class SETL extends Expression {
    public SETL(TEMP res, long val) {
        super(res, val);
        this.print = new Print("SETL %s," + val, res);
    }
}
