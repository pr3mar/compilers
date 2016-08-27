package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class DoWhileExpr extends Expr {

	public final Expr cond;

	public final Expr body;

	public DoWhileExpr(Position position, Expr cond, Expr body) {
		super(position);
		this.cond = cond;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
