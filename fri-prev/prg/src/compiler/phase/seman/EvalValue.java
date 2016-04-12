package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

import java.util.Stack;

/**
 * Computes the value of simple integer constant expressions.
 * 
 * <p>
 * Simple integer constant expressions consists of integer constants and five
 * basic arithmetic operators (<code>ADD</code>, <code>SUB</code>,
 * <code>MUL</code>, <code>DIV</code>, and <code>MOD</code>).
 * </p>
 * 
 * <p>
 * This is needed during type resolving and type checking to compute the correct
 * array types.
 * </p>
 * 
 * @author sliva
 */
public class EvalValue extends FullVisitor {

	private final Attributes attrs;
	private Stack<Object> lastVals;
	
	public EvalValue(Attributes attrs) {
		this.attrs = attrs;
		lastVals = new Stack<Object>();
	}

	public void visit(AtomExpr atomExpr) {
		if(atomExpr.type.equals(AtomExpr.AtomTypes.INTEGER)) {
			long atomExprVal = 0;
			boolean success = true;
			try {
				atomExprVal = Long.parseLong(atomExpr.value);
			} catch (NumberFormatException exception) {
				success = false;
			}
			if(success) {
				attrs.valueAttr.set(atomExpr, atomExprVal);
			} else {
				attrs.valueAttr.set(atomExpr, null);
			}
		}
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		long fVal, sVal, val = 0;
		boolean can = true;
		try { fVal = attrs.valueAttr.get(binExpr.fstExpr); }
		catch (NullPointerException err) { can = false; return; }
		try { sVal = attrs.valueAttr.get(binExpr.sndExpr); }
		catch (NullPointerException err) { can = false; return; }

		if(binExpr.oper.equals(BinExpr.Oper.ADD)) {
			val = fVal + sVal;
		}
		if(binExpr.oper.equals(BinExpr.Oper.SUB)) {
			val = fVal - sVal;
		}
		if(binExpr.oper.equals(BinExpr.Oper.MUL)) {
			val = fVal * sVal;
		}
		if(binExpr.oper.equals(BinExpr.Oper.MOD)) {
			val = fVal % sVal;
		}
		if(binExpr.oper.equals(BinExpr.Oper.DIV)) {
			val = fVal / sVal;
		}
		if(can)
			attrs.valueAttr.set(binExpr, val);
	}

	public void visit(Exprs exprs) { //TODO  just an idea...
		//long valExprs;
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
		//valExprs = attrs.valueAttr.get(exprs.expr(exprs.numExprs() - 1));
		//attrs.valueAttr.set(exprs, valExprs);
	}


	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
		long val;
		try {
			val = attrs.valueAttr.get(unExpr.subExpr);
		} catch (NullPointerException err) {
			return;
		}
		if(unExpr.oper.equals(UnExpr.Oper.ADD)) {
			attrs.valueAttr.set(unExpr, val);
		}
		if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
			attrs.valueAttr.set(unExpr, -val);
		}
	}
}
