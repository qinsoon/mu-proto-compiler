/*
 * defines uVM IR text form
 */

grammar uIR;

ir
    :   metaData
    |   inst
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
    :   '{' funcBodyInst '}'
    ;

funcBodyInst
    :   (constDef | label | inst)*
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
    :   'int' '<' intImmediate '>'
    |   'float'
    |   'double'
    |   'struct' '<' type+ '>'
    |   'array' '<' type intImmediate '>'
    |   'ref' '<' type '>'
    |   'iref' '<' type '>'
    |   'void'
    ;

inst
    :   IDENTIFIER '=' 'PARAM' intImmediate     
    |   IDENTIFIER '=' 'SGT' '<' type '>' value value
    |   'BRANCH2' value IDENTIFIER IDENTIFIER
    |   IDENTIFIER '=' 'SHL' '<' type '>' value value
    |   IDENTIFIER '=' 'ADD' '<' type '>' value value
    |   'BRANCH' IDENTIFIER
    |   IDENTIFIER '=' 'PHI' '<' type '>' value IDENTIFIER value IDENTIFIER
    |   'RET2' value
    |   IDENTIFIER '=' 'ALLOCA' '<' type '>'
    |   'STORE' '<' type '>' IDENTIFIER value
    |   IDENTIFIER '=' 'LOAD' '<' type '>' IDENTIFIER
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