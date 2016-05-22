package compiler.data.cod;

import compiler.data.cod.print.Print;

/**
 * Created by pr3mar on 5/21/16.
 */
public class LAB extends Expression{

    public LAB(String lab) {
        super();
        this.label = lab;
        this.print = new Print(lab + ":");
    }
}
