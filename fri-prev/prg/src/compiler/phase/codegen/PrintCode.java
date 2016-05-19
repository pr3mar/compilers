package compiler.phase.codegen;

import compiler.data.cod.*;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Print the generated code
 *
 * Created by pr3mar on 5/19/16.
 */
public class PrintCode {
    private LinkedList<Holder> allCode;
    private HashMap<TEMP, String> temps;

    public PrintCode(LinkedList<Holder> allCode) { // bring the hashmap somehow wrap it && shit

    }

    void print() {
        for (Holder hold : this.allCode) {
            LinkedList<Code> code = hold.code;
            this.temps = hold.temps;
            for (Code current : code) {
                switch (current.type) {
                    /* arithmetic operations */
                    case ADD:
                        ADD addition = ((ADD)current);
                        System.out.printf("ADD %s %s %s", getVal(addition.result), getVal(addition.op1), getVal(addition.op2));
                        break;
                    case SUB:
                        SUB subtraction = ((SUB)current);
                        System.out.printf("SUB %s %s %s", getVal(subtraction.result), getVal(subtraction.op1), getVal(subtraction.op2));
                        break;
                    case MUL:
                        MUL multiplication = ((MUL)current);
                        System.out.printf("MUL %s %s %s", getVal(multiplication.result), getVal(multiplication.op1), getVal(multiplication.op2));
                        break;
                    case DIV:
                        DIV division = ((DIV)current);
                        System.out.printf("DIV %s %s %s", getVal(division.result), getVal(division.op1), getVal(division.op2));
                        break;
                    case MOD:
                        DIV modulo = ((DIV)current);
                        System.out.printf("SUB %s %s %s", getVal(modulo.result), getVal(modulo.op1), getVal(modulo.op2));
                        break;

                }
            }
        }
    }


    private String getVal(TEMP reg) {
        return this.temps.get(reg);
    }
}
