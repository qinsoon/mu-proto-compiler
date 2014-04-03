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
    |   'GETELEMIREF'   '<' type '>' value value        # InstGetElemIRef
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
    ;

funcCallBody
    :   '<' funcSig '>' value args
    ;

args
    :   '(' value* ')'
    ;

callConv : 'DEFAULT' ;

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
    : 'FADD' | 'FSUB' | 'FMUL' | 'FDIV' | 'FREM'
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
    : 'FTRUE' | 'FFALSE' 
    | 'FUNO' | 'FUEQ' | 'FUNE' | 'FUGT' | 'FULT' | 'FUGE' | 'FULE'
    | 'FORD' | 'FOEQ' | 'FONE' | 'FOGT' | 'FOLT' | 'FOGE' | 'FOLE'
    ;
    
convOps
    : 'TRUNC' | 'ZEXT' | 'SEXT' | 'FPTRUNC' | 'FPEXT'
    | 'FPTOUI' | 'FPTOSI' | 'UITOFP' | 'SITOFP' | 'BITCAST'
    | 'REFCAST' | 'IREFCAST'
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
