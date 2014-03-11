/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar burg;

start
    :   declare
    |   rule
    ;

declare
    :   sourceNodeDecl
    |   targetDecl
    ;

sourceNodeDecl
    :   '.source' STRING;

targetDecl
    :   '.target' STRING;

rule
    :   '#' 
        NONTERM ':' STRING '(' any* ')'
        COST;

any
    :   NONTERM
    |   TERM
    ;

NONTERM
    :   LOWERCHAR CHAR*
    ;

TERM
    :   UPPERCHAR+
    ;

COST
    : DIGIT+
    ;

STRING
    :   CHAR+;

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

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;