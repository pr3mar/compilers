package compiler.phase.frames;

import java.util.*;

import compiler.common.report.CompilerError;
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
	private long parOffset;
	private long varOffset;
	private long outSize;
	private boolean parVar;
	private int funCount;
	private String parentFuns;
	private String parentRec;

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
		this.levels = 0;
		this.funCount = 0;
		this.parentFuns = "";
		this.parentRec = "";
		this.parOffset = 0;
		this.varOffset = 0;
		this.outSize = 0;
		this.parVar = false;
	}

	// TODO

	private String getLabel(Decl dec) {
		if(this.levels == 0) {
			if(dec instanceof CompDecl)
				return "_" + parentRec + dec.name;
			else
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
		long oldParOff = this.parOffset, oldVarOff = this.varOffset;
		this.parOffset = 8;
		this.varOffset = 0;
		this.parVar = true;
		long locSize = 8;
		for (int p = 0; p < funDef.numPars(); p++) {
			funDef.par(p).accept(this);
			locSize += this.attrs.typAttr.get(funDef.par(p)).size();
		}
		locSize += ((FunTyp)this.attrs.typAttr.get(funDef)).resultTyp.size();
		this.outSize = Math.max(locSize, this.outSize);
		this.parVar = false;
		funDef.type.accept(this);
		String label = getLabel(funDef);
		String oldParents = this.parentFuns;
		this.parentFuns += funDef.name + "_";
		this.levels++;
		long oldOut = this.outSize;
		this.outSize = 8;
		funDef.body.accept(this);
		this.levels--;
		this.parentFuns = oldParents;
		Frame f = new Frame(levels, label, Math.abs(this.parOffset), Math.abs(this.varOffset), 0, 0, this.outSize);
		this.parOffset = oldParOff; this.varOffset = oldVarOff;
		this.outSize = oldOut;
		this.attrs.frmAttr.set(funDef, f);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
		long size = this.attrs.typAttr.get(parDecl).size();
		if((this.attrs.typAttr.get(parDecl) instanceof  RecTyp))
			this.parOffset -= size;
		OffsetAccess off = new OffsetAccess(parOffset, size);
		this.parOffset += size;
		this.attrs.accAttr.set(parDecl, off);
	}

	public void visit(VarDecl varDecl) {
		Typ t = this.attrs.typAttr.get(varDecl);
		String oldParent = this.parentRec;
		if(t instanceof RecTyp) {
			parentRec = varDecl.name + "_";
		}
		varDecl.type.accept(this);
		this.parentRec = oldParent;
		Access acc;
		long size = t.size();
		if (this.levels == 0)
			acc= new StaticAccess(getLabel(varDecl), size);
		else {
			if(!(this.attrs.typAttr.get(varDecl) instanceof  RecTyp))
				this.varOffset -= size;
			acc = new OffsetAccess(this.varOffset, size);
		}
		this.attrs.accAttr.set(varDecl, acc);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
		long size = this.attrs.typAttr.get(compDecl).size();
		Access acc;
		if (this.levels == 0)
			acc= new StaticAccess(getLabel(compDecl), size);
		else if(this.parVar){
			acc = new OffsetAccess(parOffset, size);
			this.parOffset += size;
		} else {
			this.varOffset -= size;
			acc = new OffsetAccess(varOffset, size);
		}
		this.attrs.accAttr.set(compDecl, acc);
	}


}
