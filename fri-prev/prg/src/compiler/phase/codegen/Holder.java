package compiler.phase.codegen;

import compiler.data.cod.Code;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by pr3mar on 5/19/16.
 */
public class Holder {
    public LinkedList<Code> code;

    public HashMap<TEMP, String> temps;

    public Holder(LinkedList<Code> code, HashMap<TEMP, String> temps) {
        this.code = code;
        this.temps = temps;
    }
}
