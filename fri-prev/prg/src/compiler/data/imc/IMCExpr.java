package compiler.data.imc;

import compiler.data.cod.imcVisitor.IMCVisitor;

/**
 * An expression.
 * 
 * @author sliva
 */
public abstract class IMCExpr extends IMC {

	public abstract SEXPR linCode();

	public abstract void accept(IMCVisitor visitor);
}
