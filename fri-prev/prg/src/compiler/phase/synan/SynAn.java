package compiler.phase.synan;

import java.util.*;

import org.w3c.dom.*;

import compiler.*;
import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.phase.*;
import compiler.phase.lexan.*;
import compiler.data.ast.*;

/**
 * The syntax analyzer.
 *
 * @author sliva
 */
public class SynAn extends Phase {

    /**
     * The lexical analyzer.
     */
    private final LexAn lexAn;

    /**
     * Constructs a new syntax analyzer.
     *
     * @param lexAn The lexical analyzer.
     */
    public SynAn(Task task) {
        super(task, "synan");
        this.lexAn = new LexAn(task);
        /*this.logger.setTransformer(//
                new Transformer() {
                    // This transformer produces the
                    // left-most derivation.

                    private String nodeName(Node node) {
                        Element element = (Element) node;
                        String nodeName = element.getTagName();
                        if (nodeName.equals("nont")) {
                            return element.getAttribute("name");
                        }
                        if (nodeName.equals("symbol")) {
                            return element.getAttribute("name");
                        }
                        return null;
                    }

                    private void leftMostDer(Node node) {
                        if (((Element) node).getTagName().equals("nont")) {
                            String nodeName = nodeName(node);
                            NodeList children = node.getChildNodes();
                            StringBuffer production = new StringBuffer();
                            production.append(nodeName + " -->");
                            for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
                                Node child = children.item(childIdx);
                                String childName = nodeName(child);
                                production.append(" " + childName);
                            }
                            Report.info(production.toString());
                            for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
                                Node child = children.item(childIdx);
                                leftMostDer(child);
                            }
                        }
                    }

                    public Document transform(Document doc) {
                        leftMostDer(doc.getDocumentElement().getFirstChild());
                        return doc;
                    }
                });*/
    }

    /**
     * Terminates syntax analysis. Lexical analyzer is not closed and, if
     * logging has been requested, this method produces the report by closing
     * the logger.
     */
    @Override
    public void close() {
        lexAn.close();
        super.close();
    }

    /**
     * The parser's lookahead buffer.
     */
    private Symbol laSymbol;

    /**
     * Reads the next lexical symbol from the source file and stores it in the
     * lookahead buffer (before that it logs the previous lexical symbol, if
     * requested); returns the previous symbol.
     *
     * @return The previous symbol (the one that has just been replaced by the
     * new symbol).
     */
    private Symbol nextSymbol() {
        Symbol symbol = laSymbol;
        symbol.log(logger);
        laSymbol = lexAn.lexAn();
        return symbol;
    }

    /**
     * Logs the error token inserted when a missing lexical symbol has been
     * reported.
     *
     * @return The error token (the symbol in the lookahead buffer is to be used
     * later).
     */
    private Symbol nextSymbolIsError() {
        Symbol error = new Symbol(Symbol.Token.ERROR, "", new Position("", 0, 0));
        error.log(logger);
        return error;
    }

    /**
     * Starts logging an internal node of the derivation tree.
     *
     * @param nontName The name of a nonterminal the internal node represents.
     */
    private void begLog(String nontName) {
        if (logger == null)
            return;
//        System.out.println(nontName);
        logger.begElement("nont");
        logger.addAttribute("name", nontName);
    }

    /**
     * Ends logging an internal node of the derivation tree.
     */
    private void endLog() {
        if (logger == null)
            return;
        logger.endElement();
    }

    /**
     * The parser.
     * <p/>
     * This method performs the syntax analysis of the source file.
     */
    public Program synAn() {
        laSymbol = lexAn.lexAn();
        Program prg = parseProgram();
        if (laSymbol.token != Symbol.Token.EOF)
            Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
        return prg;
    }

