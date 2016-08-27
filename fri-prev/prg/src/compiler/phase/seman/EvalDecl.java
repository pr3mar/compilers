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
	/** See which run-over is 1st or 2nd*/
	boolean prototyping;


	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
//		Decl type = attrs.declAttr.get(compDecl.type);
//		attrs.declAttr.set(compDecl, type);
	}

	@Override
	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		try {
			Decl val = symbolTable.fndDecl(funCall.name());
			attrs.declAttr.set(funCall, val);
		} catch (CannotFndNameDecl err) {
			throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of function at " + funCall);
		}
	}

	@Override
	public void visit(FunDecl funDecl) {
		if(prototyping) {
			try {
				symbolTable.insDecl(funDecl.name, funDecl);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of function at " + funDecl);
			}
		} else {
			funDecl.type.accept(this);
			symbolTable.enterScope();
			for (int p = 0; p < funDecl.numPars(); p++)
				funDecl.par(p).accept(this);
			symbolTable.leaveScope();
		}
	}

	@Override
	public void visit(FunDef funDef) {
		if(prototyping) {
			try {// types of parameters in a separate scope-
				symbolTable.insDecl(funDef.name, funDef);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot insert new declaration of function at " + funDef);
			}
		} else {
			funDef.type.accept(this);
			symbolTable.enterScope();
			for (int p = 0; p < funDef.numPars(); p++)
				funDef.par(p).accept(this);
			funDef.body.accept(this);
			symbolTable.leaveScope();
		}
	}

	@Override
	public void visit(ParDecl parDecl) {
		try { symbolTable.insDecl(parDecl.name, parDecl); }
		catch (CannotInsNameDecl err) {
			throw new CompilerError("[Semantic error] Parameter name already declared at " + parDecl);
		}
		parDecl.type.accept(this);
	}

	@Override
	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
		if(prototyping) {
			try {
				symbolTable.insDecl(typDecl.name, typDecl);
			} catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error] Type name already declared at " + typDecl);
			}
		}
	}

	@Override
	public void visit(TypeName typeName) {
		if(!prototyping) {
			try {
				Decl val = symbolTable.fndDecl(typeName.name());
				attrs.declAttr.set(typeName, val);
			} catch (CannotFndNameDecl err) {
				throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of type at " + typeName);
			}
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		if(prototyping){
			try { symbolTable.insDecl(varDecl.name, varDecl); }
			catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error] Variable name already declared at " + varDecl);
			}
		} else {
			varDecl.type.accept(this);
		}
	}

	@Override
	public void visit(VarCustomMem varCustomMem) {
		if(prototyping){
			try { symbolTable.insDecl(varCustomMem.name, varCustomMem); }
			catch (CannotInsNameDecl err) {
				throw new CompilerError("[Semantic error] Variable name already declared at " + varCustomMem);
			}
		} else {
			varCustomMem.type.accept(this);
		}
	}

	@Override
	public void visit(VarName varName) {
		try {
			Decl val = symbolTable.fndDecl(varName.name());
			if (!(val instanceof VarDecl))
				throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of var at " + varName);
			else
				attrs.declAttr.set(varName, val);
		} catch (CannotFndNameDecl err) {
			throw new CompilerError("[Semantic error, evalDecl]: Cannot find declaration of var at " + varName);
		}
	}

	@Override
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

}
