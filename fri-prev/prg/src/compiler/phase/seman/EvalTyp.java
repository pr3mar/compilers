package compiler.phase.seman;

import java.util.LinkedList;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * Type checker.
 * <p/>
 * <p>
 * Type checker checks type of all sentential forms of the program and resolves
 * the component names as this cannot be done earlier, i.e., in
 * {@link compiler.phase.seman.EvalDecl}.
 * </p>
 *
 * @author sliva
 */
public class EvalTyp extends FullVisitor {

    private final Attributes attrs;
    private RecType recNow;
    boolean first;

    public EvalTyp(Attributes attrs) {
        this.attrs = attrs;
        this.recNow = null;
        this.first = true;
    }

    /**
     * The symbol table.
     */
    private SymbolTable symbolTable = new SymbolTable();

    // TODO

    public void visit(ArrType arrType) {
        if (!first) return;
        arrType.size.accept(this);
        arrType.elemType.accept(this);
        long size;
        try {
            size = attrs.valueAttr.get(arrType.size);
        } catch (NullPointerException err) {
            throw new CompilerError("[Semantic Error, EvalType] Array size not defined statically at" + arrType);
        }
        Typ type;
        try {
            type = attrs.typAttr.get(arrType.elemType);
        } catch (NullPointerException err) {
            throw new CompilerError("[Semantic Error, EvalType] Array type not defined at" + arrType);
        }
        attrs.typAttr.set(arrType, new ArrTyp(size, type));
    }

    public void visit(AtomExpr atomExpr) {
        if (!first) return;
        switch (atomExpr.type) {
            case INTEGER:
                attrs.typAttr.set(atomExpr, new IntegerTyp());
                break;
            case BOOLEAN:
                attrs.typAttr.set(atomExpr, new BooleanTyp());
                break;
            case CHAR:
                attrs.typAttr.set(atomExpr, new CharTyp());
                break;
            case STRING:
                attrs.typAttr.set(atomExpr, new StringTyp());
                break;
            case PTR:
                attrs.typAttr.set(atomExpr, new PtrTyp(null));
                break;
            case VOID:
                attrs.typAttr.set(atomExpr, new VoidTyp());
                break;
        }
    }

    public void visit(AtomType atomType) {
        if (!first) return;
        switch (atomType.type) {
            case INTEGER:
                attrs.typAttr.set(atomType, new IntegerTyp());
                break;
            case BOOLEAN:
                attrs.typAttr.set(atomType, new BooleanTyp());
                break;
            case CHAR:
                attrs.typAttr.set(atomType, new CharTyp());
                break;
            case STRING:
                attrs.typAttr.set(atomType, new StringTyp());
                break;
            case VOID:
                attrs.typAttr.set(atomType, new VoidTyp());
                break;
        }
    }

    public void visit(CastExpr castExpr) {
        castExpr.type.accept(this);
        castExpr.expr.accept(this);
    }

    public void visit(CompDecl compDecl) {
        if (!first) return;
        try {
            symbolTable.insDecl(this.recNow.toString(), compDecl.name, compDecl);
        } catch (CannotInsNameDecl err) {
            throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of component at " + compDecl);
        }
        compDecl.type.accept(this);
        attrs.typAttr.set(compDecl, attrs.typAttr.get(compDecl.type));
    }

    public void visit(CompName compName) {
    }

    public void visit(Exprs exprs) {
        for (int e = 0; e < exprs.numExprs(); e++)
            exprs.expr(e).accept(this);
    }

    public void visit(ExprError exprError) {
    }

    public void visit(ForExpr forExpr) {
        forExpr.var.accept(this);
        forExpr.loBound.accept(this);
        forExpr.hiBound.accept(this);
        forExpr.body.accept(this);
    }

    public void visit(FunCall funCall) {
        for (int a = 0; a < funCall.numArgs(); a++)
            funCall.arg(a).accept(this);
    }

    public void visit(FunDecl funDecl) {
        for (int p = 0; p < funDecl.numPars(); p++)
            funDecl.par(p).accept(this);
        funDecl.type.accept(this);
    }

    public void visit(FunDef funDef) {
        if(first) {
            for (int p = 0; p < funDef.numPars(); p++)
                funDef.par(p).accept(this);
            funDef.type.accept(this);
        } else {
            funDef.body.accept(this);
        }
    }

    public void visit(IfExpr ifExpr) {
        ifExpr.cond.accept(this);
        ifExpr.thenExpr.accept(this);
        ifExpr.elseExpr.accept(this);
    }

    public void visit(ParDecl parDecl) {
        parDecl.type.accept(this);
    }

    public void visit(Program program) {
        program.expr.accept(this);
    }

    public void visit(PtrType ptrType) {
        if (!first) return;
        ptrType.baseType.accept(this);
        Typ type;
        try {
            type = attrs.typAttr.get(ptrType.baseType);
        } catch (NullPointerException err) {
            throw new CompilerError("[Semantic Error, EvalType] Ptr type not defined at" + ptrType);
        }
        attrs.typAttr.set(ptrType, new PtrTyp(type));
    }

    public void visit(RecType recType) {
        if (!first) return;
        symbolTable.newNamespace(recType.toString());
        this.recNow = recType;
        LinkedList<Typ> compTyps = new LinkedList<Typ>();
        for (int c = 0; c < recType.numComps(); c++) {
            recType.comp(c).accept(this);
            compTyps.add(attrs.typAttr.get(recType.comp(c)));
        }
        attrs.typAttr.set(recType, new RecTyp(recType.toString(), compTyps));
        this.recNow = null;
    }

    public void visit(TypeDecl typDecl) { // TODO what??
        if (!first) return;
        typDecl.type.accept(this);
        try {
            TypName type = new TypName(typDecl.name);
            type.setType(attrs.typAttr.get(typDecl.type));
            attrs.typAttr.set(typDecl, type);
        } catch (Exception err) {
            throw new CompilerError("[Semantic error, typDecl] " + typDecl);
            // do nothing
        }
    }

    public void visit(TypeName typeName) {
        // TODO ASK SO!!!
        if (!first) return;
        TypName type = new TypName(typeName.name());
        attrs.typAttr.set(typeName, type);
    }

    public void visit(UnExpr unExpr) {
        unExpr.subExpr.accept(this);
        Typ type = attrs.typAttr.get(unExpr.subExpr);
        switch (unExpr.oper) {
            case ADD: case SUB:
                if(type instanceof IntegerTyp)
                    attrs.typAttr.set(unExpr, new IntegerTyp());
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
                break;
            case NOT:
                if(type instanceof BooleanTyp)
                    attrs.typAttr.set(unExpr, new BooleanTyp());
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
                break;
        }
    }

    public void visit(VarDecl varDecl) {
        if(!first) return;
        varDecl.type.accept(this);
        Typ type = attrs.typAttr.get(varDecl.type);
        attrs.typAttr.set(varDecl, type);
    }

    public void visit(VarName varName) {
        if(!first) return;
        Decl dec = attrs.declAttr.get(varName);
        attrs.typAttr.set(varName, attrs.typAttr.get(dec));
    }

    public void visit(WhereExpr whereExpr) {
        boolean prev = first;
        first = true;
        for (int p = 0; p < 2; p++) {
            for (int d = 0; d < whereExpr.numDecls(); d++)
                whereExpr.decl(d).accept(this);
            first = false;
        }
        first = prev;
        whereExpr.expr.accept(this);
    }

    public void visit(WhileExpr whileExpr) {
        whileExpr.cond.accept(this);
        whileExpr.body.accept(this);
    }


}