    private Program parseProgram() {
        begLog("Program");
        Program prg;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                prg = new Program( new Position(laSymbol.position), parseExpression());
                break;
            default:
                throw new CompilerError("[syntax error, parseProgram] invalid expression at " + laSymbol);
        }
        endLog();
        return prg;
    }

    private Expr parseExpression() {
        begLog("Expression");
        Expr expr;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                expr = parseAssignmentExpression();
                expr = parseExpressionPrime(expr);
                break;
            default:
                throw new CompilerError("[syntax error, parseExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return expr;
    }

    private Expr parseExpressionPrime(Expr expr) {
        begLog("ExpressionPrime");
        switch (laSymbol.token) {
            case WHERE:
                Symbol symWhere = nextSymbol();
                LinkedList<Decl> decls = parseDeclarations();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift end
                expr = new WhereExpr(symWhere.position, expr, decls);
                expr = parseExpressionPrime(expr);
                break;
            case END:
            case COMMA:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            default:
                throw new CompilerError("[syntax error, parseExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return expr;
    }

    private LinkedList<Expr> parseExpressions() {
        begLog("Expressions");
        LinkedList<Expr> exprs = new LinkedList<Expr>();
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                exprs.add(parseExpression());
                exprs = parseExpressionsPrime(exprs);
                break;
            default:
                throw new CompilerError("[syntax error, parseExpressions] invalid expression at " + laSymbol);
        }
        endLog();
        return exprs;
    }

    private LinkedList<Expr> parseExpressionsPrime(LinkedList<Expr> exprs) {
        begLog("ExpressionsPrime");
        switch (laSymbol.token) {
            case COMMA:
                nextSymbol(); // shift comma
                exprs.add(parseExpression());
                exprs = parseExpressionsPrime(exprs);
                break;
            case CLOSING_PARENTHESIS:
                break;
            default:
                throw new CompilerError("[syntax error, parseExpressionsPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return exprs;
    }

    private Expr parseAssignmentExpression() {
        begLog("AssignmentExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parseDisjunctiveExpression();
                bin = parseAssignmentExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseAssignmentExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseAssignmentExpressionPrime(Expr op1) {
        begLog("AssignmentExpressionPrime");
        Expr bin = null;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case ASSIGN:
                Symbol symAssign = nextSymbol(); // shift assign
                bin = new BinExpr(symAssign.position, BinExpr.Oper.ASSIGN, op1, parseDisjunctiveExpression());
                break;
            default:
                throw new CompilerError("[syntax error, parseAssignmentExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseDisjunctiveExpression() {
        begLog("DisjunctiveExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parseConjunctiveExpression();
                bin = parseDisjunctiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseDisjunctiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseDisjunctiveExpressionPrime(Expr op1) {
        begLog("DisjunctiveExpressionPrime");
        Expr bin = null;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case OR:
                Symbol symOr = nextSymbol(); // shift OR
                bin = new BinExpr(symOr.position, BinExpr.Oper.OR, op1, parseConjunctiveExpression());
                parseDisjunctiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseDisjunctiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseConjunctiveExpression() {
        begLog("ConjunctiveExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parseRelationalExpression();
                bin = parseConjunctiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseConjunctiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseConjunctiveExpressionPrime(Expr op1) {
        begLog("ConjunctiveExpressionPrime");
        Expr bin = null;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case AND:
                Symbol symAnd = nextSymbol();
                bin = new BinExpr(symAnd.position, BinExpr.Oper.AND, op1, parseRelationalExpression());
                bin = parseConjunctiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error,  parseConjunctiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseRelationalExpression() {
        begLog("RelationalExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parseAdditiveExpression();
                bin = parseRelationalExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseRelationalExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private BinExpr parseRelationalExpressionPrime(Expr op1) {
        begLog("RelationalExpressionPrime");
        BinExpr bin = null;
        Symbol operator;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case AND:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case EQU:
                operator = nextSymbol(); // shift ==
                bin = new BinExpr(operator.position, BinExpr.Oper.EQU, op1, parseAdditiveExpression());
                break;
            case NEQ:
                operator = nextSymbol(); // shift !=
                bin = new BinExpr(operator.position, BinExpr.Oper.NEQ, op1, parseAdditiveExpression());
                break;
            case LTH:
                operator = nextSymbol(); // shift <
                bin = new BinExpr(operator.position, BinExpr.Oper.LTH, op1, parseAdditiveExpression());
                break;
            case GTH:
                operator = nextSymbol(); // shift >
                bin = new BinExpr(operator.position, BinExpr.Oper.GTH, op1, parseAdditiveExpression());
                break;
            case LEQ:
                operator = nextSymbol(); // shift <
                bin = new BinExpr(operator.position, BinExpr.Oper.LEQ, op1, parseAdditiveExpression());
                break;
            case GEQ:
                operator = nextSymbol(); // shift >=
                bin = new BinExpr(operator.position, BinExpr.Oper.GEQ, op1, parseAdditiveExpression());
                break;
            default:
                throw new CompilerError("[syntax error, parseRelationalExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseAdditiveExpression() {
        begLog("AdditiveExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parseMultiplicativeExpression();
                bin = parseAdditiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseAdditiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseAdditiveExpressionPrime(Expr op1) {
        begLog("AdditiveExpressionPrime");
        Expr bin = null;
        Symbol operator;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case AND:
            case EQU:
            case NEQ:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case ADD:
                operator = nextSymbol();
                bin = new BinExpr(operator.position, BinExpr.Oper.ADD, op1, parseMultiplicativeExpression());
                bin = parseAdditiveExpressionPrime(bin);
                break;
            case SUB:
                operator = nextSymbol();
                bin = new BinExpr(operator.position, BinExpr.Oper.SUB, op1, parseMultiplicativeExpression());
                bin = parseAdditiveExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseAdditiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private Expr parseMultiplicativeExpression() {
        begLog("MultiplicativeExpression");
        Expr bin;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
            case OPENING_BRACKET:
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                bin = parsePrefixExpression();
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseMultiplicativeExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }

    private BinExpr parseMultiplicativeExpressionPrime(Expr op1) {
        begLog("MultiplicativeExpressionPrime");
        BinExpr bin = null;
        Symbol operator;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case AND:
            case EQU:
            case NEQ:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case ADD:
            case SUB:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case MUL:
                operator = nextSymbol();
                bin = new BinExpr(operator.position, BinExpr.Oper.MUL, op1, parsePrefixExpression());
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            case DIV:
                operator = nextSymbol();
                bin = new BinExpr(operator.position, BinExpr.Oper.DIV, op1, parsePrefixExpression());
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            case MOD:
                operator = nextSymbol();
                bin = new BinExpr(operator.position, BinExpr.Oper.MOD, op1, parsePrefixExpression());
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            default:
                throw new CompilerError("[syntax error, parseMultiplicativeExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return bin;
    }


    private Expr parsePrefixExpression() {
        begLog("PrefixExpression");
        Expr exp;
        Symbol symVal;
        switch (laSymbol.token) {
            case ADD:
                symVal = nextSymbol(); // shift mem
                exp = new UnExpr(symVal.position, UnExpr.Oper.ADD, parsePrefixExpression());
                break;
            case SUB:
                symVal = nextSymbol(); // shift mem
                exp = new UnExpr(symVal.position, UnExpr.Oper.SUB, parsePrefixExpression());
                break;
            case NOT:
                symVal = nextSymbol(); // shift mem
                exp = new UnExpr(symVal.position, UnExpr.Oper.NOT, parsePrefixExpression());
                break;
            case MEM:
                symVal = nextSymbol(); // shift mem
                exp = new UnExpr(symVal.position, UnExpr.Oper.MEM, parsePrefixExpression());
                break;
            case OPENING_BRACKET:
                symVal = nextSymbol(); // shift opening bracket
                Type typ = parseType();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePrefixExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing bracket
                exp = new CastExpr(symVal.position, typ, parsePrefixExpression());
                break;
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                exp = parsePostfixExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parsePrefixExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private Expr parsePostfixExpression() {
        begLog("PostfixExpression");
        Expr exp;
        switch (laSymbol.token) {
            case IDENTIFIER:
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
            case OPENING_PARENTHESIS:
            case IF:
            case FOR:
            case WHILE:
                exp = parseAtomicExpression();
                exp = parsePostfixExpressionPrime(exp);
                break;
            default:
                throw new CompilerError("[syntax error, parsePostfixExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private Expr parsePostfixExpressionPrime(Expr exp) {
        begLog("PostfixExpressionPrime");
        Symbol symVal;
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case AND:
            case EQU:
            case NEQ:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case CLOSING_BRACKET:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case OPENING_BRACKET:
                symVal = nextSymbol(); // shift opening bracket
                exp = new BinExpr(symVal.position, BinExpr.Oper.ARR, exp, parseExpression());
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);

                }
                nextSymbol(); // shift closing bracket
                exp = parsePostfixExpressionPrime(exp);
                break;
            case DOT:
                symVal = nextSymbol(); // shift dot
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
                }
                Symbol symID = nextSymbol(); // shift identifier
                exp = new BinExpr(symVal.position, BinExpr.Oper.REC, exp, new VarName(symID.position, symID.lexeme));
                exp = parsePostfixExpressionPrime(exp);
                break;
            case VAL:
                symVal = nextSymbol(); // shift val
                exp = new UnExpr(symVal.position, UnExpr.Oper.VAL, parsePostfixExpressionPrime(exp));
                break;
            default:
                throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private Expr parseAtomicExpression() {
        begLog("AtomicExpression");
        Expr exp = null;
        Symbol consntant;
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol funID = nextSymbol();
                exp = new FunCall(funID.position, funID.lexeme, parseArgumentsOpt());
                break;
            case CONST_INTEGER:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.INTEGER, consntant.lexeme);
                break;
            case CONST_BOOLEAN:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.BOOLEAN, consntant.lexeme);
                break;
            case CONST_CHAR:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.CHAR, consntant.lexeme);
                break;
            case CONST_STRING:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.STRING, consntant.lexeme);
                break;
            case CONST_NULL:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.PTR, consntant.lexeme);
                break;
            case CONST_NONE:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.VOID, consntant.lexeme);
                break;
            case OPENING_PARENTHESIS:
                Symbol symPar = nextSymbol(); // shift opening parenthesis
                exp = new Exprs(symPar.position, parseExpressions());
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS)
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                nextSymbol(); // shift closing parenthesis
                break;
            case IF:
                Symbol symIf = nextSymbol(); // shift if
                Expr cond = parseExpression();
                if (laSymbol.token != Symbol.Token.THEN) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift then
                Expr thenExpr = parseExpression();
                if (laSymbol.token != Symbol.Token.ELSE) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift else
                Expr elseExpr = parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift end
                exp = new IfExpr(symIf.position, cond, thenExpr, elseExpr);
                break;
            case FOR:
                Symbol symFor = nextSymbol(); // shift for
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                Symbol var = nextSymbol();
                if (laSymbol.token != Symbol.Token.ASSIGN) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift assign
                Expr loBound = parseExpression();
                if (laSymbol.token != Symbol.Token.COMMA) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift comma
                Expr hiBound = parseExpression();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                Expr body = parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift end
                exp = new ForExpr(symFor.position, new VarName(var.position, var.lexeme),loBound, hiBound, body);
                break;
            case WHILE:
                Symbol symWhile = nextSymbol(); // shift while
                Expr condWhile = parseExpression();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                Expr bodyWhile = parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift end
                exp = new WhileExpr(symWhile.position, condWhile, bodyWhile);
                break;
            default:
                throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private LinkedList<Expr> parseArgumentsOpt() {
        begLog("ArgumentsOpt");
        LinkedList<Expr> exprs = new LinkedList<Expr>();
        switch (laSymbol.token) {
            case WHERE:
            case END:
            case COMMA:
            case ASSIGN:
            case OR:
            case AND:
            case EQU:
            case NEQ:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case OPENING_BRACKET:
            case CLOSING_BRACKET:
            case DOT:
            case VAL:
            case CLOSING_PARENTHESIS:
            case THEN:
            case ELSE:
            case COLON:
            case TYP:
            case FUN:
            case VAR:
            case EOF:
                break;
            case OPENING_PARENTHESIS:
                nextSymbol(); // shift opening parenthesis
                exprs = parseExpressions();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing parenthesis
                break;
            default:
                throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);

        }
        endLog();
        return exprs;
    }

    private LinkedList<Decl> parseDeclarations() {
        begLog("Declarations");
        LinkedList<Decl> decls = new LinkedList<Decl>();
        switch (laSymbol.token) {
            case TYP:
            case FUN:
            case VAR:
                decls.add(parseDeclaration());
                decls = parseDeclarationsPrime(decls);
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclarations] invalid expression at " + laSymbol);
        }
        endLog();
        return decls;
    }

    private LinkedList<Decl> parseDeclarationsPrime(LinkedList<Decl> decls) {
        begLog("DeclarationsPrime");
        switch (laSymbol.token) {
            case TYP:
            case FUN:
            case VAR:
                decls.add(parseDeclaration());
                decls = parseDeclarationsPrime(decls);
                break;
            case END:
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclarationsPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return decls;
    }

    private Decl parseDeclaration() {
        begLog("Declaration");
        Decl decl;
        switch (laSymbol.token) {
            case TYP:
                decl = parseTypeDeclaration();
                break;
            case FUN:
                decl = parseFunctionDeclaration();
                break;
            case VAR:
                decl = parseVariableDeclaration();
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
        return  decl;
    }

    private TypeDecl parseTypeDeclaration() {
        begLog("TypeDeclaration");
        TypeDecl newTyp;
        switch (laSymbol.token) {
            case TYP:
                Symbol typ = nextSymbol(); // shift TYP
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
                }
                Symbol id = nextSymbol(); // shift identifier
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                newTyp = new TypeDecl(typ.position, id.lexeme, parseType());
                break;
            default:
                throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
        return newTyp;
    }

    private FunDecl parseFunctionDeclaration() {
        begLog("FunctionDeclaration");
        FunDecl func;
        switch (laSymbol.token) {
            case FUN:
                Symbol fun = nextSymbol(); // shift FUN
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                Symbol symID = nextSymbol();
                if (laSymbol.token != Symbol.Token.OPENING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift opening parenthesis
                LinkedList<ParDecl> pars = parseParametersOpt();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing parenthesis
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                Type type = parseType();
                Expr body = parseFunctionBodyOpt();
                if(body != null) {
                    func = new FunDef(fun.position, symID.lexeme, pars, type, body);
                } else {
                    func = new FunDecl(fun.position, symID.lexeme, pars, type);
                }
                break;
            default:
                throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
        return func;
    }

    private LinkedList<ParDecl> parseParametersOpt() {
        begLog("ParametersOpt");
        LinkedList<ParDecl> decls;
        switch (laSymbol.token) {
            case IDENTIFIER:
                decls = parseParameters();
                break;
            case CLOSING_PARENTHESIS:
                decls = null;
                break;
            default:
                throw new CompilerError("[syntax error, parseParametersOpt] invalid expression at " + laSymbol);
        }
        endLog();
        return decls;
    }

    private LinkedList<ParDecl> parseParameters() {
        begLog("Parameters");
        LinkedList<ParDecl> decls;
        decls = new LinkedList<ParDecl>();
        switch (laSymbol.token) {
            case IDENTIFIER:
                decls.add(parseParameter());
                decls = parseParametersPrime(decls);
                break;
            default:
                throw new CompilerError("[syntax error, parseParameters] invalid expression at " + laSymbol);
        }
        endLog();
        return decls;
    }

    private LinkedList<ParDecl> parseParametersPrime(LinkedList<ParDecl> decls) {
        begLog("ParametersPrime");
        switch (laSymbol.token) {
            case COMMA:
                nextSymbol(); // shift comma
                decls.add(parseParameter());
                decls = parseParametersPrime(decls);
                break;
            case CLOSING_PARENTHESIS:
                break;
            default:
                throw new CompilerError("[syntax error, parseParametersPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return decls;
    }

    private ParDecl parseParameter() {
        begLog("Parameter");
        ParDecl decl;
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol symID = nextSymbol(); // shift identifier
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseParameter] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                decl = new ParDecl(symID.position, symID.lexeme, parseType());
                break;
            default:
                throw new CompilerError("[syntax error, parseParameter] invalid expression at " + laSymbol);
        }
        endLog();
        return decl;
    }

    private Expr parseFunctionBodyOpt() {
        begLog("FunctionBodyOpt");
        Expr exp = null;
        switch (laSymbol.token) {
            case END:
            case TYP:
            case FUN:
            case VAR:
                break;
            case ASSIGN:
                nextSymbol(); // shift assign
                exp = parseExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parseFunctionBodyOpt] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private VarDecl parseVariableDeclaration() {
        begLog("VariableDeclaration");
        VarDecl vardec;
        switch (laSymbol.token) {
            case VAR: {
                Symbol var;// shift var
                var = nextSymbol();
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
                }
                Symbol name = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift colon
                Type typ = parseType();
                vardec = new VarDecl(var.position, name.lexeme, typ);
                break;
            }
            default:
                throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
        return vardec;
    }

    private Type parseType() {
        begLog("Type");
        Symbol symVal;
        Type typ;
        switch (laSymbol.token) {
            case IDENTIFIER:
                typ = new TypeName(laSymbol.position, laSymbol.lexeme);
                nextSymbol();
                break;
            case INTEGER:
                typ = new AtomType(laSymbol.position, AtomType.AtomTypes.INTEGER);
                nextSymbol();
                break;
            case BOOLEAN:
                typ = new AtomType(laSymbol.position, AtomType.AtomTypes.BOOLEAN);
                nextSymbol();
                break;
            case CHAR:
                typ = new AtomType(laSymbol.position, AtomType.AtomTypes.CHAR);
                nextSymbol();
                break;
            case STRING:
                typ = new AtomType(laSymbol.position, AtomType.AtomTypes.STRING);
                nextSymbol();
                break;
            case VOID:
                typ = new AtomType(laSymbol.position, AtomType.AtomTypes.VOID);
                nextSymbol();
                break;
            case ARR:
                Symbol arr = nextSymbol(); // shift ARR
                if (laSymbol.token != Symbol.Token.OPENING_BRACKET) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift opening bracket
                Expr size = parseExpression();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing bracket
                Type elemType = parseType();
                typ = new ArrType(arr.position, size, elemType);
                break;
            case REC:
                Symbol rec = nextSymbol(); // shift REC
                if (laSymbol.token != Symbol.Token.OPENING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift opening brace
                typ = new RecType(rec.position, parseComponents());
                if (laSymbol.token != Symbol.Token.CLOSING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing brace
                break;
            case PTR:
                symVal = nextSymbol();
                typ = new PtrType(symVal.position, parseType());
                break;
            default:
                throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
        }
        endLog();
        return typ;
    }


    private LinkedList<CompDecl> parseComponents() {
        begLog("Components");
        LinkedList<CompDecl> comps = new LinkedList<CompDecl>();
        switch (laSymbol.token) {
            case IDENTIFIER:
                comps.add(parseComponent());
                comps = parseComponentsPrime(comps);
                break;
            default:
                throw new CompilerError("[syntax error, parseComponents] invalid expression at " + laSymbol);
        }
        endLog();
        return comps;
    }

    private LinkedList<CompDecl> parseComponentsPrime(LinkedList<CompDecl> comps) {
        begLog("ComponentsPrime");
        switch (laSymbol.token) {
            case COMMA:
                nextSymbol(); // shift comma
                comps.add(parseComponent());
                comps = parseComponentsPrime(comps);
                break;
            case CLOSING_BRACE:
                break;
            default:
                throw new CompilerError("[syntax error, parseComponentsPrime] invalid expression at " + laSymbol);
        }
        endLog();
        return comps;
    }

    private CompDecl parseComponent() {
        begLog("Component");
        CompDecl comps;
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol symID = nextSymbol();
//                CompName name = new CompName(symID.position, symID.lexeme);
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseComponent] invalid expression at " + laSymbol);
                }
                nextSymbol();
                comps = new CompDecl(symID.position, symID.lexeme, parseType());
                break;
            default:
                throw new CompilerError("[syntax error, parseComponent] invalid expression at " + laSymbol);
        }
        endLog();
        return comps;
    }


// template
//	private void parseExpression() {
//		begLog("Expression");
//		switch (laSymbol.token) {
//				break;
//			default:
//				throw new CompilerError("[syntax error] invalid expression at " + laSymbol);
//		}
//		endLog();
//	}

//	private void parseVariableDeclaration() {
//		begLog("VariableDeclaration");
//		switch (laSymbol.token) {
//		case VAR: {
//			Symbol symVar = nextSymbol();
//			Symbol symId;
//			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
//				symId = nextSymbol();
//			} else {
//				Report.warning(laSymbol, "Missing identifier inserted.");
//				symId = nextSymbolIsError();
//			}
//			if (laSymbol.token == Symbol.Token.COLON) {
//				nextSymbol();
//			} else {
//				Report.warning(laSymbol, "Missing symbol ':' inserted.");
//				nextSymbolIsError();
//			}
////			parseType();
//			break;
//		}
//		default:
//			throw new InternalCompilerError();
//		}
//		endLog();
//	}

}
