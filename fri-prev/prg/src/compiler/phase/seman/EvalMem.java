package compiler.phase.seman;

import compiler.common.report.CompilerError;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * @author sliva
 */
public class EvalMem extends FullVisitor {
	
	private final Attributes attrs;
	
	public EvalMem(Attributes attrs) {
		this.attrs = attrs;
	}

	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
		attrs.memAttr.set(atomExpr, false);
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		Typ t1 = attrs.typAttr.get(binExpr.fstExpr);
		if(t1 instanceof TypName && !((TypName) t1).isCircular())
			t1 = t1.actualTyp();
		Typ t2 = attrs.typAttr.get(binExpr.sndExpr);
		if(t2 instanceof TypName && !((TypName) t2).isCircular())
			t2 = t2.actualTyp();
		switch (binExpr.oper) {
			case ASSIGN:
				boolean mem = attrs.memAttr.get(binExpr.fstExpr);
				if(mem && t1 != null && t2 != null) {
					attrs.memAttr.set(binExpr, true);
					attrs.typAttr.set(binExpr, new VoidTyp());
				} else {
					throw new CompilerError("[Semantic error, memEval] Cannot assign at " + binExpr);
				}
				break;
			case ARR:
				if(t1 != null && t2 instanceof IntegerTyp) {
					attrs.memAttr.set(binExpr, true);
				} else {
					throw new CompilerError("[Semantic error, memEval] Invalid expression " + binExpr);
				}
				break;
			case REC:
				if(t1 != null && t2 != null) {
					attrs.memAttr.set(binExpr, true);
				} else {
					throw new CompilerError("[Semantic error, memEval] Invalid expression " + binExpr);
				}
				break;
			default:
				attrs.memAttr.set(binExpr, false);
		}
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
		attrs.memAttr.set(castExpr, false);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
		attrs.memAttr.set(compName, false);
	}

	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
//		Typ exp = null;
		for (int e = 0; e < exprs.numExprs(); e++) {
			exprs.expr(e).accept(this);
//			exp = attrs.typAttr.get(exprs.expr(e));
		}
//		attrs.typAttr.set(exprs, exp);
		attrs.memAttr.set(exprs, false);
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
		attrs.memAttr.set(forExpr, false);
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		attrs.memAttr.set(funCall, false);
	}

	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	public void visit(FunDef funDef) {
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
		attrs.memAttr.set(ifExpr, false);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		program.expr.accept(this);
		attrs.memAttr.set(program, false);
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
	}

	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
	}

	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
		Typ type = attrs.typAttr.get(unExpr);
		if(type instanceof TypName && !((TypName)type).isCircular())
			type = type.actualTyp();
		switch (unExpr.oper) {
			case MEM:
				if(type instanceof PtrTyp && attrs.memAttr.get(unExpr.subExpr)) {
					attrs.memAttr.set(unExpr, true);
				} else {
					throw new CompilerError("[Semantic error, memEval] Cannot address this!!" + unExpr);
				}
				break;
			default:
				attrs.memAttr.set(unExpr, false);
				break;
		}

	}

	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
	}

	public void visit(VarName varName) {
		if(attrs.typAttr.get(varName) != null)
			attrs.memAttr.set(varName, true);
		else
			attrs.memAttr.set(varName, false);
	}

	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
		attrs.memAttr.set(whereExpr, false);
//		attrs.typAttr.set(whereExpr, attrs.typAttr.get(whereExpr.expr).actualTyp());
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
		attrs.memAttr.set(whileExpr, false);
	}

}
