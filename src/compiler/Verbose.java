package compiler;

public abstract class Verbose {
    /*
     *  generating IR tree
     */
    public static final boolean DEF_USE_GEN         = false;
    public static final boolean TREE_GEN            = true;
    public static final boolean EXPAND_RT_SERVICE	= true;
    
    /*
     *  instruction selection (use BURM)
     */
    public static final boolean INST_SEL            = true;
    public static final boolean MC_REP_GEN          = true;
    
    /*
     *  mc code transform
     */
    public static final boolean COMBINE_RET         = false;
    public static final boolean RECONSTRUCT_BB      = false;
    public static final boolean RETAIN_HLL_TYPE     = false;
    public static final boolean ADD_CALL_REG_ARG	= true;
    
    /*
     *  register allocation
     */
    public static final boolean GEN_MOV_FOR_PHI     = false;
    public static final boolean DETECT_BACK_EDGE	= false;
    public static final boolean INST_NUMBERING      = false;
    public static final boolean ALLOC_PARAM_RET_REG = false;
    public static final boolean COMPUTE_INTERVAL    = true;
    public static final boolean REG_COALESC         = false;
    public static final boolean LINEAR_SCAN         = true;
    
    public static final boolean REPLACE_MEM_OP      = false;
    public static final boolean INSERT_SPILLING     = false;
    
    public static final boolean EXPAND_CALL_SEQ     = false;
    
    /*
     * post register allocation code transform (be careful of using any registers, and concern about calling convention)
     */
    public static final boolean INSERT_YIELDPOINT   = true;
    public static final boolean SIMPLE_BRANCH_ALIGN = false;
    
    /*
     *  code emission
     */
    public static final boolean MC_CLEANUP          = false;
    
    /*
     * machine dependent transformation
     */
    public static final boolean SPILL_CONSTANT      = false;
    public static final boolean CODE_EMIT           = true;
}