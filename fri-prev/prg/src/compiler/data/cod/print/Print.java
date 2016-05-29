package compiler.data.cod.print;

import compiler.common.report.CompilerError;
import compiler.data.imc.TEMP;

import java.util.HashMap;

/**
 * A class for printing instructions
 * Created by pr3mar on 5/20/16.
 */
public class Print {

    public String printString;
    public TEMP op1, op2, op3;
    public int args;

    public Print(String print) {
        this.printString = print;
        this.args = 0;
    }

    public Print(String print, TEMP op1) {
        this.printString = print;
        this.op1 = op1;
        this.args = 1;
    }

    public Print(String print, TEMP op1, TEMP op2) {
        this.printString = print;
        this.op1 = op1;
        this.op2 = op2;
        this.args = 2;
    }

    public Print(String print, TEMP op1, TEMP op2, TEMP op3) {
        this.printString = print;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.args = 3;
    }

    public String toString(HashMap<TEMP, String> vars) {
        switch (this.args) {
            case 0:
                return this.printString;
            case 1:
                if(vars.get(this.op1) == null) {
                    throw new CompilerError("printString to string 1");
                }
                return String.format(this.printString, vars.get(this.op1));
            case 2:
                if(vars.get(this.op1) == null || vars.get(this.op2) == null) {
                    //throw new CompilerError("printString to string 2");
                }
                return String.format(this.printString, vars.get(this.op1), vars.get(this.op2));
            case 3:
                if(vars.get(this.op1) == null || vars.get(this.op2) == null || vars.get(this.op3) == null) {
                    throw new CompilerError("printString to string 3");
                }
                return String.format(this.printString, vars.get(this.op1), vars.get(this.op2), vars.get(this.op3));
            default:
                throw new CompilerError("[codegen] something funny is happening when printing stuff");
        }
    }
}
