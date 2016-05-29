package compiler.data.cod.print;

import compiler.common.report.CompilerError;
import compiler.data.cod.Code;
import compiler.data.imc.TEMP;
import compiler.data.cod.wrapper.FragmentCode;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Print the generated code
 *
 * Created by pr3mar on 5/19/16.
 */
public class PrintCode {
    private LinkedList<FragmentCode> allCode;

    public PrintCode(LinkedList<FragmentCode> allCode) { // bring the hashmap somehow wrap it && shit
        this.allCode = allCode;
    }

    public void print() {
        for(FragmentCode current : allCode) {
            System.out.println("Generated code for fragment: " + current.fragment.label);
            System.out.println(current.fragment.label + ":");
            for(Code code : current.code) {
                Print tmp = code.getPrint();
                System.out.print(tmp.toString(current.temps));
            }
            System.out.println("\nInterference graph for fragment: " + current.fragment.label);
            current.regGraph.print();
            System.out.println();
        }
    }

    public void printColored() {
        for(FragmentCode current : allCode) {
            System.out.println("Generated code for fragment: " + current.fragment.label);
            System.out.println(current.fragment.label + ":");
            for(Code code : current.code) {
                Print tmp = code.getPrint();
                System.out.print(tmp.toString(current.coloredMap));
            }
            System.out.println("\nInterference graph for fragment: " + current.fragment.label);
            current.regGraph.print();
            System.out.println();

            System.out.println("\nColoring of the graph for fragment: " + current.fragment.label);
            for(TEMP t : current.coloredMap.keySet()) {
                if(current.coloredMap.get(t) == null) {
                    throw new CompilerError("printString colored error!");
                }
                System.out.println(t + " = " + current.coloredMap.get(t));
            }
            System.out.println();
        }
    }

    /*public void printString(HashMap<TEMP, String> useThis) {
        for(int i = 0; i < allCode.size(); i++) {
            FragmentCode current = this.allCode.get(i);
            for(int j = 0; j < current.code.size(); i++) {
                System.out.println("Generated code for fragment: " + current.fragment.label);
                Print tmp = current.code.get(i).getPrint();
                System.out.printString(tmp.toString(useThis));
            }
            System.out.println("\nInterference graph for fragment: " + current.fragment.label);
            current.regGraph.printString();
            System.out.println();
        }
    }*/

}
