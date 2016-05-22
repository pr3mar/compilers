package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * SETML, sets medium high bytes registers of 8 byte register
 * Created by pr3mar on 5/20/16.
 */
public class INCMH extends Expression {
    public INCMH(TEMP res, long val) {
        super(res, val);
        this.print = new Print("INCMH %s," + val, res);
        this.def.add(res);
    }
}
