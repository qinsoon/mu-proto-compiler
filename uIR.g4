/*
 * defines uVM IR text form
 */

grammar uIR;

ir
    :   metaData*
    ;

metaData
    :   constDef
    |   funcSigDef
    |   funcDecl
    |   funcDef
    |   typeDef
    ;

constDef
    :   '.const' IDENTIFIER '<' type '>' '=' immediate
    ;

funcSigDef
    :   '.funcsig' IDENTIFIER funcSig
    ;

funcDecl
    :   '.funcdecl' IDENTIFIER '<' funcSig '>'
    ;
    
funcDef
    :   '.funcdef' IDENTIFIER '<' funcSig '>' funcBody
    ;

typeDef
    :   '.typedef' IDENTIFIER type
    ;

funcSig
    :   IDENTIFIER
    |   type '(' type* ')'
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

type
    :   IDENTIFIER
    |   typeConstructor
    ;

immediate
    :   intImmediate
    |   fpImmediate
    ;

typeConstructor
    :   'int' '<' intImmediate '>'          # IntType
    |   'float'                             # FloatType
    |   'double'                            # DoubleType
    |   'ref' '<' type '>'                  # RefType
    |   'iref' '<' type '>'                 # IRefType
    |   'struct' '<' type+ '>'              # StructType
    |   'array' '<' type intImmediate '>'   # ArrayType
    |   'hybrid' '<' type type '>'          # HybridType
    |   'void'                              # VoidType
    |   'func' '<' funcSig '>'              # FuncType
    |   'thread'                            # ThreadType
    |   'stack'                             # StackType
    ;

inst
    :   IDENTIFIER '=' instBody
    |   instBody
    ;

instBody
    :   'PARAM' intImmediate                    # InstParam

    // Integer/FP Arithmetic
    |   binOps '<' type '>' value value         # InstBinOp

    // Integer/FP Comparison
    |   cmpOps '<' type '>' value value         # InstCmp
    
    // Select
    |   'SELECT' '<' type '>' value value value     # InstSelect

    // Conversions
    |   convOps  '<' type type '>' value            # InstConversion

    // Intra-function Control Flow
    |   'BRANCH' IDENTIFIER                         # InstBranch
    |   'BRANCH2' value IDENTIFIER IDENTIFIER       # InstBranch2
    |   'SWITCH' '<' type '>' value IDENTIFIER '{'
            (value ':' IDENTIFIER ';')* '}'         # InstSwitch
    |   'PHI' '<' type '>' '{'
            (IDENTIFIER ':' value ';')* '}'         # InstPhi

    // Inter-function Control Flow
    |   'CALL' funcCallBody                         # InstCall
    |   'INVOKE' funcCallBody IDENTIFIER IDENTIFIER # InstInvoke
    |   'TAILCALL' funcCallBody                     # InstTailCall

    |   'RET' '<' type '>' value                    # InstRet
    |   'RETVOID'                                   # InstRetVoid
    |   'THROW' value                               # InstThrow
    |   'LANDINGPAD'                                # InstLandingPad

    // Aggregate Operations
    |   'EXTRACTVALUE' '<' type intImmediate '>' value  # InstExtractValue
    |   'INSERTVALUE' '<' type intImmediate '>' value value # InstInsertValue

    // Memory Operations
    |   'NEW'           '<' type '>'                # InstNew
    |   'NEWHYBRID'     '<' type '>' value          # InstNewHybrid
    |   'ALLOCA'        '<' type '>'                # InstAlloca
    |   'ALLOCAHYBRID'  '<' type '>' value          # InstAllocaHybrid
    
    |   'GETIREF'       '<' type '>' value              # InstGetIRef

    |   'GETFIELDIREF'  '<' type intImmediate '>' value # InstGetFieldIRef
    |   'GETELEMIREF'   '<' type type '>' value value        # InstGetElemIRef
    |   'SHIFTIREF'     '<' type '>' value value        # InstShiftIRef
    |   'GETFIXEDPARTIREF'  '<' type '>' value          # InstGetFixedPartIRef
    |   'GETVARPARTIREF'    '<' type '>' value          # InstGetVarPartIRef
    
    |   'LOAD' atomicDecl? '<' type '>' value           # InstLoad
    |   'STORE' atomicDecl? '<' type '>' value value    # InstStore
    |   'CMPXCHG' atomicDecl? '<' type '>' value value value   # InstCmpXChg
    |   'ATOMICRMW' atomicDecl? atomicRMWOp
                '<' type '>' value value                # InstAtomicRMW

    // Thread and Stack Operations
    |   'NEWSTACK'  funcCallBody                        # InstNewStack
    |   'NEWTHREAD' value                               # InstNewThread
    |   'SWAPSTACK' value                               # InstSwapStack
    |   'KILLSTACK' value                               # InstKillStack
    |   'SWAPANDKILL' value                             # InstSwapAndKill
    |   'THREADEXIT'                                    # InstThreadExit

    // Trap
    |   'TRAP' args                                     # InstTrap
    |   'TRAPCALL' '<' type '>' args                    # InstTrapCall

    // Foreign Function Interface
    |   'CCALL' callConv funcCallBody                   # InstCCall

    // for debug use
    |   'PRINTSTR ' STRINGLITERAL                       # InstPrintStr
    ;

