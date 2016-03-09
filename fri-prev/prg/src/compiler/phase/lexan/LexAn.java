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
    private int col;
    private int line;
    private int currentChar;
    private int nextChar;
    private boolean read;

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

        line = 1;
        col = 1;
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
            try {
                currentChar = srcFile.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            read = false;
        }

        Symbol sym;

        switch (currentChar) {
            case '+':
                sym = new Symbol(Symbol.Token.ADD, new Position(task.srcFName, line, col));
                break;
            case '&':
                sym = new Symbol(Symbol.Token.AND, new Position(task.srcFName, line, col));
                break;
            /* TODO add = and == */
            case '=':
                mode = 1;
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.ASSIGN, new Position(task.srcFName, line, col));
                } else {
                    sym = new Symbol(Symbol.Token.EQU, new Position(task.srcFName, line, col));
                }
                break;
            case ':':
                sym = new Symbol(Symbol.Token.COLON, new Position(task.srcFName, line, col));
                break;
            case ',':
                sym = new Symbol(Symbol.Token.COMMA, new Position(task.srcFName, line, col));
                break;
            case '}':
                sym = new Symbol(Symbol.Token.CLOSING_BRACE, new Position(task.srcFName, line, col));
                break;
            case ']':
                sym = new Symbol(Symbol.Token.CLOSING_BRACKET, new Position(task.srcFName, line, col));
                break;
            case ')':
                sym = new Symbol(Symbol.Token.CLOSING_PARENTHESIS, new Position(task.srcFName, line, col));
                break;
            case '.':
                sym = new Symbol(Symbol.Token.DOT, new Position(task.srcFName, line, col));
                break;
            case '>':
                mode = 2;
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.GTH, new Position(task.srcFName, line, col));
                } else {
                    sym = new Symbol(Symbol.Token.GEQ, new Position(task.srcFName, line, col));
                }
                break;
            case '<':
                mode = 3;
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.LTH, new Position(task.srcFName, line, col));
                } else {
                    sym = new Symbol(Symbol.Token.LEQ, new Position(task.srcFName, line, col));
                }
                break;
            case '@':
                sym = new Symbol(Symbol.Token.MEM, new Position(task.srcFName, line, col));
                break;
            case '%':
                sym = new Symbol(Symbol.Token.MOD, new Position(task.srcFName, line, col));
                break;
            case '*':
                sym = new Symbol(Symbol.Token.MUL, new Position(task.srcFName, line, col));
                break;
            case '!':
                mode = 4;
                if(!readNext()) {
                    sym = new Symbol(Symbol.Token.NOT, new Position(task.srcFName, line, col));
                } else {
                    sym = new Symbol(Symbol.Token.NEQ, new Position(task.srcFName, line, col));
                }
                break;
            case '{':
                sym = new Symbol(Symbol.Token.OPENING_BRACE, new Position(task.srcFName, line, col));
                break;
            case '[':
                sym = new Symbol(Symbol.Token.OPENING_BRACKET, new Position(task.srcFName, line, col));
                break;
            case '(':
                sym = new Symbol(Symbol.Token.OPENING_PARENTHESIS, new Position(task.srcFName, line, col));
                break;
            case '|':
                sym = new Symbol(Symbol.Token.OR, new Position(task.srcFName, line, col));
                break;
            case '-':
                sym = new Symbol(Symbol.Token.SUB, new Position(task.srcFName, line, col));
                break;
            case '^':
                sym = new Symbol(Symbol.Token.VAL, new Position(task.srcFName, line, col));
                break;
            case -1:
                sym = new Symbol(Symbol.Token.EOF, new Position(task.srcFName, line, col));
                break;
            default:
                sym = new Symbol(Symbol.Token.ERROR, new Position(task.srcFName, line, col));
        }

        if(currentChar == ((int) '\n')) {
            line++;
            col = 0;
        } else {
            col++;
        }

        return log(sym);
	}


    boolean readNext() {
        try {
            nextChar = srcFile.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        read = false;
        switch (mode) {
            case 1: // = ASSIGN or EQU?
                if(nextChar == ((int) '='))
                    return true;
                break;
            case 2: // > GTH or GEQ?
                if(nextChar == ((int) '='))
                    return true;
                break;
            case 3: // < LTH or LEQ?
                if(nextChar == ((int) '='))
                    return true;
                break;

            case 4: // ! NOT or NEQ?
                if(nextChar == ((int) '='))
                    return true;
                break;


        }
        read = true;
        currentChar = nextChar;
        if(currentChar == ((int) '\n')) {
            line++;
            col = 0;
        } else {
            col++;
        }
        return false;
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
