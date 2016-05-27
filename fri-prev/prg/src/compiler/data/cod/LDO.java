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
        this.print = new Print("\tLDO %s,%s,%s\n", dst, src, offset);
        this.def.add(dst);
        this.use.add(src);
        this.use.add(offset);
    }

    public LDO(TEMP dst, TEMP src, long offset) {
        super(dst, src, offset);
        this.print = new Print("\tLDO %s,%s," + offset + "\n", dst, src);
        this.def.add(dst);
        this.use.add(src);
    }

    public LDO(TEMP dst, String src, long offset) {
        super(dst, offset);
        this.print = new Print("\tLDO %s," + src + "," + offset + "\n", dst);
        this.def.add(dst);
    }
}
