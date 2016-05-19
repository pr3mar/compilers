package compiler.data.imc;

import compiler.data.cod.imcVisitor.IMCVisitor;

/**
 * A command.
 * 
 * @author sliva
 */
public abstract class IMCStmt extends IMC {
	
	public abstract STMTS linCode();

	public abstract void accept(IMCVisitor visitor);
}
