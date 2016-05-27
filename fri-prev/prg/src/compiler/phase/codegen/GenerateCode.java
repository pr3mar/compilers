package compiler.phase.codegen;

import compiler.common.report.CompilerError;
import compiler.data.cod.*;
import compiler.data.cod.imcVisitor.IMCFullVIsitor;
import compiler.data.cod.wrapper.FragmentCode;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.TEMP;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import compiler.data.imc.*;


/**
 * Visit the intermediate code and generate MMIX code
 * Created by pr3mar on 5/19/16.
 */
public class GenerateCode extends IMCFullVIsitor {

    private final int BYTE_SIZE = 8;
    private final int WIDTH_BYTES = 8;
    private final int NUM_REGS = 8;


    private CodeFragment fragment;
    private HashMap<String, Fragment> allFrags;
    private LinkedList<Expression> code;
    private Stack<TEMP> result;
    private HashMap<TEMP, String> mapping;
    private TEMP RV;
    private String FP;

    public GenerateCode(CodeFragment fragment, HashMap<String, Fragment> allFrags) {
        this.fragment = fragment;
        this.allFrags = allFrags;
        this.result = new Stack<>();
        this.mapping = new HashMap<>();
        this.code = new LinkedList<>();
        this.FP = "FP";
        this.RV = new TEMP(this.fragment.RV);
        this.mapping.put(this.RV, this.RV.toString());
//        this.SP = new TEMP(-1);
//        this.mapping.put(this.SP, "SP");
    }

    private long[] devideConst(Long val) {
        long[] ret = new long[4];
        long mask = 0xFFFFL;
        ret[0] = (val & mask);
        ret[1] = (val & (mask << 2 * BYTE_SIZE)) >> 2 * BYTE_SIZE;
        ret[2] = (val & (mask << 4 * BYTE_SIZE)) >> 4 * BYTE_SIZE;
        ret[3] = ((val & (mask << 6 * BYTE_SIZE)) >> 6 * BYTE_SIZE) & mask;
        return ret;
    }

    private TEMP newTEMP() {
        int tempName = TEMP.newTempName();
        if(tempName == this.fragment.FP || tempName == this.fragment.RV)
            tempName = TEMP.newTempName();
        TEMP ret = new TEMP(tempName);
        this.mapping.put(ret, ret.toString());
        return ret;
    }

    public FragmentCode get() {
        return new FragmentCode(this.fragment, this.code, this.mapping);
    }

    public void generate() {
        for(int i = 0; i < this.fragment.linCode.numStmts(); i++) {
            this.fragment.linCode.stmts(i).accept(this);
            this.result = new Stack<>();
        }
        Expression exp = this.code.get(this.code.size() - 1);//.getResult();
        if(exp.getResult() != null)
            this.code.add(new ADD(this.RV, exp.getResult(), 0));
        this.code.add(new SWYM());
    }

