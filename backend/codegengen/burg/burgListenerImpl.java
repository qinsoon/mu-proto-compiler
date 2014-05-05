package burg;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import burg.Burg.*;
import burg.burgParser.McOperandContext;
import burg.burgParser.NodeContext;

public class burgListenerImpl extends burgBaseListener {
    
    @Override public void exitTargetDecl(@NotNull burgParser.TargetDeclContext ctx) {
        Burg.targetName = ctx.idString().getText();
    }
    
    @Override public void exitMcCondJumpDecl(@NotNull burgParser.McCondJumpDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.MC_COND_JUMP.add(s.getText());
    }
    
    @Override public void exitMcUncondJumpDecl(@NotNull burgParser.McUncondJumpDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.MC_UNCOND_JUMP.add(s.getText());
    }
    
    @Override public void exitMcRetDecl(@NotNull burgParser.McRetDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.MC_RET.add(s.getText());
    }
    
    @Override public void exitMcMovDecl(@NotNull burgParser.McMovDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.MC_MOV.add(s.getText());
    }
    
    @Override public void exitGprDecl(@NotNull burgParser.GprDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.REG_GPR.add(s.getText());
    }
    
    @Override public void exitGprParamDecl(@NotNull burgParser.GprParamDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.REG_GPR_PARAM.add(s.getText());
    }
    
    @Override public void exitGprRetDecl(@NotNull burgParser.GprRetDeclContext ctx) {
        for (burgParser.IdStringContext s : ctx.idString())
            Burg.REG_GPR_RET.add(s.getText());
    }
    
    @Override public void exitMcDefine(@NotNull burgParser.McDefineContext ctx) {
        String op = ctx.mcOp().getText();
        
        StringBuilder emit = new StringBuilder();
        
        String format = ctx.formatString().getText();
        format = format.replaceAll("OP", "%s");
        format = format.replaceAll("WS", " ");
        format = format.replaceAll("COMMA", ",");
        emit.append(format);
        for (burgParser.McEmitOperandContext operand : ctx.mcEmitOperand()) {
            if (operand instanceof burgParser.McEmitRegOpContext) {
                emit.append(", X64Driver.emitOp(reg)");
            }
            else if (operand instanceof burgParser.McEmitOpContext) {
                emit.append(String.format(", X64Driver.emitOp(operands.get(%d))", Integer.parseInt(((burgParser.McEmitOpContext) operand).DIGITS().getText())));
            }
        }
        
        Burg.mcEmit.put(op, emit.toString());
    }
    
    List<MCRule> mcEmissionRules = new ArrayList<MCRule>();
    
    @Override public void enterTreerule(@NotNull burgParser.TreeruleContext ctx) {
        if (!mcEmissionRules.isEmpty())
            mcEmissionRules = new ArrayList<MCRule>();
    }
    
    @Override public void exitTreerule(@NotNull burgParser.TreeruleContext ctx) { 
        NonTerminal lhs = (NonTerminal) Burg.findOrCreateNonTerm(ctx.NONTERM().toString());
        TreeNode rhs = getTreeNode(ctx.node());
        int cost = Integer.parseInt(ctx.DIGITS().toString());
        
        Rule rule = new Rule(lhs, rhs, cost);
        
        if (!mcEmissionRules.isEmpty())
            rule.setMCEmissionRules(mcEmissionRules);
        
        Burg.newRule(rule);
    }
    
    private TreeNode getTreeNode(NodeContext ctx) {
        if (ctx.NONTERM() != null)
            return Burg.findOrCreateNonTerm(ctx.NONTERM().toString());
        
        Terminal t = (Terminal) Burg.findOrCreateTerm(ctx.TERM().toString());
        for (NodeContext c : ctx.node()) {
            t.children.add(getTreeNode(c));
        }
        return t;
    }
    
