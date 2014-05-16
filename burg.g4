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
// mc define
    |   mcDefine
// regs
    |   gprDecl
    |   gprParamDecl
    |   gprRetDecl
    ;

mcDefine
    :   '.mc_def' mcOp '=' '{'
        // operand type
        operandTypeDefine ?
        // emit code
        formatString (',' mcEmitOperand)* ';'
        '}'
    ;

operandTypeDefine
    :   resultOperandType ? mcOperandType+ ';'
    ;

resultOperandType
    :   (mcOperandType '=') ?
    ;

mcOperandType
    :   'DP'            # mcOperandDP
    |   'SP'            # mcOperandSP
    |   'GPR'           # mcOperandGPR
    |   'MEM'           # mcOperandMem
    |   'OTH'           # mcOperandOther
    ;

formatString
    :   '"' idString* '"'
    ;

mcEmitOperand
    :   'OP_REG'               # mcEmitRegOp
    |   'OP[' DIGITS ']'    # mcEmitOp
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
    :   TERM ('(' node+ ')')?
    |   NONTERM
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
    |   'P(' funcCallRcv ').' funcCall '()'      # mcOpdNodeFunc
    |   '$' mcImmediate         # mcOpdImm
    |   '%' mcReg               # mcOpdReg
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