package compiler.data.cod.imcVisitor;

import compiler.data.imc.*;
import compiler.data.cod.*;
/**
 * Visitor of the intermediate code
 * Created by pr3mar on 5/18/16.
 */
public class IMCFullVIsitor implements IMCVisitor {

    public void visit(BINOP binop) {
        binop.expr1.accept(this);
        binop.expr2.accept(this);
    }
    public void visit(CALL call) {
        for(int i = 0; i < call.numArgs(); i++)
            call.args(i).accept(this);
    }
    public void visit(CJUMP cjump) {
        cjump.cond.accept(this);
    }
    public void visit(CONST constant) {

    }
    public void visit(ESTMT estmt) {
        estmt.expr.accept(this);
    }
    public void visit(JUMP jump) {

    }
    public void visit(LABEL label) {

    }
    public void visit(MEM mem) {
        mem.addr.accept(this);
    }
    public void visit(MOVE move) {
        move.dst.accept(this);
        move.src.accept(this);
    }
    public void visit(NAME name) {

    }
    public void visit(NOP nop) {

    }
    public void visit(SEXPR sexpr) {
        sexpr.stmt.accept(this);
        sexpr.expr.accept(this);
    }
    public void visit(STMTS stmts) {
        for(int i = 0; i < stmts.numStmts(); i++)
            stmts.stmts(i).accept(this);
    }
    public void visit(TEMP temp) {

    }
    public void visit(UNOP unop) {
        unop.expr.accept(this);
    }

}
