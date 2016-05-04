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
	private long recOffset;
	private long outSize;
	private int funCount;
	private String parentFuns;

	private HashMap<String, Integer> globals = new HashMap<String, Integer>();



	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
		this.levels = 0;
		this.funCount = 0;
		this.parentFuns = "";
		this.parOffset = 0;
		this.varOffset = 0;
		this.recOffset = 0;
		this.outSize = 0;
	}

	private String getLabel(Decl dec) {
		if(this.levels == 0) {
			String name = "_" + dec.name;
			if(this.globals.containsKey(name)) {
				throw new CompilerError("[Frames error] Duplicate name at " + dec);
			}
			this.globals.put(name, 0);
			return name;
		} else if(dec instanceof FunDef) {
			String ret =  "f" + this.funCount + "___" + this.parentFuns + dec.name;
			this.funCount++;
			return ret;
		} else { // hope it doesn't come to this.
			System.out.println("get label of what???");
			throw new InternalError();
		}
	}

	public void visit(FunDecl funDecl) { }

	public void visit(FunDef funDef) {
		long oldParOff = this.parOffset, oldVarOff = this.varOffset;
		this.parOffset = 8;
		this.varOffset = 0;
		//long locSize = 0;
		this.levels++;
		for (int p = 0; p < funDef.numPars(); p++) {
			funDef.par(p).accept(this);
			//locSize += this.attrs.typAttr.get(funDef.par(p)).size();
		}
		//locSize += ((FunTyp)this.attrs.typAttr.get(funDef)).resultTyp.size();
		//this.outSize = Math.max(locSize, this.outSize);
		funDef.type.accept(this);
		String label = getLabel(funDef);
		String oldParents = this.parentFuns;
		this.parentFuns += funDef.name + "_";
		long oldOut = this.outSize;
		this.outSize = 0;
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
		OffsetAccess off = new OffsetAccess(this.levels - 1, parOffset, size);  // -1 because of shitty implementation
		this.parOffset += size;
		this.attrs.accAttr.set(parDecl, off);
	}

	public void visit(VarDecl varDecl) {
		Typ t = this.attrs.typAttr.get(varDecl);
		varDecl.type.accept(this);
		Access acc;
		long size = t.size();
		if (this.levels == 0)
			acc= new StaticAccess(getLabel(varDecl), size);
		else {
			this.varOffset -= size;
			acc = new OffsetAccess(this.levels - 1, this.varOffset, size); // -1 because of shitty implementation
		}
		this.attrs.accAttr.set(varDecl, acc);
	}


	public void visit(RecType recType) {
		long oldOffset = this.recOffset;
		this.recOffset = 0;
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
		this.recOffset = oldOffset;
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
		long size = this.attrs.typAttr.get(compDecl).size();
		Access acc;
		acc = new OffsetAccess(-1, this.recOffset, size);
		this.recOffset += size;
		this.attrs.accAttr.set(compDecl, acc);
	}

	public void visit(FunCall funCall) {
		long local = 8;
		for (int a = 0; a < funCall.numArgs(); a++) {
			funCall.arg(a).accept(this);
			local += this.attrs.typAttr.get(funCall.arg(a)).size();
		}
		this.outSize = Math.max(local, this.outSize);
	}
}