    @Override public void exitMcode(@NotNull burgParser.McodeContext ctx) {
        String opName = ctx.mcOp().getText();
        
        MCOp op;
        if (!Burg.mcOps.containsKey(opName)) {
            op = new MCOp();
            op.name = opName;
            op.operands = ctx.mcOperand().size();
            
            Burg.mcOps.put(opName, op);
        } else op = Burg.mcOps.get(opName);
        
        List<CCTOperand> operands = new ArrayList<CCTOperand>();
        
        for (burgParser.McOperandContext operandCtx : ctx.mcOperand()) {
            operands.add(getOperand(operandCtx));
        }
        
        CCTOperand reg = null;
        if (ctx.resOperand() != null) {
            reg = getOperand(ctx.resOperand().mcOperand());
        }
        
        MCRule r = new MCRule(op, operands, reg);
        mcEmissionRules.add(r);
    }

    private CCTOperand getOperand(McOperandContext operandCtx) {
        CCTOperand ret = null;
        if (operandCtx instanceof burgParser.McOpdNodeChildContext) {
            List<CCTOperand> indices = new ArrayList<CCTOperand>();
            
            for (burgParser.IndexContext i : ((burgParser.McOpdNodeChildContext) operandCtx).multiIndex().index()) {
                indices.add(getIndex(i));
            }
            
            ret = new Burg.OpdNode(indices);
        } else if (operandCtx instanceof burgParser.McOpdNodeFuncContext) {
            String funcName = ((burgParser.McOpdNodeFuncContext) operandCtx).funcCall().getText();
            if (((burgParser.McOpdNodeFuncContext) operandCtx).funcCallRcv() == null) {
                ret = new Burg.OpdNodeFunc(funcName);
            } else {
                String receiverName = ((burgParser.McOpdNodeFuncContext) operandCtx).funcCallRcv().getText();
                ret = new Burg.OpdNodeFunc(funcName, receiverName);
            }
        } else if (operandCtx instanceof burgParser.McOpdImmContext) {
            if (((burgParser.McOpdImmContext) operandCtx).mcImmediate().mcIntImmediate() != null)
                ret = new Burg.OpdIntImmediate(Long.parseLong(((burgParser.McOpdImmContext) operandCtx).mcImmediate().mcIntImmediate().getText()));
            else Burg.error("not support fpimm yet");
        } else if (operandCtx instanceof burgParser.McOpdRegContext) {
            burgParser.McRegContext mcRegCtx = ((burgParser.McOpdRegContext) operandCtx).mcReg();
            if (mcRegCtx instanceof burgParser.McOpdResRegContext) {
                ret = Burg.OpdRegister.findOrCreate("res_reg", OpdRegister.RES_REG);
            }
            else if (mcRegCtx instanceof burgParser.McOpdRetRegContext) {
                OpdRegister tRet = OpdRegister.findOrCreate("ret_reg", OpdRegister.RET_REG);
                CCTOperand index = getIndex(((burgParser.McOpdRetRegContext) mcRegCtx).index());
                tRet.setIndex(index);
                ret = tRet;
            }
            else if (mcRegCtx instanceof burgParser.McOpdParamRegContext) {
                OpdRegister tRet = OpdRegister.findOrCreate("param_reg", OpdRegister.PARAM_REG);
                CCTOperand index = getIndex(((burgParser.McOpdParamRegContext) mcRegCtx).index());
                tRet.setIndex(index);
                ret = tRet;
            }
            else if (mcRegCtx instanceof burgParser.McOpdTmpRegContext) {
                OpdRegister tRet = OpdRegister.findOrCreate("tmp_reg", OpdRegister.TMP_REG);
                CCTOperand index = getIndex(((burgParser.McOpdTmpRegContext) mcRegCtx).index());
                tRet.setIndex(index);
                ret = tRet;
            }
            else if (mcRegCtx instanceof burgParser.McOpdMachineRegContext) {
                ret = OpdRegister.findOrCreate(mcRegCtx.getText(), OpdRegister.MACHINE_REG);
            }
        }
        
        return ret;
    }
    
    private CCTOperand getIndex(burgParser.IndexContext i) {
        if (i.DIGITS() != null)
            return new OpdIntImmediate(Integer.parseInt(i.DIGITS().getText()));
        else return getOperand(i.mcOperand());
    }
}
