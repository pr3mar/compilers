package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Load value in a register
 * Created by pr3mar on 5/20/16.
 */
public class LDO extends Expression{
    public LDO(TEMP dst, TEMP src) {
        super(dst, src);
        this.print = new Print("LDO %s,%s,0", src, dst);
    }
}
