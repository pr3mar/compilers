package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 *
 * Created by pr3mar on 8/27/16.
 */
public class VarCustomMem extends VarDecl {

    public final String memoryLocation;

    public VarCustomMem(Position position, String name, Type type, String memoryLocation) {
        super(position, name, type);
        this.memoryLocation = memoryLocation;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
