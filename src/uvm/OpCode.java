package uvm;

import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class OpCode {
    private OpCode() {}
    
    public static final int PSEUDO_ASSIGN = 0x00;
    
    // int binop
    public static final int ADD     = 0x01;
    public static final int SUB     = 0x02;
    public static final int MUL     = 0x03;
    public static final int SDIV    = 0x04;
    public static final int SREM    = 0x05;
    public static final int UDIV    = 0x06;
    public static final int UREM    = 0x07;
    public static final int SHL     = 0x08;
    public static final int LSHR    = 0x09;
    public static final int ASHR    = 0x0A;
    public static final int AND     = 0x0B;
    public static final int OR      = 0x0C;
    public static final int XOR     = 0x0D;
    
    // fp binop
    public static final int FADD    = 0xB0;
    public static final int FSUB    = 0xB1;
    public static final int FMUL    = 0xB2;
    public static final int FDIV    = 0xB3;
    public static final int FREM    = 0xB4;
    
    // int cmp
    public static final int EQ      = 0x20;
    public static final int NE      = 0x21;
    public static final int SGE     = 0x22;
    public static final int SGT     = 0x23;
    public static final int SLE     = 0x24;
    public static final int SLT     = 0x25;
    public static final int UGE     = 0x26;
    public static final int UGT     = 0x27;
    public static final int ULE     = 0x28;
    public static final int ULT     = 0x29;
    
    // fp cmp
    public static final int FALSE   = 0xC0;
    public static final int TRUE    = 0xC1;
    public static final int FUNO    = 0xC2; // unordered
    public static final int FUEQ    = 0xC3; // unordered or EQ
    public static final int FUNE    = 0xC4; 
    public static final int FUGT    = 0xC5; 
    public static final int FUGE    = 0xC6;
    public static final int FULT    = 0xC7;
    public static final int FULE    = 0xC8;
    public static final int FORD    = 0xC9; // ordered
    public static final int FOEQ    = 0xCA; // ordered and EQ
    public static final int FONE    = 0xCB;
    public static final int FOGT    = 0xCC;
    public static final int FOGE    = 0xCD;
    public static final int FOLT    = 0xCE;
    public static final int FOLE    = 0xCF;
    
    // conversion (int and fp)
    public static final int SEXT    = 0x30;
    public static final int TRUNC   = 0x31;
    public static final int ZEXT    = 0x32;
    public static final int FPEXT   = 0x33;
    public static final int FPTOSI  = 0x34;
    public static final int FPTOUI  = 0x35;
    public static final int FPTRUNC = 0x36;
    public static final int SITOFP  = 0x37;
    public static final int UITOFP  = 0x38;
    public static final int BITCAST = 0x39;
    
    // tagged ref op
    public static final int TRISFP  = 0x50;
    public static final int TRISINT = 0x51;
    public static final int TRISREF = 0x52;
    public static final int TRTOFP  = 0x53;
    public static final int TRTOINT = 0x54;
    public static final int TRTOREF = 0x55;
    
    // call
    public static final int CALL    = 0x60;
    public static final int TAILCALL= 0x61;
    public static final int CCALL	= 0x62;
    
    // exception
    public static final int THROW   = 0x70;
    public static final int LANDPAD = 0x71;
    
    // memory alloc
    public static final int ALLOCA          = 0x10;
    public static final int NEW             = 0x11;
    public static final int ALLOCAHYBRID    = 0x12;
    public static final int NEWHYBRID       = 0x13;
    
    // aggregated values, refs
    public static final int IREFCAST        = 0x84;
    public static final int GETIREF         = 0x80;
    public static final int GETFIELD        = 0x81;
    public static final int GETELEM         = 0x82;
    public static final int SHIFTIREF       = 0x83;
    public static final int LOAD            = 0x85;
    public static final int STORE           = 0x86;
    
    // control flow
    public static final int BRANCH  = 0x90;
    public static final int BRANCH2 = 0x91;
    public static final int SWITCH  = 0x92;
    
    // misc
    public static final int PHI     = 0xA0;
    public static final int RET     = 0xA1;
    public static final int RET2    = 0xA2;
    public static final int SELECT  = 0xA3;
    public static final int PARAM   = 0xA4;
    
//    public static final int PUSH_STACK = 0xA5;
//    public static final int POP_STACK  = 0xA6;
    public static final int RT_CALL    = 0xA7;
    
    // non-op terms
    public static final int INT_IMM   = 0xF0;
//    public static final int REG     = 0xF1;
    public static final int LABEL   = 0xF2;
    public static final int NOP     = 0xFF;
    public static final int REG_I1  = 0xF3;
    public static final int REG_I8  = 0xF4;
    public static final int REG_I16 = 0xF5;
    public static final int REG_I32 = 0xF6;
    public static final int REG_I64 = 0xF7;
    public static final int REG_SP  = 0xF8;
    public static final int REG_DP  = 0xF9;
    public static final int FP_SP_IMM = 0xFA;
    public static final int FP_DP_IMM = 0xFB;
    
    public static final HashMap<Integer, String> names = new HashMap<Integer, String>();
    
    static {
        for (Field f : OpCode.class.getFields()) {
            if (f.getType().isPrimitive())
                try {
                    names.put(f.getInt(null), f.getName());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    System.exit(2);
                } catch (IllegalAccessException e) {
                    System.exit(2);
                }
        }
    }
    
    public static String getOpName(int op) {
        return names.get(op);
    }
}
