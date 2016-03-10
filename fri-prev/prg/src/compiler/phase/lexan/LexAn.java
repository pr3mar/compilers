package compiler.phase.lexan;

import java.io.*;

import compiler.*;
import compiler.common.report.*;
import compiler.phase.*;

/**
 * The lexical analyzer.
 * 
 * @author sliva
 */
public class LexAn extends Phase {

	/** The source file. */
	private FileReader srcFile;

    private int mode;
    private int begCol;
    private int endCol;
    private int begLine;
    private int endLine;
    private int currentChar;
    private int nextChar;
    private boolean read;
    private String lexeme;

	/**
	 * Constructs a new lexical analyzer.
	 * 
	 * Opens the source file and prepares the buffer. If logging is requested,
	 * sets up the logger.
	 * 
	 * @param task.srcFName
	 *            The name of the source file name.
	 */
	public LexAn(Task task) {
		super(task, "lexan");

		// Open the source file.
		try {
			srcFile = new FileReader(this.task.srcFName);
		} catch (FileNotFoundException ex) {
			throw new CompilerError("Source file '" + this.task.srcFName + "' not found.");
		}
        begCol = 1;
        begLine = 1;
        endLine = 1;
        endCol = 1;
        lexeme = "";
	}

	/**
	 * Terminates lexical analysis. Closes the source file and, if logging has
	 * been requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		// Close the source file.
		if (srcFile != null) {
			try {
				srcFile.close();
			} catch (IOException ex) {
				Report.warning("Source file '" + task.srcFName + "' cannot be closed.");
			}
		}
		super.close();
	}

	/**
	 * Returns the next lexical symbol from the source file.
	 * 
	 * @return The next lexical symbol.
	 */
	public Symbol lexAn() {
		// TODO
		// glavna metoda, ki jo moram dopolniti!
		// return log(symobl)
        if(!read) {
            currentChar = readChar();
            /*try {
                currentChar = srcFile.read();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        } else {
            read = false;
        }

        Symbol sym;

        switch (currentChar) {
            case '+':
                sym = new Symbol(Symbol.Token.ADD, new Position(task.srcFName, endLine, endCol));
                break;
            case '&':
                sym = new Symbol(Symbol.Token.AND, new Position(task.srcFName, endLine, endCol));
                break;
            case '=':
                mode = 1; // = or ==
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.ASSIGN, new Position(task.srcFName, endLine, endCol));
                } else {
                    sym = new Symbol(Symbol.Token.EQU, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                }
                break;
            case ':':
                sym = new Symbol(Symbol.Token.COLON, new Position(task.srcFName, endLine, endCol));
                break;
            case ',':
                sym = new Symbol(Symbol.Token.COMMA, new Position(task.srcFName, endLine, endCol));
                break;
            case '}':
                sym = new Symbol(Symbol.Token.CLOSING_BRACE, new Position(task.srcFName, endLine, endCol));
                break;
            case ']':
                sym = new Symbol(Symbol.Token.CLOSING_BRACKET, new Position(task.srcFName, endLine, endCol));
                break;
            case ')':
                sym = new Symbol(Symbol.Token.CLOSING_PARENTHESIS, new Position(task.srcFName, endLine, endCol));
                break;
            case '.':
                sym = new Symbol(Symbol.Token.DOT, new Position(task.srcFName, endLine, endCol));
                break;
            case '/':
                sym = new Symbol(Symbol.Token.DIV, new Position(task.srcFName, endLine, endCol));
                break;
            case '>':
                mode = 2; // > or >=
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.GTH, new Position(task.srcFName, endLine, endCol));
                } else {
                    sym = new Symbol(Symbol.Token.GEQ, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                }
                break;
            case '<':
                mode = 3; // < or <=
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.LTH, new Position(task.srcFName, endLine, endCol));
                } else {
                    sym = new Symbol(Symbol.Token.LEQ, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                }
                break;
            case '@':
                sym = new Symbol(Symbol.Token.MEM, new Position(task.srcFName, endLine, endCol));
                break;
            case '%':
                sym = new Symbol(Symbol.Token.MOD, new Position(task.srcFName, endLine, endCol));
                break;
            case '*':
                sym = new Symbol(Symbol.Token.MUL, new Position(task.srcFName, endLine, endCol));
                break;
            case '!':
                mode = 4; // ! or !=
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.NOT, new Position(task.srcFName, endLine, endCol));
                } else {
                    sym = new Symbol(Symbol.Token.NEQ, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                }
                break;
            case '{':
                sym = new Symbol(Symbol.Token.OPENING_BRACE, new Position(task.srcFName, endLine, endCol));
                break;
            case '[':
                sym = new Symbol(Symbol.Token.OPENING_BRACKET, new Position(task.srcFName, endLine, endCol));
                break;
            case '(':
                sym = new Symbol(Symbol.Token.OPENING_PARENTHESIS, new Position(task.srcFName, endLine, endCol));
                break;
            case '|':
                sym = new Symbol(Symbol.Token.OR, new Position(task.srcFName, endLine, endCol));
                break;
            case '-':
                sym = new Symbol(Symbol.Token.SUB, new Position(task.srcFName, endLine, endCol));
                break;
            case '^':
                sym = new Symbol(Symbol.Token.VAL, new Position(task.srcFName, endLine, endCol));
                break;
            case -1:
                sym = new Symbol(Symbol.Token.EOF, new Position(task.srcFName, endLine, endCol));
                break;
            case '\'':
                mode = 5; // char constant
                if(readNext()) {
                    sym = new Symbol(Symbol.Token.CONST_CHAR, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                } else {
                    throw new CompilerError("bad character constant definition");
                }
                break;
            case '\"':
                mode = 6; // string constant
                if(readNext()) {
                    sym = new Symbol(Symbol.Token.CONST_STRING, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                } else {
                    throw new CompilerError("bad string constant definition");
                }
                break;
            default:
                sym = new Symbol(Symbol.Token.ERROR, new Position(task.srcFName, endLine, endCol));
        }

        if(currentChar == ((int) '\n')) {
            endLine++;
            endCol = 0;
        } else {
            endCol++;
        }

        return log(sym);
	}


    boolean readNext() {
        nextChar = readChar();
        read = false;
        switch (mode) {
            case 1: // = ASSIGN or EQU?
                if(nextChar == ((int) '=')) {
                    begCol = endCol;
                    endCol++;
                    return true;
                }
                break;
            case 2: // > GTH or GEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol;
                    endCol++;
                    return true;
                }
                break;
            case 3: // < LTH or LEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol;
                    endCol++;
                    return true;
                }
                break;

            case 4: // ! NOT or NEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol;
                    endCol++;
                    return true;
                }
                break;
            case 5: // char constant
                if(!isBetween(nextChar, 32, 126)) {
                    throw new CompilerError("bad character constant definition [invalid character]");
                }
                if (nextChar == ((int)'\'') || nextChar == ((int)'\"')) {
                    throw new CompilerError("bad char constant definition [must escape \\, \' and \"]");
                }
                begCol = endCol;
                endCol++;
                currentChar = nextChar;
                nextChar = readChar();
                escapeChar();
//                if(currentChar == ((int) '\\')) {
//                    switch (nextChar) {
//                        case 'n':
//                        case 't':
//                        case '\\':
//                        case '\'':
//                        case '\"':
//                            lexeme = "'\\" + nextChar + "'";
//                            break;
//                        default:
//                            throw new CompilerError("bad character constant definition [invalid character is escaped]");
//                    }
//                    currentChar = nextChar;
//                    endCol++;
//                    nextChar = readChar();
//                }
                if(nextChar == ((int)'\'')) {
                    endCol++;
                    lexeme = "\'" + ((char)currentChar) + "\'";
                    return true;
                } else {
                    throw  new CompilerError("bad character constant definition [only 1 char long]");
                }
//                break;
            case 6: // string constant
                if(!isBetween(nextChar, 32, 126)) {
                    throw new CompilerError("bad string constant definition [invalid character(s)]");
                }
                begCol = endCol;
                if(nextChar == ((char) '\"')) {
                    endCol++;
                    lexeme = "\"\"";
                    return true;
                }
                lexeme = "\"";
                while(nextChar != ((char) '\"')) {
                    if(!isBetween(nextChar, 32, 126)) {
                        throw new CompilerError("bad string constant definition [invalid character(s)]");
                    }
                    currentChar = nextChar;
                    endCol++;
                    nextChar = readChar();
                    escapeChar();
//                    lexeme += "" + (char)currentChar;
                }
                lexeme += "\"";
                if(nextChar == ((int)'\"')) {
                    endCol++;
                    return true;
                } else {
                    throw  new CompilerError("bad character constant definition [only 1 char long]");
                }
//                break;
        }
        read = true;
        currentChar = nextChar;
        return false;
    }


    private int readChar() {
        int tmp = -1;
        try {
            tmp = srcFile.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }


    private static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    private void escapeChar() {
        if(currentChar == ((int) '\\')) {
            switch (nextChar) {
                case 'n':
                case 't':
                case '\\':
                case '\'':
                case '\"':
                    if(mode == 5)
                        lexeme = "'\\" + ((char)nextChar) + "'";
                    if(mode == 6)
                        lexeme += "\\" + ((char)nextChar);
                    break;
                default:
                    throw new CompilerError("bad character constant definition [invalid character is escaped]");
            }
            currentChar = nextChar;
            endCol++;
            nextChar = readChar();
        } else if(mode == 6) { // string constant
            lexeme += "" + ((char)currentChar);
        }
    }



	/**
	 * Prints out the symbol and returns it.
	 * 
	 * This method should be called by the lexical analyzer before it returns a
	 * symbol so that the symbol can be logged (even if logging of lexical
	 * analysis has not been requested).
	 * 
	 * @param symbol
	 *            The symbol to be printed out.
	 * @return The symbol received as an argument.
	 */
	private Symbol log(Symbol symbol) {
		symbol.log(logger);
		return symbol;
	}

}
