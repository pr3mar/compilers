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
        if (task.loggedPhases.equals("synan")) {
            this.logger.setTransformer(//
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
                    });
        }
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
                Symbol copy = laSymbol;
                Expr tmp = parseExpression();
                prg = new Program( new Position(copy, tmp), tmp);
//                prg = new Program( new Position(laSymbol.position), parseExpression());
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
                Symbol symWhere = nextSymbol(); // shift where
                LinkedList<Decl> decls = parseDeclarations();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error] invalid expression at " + laSymbol);
                }
                Symbol symEnd = nextSymbol(); // shift end
                expr = new WhereExpr(new Position(expr, symEnd), expr, decls);
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
                bin = op1;
                break;
            case ASSIGN:
                Symbol symAssign = nextSymbol(); // shift assign
                Expr op2 = parseDisjunctiveExpression();
                bin = parseAssignmentExpressionPrime(op2);
//                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.ASSIGN, op2, op3);
                bin = new BinExpr(new Position(op1, bin), BinExpr.Oper.ASSIGN, op1, bin);
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
                bin = op1;
                break;
            case OR:
                Symbol symOr = nextSymbol(); // shift OR
                Expr op2 = parseConjunctiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.OR, op1, op2);
                bin = parseDisjunctiveExpressionPrime(bin);
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
                bin = op1;
                break;
            case AND:
                Symbol symAnd = nextSymbol(); // shift and
                Expr op2 = parseRelationalExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.AND, op1, op2);
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

    private Expr parseRelationalExpressionPrime(Expr op1) {
        begLog("RelationalExpressionPrime");
        Expr bin = null;
        Expr op2;
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
                bin = op1;
                break;
            case EQU:
                operator = nextSymbol(); // shift ==
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.EQU, op1, op2);
                break;
            case NEQ:
                operator = nextSymbol(); // shift !=
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.NEQ, op1, op2);
                break;
            case LTH:
                operator = nextSymbol(); // shift <
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.LTH, op1, op2);
                break;
            case GTH:
                operator = nextSymbol(); // shift >
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.GTH, op1, op2);
                break;
            case LEQ:
                operator = nextSymbol(); // shift <
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.LEQ, op1, op2);
                break;
            case GEQ:
                operator = nextSymbol(); // shift >=
                op2 = parseAdditiveExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.GEQ, op1, op2);
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
        Expr op2;
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
                bin = op1;
                break;
            case ADD:
                operator = nextSymbol(); // shift +
                op2 = parseMultiplicativeExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.ADD, op1, op2);
                bin = parseAdditiveExpressionPrime(bin);
                break;
            case SUB:
                operator = nextSymbol(); // shift -
                op2 = parseMultiplicativeExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.SUB, op1, op2);
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

    private Expr parseMultiplicativeExpressionPrime(Expr op1) {
        begLog("MultiplicativeExpressionPrime");
        Expr bin = null;
        Expr op2;
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
                bin = op1;
                break;
            case MUL:
                operator = nextSymbol();
                op2 = parsePrefixExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.MUL, op1, op2);
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            case DIV:
                operator = nextSymbol();
                op2 = parsePrefixExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.DIV, op1, op2);
                bin = parseMultiplicativeExpressionPrime(bin);
                break;
            case MOD:
                operator = nextSymbol();
                op2 = parsePrefixExpression();
                bin = new BinExpr(new Position(op1, op2), BinExpr.Oper.MOD, op1, op2);
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
                exp = parsePrefixExpression();
                exp = new UnExpr(new Position(symVal, exp), UnExpr.Oper.ADD, exp);
                break;
            case SUB:
                symVal = nextSymbol(); // shift mem
                exp = parsePrefixExpression();
                exp = new UnExpr(new Position(symVal, exp), UnExpr.Oper.SUB, exp);
                break;
            case NOT:
                symVal = nextSymbol(); // shift mem
                exp = parsePrefixExpression();
                exp = new UnExpr(new Position(symVal, exp), UnExpr.Oper.NOT, exp);
                break;
            case MEM:
                symVal = nextSymbol(); // shift mem
                exp = parsePrefixExpression();
                exp = new UnExpr(new Position(symVal, exp), UnExpr.Oper.MEM, exp);
                break;
            case OPENING_BRACKET:
                symVal = nextSymbol(); // shift opening bracket
                Type typ = parseType();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePrefixExpression] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift closing bracket
                exp = parsePrefixExpression();
                exp = new CastExpr(new Position(symVal, exp), typ, exp);
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
        Expr tmp;
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
                tmp = parseExpression();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol(); // shift closing bracket
                exp = new BinExpr(new Position(exp, symVal), BinExpr.Oper.ARR, exp, tmp);
                exp = parsePostfixExpressionPrime(exp);
                break;
            case DOT:
                symVal = nextSymbol(); // shift dot
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
                }
                Symbol symID = nextSymbol(); // shift identifier
                exp = new BinExpr(new Position(exp, symID), BinExpr.Oper.REC, exp, new CompName(symID.position, symID.lexeme));
                exp = parsePostfixExpressionPrime(exp);
                break;
            case VAL:
                symVal = nextSymbol(); // shift val
                exp = new UnExpr(new Position(exp, symVal), UnExpr.Oper.VAL, exp);
                exp = parsePostfixExpressionPrime(exp);
