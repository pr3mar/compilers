package compiler.phase.synan;

import java.util.*;

import org.w3c.dom.*;

import compiler.*;
import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.phase.*;
import compiler.phase.lexan.*;
import sun.org.mozilla.javascript.ast.Assignment;

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
    public void synAn() {
        laSymbol = lexAn.lexAn();
        parseProgram();
        if (laSymbol.token != Symbol.Token.EOF)
            Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
    }

    // TODO

    // All these methods are a part of a recursive descent implementation of an
    // LL(1) parser.

    private void parseProgram() {
        begLog("Program");
//		parseExpression();
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
                parseExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parseProgram] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseExpression() {
        begLog("Expression");
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
                parseAssignmentExpression();
                parseExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseExpressionPrime() {
        begLog("ExpressionPrime");
        switch (laSymbol.token) {
            case WHERE:
                Symbol symVar = nextSymbol();
                parseDeclarations();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error] invalid expression at " + laSymbol);
                }
                Symbol symVal = nextSymbol();
                parseExpressionPrime();
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
    }

    private void parseExpressions() {
        begLog("Expressions");
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
                parseExpression();
                parseExpressionsPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseExpressions] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseExpressionsPrime() {
        begLog("ExpressionsPrime");
        switch (laSymbol.token) {
            case COMMA:
                Symbol symVar = nextSymbol();
                parseExpression();
                parseExpressionsPrime();
                break;
            case CLOSING_PARENTHESIS:
                break;
            default:
                throw new CompilerError("[syntax error, parseExpressionsPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseAssignmentExpression() {
        begLog("AssignmentExpression");
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
                parseDisjunctiveExpression();
                parseAssignmentExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseAssignmentExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseAssignmentExpressionPrime() {
        begLog("AssignmentExpressionPrime");
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
                Symbol symVal = nextSymbol();
                parseDisjunctiveExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parseAssignmentExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseDisjunctiveExpression() {
        begLog("DisjunctiveExpression");
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
                parseConjunctiveExpression();
                parseDisjunctiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseDisjunctiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseDisjunctiveExpressionPrime() {
        begLog("DisjunctiveExpressionPrime");
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
                Symbol symVal = nextSymbol();
                parseConjunctiveExpression();
                parseDisjunctiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseDisjunctiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseConjunctiveExpression() {
        begLog("ConjunctiveExpression");
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
                parseRelationalExpression();
                parseConjunctiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseConjunctiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseConjunctiveExpressionPrime() {
        begLog("ConjunctiveExpressionPrime");
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
                Symbol symVal = nextSymbol();
                parseRelationalExpression();
                parseConjunctiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error,  parseConjunctiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseRelationalExpression() {
        begLog("RelationalExpression");
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
                parseAdditiveExpression();
                parseRelationalExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseRelationalExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseRelationalExpressionPrime() {
        begLog("RelationalExpressionPrime");
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
            case NEQ:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
                Symbol symVal = nextSymbol();
                parseAdditiveExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parseRelationalExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseAdditiveExpression() {
        begLog("AdditiveExpression");
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
                parseMultiplicativeExpression();
                parseAdditiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseAdditiveExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseAdditiveExpressionPrime() {
        begLog("AdditiveExpressionPrime");
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
            case SUB:
                Symbol symVal = nextSymbol();
                parseMultiplicativeExpression();
                parseAdditiveExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseAdditiveExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseMultiplicativeExpression() {
        begLog("MultiplicativeExpression");
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
                parsePrefixExpression();
                parseMultiplicativeExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseMultiplicativeExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseMultiplicativeExpressionPrime() {
        begLog("MultiplicativeExpressionPrime");
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
            case DIV:
            case MOD:
                Symbol symVal = nextSymbol();
                parsePrefixExpression();
                parseMultiplicativeExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseMultiplicativeExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }


    private void parsePrefixExpression() {
        begLog("PrefixExpression");
        Symbol symVal;
        switch (laSymbol.token) {
            case ADD:
            case SUB:
            case NOT:
            case MEM:
                symVal = nextSymbol();
                parsePrefixExpression();
                break;
            case OPENING_BRACKET:
                symVal = nextSymbol();
                parseType();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePrefixExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parsePrefixExpression();
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
                parsePostfixExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parsePrefixExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parsePostfixExpression() {
        begLog("PostfixExpression");
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
                parseAtomicExpression();
                parsePostfixExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parsePostfixExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parsePostfixExpressionPrime() {
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
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);

                }
                symVal = nextSymbol();
                parsePostfixExpressionPrime();
                break;
            case DOT:
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parsePostfixExpressionPrime();
                break;
            case VAL:
                symVal = nextSymbol();
                parsePostfixExpressionPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parsePostfixExpressionPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseAtomicExpression() {
        begLog("AtomicExpression");
        Symbol symVal;
        switch (laSymbol.token) {
            case IDENTIFIER:
                symVal = nextSymbol();
                parseArgumentsOpt();
                break;
            case CONST_INTEGER:
            case CONST_BOOLEAN:
            case CONST_CHAR:
            case CONST_STRING:
            case CONST_NULL:
            case CONST_NONE:
                symVal = nextSymbol();
                break;
            case OPENING_PARENTHESIS:
                symVal = nextSymbol();
                parseExpressions();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS)
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                symVal = nextSymbol();
                break;
            case IF:
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.THEN) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.ELSE) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                break;
            case FOR:
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.ASSIGN) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.COMMA) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                break;
            case WHILE:
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.END) {
                    throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                break;
            default:
                throw new CompilerError("[syntax error, parseAtomicExpression] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseArgumentsOpt() {
        begLog("Expression");
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
                Symbol symVal = nextSymbol();
                parseExpressions();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                break;
            default:
                throw new CompilerError("[syntax error, parseArgumentsOpt] invalid expression at " + laSymbol);

        }
        endLog();
    }

    private void parseDeclarations() {
        begLog("Declarations");
        switch (laSymbol.token) {
            case TYP:
            case FUN:
            case VAR:
                parseDeclaration();
                parseDeclarationsPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclarations] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseDeclarationsPrime() {
        begLog("DeclarationsPrime");
        switch (laSymbol.token) {
            case TYP:
            case FUN:
            case VAR:
                parseDeclaration();
                parseDeclarationsPrime();
                break;
            case END:
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclarationsPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseDeclaration() {
        begLog("DeclarationsPrime");
        switch (laSymbol.token) {
            case TYP:
                parseTypeDeclaration();
                break;
            case FUN:
                parseFunctionDeclaration();
                break;
            case VAR:
                parseVariableDeclaration();
                break;
            default:
                throw new CompilerError("[syntax error, parseDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseTypeDeclaration() {
        begLog("TypeDeclaration");
        switch (laSymbol.token) {
            case TYP:
                Symbol symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                break;
            default:
                throw new CompilerError("[syntax error, parseTypeDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseFunctionDeclaration() {
        begLog("FunctionDeclaration");
        switch (laSymbol.token) {
            case FUN:
                Symbol symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.OPENING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseParametersOpt();
                if (laSymbol.token != Symbol.Token.CLOSING_PARENTHESIS) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                parseFunctionBodyOpt();
                break;
            default:
                throw new CompilerError("[syntax error, parseFunctionDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseParametersOpt() {
        begLog("ParametersOpt");
        switch (laSymbol.token) {
            case IDENTIFIER:
                parseParameters();
                break;
            case CLOSING_PARENTHESIS:
                break;
            default:
                throw new CompilerError("[syntax error, parseParametersOpt] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseParameters() {
        begLog("Parameters");
        switch (laSymbol.token) {
            case IDENTIFIER:
                parseParameter();
                parseParametersPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseParameters] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseParametersPrime() {
        begLog("ParametersPrime");
        switch (laSymbol.token) {
            case COMMA:
                Symbol symVal = nextSymbol();
                parseParameter();
                parseParametersPrime();
                break;
            case CLOSING_PARENTHESIS:
                break;
            default:
                throw new CompilerError("[syntax error, parseParametersPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseParameter() {
        begLog("Parameter");
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseParameter] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                break;
            default:
                throw new CompilerError("[syntax error, parseParameter] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseFunctionBodyOpt() {
        begLog("FunctionBodyOpt");
        Symbol symVal;
        switch (laSymbol.token) {
            case END:
            case TYP:
            case FUN:
            case VAR:
                break;
            case ASSIGN:
                symVal = nextSymbol();
                parseExpression();
                break;
            default:
                throw new CompilerError("[syntax error, parseFunctionBodyOpt] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseVariableDeclaration() {
        begLog("VariableDeclaration");
        switch (laSymbol.token) {
            case VAR: {
                Symbol symVal = nextSymbol();
//                Symbol symId;
                if (laSymbol.token != Symbol.Token.IDENTIFIER) {
                    throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                break;
            }
            default:
                throw new CompilerError("[syntax error, parseVariableDeclaration] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseType() {
        begLog("Type");
        Symbol symVal;
        switch (laSymbol.token) {
            case IDENTIFIER:
            case INTEGER:
            case BOOLEAN:
            case CHAR:
            case STRING:
            case VOID:
                symVal = nextSymbol();
                break;
            case ARR:
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.OPENING_BRACKET) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseExpression();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACKET) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                break;
            case REC:
                symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.OPENING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseComponents();
                if (laSymbol.token != Symbol.Token.CLOSING_BRACE) {
                    throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                break;
            case PTR:
                symVal = nextSymbol();
                parseType();
                break;
            default:
                throw new CompilerError("[syntax error, parseType] invalid expression at " + laSymbol);
        }
        endLog();
    }


    private void parseComponents() {
        begLog("Components");
        switch (laSymbol.token) {
            case IDENTIFIER:
                parseComponent();
                parseComponentsPrime();
                break;
            default:
                throw new CompilerError("[syntax error, parseComponents] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseComponentsPrime() {
        begLog("ComponentsPrime");
        switch (laSymbol.token) {
            case COMMA:
                Symbol symVal = nextSymbol();
                parseComponent();
                parseComponentsPrime();
                break;
            case CLOSING_BRACE:
                break;
            default:
                throw new CompilerError("[syntax error, parseComponentsPrime] invalid expression at " + laSymbol);
        }
        endLog();
    }

    private void parseComponent() {
        begLog("Component");
        switch (laSymbol.token) {
            case IDENTIFIER:
                Symbol symVal = nextSymbol();
                if (laSymbol.token != Symbol.Token.COLON) {
                    throw new CompilerError("[syntax error, parseComponent] invalid expression at " + laSymbol);
                }
                symVal = nextSymbol();
                parseType();
                break;
            default:
                throw new CompilerError("[syntax error, parseComponent] invalid expression at " + laSymbol);
        }
        endLog();
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
