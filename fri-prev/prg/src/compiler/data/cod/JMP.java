package compiler.data.cod;

import compiler.data.cod.print.Print;

/**
 * Created by pr3mar on 5/21/16.
 */
public class JMP extends Expression {
    public JMP(String lab) {
        super();
        this.print = new Print("JMP " + lab);
    }
}
