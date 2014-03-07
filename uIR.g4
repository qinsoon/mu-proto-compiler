/*
 * defines uVM IR text form
 */

grammar uIR;

ir
    :   metaData*
    ;

metaData
    :   constDef
    |   funcDef
    |   label
    |   typeDef
    ;

constDef
    :   '.const' IDENTIFIER '=' '<' type '>' immediate
    ;

funcDef
    :   '.funcdef' IDENTIFIER '<' funcSig '>' funcBody
    ;

funcBody
    :   '{' funcBodyInst+ '}'
    ;

funcBodyInst
    :   constDef
    |   label
    |   inst
    ;

label
    :   '.label' IDENTIFIER ':'
    ;

typeDef
    :   '.typedef' IDENTIFIER typeDescriptor
    ;

type
    :   IDENTIFIER
    |   typeDescriptor
    ;

immediate
    :   intImmediate
    |   fpImmediate
    ;

funcSig
    :   type '(' type* ')'
    ;

typeDescriptor
    :   'int' '<' intImmediate '>'          # IntType
    |   'float'                             # FloatType
    |   'double'                            # DoubleType
    |   'struct' '<' type+ '>'              # StructType
    |   'array' '<' type intImmediate '>'   # ArrayType
    |   'ref' '<' type '>'                  # RefType
    |   'iref' '<' type '>'                 # IRefType
    |   'void'                              # VoidType
    ;

inst
    :   IDENTIFIER '=' 'PARAM' intImmediate                 # InstParam

    |   'BRANCH' IDENTIFIER                                 # InstBranch
    |   'BRANCH2' value IDENTIFIER IDENTIFIER               # InstBranch2
    |   IDENTIFIER '=' 'SHL' '<' type '>' value value       # InstShl

    |   IDENTIFIER '=' 'ADD' '<' type '>' value value       # InstAdd
    |   IDENTIFIER '=' 'SREM' '<' type '>' value value      # InstSrem

    |   'BRANCH' IDENTIFIER                                 # InstBranch
    |   IDENTIFIER '=' 'PHI' '<' type '>' value IDENTIFIER value IDENTIFIER # InstPhi
    |   'RET2' value                                        # InstRet2

    |   IDENTIFIER '=' 'ALLOCA' '<' type '>'                # InstAlloca
    |   'STORE' '<' type '>' IDENTIFIER value               # InstStore
    |   IDENTIFIER '=' 'LOAD' '<' type '>' IDENTIFIER       # InstLoad

    |   IDENTIFIER '=' 'SGT' '<' type '>' value value       # InstSgt
    |   IDENTIFIER '=' 'EQ' '<' type '>' value value        # InstEq
    |   IDENTIFIER '=' 'SLT' '<' type '>' value value       # InstSlt
    ;

value
    :   IDENTIFIER
    |   immediate
    ;

intImmediate
    :   ('+'|'-')? DIGITS
    ;

fpImmediate
    :   ('+'|'-')? DIGITS 'e' ('+'|'-')? DIGITS
    |   ('+'|'-')? DIGITS '.' DIGITS
    ;

// LEXER

DIGITS
    : DIGIT+
    ;

fragment
DIGIT
    :   [0-9]
    ;

IDENTIFIER
    :   GLOBAL_ID_PREFIX IDCHAR+
    |   LOCAL_ID_PREFIX IDCHAR+
    ;

GLOBAL_ID_PREFIX: '@';
LOCAL_ID_PREFIX: '%';

fragment
IDCHAR
    :   [a-z]
    |   [A-Z]
    |   [0-9]
    |   '-'
    |   '_'
    |   '.'
    ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;