package compiler.phase.frames;

import java.util.*;

import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frm.*;
import compiler.data.typ.*;

/**
 * Frame and access evaluator.
 * 
 * @author sliva
 */
public class EvalFrames extends FullVisitor {

	private final Attributes attrs;

	private int levels;
	private int funCount;
	private String parentFuns;

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
		this.levels = 0;
		this.funCount = 0;
		this.parentFuns = "";
	}

	// TODO

	private String getLabel(Decl dec) {
		if(this.levels == 0) {
			return "_" + dec.name;
		} else if(dec instanceof FunDef) {
			String ret =  "f" + this.funCount + "___" + this.parentFuns + dec.name;
			this.funCount++;
			return ret;
		} else { // hope it doesn't come to this.
			System.out.println("get label of what???");
			throw new InternalError();
		}
	}

	public void visit(FunDef funDef) {
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		String label = getLabel(funDef);
		this.levels++;
		String oldParents = this.parentFuns;
		this.parentFuns += funDef.name + "_";
		funDef.body.accept(this);
		this.parentFuns = oldParents;
		this.levels--;
		Frame f = new Frame(levels, label, 1, 1, 0, 0, 1);
		this.attrs.frmAttr.set(funDef, f);
	}

}
