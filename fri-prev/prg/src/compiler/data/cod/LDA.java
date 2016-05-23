package compiler.data.cod;

import compiler.data.cod.print.Print;
import compiler.data.imc.TEMP;

/**
 * LDA - Stores the address indicated by the label in register result
 * Created by pr3mar on 5/21/16.
 */
public class LDA extends Expression {
    public LDA(TEMP result, String source) {
        super(result);
        this.label = source;
        print = new Print("\tLDA %s," + source + "\n", result);
        this.def.add(result);
    }
}
