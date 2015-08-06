package uvm;

import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class OpCode {
    private OpCode() {}
    
    /*
     * reserve numbers under 0xF00 for non-terminals in BURG 
     */
    
    public static final int PSEUDO_ASSIGN = 0xF00;
    
    // int binop
    public static final int ADD     = 0xF01;
    public static final int SUB     = 0xF02;
    public static final int MUL     = 0xF03;
    public static final int SDIV    = 0xF04;
    public static final int SREM    = 0xF05;
    public static final int UDIV    = 0xF06;
    public static final int UREM    = 0xF07;
    public static final int SHL     = 0xF08;
    public static final int LSHR    = 0xF09;
    public static final int ASHR    = 0xF0A;
    public static final int AND     = 0xF0B;
    public static final int OR      = 0xF0C;
    public static final int XOR     = 0xF0D;
    
    // memory alloc
    public static final int ALLOCA          = 0xF10;
    public static final int NEW             = 0xF11;
    public static final int ALLOCAHYBRID    = 0xF12;
    public static final int NEWHYBRID       = 0xF13;
    public static final int NEWSTACK		= 0xF14;
    public static final int NEWTHREAD		= 0xF15;
    
    // int cmp
    public static final int EQ      = 0xF20;
    public static final int NE      = 0xF21;
    public static final int SGE     = 0xF22;
    public static final int SGT     = 0xF23;
    public static final int SLE     = 0xF24;
    public static final int SLT     = 0xF25;
    public static final int UGE     = 0xF26;
    public static final int UGT     = 0xF27;
    public static final int ULE     = 0xF28;
    public static final int ULT     = 0xF29;
    
    // conversion (int and fp)
    public static final int SEXT    = 0xF30;
    public static final int TRUNC   = 0xF31;
    public static final int ZEXT    = 0xF32;
    public static final int FPEXT   = 0xF33;
    public static final int FPTOSI  = 0xF34;
    public static final int FPTOUI  = 0xF35;
    public static final int FPTRUNC = 0xF36;
    public static final int SITOFP  = 0xF37;
    public static final int UITOFP  = 0xF38;
    public static final int BITCAST = 0xF39;
    
    // tagged ref op
    public static final int TRISFP  = 0xF50;
    public static final int TRISINT = 0xF51;
    public static final int TRISREF = 0xF52;
    public static final int TRTOFP  = 0xF53;
    public static final int TRTOINT = 0xF54;
    public static final int TRTOREF = 0xF55;
    
    // call
    public static final int CALL    = 0xF60;
    public static final int TAILCALL= 0xF61;
    public static final int CCALL	= 0xF62;
    
    // exception
    public static final int THROW   = 0xF70;
    public static final int LANDPAD = 0xF71;
    
    // aggregated values, refs
    public static final int IREFCAST        = 0xF84;
    public static final int GETIREF         = 0xF80;
    public static final int GETFIELD        = 0xF81;
    public static final int GETELEM_CONST   = 0xF82;
    public static final int SHIFTIREF       = 0xF83;
    public static final int LOADINT         = 0xF85;
    public static final int STORE           = 0xF86;
    public static final int GETELEM_VAR		= 0xF87;
    public static final int LOADDP			= 0xF88;
    
    // control flow
    public static final int BRANCH  = 0xF90;
    public static final int BRANCH2 = 0xF91;
    public static final int SWITCH  = 0xF92;
    
    // misc
    public static final int PHI     = 0xFA0;
    public static final int RET     = 0xFA1;
    public static final int RETVOID    = 0xFA2;
    public static final int SELECT  = 0xFA3;
    public static final int PARAM_GPR   = 0xFA4;
    public static final int PARAM_SP = 0xFA5;
    public static final int PARAM_DP = 0xFA6;
    public static final int RT_CALL    = 0xFA7;
    
    // fp binop
    public static final int FADD    = 0xFB0;
    public static final int FSUB    = 0xFB1;
    public static final int FMUL    = 0xFB2;
    public static final int FDIV    = 0xFB3;
    public static final int FREM    = 0xFB4;
    
    // fp cmp
    public static final int FALSE   = 0xFC0;
    public static final int TRUE    = 0xFC1;
    public static final int FUNO    = 0xFC2; // unordered
    public static final int FUEQ    = 0xFC3; // unordered or EQ
    public static final int FUNE    = 0xFC4; 
    public static final int FUGT    = 0xFC5; 
    public static final int FUGE    = 0xFC6;
    public static final int FULT    = 0xFC7;
    public static final int FULE    = 0xFC8;
    public static final int FORD    = 0xFC9; // ordered
    public static final int FOEQ    = 0xFCA; // ordered and EQ
    public static final int FONE    = 0xFCB;
    public static final int FOGT    = 0xFCC;
    public static final int FOGE    = 0xFCD;
    public static final int FOLT    = 0xFCE;
    public static final int FOLE    = 0xFCF;
    
    // internals to compiler (not exposed in uIR)
    public static final int IREF_OFFSET = 0xFD0;
    
    // non-op terms
    public static final int INT_IMM   = 0xFF0;
//    public static final int REG     = 0xFF1;
    public static final int LABEL   = 0xFF2;
    public static final int NOP     = 0xFFF;
    public static final int REG_I1  = 0xFF3;
    public static final int REG_I8  = 0xFF4;
    public static final int REG_I16 = 0xFF5;
    public static final int REG_I32 = 0xFF6;
    public static final int REG_I64 = 0xFF7;
    public static final int REG_SP  = 0xFF8;
    public static final int REG_DP  = 0xFF9;
    public static final int FP_SP_IMM = 0xFFA;
    public static final int FP_DP_IMM = 0xFFB;
    
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
