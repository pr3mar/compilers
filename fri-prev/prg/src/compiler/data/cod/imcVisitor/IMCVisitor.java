package compiler.data.cod.imcVisitor;

import compiler.data.imc.*;

/**
 * Created by pr3mar on 5/18/16.
 */
public interface IMCVisitor {
    public void visit(BINOP binop);
    public void visit(CALL call);
    public void visit(CJUMP cjump);
    public void visit(CONST cons);
    public void visit(ESTMT estmt);
    public void visit(JUMP jump);
    public void visit(LABEL label);
    public void visit(MEM mem);
    public void visit(MOVE move);
    public void visit(NAME name);
    public void visit(NOP nop);
    public void visit(SEXPR sexpr);
    public void visit(STMTS stmts);
    public void visit(TEMP temp);
    public void visit(UNOP unop);
}