    @Override
    public void visit(BINOP binop) {
        binop.expr1.accept(this);
        binop.expr2.accept(this);
        TEMP left = this.result.pop();
        TEMP right = this.result.pop();
        TEMP res = newTEMP();
        switch (binop.oper) {
            /* arithmetical operations */
            case ADD:
                this.code.add(new ADD(res, left, right));
                break;
            case SUB:
                this.code.add(new SUB(res, left, right));
                break;
            case MUL:
                this.code.add(new MUL(res, left, right));
                break;
            case DIV:
                this.code.add(new DIV(res, left, right));
                break;
            case MOD:
                this.code.add(new DIV(res, left, right));
                this.code.add(new GET(res, "$rR"));
                break;

            /* logical operations */
            case AND:
                this.code.add(new AND(res, left, right));
                break;
            case OR:
                this.code.add(new OR(res, left, right));
                break;
            case EQU:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSZ(res, res, 1));
                break;
            case NEQ:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSNZ(res, res, 1));
                break;
            case GEQ:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSNN(res, res, 1));
                break;
            case GTH:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSP(res, res, 1));
                break;
            case LEQ:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSNP(res, res, 1));
                break;
            case LTH:
                this.code.add(new CMP(res, left, right));
                this.code.add(new ZSN(res, res, 1));
                break;
        }
        this.result.push(res);
    }

    @Override
    public void visit(CALL call) {
//        TEMP SP = new TEMP(this.fragment.RV);
//        if(!this.mapping.containsKey(SP))
//            this.mapping.put(SP, "SP");
        TEMP res;
        for(int i = 0; i < call.numArgs(); i++) {
            call.args(i).accept(this);
            res = this.result.pop();
            this.code.add(new STO(res, "SP", i * WIDTH_BYTES)); // may be a difficulty??
        }
        this.code.add(new PUSHJ(NUM_REGS, call.label));
        res = newTEMP();
        this.code.add(new LDO(res, "SP", 0)); // may be a difficulty??
        this.result.push(res);
    }

    @Override
    public void visit(CJUMP cjump) {
        cjump.cond.accept(this);
        TEMP res = this.result.pop();
        this.code.add(new BP(res, cjump.posLabel));
        this.code.add(new JMP(cjump.negLabel));
    }

    @Override
    public void visit(CONST constant) {
        TEMP res = newTEMP();
        long[] bytes = devideConst(constant.value);
        int noInstr = 3;
        for(; noInstr > 0; noInstr--) {
            if(bytes[noInstr] != 0) {
                break;
            }
        }
        switch (noInstr) {
            case 0:
                this.code.add(new SETL(res, bytes[0]));
                break;
            case 1:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new INCML(res, bytes[1]));
                break;
            case 2:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new INCML(res, bytes[1]));
                this.code.add(new INCMH(res, bytes[2]));
                break;
            case 3:
                this.code.add(new SETL(res, bytes[0]));
                this.code.add(new INCML(res, bytes[1]));
                this.code.add(new INCMH(res, bytes[2]));
                this.code.add(new INCH(res, bytes[3]));
                break;
            default:
                throw new CompilerError("[codegen] something funny is happening at code for constant");
        }
        this.result.push(res);
    }

    @Override
    public void visit(ESTMT estmt) {
        estmt.expr.accept(this);
    }

    @Override
    public void visit(JUMP jump) {
        this.code.add(new JMP(jump.label));
    }

    @Override
    public void visit(LABEL label) {
        this.code.add(new LAB(label.label));
    }

    @Override
    public void visit(MEM mem) {
        TEMP res = newTEMP();
        if( mem.addr instanceof NAME) {
            TEMP tmp = newTEMP();
            this.code.add(new LDA(tmp, ((NAME)mem.addr).name));
            this.code.add(new LDO(res, tmp, 0));
        } else {
            mem.addr.accept(this);
            TEMP op = this.result.pop();
            this.code.add(new LDO(res, op, 0));
        }
        this.result.push(res);
    }

    @Override
    public void visit(MOVE move) {
        boolean mem = false;
        if(move.dst instanceof MEM) {
            ((MEM) move.dst).addr.accept(this);
            mem = true;
        } else {
            move.dst.accept(this);
        }
        move.src.accept(this);
        if(this.result.size() == 1) {
            return;
        }
        TEMP src = this.result.pop();
        TEMP dst = this.result.pop();
        if(mem) {
            this.code.add(new STO(src, dst, 0));
        } else {
            this.code.add(new ADD(dst, src, 0));
        }
    }

    @Override
    public void visit(NAME name) {
        TEMP res = newTEMP();
        this.code.add(new GETA(res, name.name));
        this.result.push(res);
    }

    @Override
    public void visit(NOP nop) {
        // do nothing
    }

    @Override
    public void visit(SEXPR sexpr) {
        sexpr.stmt.accept(this);
        sexpr.expr.accept(this);
    }

    @Override
    public void visit(STMTS stmts) {
        for(int i = 0; i < stmts.numStmts(); i++)
            stmts.stmts(i).accept(this);
    }

    @Override
    public void visit(TEMP temp) {
        if(this.fragment.FP == temp.name)
            return;
        if(!this.mapping.containsKey(temp)) {
            this.mapping.put(temp, temp.toString());
            this.result.push(temp);
        }
    }

    @Override
    public void visit(UNOP unop) {
        unop.expr.accept(this);
        TEMP operand = this.result.pop();
        TEMP res = null;
        switch (unop.oper) {
            case ADD:
                res = operand;
                break;
            case NOT:
                res = newTEMP(); // ZSP ili XOR
                this.code.add(new ZSP(res, operand, 1));
                break;
            case SUB:
                res = newTEMP();
                this.code.add(new NEG(res, 0, operand));
                break;
        }
        this.result.push(res);
    }

}
