package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;
import compiler.data.cod.imcVisitor.IMCVisitor;

/**
 * JUMP represents an unconditional jump.
 * 
 * @author sliva
 */
public class JUMP extends IMCStmt {

	/** The label. */
	public final String label;

	/**
	 * Constructs a new unconditional jump.
	 * 
	 * @param label
	 *            The label.
	 */
	public JUMP(String label) {
		this.label = label;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "JUMP:" + label);
		logger.endElement();
	}
	
	@Override
	public STMTS linCode() {
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.add(new JUMP(label));
		return new STMTS(lc);
	}


	public void accept(IMCVisitor visitor) {
		visitor.visit(this);
	}
}
