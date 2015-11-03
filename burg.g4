/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar burg;

start
    :   (declare | treerule)*
    ;

declare
    :   targetDecl
// mc    
    |   mcCondJumpDecl
    |   mcUncondJumpDecl
    |   mcRetDecl
    |   mcMovDecl
    |   mcDPMovDecl
    |   mcSPMovDecl
    |   mcPhiDecl
    |   mcNopDecl
    |   mcInstPtrDecl
    |   mcStackPtrDecl
    |   mcFramePtrDecl
    |   mcCallDecl
    |   mcCallExpDecl
    |   mcTailCallExpDecl
    |   mcCmpDecl
// op emit
    |   opEmitRule
// mc define
    |   mcDefine
// regs
    |   gprDecl
    |   gprParamDecl
    |   gprRetDecl
    |   fpRegDecl
    |   fpRegParamDecl
    |   fpRegRetDecl
    |   calleeSaveDecl
    ;

opEmitRule
    :   '.emit_op' opClass '=' '{'
        formatString (',' singleNodeFuncCall)* ';'
        '}'
    ;

opClass
    :   idString
    ;

mcDefine
    :   '.mc_def' mcOp ('(' DIGITS ')')? '=' '{'
        // operand type
        operandTypeDefine ?
        // emit code
        formatString (',' mcEmitOperand)* ';'
        // implicit uses
        implicitUses ?
        // implicit defines
        implicitDefines ?
        '}'
    ;

implicitUses
    :   'uses:' (mcReg '(' mcOperandType ')')+ ';'
    ;

implicitDefines
    :   'defines:' (mcReg '(' mcOperandType ')')+ ';'
    ;

operandTypeDefine
    :   resultOperandType ? mcOperandType+ ';'
    ;

resultOperandType
    :   (mcOperandType '=') ?
    ;

mcOperandType
    :   'DP'            # mcOperandDP
    |   'DP/MEM'        # mcOperandDPOrMem
    |   'SP'            # mcOperandSP
    |   'SP/MEM'        # mcOperandSPOrMem
    |   'GPR'           # mcOperandGPR
    |   'GPR/MEM'       # mcOperandGPROrMem
    |   'OTH'           # mcOperandOther
    ;

formatString
    :   QUOTED_STRING
    ;

mcEmitOperand
    :   'OP_REG'               # mcEmitRegOp
    |   'OP[' DIGITS ']'    # mcEmitOp
    ;

mcInstPtrDecl
    :   '.inst_ptr' idString
    ;

mcStackPtrDecl
    :   '.stack_ptr' idString
    ;

mcFramePtrDecl
    :   '.frame_ptr' idString
    ;

mcCallDecl
    :   '.mc_call' idString+
    ;

mcCallExpDecl
    :   '.mc_call_exp' idString+
    ;

mcTailCallExpDecl
    :   '.mc_tailcall' idString+
    ;

gprDecl
    :   '.gpr' idString+
    ;

gprParamDecl
    :   '.gpr_param' idString+
    ;

gprRetDecl
    :   '.gpr_ret' idString+
    ;

fpRegDecl
    :   '.fp_reg' idString+
    ;

fpRegParamDecl
    :   '.fp_reg_param' idString+
    ;

fpRegRetDecl
    :   '.fp_reg_ret' idString+
    ;

calleeSaveDecl
    :   '.callee_save' idString+
    ;

mcNopDecl
    :   '.mc_nop' idString
    ;

mcPhiDecl
    :   '.mc_phi' idString+
    ;

mcMovDecl
    :   '.mc_mov' idString+
    ;

mcDPMovDecl
    :   '.mc_dpmov' idString+
    ;

mcSPMovDecl
    :   '.mc_spmov' idString+
    ;

mcRetDecl
    :   '.mc_ret' idString+
    ;

mcCondJumpDecl
    :   '.mc_cond_jump' ('{' idString idString '}')+
    ;

mcUncondJumpDecl
    :   '.mc_uncond_jump' idString+
    ;

mcCmpDecl
    :   '.mc_cmp' idString+
    ;

targetDecl
    :   '.target' idString
    ;

treerule
    :   NONTERM ':' node
        mcodes ?
        DIGITS
    ;

idString
    :   (NONTERM | TERM)
    ;

node
    :   TERM ('(' node+ ')')?   # TermNode
    |   NONTERM                 # NonTermNode
    ;

/*
 * about machine code generation rule
 */
mcodes
    :   ('>' mcode)+
    ;
                
mcode
    :   mcOp mcOperand* ('->' resOperand)?
    ;

resOperand
    :   mcOperand
    ;

mcOp
    :   idString
    ;

mcOperand
    :   'P' multiIndex          # mcOpdNodeChild
    |   singleNodeFuncCall      # mcOpdNodeFunc
    |   '$' mcImmediate         # mcOpdImm
    |   '%' mcReg               # mcOpdReg
    |   '*(' mcOperand '+' mcOperand '+' mcOperand 'x' mcOperand ')'    # mcOpdAddress
    ;

singleNodeFuncCall
    :   'P(' funcCallRcv ')' ('.' funcCall '()')+
    ;

funcCallRcv
    :   idString
    ;

funcCall
    :   idString
    ;

multiIndex
    :   index+
    ;

index
    :   '[' DIGITS ']'
    |   '[' mcOperand ']'
    ;

mcReg
    :   'res_reg'                   # mcOpdResReg
    |   'ret_reg' index             # mcOpdRetReg
    |   'param_reg' index           # mcOpdParamReg
    |   'tmp_reg' index             # mcOpdTmpReg
    |   idString                    # mcOpdMachineReg
    ;

mcImmediate
    :   mcIntImmediate
    |   mcFpImmediate
    ;

mcIntImmediate
    :   ('+'|'-')? DIGITS
    ;

mcFpImmediate
    :   ('+'|'-')? DIGITS 'e' ('+'|'-')? DIGITS
    |   ('+'|'-')? DIGITS '.' DIGITS
    ;

/*
 * terminals
 */

fragment ESCAPED_QUOTE : '\\"';
QUOTED_STRING :   '"' ( ESCAPED_QUOTE | ~('\n'|'\r') )*? '"';

DIGITS
    :   DIGIT+
    ;

NONTERM
    :   LOWERCHAR CHAR+
    ;

TERM
    :   UPPERCHAR CHAR+
    ;

fragment
CHAR
    :   LOWERCHAR
    |   UPPERCHAR
    |   DIGIT
    |   '-'
    |   '_'
    ;

fragment
DIGIT
    :   [0-9]
    ;

fragment
LOWERCHAR
    :   [a-z]
    ;

fragment
UPPERCHAR
    :   [A-Z]
    ;

WS 
    :   [ \t\r\n]+ -> skip 
    ; // skip spaces, tabs, newlines

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;