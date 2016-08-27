package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class ForEachExpr extends Expr {

	public final VarName var;

	public final Expr array;

	public final Expr body;

	public ForEachExpr(Position position, VarName var, Expr array, Expr body) {
		super(position);
		this.var = var;
		this.array = array;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
