package compiler.phase.codegen;

import compiler.common.report.CompilerError;
import compiler.data.cod.*;
import compiler.data.cod.imcVisitor.IMCFullVIsitor;
import compiler.data.frg.CodeFragment;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import compiler.data.imc.*;


/**
 * Visit the intermediate code and generate MMIX code
 * Created by pr3mar on 5/19/16.
 */
public class GenerateCode extends IMCFullVIsitor {

    private final int BYTE_SIZE = 8;

    private CodeFragment fragment;
    private LinkedList<Code> code;
    private Stack<TEMP> result;
    private HashMap<TEMP, String> mapping;
    private int countMoves;
    public GenerateCode(CodeFragment fragment) {
        this.fragment = fragment;
        this.result = new Stack<>();
        this.mapping = new HashMap<>();
        this.code = new LinkedList<>();
        this.countMoves = 0;
    }

    private int[] devideConst(Long val) {
        int[] ret = new int[4];
        long mask = 0xFFFF, shiftRight = 0, shiftLeft = 0;
        for(int i = 0; i < ret.length; i++) {
            ret[i] = (int) ((val & mask) >> shiftRight);
            mask = mask << shiftLeft;
            shiftRight = shiftLeft;
            shiftLeft += 2 * BYTE_SIZE;
        }
        return ret;
    }

    private TEMP newTEMP() {
        TEMP ret = new TEMP(TEMP.newTempName());
        this.mapping.put(ret, ret.toString());
        return ret;
    }

    public FragmentCode get() {
        return new FragmentCode(this.code, this.mapping);
    }

    public void generate() {
        for(int i = 0; i < this.fragment.linCode.numStmts(); i++) {
            this.fragment.linCode.stmts(i).accept(this);
            this.result = new Stack<>();
        }
    }

    @Override
    public void visit(BINOP binop) {
        binop.expr1.accept(this);
        binop.expr2.accept(this);
        TEMP left = this.result.pop();
        TEMP right = this.result.pop();
        TEMP res = newTEMP();
        switch (binop.oper) {
            /* arithmetical operations */
            case ADD:
                this.code.add(new ADD(res, left, right));
                break;
            case SUB:
                this.code.add(new SUB(res, left, right));
                break;
            case MUL:
                this.code.add(new MUL(res, left, right));
                break;
            case DIV:
                this.code.add(new DIV(res, left, right));
                break;
            case MOD:
//                this.code.add(new ADD(left, right));
                break;

            /* logical operations */
            case AND:
                break;
            case OR:
                break;
            case EQU:
                break;
            case NEQ:
                break;
            case GEQ:
                break;
            case GTH:
                break;
            case LEQ:
                break;
            case LTH:
                break;
        }
        this.result.push(res);
    }

    @Override
    public void visit(CALL call) {
        for(int i = 0; i < call.numArgs(); i++)
            call.args(i).accept(this);
    }

    @Override
    public void visit(CJUMP cjump) {
        cjump.cond.accept(this);
    }

    @Override
    public void visit(CONST constant) {
        TEMP res = newTEMP();
        int[] bytes = devideConst(constant.value);
        int noInstr = 3;
        for(; noInstr >= 0; noInstr--) {
            if(bytes[noInstr] != 0) {
                break;
            }
        }
        switch (noInstr) {
            case 0:
                this.code.add(new SETL(res, bytes[0]));
                break;
            case 1:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new SETML(res, bytes[1]));
                break;
            case 2:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new SETML(res, bytes[1]));
                this.code.add(new SETMH(res, bytes[2]));
                break;
            case 3:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new SETML(res, bytes[1]));
                this.code.add(new SETMH(res, bytes[2]));
                this.code.add(new SETH(res, bytes[3]));
                break;
            default:
                throw new CompilerError("[codegen] something funny is happening at code for constant");
        }
        this.result.push(res);
    }

    @Override
    public void visit(ESTMT estmt) {
        estmt.expr.accept(this);
    }

    @Override
    public void visit(JUMP jump) {

    }

    @Override
    public void visit(LABEL label) {

    }

    @Override
    public void visit(MEM mem) {
        mem.addr.accept(this);
    }

    @Override
    public void visit(MOVE move) {
        this.countMoves++;
        boolean mem = false;
        if(move.dst instanceof MEM) {
            ((MEM) move.dst).addr.accept(this);
            mem = true;
        } else {
            move.dst.accept(this);
        }
        move.src.accept(this);
        if(this.result.size() == 1) {
            return;
        }
        TEMP src = this.result.pop();
        TEMP dst = this.result.pop();
        if(mem) {
            this.code.add(new STO(dst, src));
        } else {
            this.code.add(new LDO(dst, src));
        }
    }

    @Override
    public void visit(NAME name) {
        this.result.push(newTEMP());
    }

    @Override
    public void visit(NOP nop) {

    }

    @Override
    public void visit(SEXPR sexpr) {
        sexpr.stmt.accept(this);
        sexpr.expr.accept(this);
    }

    @Override
    public void visit(STMTS stmts) {
        for(int i = 0; i < stmts.numStmts(); i++)
            stmts.stmts(i).accept(this);
    }

    @Override
    public void visit(TEMP temp) {
        if(!this.mapping.containsKey(temp))
            this.mapping.put(temp, temp.toString());
        this.result.push(temp);
    }

    @Override
    public void visit(UNOP unop) {
        unop.expr.accept(this);
        TEMP operand = this.result.pop();
        TEMP res = null;
        switch (unop.oper) {
            case ADD:
                res = operand;
                break;
            case NOT:
                res = newTEMP();
                break;
            case SUB:
                res = newTEMP();
                this.code.add(new NEG(res, 0, operand));
                break;
        }
        this.result.push(res);
    }

}
