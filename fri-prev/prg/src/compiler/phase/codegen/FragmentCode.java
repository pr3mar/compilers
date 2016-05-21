package compiler.phase.codegen;

import compiler.data.cod.Code;
import compiler.data.frg.CodeFragment;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Code holder for each fragment
 * Created by pr3mar on 5/19/16.
 */
public class FragmentCode {

    public LinkedList<Code> code;

    public HashMap<TEMP, String> temps;

    public CodeFragment fragment;

    public FragmentCode(CodeFragment fragment, LinkedList<Code> code, HashMap<TEMP, String> temps) {
        this.fragment = fragment;
        this.code = code;
        this.temps = temps;
    }
}
