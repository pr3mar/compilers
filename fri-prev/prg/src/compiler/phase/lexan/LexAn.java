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
    private boolean escaped;

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
        } else {
            read = false;
        }
        while(currentChar == ((int)'#') || currentChar == ((int) ' ')
                || currentChar == ((int) '\n') || currentChar == ((int) '\r')
                    || currentChar == ((int) '\t')) {
            detectComment();
            detectWhiteSpace();
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
                if(isBetween(currentChar, 48, 57)) {
                    sym = getIntegers();
                } else if (currentChar == ((int) '_') ||
                            isBetween(currentChar, 48, 57) ||
                                isBetween(currentChar, 65, 90) ||
                                    isBetween(currentChar, 97, 122)){
                    sym = getIdentifiers();
                } else {
                    throw  new CompilerError("invalid character!");
                }
//                sym = new Symbol(Symbol.Token.ERROR, new Position(task.srcFName, endLine, endCol));

        }

//        if(currentChar == ((int) '\n')) {
//            endLine++;
//            endCol = 1;
//        } else {
            endCol++;
//        }

        return log(sym);
	}

    Symbol getIntegers() {
        lexeme = "";
        begLine = endLine; begCol = endCol;
        read = true;
        while(isBetween(currentChar, 48, 57)) {
            lexeme += (char) currentChar;
            endCol++;
            currentChar = readChar();
        }
        endCol--;
        try {
            long num = Long.parseLong(lexeme);
            return new Symbol(Symbol.Token.CONST_INTEGER, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
        } catch (NumberFormatException e) {
            throw new CompilerError("integer too big or too small");
        }
    }

    Symbol getIdentifiers() {
        lexeme = "";
        begLine = endLine; begCol = endCol;
        boolean underscore = false;
        boolean number = false;
        read = true;
        while(currentChar == ((int) '_') ||
                isBetween(currentChar, 48, 57) ||
                    isBetween(currentChar, 65, 90) ||
                        isBetween(currentChar, 97, 122)) {
            if(currentChar == ((char) '_')) underscore = true;
            if(isBetween(currentChar, 48, 57)) number = true;
            lexeme += (char) currentChar;
            endCol++;
            currentChar = readChar();
        }
        endCol--;
        if(underscore || number)
            return new Symbol(Symbol.Token.IDENTIFIER, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
        else {
            switch (lexeme) {
                // constants
                case "true":
                case "false":
                    return new Symbol(Symbol.Token.CONST_BOOLEAN, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "null":
                    return new Symbol(Symbol.Token.CONST_NULL, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "none":
                    return new Symbol(Symbol.Token.CONST_NONE, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));

                // type names
                case "integer":
                    return new Symbol(Symbol.Token.INTEGER, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "boolean":
                    return new Symbol(Symbol.Token.BOOLEAN, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "char":
                    return new Symbol(Symbol.Token.CHAR, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "string":
                    return new Symbol(Symbol.Token.STRING, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "void":
                    return new Symbol(Symbol.Token.VOID, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));

                // keywords
                case "arr":
                    return new Symbol(Symbol.Token.ARR, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "else":
                    return new Symbol(Symbol.Token.ELSE, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "end":
                    return new Symbol(Symbol.Token.END, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "for":
                    return new Symbol(Symbol.Token.FOR, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "fun":
                    return new Symbol(Symbol.Token.FUN, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "if":
                    return new Symbol(Symbol.Token.IF, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "then":
                    return new Symbol(Symbol.Token.THEN, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "ptr":
                    return new Symbol(Symbol.Token.PTR, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "rec":
                    return new Symbol(Symbol.Token.REC, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "typ":
                    return new Symbol(Symbol.Token.TYP, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "var":
                    return new Symbol(Symbol.Token.VAR, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "where":
                    return new Symbol(Symbol.Token.WHERE, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                case "while":
                    return new Symbol(Symbol.Token.WHILE, "", new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
                default:
                    return new Symbol(Symbol.Token.IDENTIFIER, lexeme, new Position(task.srcFName, begLine, begCol, task.srcFName, endLine, endCol));
            }
        }

    }


    boolean readNext() {
        nextChar = readChar();
        read = false;
        escaped = false;
        switch (mode) {
            case 1: // = ASSIGN or EQU?
                if(nextChar == ((int) '=')) {
                    begCol = endCol; begLine = endLine;
                    endCol++;
                    return true;
                }
                break;
            case 2: // > GTH or GEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol; begLine = endLine;
                    endCol++;
                    return true;
                }
                break;
            case 3: // < LTH or LEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol; begLine = endLine;
                    endCol++;
                    return true;
                }
                break;

            case 4: // ! NOT or NEQ?
                if(nextChar == ((int) '=')) {
                    begCol = endCol; begLine = endLine;
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
                begCol = endCol; begLine = endLine;
                endCol++;
                currentChar = nextChar;
                nextChar = readChar();
//                escaped = false;
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
                    if(!escaped)
                        lexeme = "\'" + ((char)currentChar) + "\'";
                    else
                        escaped = false;
                    return true;
                } else {
                    throw  new CompilerError("bad character constant definition [only 1 char long]");
                }
//                break;
            case 6: // string constant
                if(!isBetween(nextChar, 32, 126)) {
                    throw new CompilerError("bad string constant definition [invalid character(s)]");
                }
                begCol = endCol; begLine = endLine;
                if(nextChar == ((char) '\"')) {
                    endCol++;
                    lexeme = "\"\"";
                    return true;
                }
                lexeme = "\"";
                while(nextChar != ((char) '\"')) {
                    if (nextChar == ((int)'\'') || nextChar == ((int)'\"')) {
                        throw new CompilerError("bad string  constant definition [must escape \\, \' and \"]");
                    }
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

    private void detectWhiteSpace() {
        while(currentChar == ((int) ' ') || currentChar == ((int) '\n') ||
                currentChar == ((int) '\r') || currentChar == ((int) '\t')) {
            if(currentChar == ((int)'\n')) {
                endLine++;
                endCol = 1;
            } else
                endCol++;
            currentChar = readChar();
        }
    }

    private void detectComment() {
        if(currentChar != ((int)'#')) {
            return;
        }
        while(currentChar == ((int)'#')) {
            while (currentChar != '\n') {
                currentChar = readChar();
                if(currentChar == -1) {
                    throw new CompilerError("end of file detected");
                }
            }
            endLine++;
            endCol = 1;
            currentChar = readChar();
//            if(currentChar == -1) {
//                throw new CompilerError("end of file detected");
//            }
        }
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
                    escaped = true;
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
