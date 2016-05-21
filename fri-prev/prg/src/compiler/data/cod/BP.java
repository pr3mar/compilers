package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Created by pr3mar on 5/21/16.
 */
public class BP extends Expression {
    public BP(TEMP result, String posLabel) {
        super(result);
        print = new Print("BP %s," + posLabel, result);
    }
}
