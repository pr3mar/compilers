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
			if (atomExpr.value.charAt(1) == '\'' || atomExpr.value.charAt(1) == '\\')
				attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(2)));
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
                binop = new MOVE(fst, snd);
                break;
            /* access record components */
            case REC:
                //TODO
                break;
            /* access array element */
            case ARR:
                long size = ((ArrTyp) this.attrs.typAttr.get(binExpr.fstExpr)).elemTyp.size();
                IMCExpr tmp = new BINOP(BINOP.Oper.MUL, snd, new CONST(size));
                tmp = new BINOP(BINOP.Oper.ADD, fst, tmp);
                binop = new MEM(tmp, size);
                break;

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
        for (int e = 0; e < exprs.numExprs(); e++)
            exprs.expr(e).accept(this);
    }

    @Override
    public void visit(FunCall funCall) {
        CodeFragment topFrag = this.codeFragments.peek();
        Frame topFrame = topFrag.frame;
        Frame callFragment = this.attrs.frmAttr.get((FunDecl) this.attrs.declAttr.get(funCall));
        int diff = topFrame.level - callFragment.level;
        IMCExpr expr = new MEM(new TEMP(topFrag.FP), 8);
        for(int i = 0; i < diff; i++) {
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
        expr = new CALL(callFragment.label, args, lengths);
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

    public void visit(Program program) {
        Frame frame = new Frame(0, "_", 0, 0, 0, 0, 0);
        int FP = TEMP.newTempName();
        int RV = TEMP.newTempName();
        CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
        this.codeFragments.push(tmpFragment);

        program.expr.accept(this);

        this.codeFragments.pop();
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

}
