package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * Get a value from a special register into an ordinary one
 * Created by pr3mar on 5/21/16.
 */
public class GET extends Expression {

    public GET(TEMP result, String special) {
        super(result);
        print = new Print("\tGET %s," + special + "\n", result);
        this.def.add(result);
        this.special = special;
    }
}
