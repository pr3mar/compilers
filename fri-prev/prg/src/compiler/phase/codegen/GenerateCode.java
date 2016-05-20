package compiler.phase.codegen;

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
    private CodeFragment fragment;
    private LinkedList<Code> code;
    private Stack<TEMP> result;
    private HashMap<TEMP, String> mapping;

    public GenerateCode(CodeFragment fragment) {
        this.fragment = fragment;
        result = new Stack<>();
        mapping = new HashMap<>();
    }

    public FragmentCode get() {
        return new FragmentCode(this.code, this.mapping);
    }

    public void generate() {
        for(int i = 0; i < this.fragment.linCode.numStmts(); i++) {
            this.fragment.linCode.stmts(i).accept(this);
        }
    }

    @Override
    public void visit(BINOP binop) {
        binop.expr1.accept(this);
        binop.expr2.accept(this);
        TEMP left = this.result.pop();
        TEMP right = this.result.pop();
        TEMP res = new TEMP(TEMP.newTempName());
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
        result.push(res);
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
        move.dst.accept(this);
        move.src.accept(this);
        this.result.pop();
        this.result.pop();
    }

    @Override
    public void visit(NAME name) {
        this.result.push(new TEMP(TEMP.newTempName()));
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
        switch (unop.oper) {
            case ADD:
                this.result.push(operand);
                break;
            case NOT:
                break;
            case SUB:
                TEMP res = new TEMP(TEMP.newTempName());
                this.code.add(new NEG(res, 0, operand));
                this.result.push(res);
                break;
        }
    }

}
