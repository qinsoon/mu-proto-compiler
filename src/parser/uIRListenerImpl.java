package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import uvm.BasicBlock;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.metadata.Const;
import compiler.UVMCompiler;

public class uIRListenerImpl extends uIRBaseListener {
    Function currentFunc;
    
    HashMap<String, Const> localConstPool;
    List<BasicBlock> BBs;
    
    BasicBlock curBB;
    
    @Override
    public void enterFuncDef(@NotNull uIRParser.FuncDefContext ctx) {
        try {
            String funcName = ASTHelper.getIdentifierName(ctx.IDENTIFIER(), true);
            FunctionSignature sig = ASTHelper.getFunctionSignature(ctx.funcSig());
            
            currentFunc = new Function(funcName, sig);
            MicroVM.v.declareFunc(funcName, currentFunc);
            
            // init
            localConstPool = new HashMap<String, Const>();
            BBs = new ArrayList<BasicBlock>();
        } catch (ASTParsingException e) {
            error(ctx, e.getMessage());
        }
    }
    
    @Override
    public void exitFuncDef(@NotNull uIRParser.FuncDefContext ctx) {
        currentFunc.defineFunction(localConstPool, BBs);
        
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
                Const c = ASTHelper.getConst(ctx.constDef());
                localConstPool.put(c.getName(), c);
            } else if (ctx.label() != null) {
                // label - end of last BB, starts of a new BB
                if (curBB != null)
                    BBs.add(curBB);
                
                String labelName = ASTHelper.getIdentifierName(ctx.label().IDENTIFIER(), false);
                curBB = new BasicBlock(labelName);
            } else if (ctx.inst() != null) {
                // inst
                Instruction i = ASTHelper.getInstruction(ctx.inst());
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
