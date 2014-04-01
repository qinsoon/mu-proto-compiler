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
    |   mcCondJumpDecl
    |   mcUncondJumpDecl
    |   mcRetDecl
    |   mcMovDecl
    ;

mcMovDecl
    :   '.mc_mov' string
    ;

mcRetDecl
    :   '.mc_ret' string+
    ;

mcCondJumpDecl
    :   '.mc_cond_jump' string+
    ;

mcUncondJumpDecl
    :   '.mc_uncond_jump' string+
    ;

targetDecl
    :   '.target' string
    ;

treerule
    :   NONTERM ':' node
        mcodes?
        asOperand?
        DIGITS
    ;

asOperand
    :   '=' 
    ;

string
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
    :   mcOp mcOperand*
    ;

mcOp
    :   string
    ;

mcOperand
    :   'P' multiIndex          # mcOpdNodeChild
    |   'P(' funcCallRcv ').' funcCall '()'      # mcOpdNodeFunc
    |   '$' mcImmediate         # mcOpdImm
    |   '%' mcReg               # mcOpdReg
    ;

funcCallRcv
    :   string
    ;

funcCall
    :   string
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
    |   string                  # mcOpdMachineReg
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