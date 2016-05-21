package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Load value in a register
 * Created by pr3mar on 5/20/16.
 */
public class LDO extends Expression{

    public LDO(TEMP dst, TEMP src, TEMP offset) {
        super(dst, src);
        this.print = new Print("LDO %s,%s,%s", src, dst, offset);
    }

    public LDO(TEMP dst, TEMP src, long offset) {
        super(dst, src, offset);
        this.print = new Print("LDO %s,%s," + offset, src, dst);
    }
}
