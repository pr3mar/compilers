package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Store value in a register
 * Created by pr3mar on 5/20/16.
 */
public class STO extends Expression {

    public STO(TEMP src, TEMP dst, long offset) {
        super(dst, src, offset);
        this.print = new Print("\tSTO %s,%s," + offset + "\n", src, dst);
        this.use.add(src);
        this.use.add(dst);
    }

    public STO(TEMP src, TEMP dst, TEMP offset) {
        super(dst, src, offset);
        this.print = new Print("\tSTO %s,%s,%s\n", src, dst, offset);
        this.use.add(src);
        this.use.add(dst);
        this.use.add(offset);
    }


    public STO(TEMP dst, String src, long offset) {
        super(dst, offset);
        this.print = new Print("\tSTO %s," + src + "," + offset + "\n", dst);
        this.def.add(dst);
    }
}
