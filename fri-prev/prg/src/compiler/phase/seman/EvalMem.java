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

	@Override
	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		attrs.memAttr.set(atomExpr, false);
	}

	@Override
	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		Typ t1 = attrs.typAttr.get(binExpr.fstExpr);
		if(t1 instanceof TypName && !((TypName) t1).isCircular())
			t1 = t1.actualTyp();
		Typ t2 = attrs.typAttr.get(binExpr.sndExpr);
		if(t2 instanceof TypName && !((TypName) t2).isCircular())
			t2 = t2.actualTyp();
		if(t2 instanceof FunTyp) {
			t2 = ((FunTyp) t2).resultTyp;
			if(t2 instanceof TypName && !((TypName) t2).isCircular())
				t2 = t2.actualTyp();
		}
		if(t2 instanceof ArrTyp) {
			t2 = ((ArrTyp) t2).elemTyp;
			if(t2 instanceof TypName && !((TypName) t2).isCircular())
				t2 = t2.actualTyp();
		}
		switch (binExpr.oper) {
			case ASSIGN:
				boolean mem = attrs.memAttr.get(binExpr.fstExpr);
				if(mem) {
					if (!(t1 instanceof BooleanTyp ||  t1 instanceof IntegerTyp ||
							t1 instanceof CharTyp || t1 instanceof StringTyp || t1 instanceof PtrTyp)) {
						throw new CompilerError("[Semantic error, memEval] Cannot assign at " + binExpr.fstExpr);
					}
					if (!(t2 instanceof BooleanTyp ||  t2 instanceof IntegerTyp ||
							t2 instanceof CharTyp || t2 instanceof StringTyp || t2 instanceof PtrTyp)){
						throw new CompilerError("[Semantic error, memEval] Cannot assign at " + binExpr.sndExpr);
					}
					if(!t1.getClass().equals(t2.getClass())) {
						throw new CompilerError("[Semantic error, memEval] Cannot assign at " + binExpr.sndExpr);
					}
					attrs.memAttr.set(binExpr, false);
					attrs.typAttr.set(binExpr, new VoidTyp());
				} else {
					throw new CompilerError("[Semantic error, memEval] Cannot assign at " + binExpr);
				}
				break;
			case ARR:
				if(t1 instanceof ArrTyp && t2 instanceof IntegerTyp) {
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

	@Override
	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
		attrs.memAttr.set(castExpr, false);
	}

	@Override
	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	@Override
	public void visit(CompName compName) {
		attrs.memAttr.set(compName, false);
	}

	@Override
	public void visit(DeclError declError) {
	}

	@Override
	public void visit(Exprs exprs) {
//		Typ exp = null;
		for (int e = 0; e < exprs.numExprs(); e++) {
			exprs.expr(e).accept(this);
//			exp = attrs.typAttr.get(exprs.expr(e));
		}
//		attrs.typAttr.set(exprs, exp);
		attrs.memAttr.set(exprs, false);
	}

	@Override
	public void visit(ExprError exprError) {
	}

	@Override
	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
		attrs.memAttr.set(forExpr, false);
	}

	@Override
	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		attrs.memAttr.set(funCall, false);
	}

	@Override
	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	@Override
	public void visit(FunDef funDef) {
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
	}

	@Override
	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
		attrs.memAttr.set(ifExpr, false);
	}

	@Override
	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	@Override
	public void visit(Program program) {
		program.expr.accept(this);
		attrs.memAttr.set(program, false);
	}

	@Override
	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	@Override
	public void visit(RecType recType) {
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
	}

	@Override
	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
	}

	@Override
	public void visit(TypeError typeError) {
	}

	@Override
	public void visit(TypeName typeName) {
	}

	@Override
	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
		Typ type = attrs.typAttr.get(unExpr);
		if(type instanceof TypName && !((TypName)type).isCircular())
			type = type.actualTyp();
		Typ typeSub = attrs.typAttr.get(unExpr.subExpr);
		if(typeSub instanceof TypName && !((TypName)typeSub).isCircular())
			typeSub = typeSub.actualTyp();
		switch (unExpr.oper) {
			case MEM:
				if(!(type instanceof PtrTyp && attrs.memAttr.get(unExpr.subExpr))) {
					throw new CompilerError("[Semantic error, memEval] Cannot address this!!" + unExpr);
				}
				attrs.memAttr.set(unExpr, false);
				break;
			case VAL:
				if(typeSub instanceof PtrTyp) {
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

	@Override
	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
	}

	@Override
	public void visit(VarName varName) {
		if(attrs.typAttr.get(varName) != null)
			attrs.memAttr.set(varName, true);
		else
			attrs.memAttr.set(varName, false);
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
		attrs.memAttr.set(whereExpr, false);
//		attrs.typAttr.set(whereExpr, attrs.typAttr.get(whereExpr.expr).actualTyp());
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
		attrs.memAttr.set(whileExpr, false);
	}

}
