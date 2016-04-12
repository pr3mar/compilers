package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Declaration resolver.
 * 
 * <p>
 * Declaration resolver maps each AST node denoting a
 * {@link compiler.data.ast.Declarable} name to the declaration where
 * this name is declared. In other words, it links each use of each name to a
 * declaration of that name.
 * </p>
 * 
 * @author sliva
 */
public class EvalDecl extends FullVisitor {

	private final Attributes attrs;
	
	public EvalDecl(Attributes attrs) {
		this.attrs = attrs;
	}

	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();
	/** See which run-over is 1st or second*/
	boolean prototyping;

	// TODO
	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
	}

	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		try {
			Decl val = symbolTable.fndDecl(funCall.name());
			attrs.declAttr.set(funCall, val);
		} catch (CannotFndNameDecl err) {
			throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of var at " + funCall);
		}
	}

	public void visit(FunDecl funDecl) {
		if(prototyping) {
			funDecl.type.accept(this);
			try {
				symbolTable.insDecl(funDecl.name, funDecl);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of function at " + funDecl);
			}
		} else {
			symbolTable.enterScope();
			for (int p = 0; p < funDecl.numPars(); p++)
				funDecl.par(p).accept(this);
			symbolTable.leaveScope();
		}
	}

	public void visit(FunDef funDef) {
		if(prototyping) {
			funDef.type.accept(this);
			try {
				symbolTable.insDecl(funDef.name, funDef);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of function at " + funDef);
			}
		} else { //TODO: are parameters in 1st or 2nd run?
			symbolTable.enterScope();
			for (int p = 0; p < funDef.numPars(); p++)
				funDef.par(p).accept(this);
			funDef.type.accept(this);
			funDef.body.accept(this);
			symbolTable.leaveScope();
		}
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
	}

	public void visit(ParDecl parDecl) {
		try { symbolTable.insDecl(parDecl.name, parDecl); }
		catch (CannotInsNameDecl err) { throw new CompilerError(err.getMessage()); }
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		program.expr.accept(this);
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		// TODO how to identify namespaces
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
	}

	public void visit(TypeDecl typDecl) {
		if(prototyping) {
			try {
				symbolTable.insDecl(typDecl.name, typDecl);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError(err.getMessage());
			}
		}
		typDecl.type.accept(this);

	}

	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
		if(!prototyping) {
			try {
				Decl val = symbolTable.fndDecl(typeName.name());
				attrs.declAttr.set(typeName, val);
			} catch (CannotFndNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of var at " + typeName);
			}
		}
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
	}

	public void visit(VarDecl varDecl) {
		if(prototyping){
			try { symbolTable.insDecl(varDecl.name, varDecl); }
			catch (CannotInsNameDecl err) { throw new CompilerError(err.getMessage()); }
		}
		varDecl.type.accept(this);
	}

	public void visit(VarName varName) {
		try {
			Decl val = symbolTable.fndDecl(varName.name());
			attrs.declAttr.set(varName, val);
		} catch (CannotFndNameDecl err) {
			throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of var at " + varName);
		}
	}

	public void visit(WhereExpr whereExpr) {
		symbolTable.enterScope();
		// go through the declarations first
		prototyping = true;
		for (int p = 0; p < 2; p++) {
			for (int d = 0; d < whereExpr.numDecls(); d++)
				whereExpr.decl(d).accept(this);
			prototyping = false;
		}
		// go through the uses second
		whereExpr.expr.accept(this);

		symbolTable.leaveScope();
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
	}

}
