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
    private int turn;

    public EvalTyp(Attributes attrs) {
        this.attrs = attrs;
        this.recNow = null;
        this.recUse = null;
    }

    /**
     * The symbol table.
     */
    private SymbolTable symbolTable = new SymbolTable();

    @Override
    public void visit(ArrType arrType) {
        if (turn != 1) return;
        arrType.size.accept(this);
        arrType.elemType.accept(this);
        long size;
        try {
            size = attrs.valueAttr.get(arrType.size);
        } catch (NullPointerException err) {
            throw new CompilerError("[Semantic Error, EvalType] Array size not constant at" + arrType.size);
        }
        Typ type;
        try {
            type = attrs.typAttr.get(arrType.elemType);
        } catch (NullPointerException err) {
            throw new CompilerError("[Semantic Error, EvalType] Array type not defined at" + arrType.elemType);
        }
        if(size < 1)
            throw new CompilerError("[Semantic Error, EvalType] Array size invalid at" + arrType);
        attrs.typAttr.set(arrType, new ArrTyp(size, type));
    }

    @Override
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
                attrs.typAttr.set(atomExpr, new PtrTyp(new VoidTyp()));
                break;
            case VOID:
                attrs.typAttr.set(atomExpr, new VoidTyp());
                break;
        }
    }

    @Override
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

    @Override
    public void visit(BinExpr binExpr) {
        binExpr.fstExpr.accept(this);
        Typ fst = attrs.typAttr.get(binExpr.fstExpr);
        Typ fstAct = fst;
        RecTyp tmp = recUse;
        if(fstAct instanceof TypName && !((TypName) fst).isCircular())
            fstAct = fstAct.actualTyp();
        if(fstAct instanceof RecTyp) {
            recUse = (RecTyp) fstAct;
        }

        binExpr.sndExpr.accept(this);
        Typ snd = attrs.typAttr.get(binExpr.sndExpr);
        Typ sndAct = snd;

        if(sndAct instanceof TypName && !((TypName) sndAct).isCircular())
            sndAct = sndAct.actualTyp();

        if(fstAct instanceof  ArrTyp
                && sndAct instanceof IntegerTyp
                    && binExpr.oper == BinExpr.Oper.ARR) {
            Typ t = ((ArrTyp)fstAct).elemTyp;
            /*if(t instanceof TypName && !((TypName) t).isCircular())
                t = t.actualTyp();*/
            attrs.typAttr.set(binExpr, t);
            return;
        }

        if(fstAct instanceof RecTyp
                && sndAct != null
                    && binExpr.oper == BinExpr.Oper.REC) {
            attrs.typAttr.set(binExpr, snd);
            recUse = tmp;
            return;
        }

        if(fstAct instanceof FunTyp) {
            fstAct = ((FunTyp) fstAct).resultTyp;
            if(fstAct instanceof TypName && !((TypName) fst).isCircular())
                fstAct = fstAct.actualTyp();
        }
        if(fstAct instanceof ArrTyp) {
            fstAct = ((ArrTyp) fstAct).elemTyp;
            if(fstAct instanceof TypName && !((TypName) fst).isCircular())
                fstAct = fstAct.actualTyp();
        }
        if(sndAct instanceof FunTyp) {
            sndAct = ((FunTyp) sndAct).resultTyp;
            if(sndAct instanceof TypName && !((TypName) sndAct).isCircular())
                sndAct = sndAct.actualTyp();
        }
        if(sndAct instanceof ArrTyp) {
            sndAct = ((ArrTyp) sndAct).elemTyp;
            if(sndAct instanceof TypName && !((TypName) sndAct).isCircular())
                sndAct = sndAct.actualTyp();
        }
        if(fstAct instanceof IntegerTyp && sndAct instanceof IntegerTyp) {
            switch (binExpr.oper) {
                case ADD: case SUB: case MUL: case MOD: case DIV:
                    attrs.typAttr.set(binExpr, new IntegerTyp());
                    break;
                case EQU: case NEQ: case LTH: case GTH: case GEQ: case LEQ:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
            }
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
                //|| fstAct instanceof StringTyp && sndAct instanceof StringTyp
                    || fstAct instanceof PtrTyp && sndAct instanceof PtrTyp && fstAct.isStructEquivTo(sndAct)) {
            switch (binExpr.oper) {
                case EQU: case NEQ: case LTH: case GTH: case GEQ: case LEQ:
                    attrs.typAttr.set(binExpr, new BooleanTyp());
                    break;
            }
        } else if(binExpr.oper == BinExpr.Oper.ASSIGN) {
//            attrs.typAttr.set(binExpr, new VoidTyp());
        }/* else if(fstAct != null && sndAct != null
                && binExpr.oper == BinExpr.Oper.REC) {
            attrs.typAttr.set(binExpr, sndAct);
            recUse = null;
        } */else {
            throw new CompilerError("[Semantic error, binExpr]: Ambiguous types " + binExpr);
        }
    }

    @Override
    public void visit(CastExpr castExpr) {
        castExpr.type.accept(this);
        castExpr.expr.accept(this);

        Typ type = attrs.typAttr.get(castExpr.type);
        Typ expr = attrs.typAttr.get(castExpr.expr);

        Typ actExpr = expr;
        if(actExpr instanceof TypName && !((TypName) expr).isCircular() )
            actExpr = actExpr.actualTyp();

        if(!(actExpr instanceof PtrTyp) ||
                !(actExpr instanceof PtrTyp) && !(((PtrTyp) expr).baseTyp instanceof VoidTyp))
            throw new CompilerError("[Semantic error, evalTyp-cast]: Type missmatch at cast: " + castExpr);
        attrs.typAttr.set(castExpr, type);
    }

    @Override
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

    @Override
    public void visit(CompName compName) {
        try {
            Decl dec = symbolTable.fndDecl(recUse.nameSpace, compName.name());
            attrs.declAttr.set(compName, dec);
            Typ type = attrs.typAttr.get(dec);
            attrs.typAttr.set(compName, type);
        } catch (Exception e) {
            throw new CompilerError("[Semantic error, evalTyp] Cannot access component." + compName);
        }
    }

    @Override
    public void visit(Exprs exprs) {
        Typ exp = null;
        for (int e = 0; e < exprs.numExprs(); e++) {
            exprs.expr(e).accept(this);
            exp = attrs.typAttr.get(exprs.expr(e));
        }
        if(exp != null) // very unsafe!!
            attrs.typAttr.set(exprs, exp);
        else
            attrs.typAttr.set(exprs, new VoidTyp());
    }

    @Override
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

        if( (var instanceof TypName) && !((TypName)var).isCircular())
            var = var.actualTyp();
        if( (lo instanceof TypName) && !((TypName)lo).isCircular())
            lo = lo.actualTyp();
        if( (hi instanceof TypName) && !((TypName)hi).isCircular())
            hi = hi.actualTyp();
        if( body == null)
            body = new VoidTyp();
        if(!(var instanceof IntegerTyp) || !(lo instanceof IntegerTyp) || !(hi instanceof IntegerTyp) || body == null) {
            throw new CompilerError("[Semantic Error, EvalTyp, for] Type missmatch in for loop" + forExpr);
        }
        attrs.typAttr.set(forExpr, new VoidTyp());
    }

    @Override
    public void visit(FunCall funCall) {
        Decl fun = attrs.declAttr.get(funCall);
        FunTyp funTyp = null;
        try {
            funTyp = (FunTyp) attrs.typAttr.get(fun);
        } catch (Exception e) {
            throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of function at " + funCall);
        }
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
            attrs.typAttr.set(funCall, call.resultTyp);
        else
            throw new CompilerError("[Semantic Error, EvalTyp, funcall] Type missmatch at function call " + funCall);
    }

    @Override
    public void visit(FunDecl funDecl) {
        if(turn == 1) {
            funDecl.type.accept(this);
            Typ type = attrs.typAttr.get(funDecl.type);
            LinkedList<Typ> params = new LinkedList<>();
            for (int p = 0; p < funDecl.numPars(); p++) {
                funDecl.par(p).accept(this);
                params.add(attrs.typAttr.get(funDecl.par(p)));
            }
            attrs.typAttr.set(funDecl, new FunTyp(params, type));
        } else if(turn == 2) {
            for (int p = 0; p < funDecl.numPars(); p++) {
                funDecl.par(p).accept(this);
            }
            Typ type = attrs.typAttr.get(funDecl.type);
            if(type instanceof TypName && !((TypName)type).isCircular()) {
                type = type.actualTyp();
            }
            if (!(type instanceof BooleanTyp ||  type instanceof IntegerTyp || type instanceof CharTyp || type instanceof VoidTyp || type instanceof StringTyp || type instanceof PtrTyp))
                throw new CompilerError("[Semantic error, evalTyp] Return type not allowed at " + funDecl.type);
        }
    }

    @Override
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
        } else if(turn == 2) {
            for (int p = 0; p < funDef.numPars(); p++) {
                funDef.par(p).accept(this);
            }
            Typ type = attrs.typAttr.get(funDef.type);
            if(type instanceof TypName && !((TypName)type).isCircular()) {
                type = type.actualTyp();
            }
            if (!(type instanceof BooleanTyp ||  type instanceof IntegerTyp || type instanceof CharTyp || type instanceof VoidTyp || type instanceof StringTyp || type instanceof PtrTyp))
                throw new CompilerError("[Semantic error, evalTyp] Return type not allowed at " + funDef.type);
            funDef.body.accept(this);
            Typ funRet = ((FunTyp) attrs.typAttr.get(funDef)).resultTyp;
            if(funRet instanceof TypName && !((TypName) funRet).isCircular())
                funRet = funRet.actualTyp();
            Typ bodyTyp = attrs.typAttr.get(funDef.body);
            if(bodyTyp == null)
                bodyTyp = new VoidTyp();
            if(bodyTyp instanceof TypName && !((TypName) bodyTyp).isCircular())
                bodyTyp = bodyTyp.actualTyp();
            if(!bodyTyp.getClass().equals(funRet.getClass()))
               throw new CompilerError("[Semantic error, evalTyp, fundef] Inconsistent types at " + funDef);
        }
    }

    @Override
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

    @Override
    public void visit(ParDecl parDecl) {
        if(turn == 1) {
            parDecl.type.accept(this);
            attrs.typAttr.set(parDecl, attrs.typAttr.get(parDecl.type));
        } else if(turn == 2){
            Typ parTyp = attrs.typAttr.get(parDecl.type);
            if(parTyp instanceof TypName && !((TypName)parTyp).isCircular()) {
                parTyp = parTyp.actualTyp();
            }
            if (!(parTyp instanceof BooleanTyp ||  parTyp instanceof IntegerTyp
                    || parTyp instanceof CharTyp || parTyp instanceof StringTyp || parTyp instanceof PtrTyp)) {
                throw new CompilerError("[Semantic error, evalTyp] Parameter type not allowed at " + parDecl);
            }
        }
    }

    @Override
    public void visit(Program program) {
        program.expr.accept(this);
        Typ exprTyp = attrs.typAttr.get(program.expr);
        if(exprTyp instanceof TypName && !((TypName)exprTyp).isCircular())
            exprTyp = exprTyp.actualTyp();
        if(exprTyp != null)
            attrs.typAttr.set(program, exprTyp);
        else
            attrs.typAttr.set(program, new VoidTyp());
    }

    @Override
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

    @Override
    public void visit(RecType recType) {
        if (turn != 1) return;
        symbolTable.newNamespace(recType.toString());
        RecType tmp = this.recNow;
        this.recNow = recType;
        LinkedList<Typ> compTyps = new LinkedList<Typ>();
        for (int c = 0; c < recType.numComps(); c++) {
            recType.comp(c).accept(this);
            compTyps.add(attrs.typAttr.get(recType.comp(c)));
        }
        attrs.typAttr.set(recType, new RecTyp(recType.toString(), compTyps));
        this.recNow = tmp;
    }

    @Override
    public void visit(TypeDecl typDecl) {
        TypName type;
        if(turn == 0) {
             type = new TypName(typDecl.name);
            attrs.typAttr.set(typDecl, type);
        } else if(turn == 1) {
            typDecl.type.accept(this);
            type = (TypName) this.attrs.typAttr.get(typDecl);
            type.setType(attrs.typAttr.get(typDecl.type));
        } else if(turn == 2) {
            try {
                type = (TypName) attrs.typAttr.get(typDecl.type);
                if(type.isCircular()) {
                    Report.warning(typDecl, "[Semantic warning] Circular types at " + typDecl);
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    @Override
    public void visit(TypeName typeName) {
        try {
            attrs.typAttr.get(typeName);
        } catch (Exception e) {
            return;
        }
        Decl dec = attrs.declAttr.get(typeName);
        Typ type = attrs.typAttr.get(dec);
        attrs.typAttr.set(typeName, type);
    }

    @Override
    public void visit(UnExpr unExpr) {
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
                    attrs.typAttr.set(unExpr,attrs.typAttr.get(unExpr.subExpr) );
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
                break;
            case NOT:
                if(type instanceof BooleanTyp)
                    attrs.typAttr.set(unExpr, attrs.typAttr.get(unExpr.subExpr));
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
                break;
            case MEM:
                attrs.typAttr.set(unExpr, new PtrTyp(attrs.typAttr.get(unExpr.subExpr)));
                break;
            case VAL:
                if(type instanceof PtrTyp)
                    attrs.typAttr.set(unExpr, ((PtrTyp) type).baseTyp);
                else
                    throw new CompilerError("[Semantic Error, EvalType] Inconsistent types at " + unExpr);
        }
    }

    @Override
    public void visit(VarDecl varDecl) {
        if(turn != 1) return;
        varDecl.type.accept(this);
        Typ type = attrs.typAttr.get(varDecl.type);
        attrs.typAttr.set(varDecl, type);
    }

    @Override
    public void visit(VarName varName) {
        Decl dec = attrs.declAttr.get(varName);
        attrs.typAttr.set(varName, attrs.typAttr.get(dec));
    }

    @Override
    public void visit(WhereExpr whereExpr) {
        int prevTurn = turn;
        turn = 0;
        for (int p = 0; p < 3; p++) {
            for (int d = 0; d < whereExpr.numDecls(); d++)
                whereExpr.decl(d).accept(this);
            turn++;
        }
        turn = prevTurn;
        whereExpr.expr.accept(this);
        if(attrs.typAttr.get(whereExpr.expr) != null) {
            Typ act = attrs.typAttr.get(whereExpr.expr);
            /*if (act instanceof TypName && !((TypName) act).isCircular())
                act = act.actualTyp();*/
            attrs.typAttr.set(whereExpr, act);
        } else {
            attrs.typAttr.set(whereExpr, new VoidTyp());
        }
    }

    @Override
    public void visit(WhileExpr whileExpr) {
        whileExpr.cond.accept(this);
        whileExpr.body.accept(this);

        Typ cond = attrs.typAttr.get(whileExpr.cond);
        Typ body = attrs.typAttr.get(whileExpr.body);

        if( (cond instanceof TypName) && !((TypName)cond).isCircular())
            cond = cond.actualTyp();
        if(body == null)
            body = new VoidTyp();

        if(!(cond instanceof BooleanTyp) || body == null)
            throw new CompilerError("[Semantic Error, EvalTyp, while] Type missmatch at while loop " + whileExpr);
        attrs.typAttr.set(whileExpr, new VoidTyp());
    }

    @Override
    public void visit(BreakExpr breakExpr) {
        this.attrs.typAttr.set(breakExpr, new VoidTyp());
    }

    @Override
    public void visit(ContinueExpr continueExpr) {
        this.attrs.typAttr.set(continueExpr, new VoidTyp());
    }

}
