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
    :   '.const' IDENTIFIER '=' '<' type '>' immediate
    ;

funcSigDef
    :   '.funcsig' IDENTIFIER '=' funcSig
    ;

funcSig
    :   IDENTIFIER
    |   type '(' type* ')'
    ;

funcDecl
    :   '.funcdecl' IDENTIFIER '<' funcSig '>'
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
    :   IDENTIFIER '=' instBody
    |   instBody
    ;

instBody
    :   'PARAM' intImmediate                    # InstParam

    // Integer/FP Arithmetic
    |   binOps '<' type '>' value value         # BinOp

    // Integer/FP Comparison
    |   'ICMP' iCmpOps '<' type '>' value value     # ICmp
    |   'FCMP' fCmpOps '<' type '>' value value     # FCmp
    
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
    |   'CALL' '<' funcSig '>' IDENTIFIER '('
            ('<' type '>' value)* ')'               # InstCall
    |   'INVOKE' '<' funcSig '>' IDENTIFIER '('
            ('<' type '>' value)* ')' IDENTIFIER IDENTIFIER # InstInvoke
    |   'TAILCALL' '<' funcSig '>' IDENTIFIER '('
            ('<' type '>' value)* ')'               # InstTailCall

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

    // TODO: add thread/stack operations
    // TODO: add native interfaces
    // TODO: add traps
    ;

binOps : iBinOps | fBinOps ;

iBinOps
    : 'ADD' | 'SUB' | 'MUL' | 'UDIV' | 'SDIV' | 'UREM' | 'SREM' 
    | 'SHL' | 'LSHR' | 'ASHR' | 'AND' | 'OR' | 'XOR'
    ;
    
fBinOps
    : 'FADD' | 'FSUB' | 'FMUL' | 'FDIV' | 'FREM'
    ;
    
iCmpOps
    : 'EQ' | 'NE' | 'SGT'| 'SLT'| 'SGE'| 'SLE' | 'UGT' | 'ULT' | 'UGE' | 'ULE'
    ;

fCmpOps
    : 'TRUE' | 'FALSE' 
    | 'UNO' | 'UEQ' | 'UNE' | 'UGT' | 'ULT' | 'UGE' | 'ULE'
    | 'ORD' | 'OEQ' | 'ONE' | 'OGT' | 'OLT' | 'OGE' | 'OLE'
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