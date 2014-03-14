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
    ;

targetDecl
    :   '.target' string
    ;

string
    :   (NONTERM | TERM)
    ;

treerule
    :   NONTERM ':' node
        COST
    ;

node
    :   TERM ('(' node+ ')')?
    |   NONTERM
    ;

NONTERM
    :   LOWERCHAR CHAR+
    ;

TERM
    :   UPPERCHAR CHAR+
    ;

COST
    :   DIGIT+
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