//                if(symVal.cmpEnd(tmp) == 1) {
//                    exp = new UnExpr(new Position(exp, symVal), UnExpr.Oper.VAL, tmp);
//                } else {
//                    exp = new UnExpr(new Position(exp, tmp), UnExpr.Oper.VAL, tmp);
//                }
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
        Expr tmp;
        Symbol consntant;
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol funID = nextSymbol();
//                exp = new FunCall(funID.position, funID.lexeme, parseArgumentsOpt());
                tmp = parseArgumentsOpt(funID);
                if(tmp == null) {
                    exp = new VarName(funID.position, funID.lexeme);
                } else {
                    exp = tmp;
                }
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
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.PTR, "null");
                break;
            case CONST_NONE:
                consntant = nextSymbol();
                exp = new AtomExpr(consntant.position, AtomExpr.AtomTypes.VOID, "none");
                break;
            case OPENING_PARENTHESIS:
                Symbol symPar = nextSymbol(); // shift opening parenthesis
                LinkedList<Expr> exprs = parseExpressions();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS)
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                Symbol symCloP = nextSymbol(); // shift closing parenthesis
                if(exprs.size() > 1)
                    exp = new Exprs(new Position(symPar, symCloP), exprs);
                else
                    exp = exprs.get(0);
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
                Symbol symEndif = nextSymbol(); // shift end
                exp = new IfExpr(new Position(symIf, symEndif), cond, thenExpr, elseExpr);
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
                Symbol symEndFor = nextSymbol(); // shift end
                exp = new ForExpr(new Position(symFor, symEndFor), new VarName(var.position, var.lexeme),loBound, hiBound, body);
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
                Symbol endWhile = nextSymbol(); // shift end
                exp = new WhileExpr(new Position(symWhile, endWhile), condWhile, bodyWhile);
                break;
            default:
                throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
        }
        endLog();
        return exp;
    }

    private Expr parseArgumentsOpt(Symbol name) {
        begLog("ArgumentsOpt");
        Expr expr = null;
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
                Symbol opPar = nextSymbol(); // shift opening parenthesis
//                LinkedList<Expr> tmp = parseExpressions();
//                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
//                    throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);
//                }
//                Symbol clPar = nextSymbol(); // shift closing parenthesis
//                expr = new FunCall(new Position(name, clPar), name.lexeme, tmp);
                expr = parseArgumentsOptPrime(name);
                break;
            default:
                throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);

        }
        endLog();
        return expr;
    }

    private Expr parseArgumentsOptPrime(Symbol name) {
        Expr expr = null;
        LinkedList<Expr> tmp;
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
//                nextSymbol(); // shift
                tmp = parseExpressions();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);
                }
                Symbol clPar = nextSymbol(); // shift closing parenthesis
                expr = new FunCall(new Position(name, clPar), name.lexeme, tmp);
                break;
            case CLOSING_PARENTHESIS:
                Symbol symb = nextSymbol(); // shift closing parenthesis
                tmp = new LinkedList<>();
                expr = new FunCall(new Position(name, symb), name.lexeme, tmp);
                break;
            default:
                throw new CompilerError("[syntax error, parseArgumentsOptPrime] invalid expression at " + laSymbol);
        }
        return expr;
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
                Type tmp = parseType();
                newTyp = new TypeDecl(new Position(typ, tmp), id.lexeme, tmp);
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
                    func = new FunDef(new Position(fun, body), symID.lexeme, pars, type, body);
                } else {
                    func = new FunDecl(new Position(fun, type), symID.lexeme, pars, type);
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
        LinkedList<ParDecl> decls = new LinkedList<ParDecl>();
        switch (laSymbol.token) {
            case IDENTIFIER:
                decls = parseParameters();
                break;
            case CLOSING_PARENTHESIS:
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
                Type type = parseType();
                decl = new ParDecl(new Position(symID, type), symID.lexeme, type);
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
                vardec = new VarDecl(new Position(var, typ), name.lexeme, typ);
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
                Symbol closBrack = nextSymbol(); // shift closing bracket
                Type elemType = parseType();
                typ = new ArrType(new Position(arr, elemType), size, elemType);
                break;
            case REC:
                Symbol rec = nextSymbol(); // shift REC
                if (laSymbol.token != Symbol.Token.OPENING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                nextSymbol(); // shift opening brace
                LinkedList<CompDecl> comps = parseComponents();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                Symbol closBrace = nextSymbol(); // shift closing brace
                typ = new RecType(new Position(rec, closBrace), comps);
                break;
            case PTR:
                symVal = nextSymbol();
                Type type = parseType();
                typ = new PtrType(new Position(symVal, type), type);
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
                Type type = parseType();
                comps = new CompDecl(new Position(symID, type), symID.lexeme, type);
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
