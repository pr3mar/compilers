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
    private RecTyp recUse;
    boolean first;
    int turn;

    public EvalTyp(Attributes attrs) {
        this.attrs = attrs;
        this.recNow = null;
        this.recUse = null;
        this.first = true;
    }

    /**
     * The symbol table.
     */
    private SymbolTable symbolTable = new SymbolTable();

    // TODO

    public void visit(ArrType arrType) {
        if (turn != 1) return;
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
//                attrs.typAttr.set(atomExpr, new PtrTyp(null));
                break;
            case VOID:
                attrs.typAttr.set(atomExpr, new VoidTyp());
                break;
        }
    }

    public void visit(AtomType atomType) {
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

    public void visit(BinExpr binExpr) {
        binExpr.fstExpr.accept(this);
        Typ fst = attrs.typAttr.get(binExpr.fstExpr);
        Typ fstAct = fst;
        if(fstAct instanceof TypName && !((TypName) fst).isCircular())
            fstAct = fst.actualTyp();
        if(fstAct instanceof RecTyp)
            recUse = (RecTyp)fstAct;

        binExpr.sndExpr.accept(this);
        Typ snd = attrs.typAttr.get(binExpr.sndExpr);
        Typ sndAct = snd;

        if(sndAct instanceof TypName && !((TypName) sndAct).isCircular())
            sndAct = sndAct.actualTyp();

        if(fstAct instanceof  ArrTyp
                && sndAct instanceof IntegerTyp
                    && binExpr.oper == BinExpr.Oper.ARR) {
            attrs.typAttr.set(binExpr, ((ArrTyp)fstAct).elemTyp);
            return;
        }

//        if(fstAct instanceof RecTyp) {
//            attrs.typAttr.set(binExpr, sndAct);
//            recUse = null;
//        }
        if(fstAct instanceof FunTyp)
            fstAct = ((FunTyp) fstAct).resultTyp;
        if(fstAct instanceof ArrTyp)
            fstAct = ((ArrTyp) fstAct).elemTyp;
        if(sndAct instanceof FunTyp)
            sndAct = ((FunTyp) sndAct).resultTyp;
        if(sndAct instanceof ArrTyp)
            sndAct = ((ArrTyp) sndAct).elemTyp;

        if(fstAct instanceof IntegerTyp && sndAct instanceof IntegerTyp) {
            switch (binExpr.oper) {
                case ADD: case SUB: case MUL: case MOD: case DIV:
                    attrs.typAttr.set(binExpr, new IntegerTyp());
                    break;
                case EQU: case NEQ: case LTH: case GTH: case GEQ: case LEQ:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
            }
//        } else if(fstAct instanceof BooleanTyp && sndAct instanceof BooleanTyp) {
        } else if(fstAct instanceof BooleanTyp && sndAct instanceof BooleanTyp) {
            switch (binExpr.oper) {
                case AND: case OR:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
                case EQU: case NEQ: case LTH: case GTH: case GEQ: case LEQ:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
            }
        } else if(fstAct instanceof CharTyp && sndAct instanceof CharTyp
                || fstAct instanceof PtrTyp && sndAct instanceof PtrTyp && fstAct.isStructEquivTo(sndAct)) {
            switch (binExpr.oper) {
                case EQU: case NEQ: case LTH: case GTH: case GEQ: case LEQ:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
            }
        } else if(binExpr.oper == BinExpr.Oper.ASSIGN) {
//            attrs.typAttr.set(binExpr, new VoidTyp());
        } else if(fstAct != null && sndAct != null
                && binExpr.oper == BinExpr.Oper.REC) {
            attrs.typAttr.set(binExpr, sndAct);
            recUse = null;
        } else {
            throw new CompilerError("[Semantic error, binExpr]: Ambiguous types " + binExpr);
        }
    }

    public void visit(CastExpr castExpr) {
        castExpr.type.accept(this);
        castExpr.expr.accept(this);

        Typ type = attrs.typAttr.get(castExpr.type);
        Typ expr = attrs.typAttr.get(castExpr.expr);

        Typ actExpr = expr;
        if(actExpr instanceof TypName && !((TypName) expr).isCircular() )
            actExpr = actExpr.actualTyp();

        if(!(type instanceof PtrTyp) ||
                !(actExpr instanceof PtrTyp) && !(((PtrTyp) expr).baseTyp instanceof VoidTyp))
            throw new CompilerError("[Semantic error, evalTyp-cast]: Type missmatch at cast: " + castExpr);
        attrs.typAttr.set(castExpr, type);
    }

    public void visit(CompDecl compDecl) {
        if (turn != 1) return;
        try {
            symbolTable.insDecl(this.recNow.toString(), compDecl.name, compDecl);
        } catch (CannotInsNameDecl err) {
            throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of component at " + compDecl);
        }
        compDecl.type.accept(this);
        attrs.typAttr.set(compDecl, attrs.typAttr.get(compDecl.type));
    }

    public void visit(CompName compName) {
        try {
            Decl dec = symbolTable.fndDecl(recUse.nameSpace, compName.name());
            attrs.declAttr.set(compName, dec);
            Typ type = attrs.typAttr.get(dec);
            attrs.typAttr.set(compName, type);
        } catch (CannotFndNameDecl cannotFndNameDecl) {
            cannotFndNameDecl.printStackTrace();
        }
    }

    public void visit(Exprs exprs) {
        Typ exp = null;
        for (int e = 0; e < exprs.numExprs(); e++) {
            exprs.expr(e).accept(this);
            exp = attrs.typAttr.get(exprs.expr(e));
        }
        if(exp != null)
            attrs.typAttr.set(exprs, exp);
        else
            attrs.typAttr.set(exprs, new VoidTyp());
    }

    public void visit(ExprError exprError) {
    }

    public void visit(ForExpr forExpr) {
        Typ var, lo, hi, body;
        forExpr.var.accept(this);
        forExpr.loBound.accept(this);
        forExpr.hiBound.accept(this);
        forExpr.body.accept(this);

        var = attrs.typAttr.get(forExpr.var);
        lo = attrs.typAttr.get(forExpr.loBound);
        hi = attrs.typAttr.get(forExpr.hiBound);
        body = attrs.typAttr.get(forExpr.body);

        if(var == null || lo == null || hi == null || body == null) {
            throw new CompilerError("[Semantic Error, EvalTyp, for] Type missmatch in for loop" + forExpr);
        }
        attrs.typAttr.set(forExpr, new VoidTyp());
    }

    public void visit(FunCall funCall) {
        Decl fun = attrs.declAttr.get(funCall);
        FunTyp funTyp = (FunTyp) attrs.typAttr.get(fun);
        LinkedList<Typ> params = new LinkedList<>();
        for (int a = 0; a < funCall.numArgs(); a++) {
            funCall.arg(a).accept(this);
            Typ typ = attrs.typAttr.get(funCall.arg(a));
            if(typ instanceof TypName && !((TypName) typ).isCircular())
                typ = typ.actualTyp();
            if(typ instanceof FunTyp)
                typ = ((FunTyp) typ).resultTyp;
            params.add(typ);
        }
        FunTyp call = new FunTyp(params, funTyp.resultTyp);
        if(funTyp.isStructEquivTo(call))
            attrs.typAttr.set(funCall, call);
        else
            throw new CompilerError("[Semantic Error, EvalTyp, funcall] Type missmatch at function call " + funCall);
    }

    public void visit(FunDecl funDecl) {
        if(turn != 1) return;
        funDecl.type.accept(this);
        Typ type = attrs.typAttr.get(funDecl.type);
        LinkedList<Typ> params = new LinkedList<>();
        for (int p = 0; p < funDecl.numPars(); p++) {
            funDecl.par(p).accept(this);
            params.add(attrs.typAttr.get(funDecl.par(p)));
        }
        attrs.typAttr.set(funDecl, new FunTyp(params, type));
    }

    public void visit(FunDef funDef) {
        if(turn == 1) {
            funDef.type.accept(this);
            Typ type = attrs.typAttr.get(funDef.type);
            LinkedList<Typ> params = new LinkedList<>();
            for (int p = 0; p < funDef.numPars(); p++) {
                funDef.par(p).accept(this);
                params.add(attrs.typAttr.get(funDef.par(p)));
            }
            attrs.typAttr.set(funDef, new FunTyp(params, type));
        } else if(turn == 2)
            funDef.body.accept(this);
    }

    public void visit(IfExpr ifExpr) {
        ifExpr.cond.accept(this);
        ifExpr.thenExpr.accept(this);
        ifExpr.elseExpr.accept(this);

        Typ cond = attrs.typAttr.get(ifExpr.cond);
        if(cond instanceof TypName && !((TypName) cond).isCircular())
            cond = cond.actualTyp();
        if(cond instanceof FunTyp)
            cond = ((FunTyp) cond).resultTyp;

        Typ then = attrs.typAttr.get(ifExpr.thenExpr);
        if(then instanceof TypName && !((TypName) then).isCircular())
            then = then.actualTyp();
        if(cond instanceof FunTyp)
            then = ((FunTyp) then).resultTyp;
        if(then == null) // assingment
            then = new VoidTyp();


        Typ elseExpr = attrs.typAttr.get(ifExpr.elseExpr);
        if(elseExpr instanceof TypName && !((TypName) elseExpr).isCircular())
            elseExpr = elseExpr.actualTyp();
        if(cond instanceof FunTyp)
            elseExpr = ((FunTyp) elseExpr).resultTyp;
        if(elseExpr == null) // assingment
            elseExpr = new VoidTyp();

        if(cond instanceof TypName && !((TypName) cond).isCircular())
            cond = cond.actualTyp();
        if(cond instanceof FunTyp)
            cond = ((FunTyp) cond).resultTyp;

        if(!(cond instanceof BooleanTyp) || then == null || elseExpr == null)
            throw new CompilerError("[Semantic Error, EvalTyp, if] Type missmatch at if expression " + ifExpr);
        attrs.typAttr.set(ifExpr, new VoidTyp());
    }

    public void visit(ParDecl parDecl) {
        if(turn != 1) return;
        parDecl.type.accept(this);
        attrs.typAttr.set(parDecl, attrs.typAttr.get(parDecl.type));
    }

    public void visit(Program program) {
        program.expr.accept(this);
    }

    public void visit(PtrType ptrType) {
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
        if (turn != 1) return;
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

    public void visit(TypeDecl typDecl) {
        TypName type;
        if(turn == 0) {
             type = new TypName(typDecl.name);
            attrs.typAttr.set(typDecl, type);
        } else if(turn == 1) {
            typDecl.type.accept(this);
            type = (TypName) this.attrs.typAttr.get(typDecl);
            type.setType(attrs.typAttr.get(typDecl.type));
        }
    }

    public void visit(TypeName typeName) {
        if (turn != 1) return;
        Decl dec = attrs.declAttr.get(typeName);
        Typ type = attrs.typAttr.get(dec);
        attrs.typAttr.set(typeName, type);
    }

    public void visit(UnExpr unExpr) {
//        if (turn != 1) return;
        unExpr.subExpr.accept(this);
        Typ type = attrs.typAttr.get(unExpr.subExpr);
        if(type instanceof TypName && !((TypName) type).isCircular())
            type = type.actualTyp();
        if(type instanceof FunTyp)
            type = ((FunTyp) type).resultTyp;
        if(type == null)
            type = new VoidTyp();
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
            case MEM:
                attrs.typAttr.set(unExpr, new PtrTyp(type));
                break;
            case VAL:
                if(type instanceof PtrTyp)
                    attrs.typAttr.set(unExpr, ((PtrTyp) type).baseTyp);
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
        }
    }

    public void visit(VarDecl varDecl) {
        if(turn != 1) return;
        varDecl.type.accept(this);
        Typ type = attrs.typAttr.get(varDecl.type);
        attrs.typAttr.set(varDecl, type);
    }

    public void visit(VarName varName) {
        Decl dec = attrs.declAttr.get(varName);
        attrs.typAttr.set(varName, attrs.typAttr.get(dec));
    }

    public void visit(WhereExpr whereExpr) {
        int prevTurn = turn;
        turn = 0;
        for (int p = 0; p < 3; p++) {
            for (int d = 0; d < whereExpr.numDecls(); d++)
                whereExpr.decl(d).accept(this);
            first = false;
            turn++;
        }
        turn = prevTurn;// TODO change @first with @turn
        whereExpr.expr.accept(this);
        if(attrs.typAttr.get(whereExpr.expr) != null) {
            Typ act = attrs.typAttr.get(whereExpr.expr);
            if (act instanceof TypName && !((TypName) act).isCircular())
                act = act.actualTyp();
            attrs.typAttr.set(whereExpr, act);
        } else
            attrs.typAttr.set(whereExpr, new VoidTyp());
    }

    public void visit(WhileExpr whileExpr) {
        whileExpr.cond.accept(this);
        whileExpr.body.accept(this);

        Typ cond = attrs.typAttr.get(whileExpr.cond);
        Typ body = attrs.typAttr.get(whileExpr.body);

        if(!(cond instanceof BooleanTyp) || body == null)
            throw new CompilerError("[Semantic Error, EvalTyp, while] Type missmatch at while loop " + whileExpr);
        attrs.typAttr.set(whileExpr, new VoidTyp());
    }


}
