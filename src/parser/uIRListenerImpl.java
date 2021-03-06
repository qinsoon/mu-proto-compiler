package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import uvm.BasicBlock;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.ImmediateValue;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Type;
import uvm.Value;
import uvm.metadata.Const;
import compiler.UVMCompiler;

public class uIRListenerImpl extends uIRBaseListener {
    Function currentFunc;
    
    HashMap<String, ImmediateValue> bundleConstPool = new HashMap<String, ImmediateValue>();
    
    HashMap<String, ImmediateValue> localConstPool;
    List<BasicBlock> BBs;
    
    BasicBlock curBB;
    
    @Override
    public void exitTypeDef(@NotNull uIRParser.TypeDefContext ctx) {
    	try {
	    	String typeName = ASTHelper.getIdentifierName(ctx.IDENTIFIER(), true);
	    	if (MicroVM.v.getType(typeName) != null) {
	    		UVMCompiler.error("duplicate type def on " + typeName);
	    	} else {
				Type t = ASTHelper.defineType(typeName, ctx.type());
	    	}
    	} catch (ASTParsingException e) {
    		UVMCompiler.error("exception in processing typedef: (" + e.getClass().toString() + ")" + e.getMessage());
    	}
    }
    
    @Override
    public void exitConstDef(@NotNull uIRParser.ConstDefContext ctx) {
    	try{
    		String id = ASTHelper.getIdentifierName(ctx.IDENTIFIER(), true);
    		ImmediateValue v = ASTHelper.getConst(ctx); 
    		MicroVM.v.defineGlobalConsts(id, v);
    	} catch (ASTParsingException e) {
    		error(ctx, e.getMessage());
    	}
    }
    
    @Override
    public void enterFuncDef(@NotNull uIRParser.FuncDefContext ctx) {
        try {
            String funcName = ASTHelper.getIdentifierName(ctx.IDENTIFIER(), true);
            FunctionSignature sig = ASTHelper.getFunctionSignature(ctx.funcSig());
            
            currentFunc = new Function(funcName, sig);
            
            if (ctx.FUNC_INLINE_ANNO() != null) {
            	currentFunc.setInlined(true);
            }
            
            MicroVM.v.declareFunc(funcName, currentFunc);
            
            // init
            localConstPool = new HashMap<String, ImmediateValue>();
            BBs = new ArrayList<BasicBlock>();
        } catch (ASTParsingException e) {
            error(ctx, e.getMessage());
        }
    }
    
    @Override
    public void exitFuncDef(@NotNull uIRParser.FuncDefContext ctx) {
        // label - end of last BB
        if (curBB != null)
            BBs.add(curBB);
        
        currentFunc.defineFunction(localConstPool, BBs);
        currentFunc.resolveLabels();
        
        // reset
        currentFunc = null;
        localConstPool = null;
        BBs = null;
        curBB = null;
    }
    
    @Override
    public void exitFuncBodyInst(@NotNull uIRParser.FuncBodyInstContext ctx) {
        try {
            if (ctx.constDef() != null) {
                // local const
            	String id = ASTHelper.getIdentifierName(ctx.constDef().IDENTIFIER(), false);
            	ImmediateValue v= ASTHelper.getConst(ctx.constDef());
                localConstPool.put(id, v);
            } else if (ctx.label() != null) {
                // label - end of last BB, starts of a new BB
                if (curBB != null) {
                    UVMCompiler._assert(curBB.getInsts().size() != 0, "A basic block needs at least one instruction. BB " + curBB.getName() + " is empty. ");
                    BBs.add(curBB);
                }
                
                String labelName = ASTHelper.getIdentifierName(ctx.label().IDENTIFIER(), false);
                curBB = new BasicBlock(currentFunc, labelName);
            } else if (ctx.inst() != null) {
                // inst
                Instruction i = ASTHelper.getInstruction(currentFunc, ctx.inst());
                curBB.addInstruction(i);
            }
        } catch (ASTParsingException e) {
            error(ctx, e.getMessage());
        }
    }

    public static void error(ParserRuleContext ctx, String message) {
        UVMCompiler.error("L" + ctx.start.getLine() + ": " + message);
    }
}
