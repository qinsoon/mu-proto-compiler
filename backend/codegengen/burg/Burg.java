package burg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uvm.OpCode;

public class Burg {
    public static final Map<String, Integer> termNames = new HashMap<String, Integer>();
    public static final Map<String, Integer> ntNames = new HashMap<String, Integer>();
    
    public static final List<Terminal> terms = new ArrayList<Terminal>();
    public static final List<NonTerminal> nonterms = new ArrayList<NonTerminal>();
    
    public static String ruleFile;
    public static String output;
    
    public static final String BURM_FILE = "BURM_GENERATED.java";
    
    public static boolean debug = false;
    
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-d")) {
                debug = true;
            }        
            else if (args[i].equalsIgnoreCase("-o")) {
                output = args[i+1];
                if (!output.endsWith("/"))
                    output += "/";
                i++;
            } 
            else {
                // naming a rule file
                if (ruleFile == null)
                    ruleFile = args[i];
                else{
                    System.out.println("Only one rule file is allowed. ");
                    System.exit(1);
                }
            }
        }
        
        try {
            ANTLRInputStream input  = new ANTLRInputStream(new FileInputStream(ruleFile));
            burgLexer lexer = new burgLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            burgParser parser = new burgParser(tokens);
            ParseTree tree = parser.start();
            
            System.out.println("Parsing Tree:");
            System.out.println(tree.toStringTree(parser));
            System.out.println();
            
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new burgListenerImpl(), tree);
            
            System.out.println("rules:");
            for (Rule r : rules) {
                System.out.println(r.prettyPrint());
            }
            System.out.println();
            
            System.out.println("non-terms:");
            for (String nt : ntNames.keySet()) {
                System.out.println("static final int " + nt + " = " + ntNames.get(nt) + ";");
            }
            System.out.println();
            
            System.out.println("terms:");
            for (String t : termNames.keySet()) {
                System.out.println("static final int " + t + " = " + termNames.get(t) + ";");
            }
            System.out.println();
            
            generateBURM();
            
            generateMCLayer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String targetName = null;
    
    private static String curCost = null;
    
    public static void generateBURM() {
        CodeBuilder code = new CodeBuilder();
        
        String pkg = "burm";
        
        code.appendStmtln("package " + pkg);
        code.appendStmtln("import static burm.BurmState.INFINITE");
        code.appendStmtln("import uvm.IRTreeNode;");
        
        code.appendln("public class BURM_GENERATED {");
        code.increaseIndent();
        
        /*
         * terms / nonterms
         */
        code.appendCommentln("non terms");
        for (String nt : ntNames.keySet()) {
            code.appendStmtln("static final int " + nt + " = " + ntNames.get(nt));
        }
        code.appendln();
        
        code.appendCommentln("terms");
        for (String t : termNames.keySet()) {
            code.appendStmtln("static final int " + t + " = " + termNames.get(t));
        }
        code.appendln();
        
        /*
         * first few lines of state()
         */
        code.appendln("public static BurmState state(IRTreeNode node, BurmState[] leaves) {");
        code.increaseIndent();
        code.appendStmtln("BurmState p = new BurmState()");
        code.appendln();
        code.appendStmtln("p.node = node");
        code.appendStmtln("p.leaves = leaves");
        
        /*
         * rest of state()
         */
        code.append("p.cost = new short[]{");
        int ntCount = ntNames.size();
        for (int i = 0; i <= ntCount; i++)
            if (i != ntCount)
                code.appendNoIndent("INFINITE,");
            else code.appendlnNoIndent("INFINITE};");
        code.append("p.rule = new short[]{");
        for (int i = 0; i <= ntCount; i++)
            if (i != ntCount)
                code.appendNoIndent("-1,");
            else code.appendlnNoIndent("-1};");
        
        code.appendln();
        code.appendStmtln("int c");
        code.appendln();
        
        code.appendln("switch (node.getOpcode()) {");
        
        // for every terminal
        for (String term : termNames.keySet()) {
            code.appendln("case " + term + ":");
            code.increaseIndent();
            
            // for every rule, this terminal is rhs
            for (Rule rule : rules) {
                if (rule.rhs.id == termNames.get(term)) {
                    boolean needToMatchChildren = rule.rhs.children.size() != 0;
                    
                    if (needToMatchChildren) {
                        curCost = "";
                        code.append("if (");
                        code.appendNoIndent(matchChildren(rule.rhs, null));
                        code.appendNoIndent(") {");
                        code.appendlnNoIndent();
                        
                        code.increaseIndent();
                    }
                    
                    // current cost
                    code.appendCommentln(rule.prettyPrint());
                    code.append("c = ");
                    if (curCost != null)
                        code.appendNoIndent(curCost);
                    code.appendNoIndent(Integer.toString(rule.cost));
                    code.appendNoIndent(";\n");
                    code.appendStmtln(String.format("p.record(%s, c, %d)", rule.lhs.name, rule.ruleno));
                    
                    // chain cost
                    code.appendln();
                    code.appendCommentln("chain cost");
                    genChainCost(code, rule.lhs, "c");
                    
                    if (needToMatchChildren) {
                        code.decreaseIndent();
                        code.appendln("}");
                        curCost = null;
                    }
                }
            }
            
            code.appendln("break;");
            code.decreaseIndent();
        }
        
        // end of switch
        code.appendln("}");
        
        code.appendStmtln("return p");
        code.decreaseIndent();
        
        // end of function
        code.appendln("}");
        code.decreaseIndent();
        
        // end of class
        code.appendln("}");
        
        System.out.println("========BURM Generated========");
        System.out.println(code.toString());
        
        writeTo(output + BURM_FILE, code.toString());
    }
    
    public static void writeTo(String file, String code) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(file)));
            writer.write(code);
        } catch (IOException e) {
            System.out.println("Error when writing to file:" + file);
            e.printStackTrace();
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    public static String matchChildren(TreeNode node, String base) {
        if (base == null)
            base = "";
        
        StringBuilder ret = new StringBuilder();
        
        for (int i = 0; i < node.children.size(); i++) {
            TreeNode cur = node.children.get(i);
            if (cur instanceof NonTerminal) {
                ret.append(String.format(base + "leaves[%d].rule[%s] != -1", i, cur.name));
                curCost += String.format(base + "leaves[%d].cost[%s] + ", i, cur.name);
            } else {
                // cur is terminal
                ret.append(String.format(base + "leaves[%d].node.getOpcode() == %s", i, cur.name));
            }
            
            if (cur.children.size() != 0)
                ret.append(" && " + matchChildren(cur, String.format(base+"leaves[%d].", i)));
            
            if (i != node.children.size() - 1)
                ret.append(" && ");
        }
        
        return ret.toString();
    }
    
    public static void genChainCost(CodeBuilder code, NonTerminal nt, String cost) {
        System.out.println("genChainCost(" + nt.prettyPrint() + ", " + cost + ")");
        for (Rule rule : rules) {
            if (rule.rhs instanceof NonTerminal && rule.rhs.id == nt.id) {
                String chainCost = cost + "+" + rule.cost;
                code.appendCommentln(rule.prettyPrint());
                code.appendStmtln(
                        String.format("p.record(%s, %s, %d)", 
                        rule.lhs.name,
                        chainCost,
                        rule.ruleno));
                genChainCost(code, rule.lhs, chainCost);
            }
        }
    }
    
    /*
     * Machine Code Layer
     */
    static class MCOp {
        String name;
        int operands;
    }
    
    public static final HashMap<String, MCOp> mcOps = new HashMap<String, MCOp>();
    
    public static String abstractOpClass = "MachineCode";    
    
    public static void generateMCLayer() {
        System.out.println("MC ops:");
        for (String op : mcOps.keySet()) {
            System.out.println("-" + op + "(" + mcOps.get(op).operands + ")");
        }
        
        generateMCOpAbstractClass();
        for (MCOp op : mcOps.values()) {
            generateMCOpClass(op);
        }
    }
    
    public static void generateMCOpAbstractClass() {
        abstractOpClass = targetName + abstractOpClass;
        
        CodeBuilder code = new CodeBuilder();
        
        code.appendStmtln("package burm.mc");
        
        code.appendln("public abstract class " + abstractOpClass + " {");
        code.increaseIndent();
        
        code.appendStmtln("public String name");
        code.decreaseIndent();
        code.appendln("}");
        
        writeTo(output + "mc/" + abstractOpClass + ".java", code.toString());
    }
    
    public static void generateMCOpClass(MCOp op) {
        CodeBuilder code = new CodeBuilder();
        
        code.appendStmtln("package burm.mc");
        
        code.appendln("public class " + op.name + " extends " + abstractOpClass + "{");
        code.appendln("}");
        
        writeTo(output + "mc/" + op.name + ".java", code.toString());
    }
    
    static class CodeBuilder {
        StringBuilder out = new StringBuilder();
        
        int indent = 0;
        
        public static final String INDENT1 = "  ";
        public static final String INDENT2 = "    ";
        public static final String INDENT3 = "      ";
        
        public void increaseIndent() {indent++;}
        public void decreaseIndent() {indent--;};
        
        public void appendNoIndent(String str) {
            out.append(str);
        }
        
        public void appendlnNoIndent(String str) {
            out.append(str);
            out.append('\n');
        }
        
        public void appendlnNoIndent() {
            out.append('\n');
        }
        
        public void append(String str) {
            if (indent == 0) {
                
            }
            else if (indent == 1)
                out.append(INDENT1);
            else if (indent == 2)
                out.append(INDENT2);
            else if (indent == 3)
                out.append(INDENT3);
            else {
                for (int i = 0; i < indent; i++)
                    out.append(INDENT1);
            }
             
            out.append(str);
        }    
        
        public void appendStmtln(String str) {
            append(str);
            out.append(';');
            out.append('\n');
        }
        
        public void appendln(String str) {
            append(str);
            out.append('\n');
        }
        
        public void appendln() {
            out.append('\n');
        }
        
        public void appendCommentln(String str) {
            appendln("// " + str);
        }
        
        @Override
        public String toString() {
            return out.toString();
        }
    }



    public static String IR_NODE_TYPE = "IRTreeNode";
    
    static abstract class TreeNode {
        int id;
        String name;
        List<TreeNode> children = new ArrayList<TreeNode>();
        
        TreeNode(String name) {
            this.name = name;
        }
        
        @Override public boolean equals(Object o) {
            if (!(o instanceof TreeNode))
                return false;
            
            else return this.id == ((TreeNode)o).id;
        }
        
        String prettyPrint() {
            StringBuilder ret = new StringBuilder();
            ret.append("(" + name);
            ret.append("=" + id);
            if (!children.isEmpty()) {
                ret.append("(");
                for (int i = 0; i < children.size(); i++) {
                    ret.append(children.get(i).prettyPrint());
                    if (i != children.size() - 1)
                        ret.append(" ");
                }
                ret.append(")");
            }
            ret.append(")");
            return ret.toString();
        }
    }
    
    static class Terminal extends TreeNode {        
        Terminal(String name, int op) {
            super(name);
            
            if (termNames.containsKey(name))
                this.id = termNames.get(name);
            else {            
                this.id = op;
                termNames.put(name, op);
            }
        }        
    }
    
    static class NonTerminal extends TreeNode {
        static int nextId = 1;
        NonTerminal(String name) {
            super(name + "_NT");
            name = name + "_NT";
            
            if (ntNames.containsKey(name)) {
                this.id = ntNames.get(name);
            } else {
                this.id = nextId;
                nextId ++;
                ntNames.put(name, this.id);
            }
        }
    }
    
    static class Rule {
        static int count = 0;
        
        int ruleno;
        NonTerminal lhs;
        TreeNode rhs;
        int cost;
        
        Rule(NonTerminal lhs, TreeNode rhs, int cost) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.cost = cost;
            
            this.ruleno = count;
            count++;
        }
        
        String prettyPrint() {
            return lhs.prettyPrint() + " := " + rhs.prettyPrint() + 
                    " (cost:" + cost + ", ruleno:" + ruleno + ")";
        }
    }
    
    public static NonTerminal findOrCreateNonTerm(String name) {
        NonTerminal nt = new NonTerminal(name);
        nonterms.add(nt);
        return nt;
    }
    
    public static Terminal findOrCreateTerm(String name) {
        Field f = null;
        try {
            f = OpCode.class.getField(name);
            Terminal t = new Terminal(name, f.getInt(null));
            terms.add(t);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            error("Cannot find terminal : " + name + " in uvm.OpCode");
            return null;
        } 
    }
    
    public static List<Rule> rules = new ArrayList<Rule>();    
    public static void newRule(Rule r) {
        rules.add(r);
    }
    
    public static void error(String message) {
        System.out.println(message);
        System.exit(1);
    }
}
