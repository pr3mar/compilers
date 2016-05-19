package compiler.data.cod;

/**
 * Code to be generated
 * Created by pr3mar on 5/19/16.
 */
public class Code {

    public enum NodeType {
        Code, Expression, Statement, BIN,
        ADD, SUB, MUL, DIV, MOD

    };

    public NodeType type;

    public Code() {
        this.type = NodeType.Code;
    }
}
