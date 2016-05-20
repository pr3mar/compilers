package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Store value in a register
 * Created by pr3mar on 5/20/16.
 */
public class STO extends Expression {
    public STO(TEMP dst, TEMP src) {
        super(dst, src);
        this.print = new Print("STO %s,%s,0", src, dst);
    }
}
