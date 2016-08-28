package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class ReturnExpr extends Expr {

	public final Expr retExpr;

	public ReturnExpr(Position position) {
		super(position);
		retExpr = null;
	}

	public ReturnExpr(Position position, Expr retExpr) {
		super(position);
		this.retExpr = retExpr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
