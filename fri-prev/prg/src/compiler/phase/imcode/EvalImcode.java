package compiler.phase.imcode;

import java.util.*;

import compiler.common.report.*;
import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frg.*;
import compiler.data.frm.*;
import compiler.data.imc.*;
import compiler.data.typ.*;

/**
 * Evaluates intermediate code.
 * 
 * @author sliva
 */
public class EvalImcode extends FullVisitor {

	private final Attributes attrs;

	private final HashMap<String, Fragment> fragments;

	private Stack<CodeFragment> codeFragments = new Stack<CodeFragment>();

	public EvalImcode(Attributes attrs, HashMap<String, Fragment> fragments) {
		this.attrs = attrs;
		this.fragments = fragments;
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		switch (atomExpr.type) {
		case INTEGER:
			try {
				long value = Long.parseLong(atomExpr.value);
				attrs.imcAttr.set(atomExpr, new CONST(value));
			} catch (NumberFormatException ex) {
				Report.warning(atomExpr, "Illegal integer constant.");
			}
			break;
		case BOOLEAN:
			if (atomExpr.value.equals("true"))
				attrs.imcAttr.set(atomExpr, new CONST(1));
			if (atomExpr.value.equals("false"))
				attrs.imcAttr.set(atomExpr, new CONST(0));
			break;
		case CHAR:
			if (atomExpr.value.charAt(1) == '\\')
                switch (atomExpr.value.charAt(2)) {
                    case 'n':
                        attrs.imcAttr.set(atomExpr, new CONST('\n'));
                        break;
                    case 't':
                        attrs.imcAttr.set(atomExpr, new CONST('\t'));
                        break;
                    case '\\':
                        attrs.imcAttr.set(atomExpr, new CONST('\\'));
                        break;
                    case '\'':
                        attrs.imcAttr.set(atomExpr, new CONST('\''));
                        break;
                    case '\"':
                        attrs.imcAttr.set(atomExpr, new CONST('\"'));
                        break;
                }
			else
				attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(1)));
			break;
		case STRING:
			String label = LABEL.newLabelName();
			attrs.imcAttr.set(atomExpr, new NAME(label));
			ConstFragment fragment = new ConstFragment(label, atomExpr.value);
			attrs.frgAttr.set(atomExpr, fragment);
			fragments.put(fragment.label, fragment);
			break;
		case PTR:
			attrs.imcAttr.set(atomExpr, new CONST(0));
			break;
		case VOID:
			attrs.imcAttr.set(atomExpr, new NOP());
			break;
		}
	}

    @Override
    public void visit(BinExpr binExpr) {
        binExpr.fstExpr.accept(this);
        binExpr.sndExpr.accept(this);
        IMCExpr fst = (IMCExpr) this.attrs.imcAttr.get(binExpr.fstExpr);
        IMCExpr snd = (IMCExpr) this.attrs.imcAttr.get(binExpr.sndExpr);
        IMC binop = null;
        switch (binExpr.oper) {
            /* logical operations */
            case OR:
                binop = new BINOP(BINOP.Oper.OR, fst, snd);
                break;
            case AND:
                binop = new BINOP(BINOP.Oper.AND, fst, snd);
                break;
            case EQU:
                binop = new BINOP(BINOP.Oper.EQU, fst, snd);
                break;
            case NEQ:
                binop = new BINOP(BINOP.Oper.NEQ, fst, snd);
                break;
            case LEQ:
                binop = new BINOP(BINOP.Oper.LEQ, fst, snd);
                break;
            case GEQ:
                binop = new BINOP(BINOP.Oper.GEQ, fst, snd);
                break;
            case GTH:
                binop = new BINOP(BINOP.Oper.GTH, fst, snd);
                break;
            case LTH:
                binop = new BINOP(BINOP.Oper.LTH, fst, snd);
                break;

            /* arithmetic operations */
            case ADD:
                binop = new BINOP(BINOP.Oper.ADD, fst, snd);
                break;
            case SUB:
                binop = new BINOP(BINOP.Oper.SUB, fst, snd);
                break;
            case MUL:
                binop = new BINOP(BINOP.Oper.MUL, fst, snd);
                break;
            case DIV:
                binop = new BINOP(BINOP.Oper.DIV, fst, snd);
                break;
            case MOD:
                binop = new BINOP(BINOP.Oper.MOD, fst, snd);
                break;

            /* assign */
            case ASSIGN:
                binop = new SEXPR(new MOVE(fst, snd), new NOP());
                break;
            /* access record components */
            case REC:
                VarDecl dec =  (VarDecl) this.attrs.declAttr.get((VarName) binExpr.sndExpr);
                OffsetAccess acc = (OffsetAccess) this.attrs.accAttr.get(dec);
                binop = new MEM(new BINOP(BINOP.Oper.ADD, ((MEM)fst).addr,new CONST(acc.offset)), acc.size);
                break;
            /* access array element */
            case ARR:
                long size = ((ArrTyp) this.attrs.typAttr.get(binExpr.fstExpr).actualTyp()).elemTyp.size();
                IMCExpr tmp = new BINOP(BINOP.Oper.MUL, snd, new CONST(size));
                tmp = new BINOP(BINOP.Oper.ADD, ((MEM)fst).addr, tmp);
                binop = new MEM(tmp, size);
                break;
            default:
                // wWTF??M
                throw new CompilerError("[Imcode] wtf man?" + binExpr);
        }
        this.attrs.imcAttr.set(binExpr, binop);
    }

    @Override
    public void visit(CastExpr castExpr) {
        castExpr.type.accept(this);
        castExpr.expr.accept(this);
        this.attrs.imcAttr.set(castExpr, this.attrs.imcAttr.get(castExpr.expr));
    }

    @Override
    public void visit(Exprs exprs) {
        IMC expr = null;
        Vector<IMCStmt> tmp = new Vector<IMCStmt>();
        for (int e = 0; e < exprs.numExprs() - 1; e++) {
            exprs.expr(e).accept(this);
            IMC exp = this.attrs.imcAttr.get(exprs.expr(e));
            if(exp instanceof IMCExpr)
                tmp.add(new ESTMT((IMCExpr)exp));
            else
                tmp.add((IMCStmt)exp);
        }
        exprs.expr(exprs.numExprs() - 1).accept(this);
        this.attrs.imcAttr.set(exprs, new SEXPR(new STMTS(tmp), (IMCExpr) this.attrs.imcAttr.get(exprs.expr(exprs.numExprs() - 1))));
    }

    @Override
    public void visit(ForExpr forExpr) {
        forExpr.var.accept(this);
        forExpr.loBound.accept(this);
        forExpr.hiBound.accept(this);
        forExpr.body.accept(this);

//        LABEL begin = new LABEL("for_begin" + LABEL.newLabelName());
        LABEL body = new LABEL("for_body_" + LABEL.newLabelName());
        LABEL sndCheck = new LABEL("for_check_" + LABEL.newLabelName());
        LABEL exit = new LABEL("for_exit_" + LABEL.newLabelName());

        IMCExpr var = (IMCExpr) this.attrs.imcAttr.get(forExpr.var);
        IMCExpr lo = (IMCExpr) this.attrs.imcAttr.get(forExpr.loBound);
        IMCExpr hi = (IMCExpr) this.attrs.imcAttr.get(forExpr.hiBound);
        IMCExpr bodyExpr = (IMCExpr) this.attrs.imcAttr.get(forExpr.body);

        IMC cjump_begin, cjump_end;
        cjump_begin = new BINOP(BINOP.Oper.LEQ, var, hi);
        cjump_begin = new CJUMP((IMCExpr) cjump_begin, body.label, exit.label);
        cjump_end = new BINOP(BINOP.Oper.LTH, var, hi);
        cjump_end = new CJUMP((IMCExpr) cjump_end, sndCheck.label, exit.label);

        IMCStmt increment = new MOVE(var, new BINOP(BINOP.Oper.ADD, var, new CONST(1)));

        Vector<IMCStmt> stmts = new Vector<IMCStmt>();
        stmts.add(new MOVE(var, lo));
//        stmts.add(begin);
        stmts.add((IMCStmt) cjump_begin);
        stmts.add(body);
        stmts.add(new ESTMT(bodyExpr));
        stmts.add((IMCStmt) cjump_end);
        stmts.add(sndCheck);
        stmts.add(increment);
        stmts.add(new JUMP(body.label));
        stmts.add(exit);
        this.attrs.imcAttr.set(forExpr, new SEXPR(new STMTS(stmts), new NOP()));
    }

    @Override
    public void visit(FunCall funCall) {
        CodeFragment topFrag = this.codeFragments.peek();
        Frame topFrame = topFrag.frame;
        Frame callFrame = null;
        callFrame = this.attrs.frmAttr.get((FunDecl) this.attrs.declAttr.get(funCall));
        if(callFrame == null) {
//            throw new CompilerError("[Imcode] no frame found @ " + funCall);
            callFrame = new Frame(-1, "_" + funCall.name(), 0, 0, 0, 0, 0);
        }
        int diff = topFrame.level - callFrame.level;
        IMCExpr expr = new TEMP(topFrag.FP);
        for(int i = 0; i <= diff; i++) {
            expr = new MEM(expr, 8);
        }
        Vector<IMCExpr> args = new Vector<IMCExpr>();
        Vector<Long> lengths = new Vector<Long>();
        args.add(expr);lengths.add(8L);
        for (int a = 0; a < funCall.numArgs(); a++) {
            funCall.arg(a).accept(this);
            args.add((IMCExpr)this.attrs.imcAttr.get(funCall.arg(a)));
            lengths.add(this.attrs.typAttr.get(funCall.arg(a)).size());
        }
        expr = new CALL(callFrame.label, args, lengths);
        this.attrs.imcAttr.set(funCall, expr);
    }

    @Override
    public void visit(FunDef funDef) {
        Frame frame = attrs.frmAttr.get(funDef);
        int FP = TEMP.newTempName();
        int RV = TEMP.newTempName();
        CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
        codeFragments.push(tmpFragment);

        for (int p = 0; p < funDef.numPars(); p++)
            funDef.par(p).accept(this);
        funDef.type.accept(this);
        funDef.body.accept(this);

        codeFragments.pop();
        IMCExpr expr = (IMCExpr) attrs.imcAttr.get(funDef.body);
        MOVE move = new MOVE(new TEMP(RV), expr);
        Fragment fragment = new CodeFragment(tmpFragment.frame, tmpFragment.FP, tmpFragment.RV, move);
        attrs.frgAttr.set(funDef, fragment);
        attrs.imcAttr.set(funDef, move);
        fragments.put(fragment.label, fragment);
    }

    @Override
    public void visit(IfExpr ifExpr) {
        ifExpr.cond.accept(this);
        ifExpr.thenExpr.accept(this);
        ifExpr.elseExpr.accept(this);

        LABEL thenLabel = new LABEL("if_then_" + LABEL.newLabelName());
        LABEL elseLabel = new LABEL("if_else_" + LABEL.newLabelName());
        LABEL exitLabel = new LABEL("if_end_" + LABEL.newLabelName());

        IMCExpr cond   = (IMCExpr) this.attrs.imcAttr.get(ifExpr.cond);
        IMCExpr thenEx = (IMCExpr) this.attrs.imcAttr.get(ifExpr.thenExpr);
        IMCExpr elseEx = (IMCExpr) this.attrs.imcAttr.get(ifExpr.elseExpr);

        Vector<IMCStmt> condStms = new Vector<IMCStmt>();
        condStms.add(new CJUMP(cond, thenLabel.label, elseLabel.label));
        condStms.add(thenLabel);
        condStms.add( new ESTMT(thenEx));
        condStms.add( new JUMP(exitLabel.label));
        condStms.add(elseLabel);
        condStms.add( new ESTMT(elseEx));
        condStms.add(exitLabel);

        this.attrs.imcAttr.set(ifExpr, new SEXPR(new STMTS(condStms), new NOP()));
    }

    @Override
    public void visit(Program program) {
        Frame frame = new Frame(-1, "_", 0, 0, 0, 0, 0);
        int FP = TEMP.newTempName();
        int RV = TEMP.newTempName();
        CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
        this.codeFragments.push(tmpFragment);
        program.expr.accept(this);
        IMCExpr code = (IMCExpr) this.attrs.imcAttr.get(program.expr);
        Fragment prog = new CodeFragment(frame, FP, RV, new ESTMT(code));
        this.fragments.put("_", prog);
        this.attrs.frgAttr.set(program, prog);
    }

    @Override
    public void visit(UnExpr unExpr) {
        unExpr.subExpr.accept(this);
        IMC code = null;
        switch (unExpr.oper) {
            case ADD:
                code = new UNOP(UNOP.Oper.ADD, (IMCExpr) this.attrs.imcAttr.get(unExpr.subExpr));
                break;
            case SUB:
                code = new UNOP(UNOP.Oper.SUB, (IMCExpr) this.attrs.imcAttr.get(unExpr.subExpr));
                break;
            case NOT:
                code = new UNOP(UNOP.Oper.NOT, (IMCExpr) this.attrs.imcAttr.get(unExpr.subExpr));
                break;
            case VAL:
                code = new MEM((IMCExpr) this.attrs.imcAttr.get(unExpr.subExpr), 8L);
                break;
            case MEM:
                code = ((MEM) this.attrs.imcAttr.get(unExpr.subExpr)).addr;
                break;
        }
        this.attrs.imcAttr.set(unExpr, code);
    }

    @Override
    public void visit(VarDecl varDecl) {
        varDecl.type.accept(this);
        if(this.codeFragments.size() == 1) {
            StaticAccess acc = (StaticAccess) this.attrs.accAttr.get(varDecl);
            Fragment frag = new DataFragment(acc.label, acc.size);
            this.fragments.put(acc.label, frag);
            this.attrs.frgAttr.set(varDecl, frag);
        }
    }

    @Override
    public void visit(VarCustomMem varCustomMem) {
        varCustomMem.type.accept(this);
        StaticAccessMem acc = (StaticAccessMem) this.attrs.accAttr.get(varCustomMem);
        Fragment frag = new DataFragmentMem(acc.label, acc.size, acc.memoryLocation);
        this.fragments.put(acc.label, frag);
        this.attrs.frgAttr.set(varCustomMem, frag);
    }

    @Override
	public void visit(VarName varName) {
        Access acc = this.attrs.accAttr.get((VarDecl) this.attrs.declAttr.get(varName));
        IMC exprs = null;
        if(acc instanceof StaticAccess) {
            StaticAccess statAcc = ((StaticAccess)acc);
            NAME var = new NAME(statAcc.label);
            exprs = new MEM(var, statAcc.size);
        } else {
            OffsetAccess offAccess = ((OffsetAccess) acc);
            CodeFragment fg = this.codeFragments.peek();
            Frame fr = fg.frame;
            IMCExpr expr = new TEMP(fg.FP);
            if(Math.abs(fr.level - offAccess.level) != 0) {
                for (int i = fr.level; i > offAccess.level; i--) {
                    expr = new MEM(expr, 8);
                }
            }
            exprs = new MEM(new BINOP(BINOP.Oper.ADD, expr, new CONST(offAccess.offset)), offAccess.size);
        }
        this.attrs.imcAttr.set(varName, exprs);
	}

    @Override
    public void visit(WhereExpr whereExpr) {
        whereExpr.expr.accept(this);
        for (int d = 0; d < whereExpr.numDecls(); d++)
            whereExpr.decl(d).accept(this);
        IMC exp = this.attrs.imcAttr.get(whereExpr.expr);
        this.attrs.imcAttr.set(whereExpr, exp);
    }

    @Override
    public void visit(WhileExpr whileExpr) {
        whileExpr.cond.accept(this);
        whileExpr.body.accept(this);

        LABEL begin = new LABEL("while_begin_" + LABEL.newLabelName());
        LABEL body = new LABEL("while_body_" +LABEL.newLabelName());
        LABEL exit = new LABEL("while_exit_" + LABEL.newLabelName());

        IMC ex = this.attrs.imcAttr.get(whileExpr.cond);
        ex = new CJUMP((IMCExpr) ex, body.label, exit.label);

        Vector<IMCStmt> stmts = new Vector<IMCStmt>();
        stmts.add(begin);
        stmts.add((IMCStmt) ex);
        stmts.add(body);
        stmts.add( new ESTMT( (IMCExpr)this.attrs.imcAttr.get(whileExpr.body)) );
        stmts.add(new JUMP(begin.label));
        stmts.add(exit);
        this.attrs.imcAttr.set(whileExpr, new SEXPR(new STMTS(stmts), new NOP()));
    }
}
