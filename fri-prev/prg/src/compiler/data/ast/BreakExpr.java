package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author pr3mar
 */
public class BreakExpr extends Expr {

    public BreakExpr(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
