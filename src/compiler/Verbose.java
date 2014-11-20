package compiler;

public abstract class Verbose {
    public static final boolean DEF_USE_GEN         = false;
    public static final boolean TREE_GEN            = false;
    
    public static final boolean INST_SEL            = false;
    public static final boolean MC_REP_GEN          = false;
    
    public static final boolean COMBINE_RET         = false;
    public static final boolean RECONSTRUCT_BB      = false;
    public static final boolean RETAIN_HLL_TYPE     = false;
    
    public static final boolean GEN_MOV_FOR_PHI     = false;
    public static final boolean DETECT_BACK_EDGE	= false;
    public static final boolean INST_NUMBERING      = false;
    public static final boolean ALLOC_PARAM_RET_REG = false;
    public static final boolean COMPUTE_INTERVAL    = false;
    public static final boolean REG_COALESC         = false;
    public static final boolean LINEAR_SCAN         = false;
    
    public static final boolean REPLACE_MEM_OP      = false;
    public static final boolean INSERT_SPILLING     = false;
    
    public static final boolean EXPAND_CALL_SEQ     = false;
    
    public static final boolean SIMPLE_BRANCH_ALIGN = false;
    public static final boolean MC_CLEANUP          = false;
    
    public static final boolean SPILL_CONSTANT      = false;
    public static final boolean CODE_EMIT           = false;
}