funcCallBody
    :   '<' funcSig '>' value args
    ;

args
    :   '(' value* ')'
    ;

callConv : 'DEFAULT'    # CCALL_DEFAULT_CC;

binOps : iBinOps | fBinOps ;

iBinOps
    : 'ADD'                                             # InstAdd
    | 'SUB'                                             # InstSub
    | 'MUL'                                             # InstMul
    | 'UDIV'                                            # InstUDiv
    | 'SDIV'                                            # InstSDiv
    | 'UREM'                                            # InstURem
    | 'SREM'                                            # InstSRem
    | 'SHL'                                             # InstShl
    | 'LSHR'                                            # InstLshr
    | 'ASHR'                                            # InstAshr
    | 'AND'                                             # InstAnd
    | 'OR'                                              # InstOr
    | 'XOR'                                             # InstXor
    ;
    
fBinOps
    : 'FADD'                                            # InstFAdd
    | 'FSUB'                                            # InstFSub
    | 'FMUL'                                            # InstFMul
    | 'FDIV'                                            # InstFDiv
    | 'FREM'                                            # InstFRem
    ;

cmpOps : iCmpOps | fCmpOps ;

iCmpOps
    : 'EQ'                                              # InstEq
    | 'NE'                                              # InstNe
    | 'SGT'                                             # InstSgt
    | 'SLT'                                             # InstSlt
    | 'SGE'                                             # InstSge
    | 'SLE'                                             # InstSle
    | 'UGT'                                             # InstUgt
    | 'ULT'                                             # InstUlt
    | 'UGE'                                             # InstUge
    | 'ULE'                                             # InstUle
    ;

fCmpOps
    : 'FTRUE'                                           # InstFTrue
    | 'FFALSE'                                          # InstFFalse
    | 'FUNO'                                            # InstFUno
    | 'FUEQ'                                            # InstFUeq
    | 'FUNE'                                            # InstFUne
    | 'FUGT'                                            # InstFUgt
    | 'FULT'                                            # InstFUlt
    | 'FUGE'                                            # InstFUge
    | 'FULE'                                            # InstFUle
    | 'FORD'                                            # InstFOrd
    | 'FOEQ'                                            # InstFOeq
    | 'FONE'                                            # InstFOne
    | 'FOGT'                                            # InstFOgt
    | 'FOLT'                                            # InstFOlt
    | 'FOGE'                                            # InstFOge
    | 'FOLE'                                            # InstFOle
    ;
    
convOps
    : 'TRUNC'                                           # InstTrunc
    | 'ZEXT'                                            # InstZExt
    | 'SEXT'                                            # InstSExt
    | 'FPTRUNC'                                         # InstFPTrunc
    | 'FPEXT'                                           # InstFPExt
    | 'FPTOUI'                                          # InstFPToUI
    | 'FPTOSI'                                          # InstFPToSI
    | 'UITOFP'                                          # InstUIToFP
    | 'SITOFP'                                          # InstSIToFP
    | 'BITCAST'                                         # InstBitcast
    | 'REFCAST'                                         # InstRefcast
    | 'IREFCAST'                                        # InstIRefcast
    ;

atomicDecl
    : 'NOT_ATOMIC' | 'UNORDERED' | 'MONOTONIC' | 'AQUIRE' | 'RELEASE'
    | 'ACQ_REL' | 'SQL_CST'
    ;

atomicRMWOp
    : 'XCHG' | 'ADD' | 'SUB' | 'AND' | 'NAND' | 'OR' | 'XOR'
    | 'MAX' | 'MIN' | 'UMAX' | 'UMIN'
    ;
value
    :   IDENTIFIER
    |   immediate
    ;

intImmediate
    :   ('+'|'-')? DIGITS
    ;

fpImmediate
    :   ('+'|'-')? decimalFP 'e' ('+'|'-')? DIGITS      # exponentFPImm
    |   decimalFP                                       # decimalFPImm
    ;

decimalFP
    :   ('+'|'-')? DIGITS '.' DIGITS
    ;

// LEXER

STRINGLITERAL
    :   '"' (IDCHAR | SPECIAL_CHAR)* '"'
    ;

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
SPECIAL_CHAR
    :   '('
    |   ')'
    |   ' '
    ;

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
