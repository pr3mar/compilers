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
	private String unary;
	
	public EvalValue(Attributes attrs) {
		this.attrs = attrs;
		unary = "";
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		if(atomExpr.type.equals(AtomExpr.AtomTypes.INTEGER)) {
			long atomExprVal;
			try {
				atomExprVal = Long.parseLong(unary + atomExpr.value);
			} catch (NumberFormatException exception) {
				throw new CompilerError("[Semantic error, evalValue]: Invalid constant at " + atomExpr);
			}
			attrs.valueAttr.set(atomExpr, Math.abs(atomExprVal));
		}
	}

	@Override
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

	@Override
	public void visit(UnExpr unExpr) {
		if(unExpr.oper.equals(UnExpr.Oper.ADD)) {
			unary = "+";
		}
		if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
			unary = "-";
		}
		unExpr.subExpr.accept(this);
		long val;
		try {
			val = attrs.valueAttr.get(unExpr.subExpr);
		} catch (NullPointerException err) {
			unary = "";
			return;
		}
		if(unExpr.oper.equals(UnExpr.Oper.ADD)) {
			attrs.valueAttr.set(unExpr, val);
		}
		if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
			attrs.valueAttr.set(unExpr, -val);
		}
		if(unExpr.oper.equals(UnExpr.Oper.INC)) {
			attrs.valueAttr.set(unExpr, val + 1);
		}
		if(unExpr.oper.equals(UnExpr.Oper.DEC)) {
			attrs.valueAttr.set(unExpr, val - 1);
		}
		unary = "";
	}
}
