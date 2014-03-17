package burg;

import org.antlr.v4.runtime.misc.NotNull;

import burg.Burg.NonTerminal;
import burg.Burg.Rule;
import burg.Burg.Terminal;
import burg.Burg.TreeNode;
import burg.burgParser.NodeContext;

public class burgListenerImpl extends burgBaseListener {
    
    @Override public void exitTreerule(@NotNull burgParser.TreeruleContext ctx) { 
        NonTerminal lhs = (NonTerminal) Burg.findOrCreateNonTerm(ctx.NONTERM().toString());
        TreeNode rhs = getTreeNode(ctx.node());
        int cost = Integer.parseInt(ctx.COST().toString());
        
        Rule rule = new Rule(lhs, rhs, cost);
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
}