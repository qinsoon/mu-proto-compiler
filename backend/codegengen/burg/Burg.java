package burg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Burg {
    public static final String HARDCODE_PIECES = "/Users/apple/uvm-compiler-antlr/backend/codegengen/burg/burm-pieces.txt";
    
    public static final HashMap<String, Terminal> terminals = new HashMap<String, Terminal>();
    public static final HashMap<String, NonTerminal> nonterminals = new HashMap<String, NonTerminal>();
    
    public static String ruleFile;
    public static String output;
    
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-o")) {
                output = args[i+1];
                i++;
            } else {
                // naming a rule file
                if (ruleFile == null)
                    ruleFile = args[i];
                else{
                    System.err.println("Only one rule file is allowed. ");
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
            for (NonTerminal nt : nonterminals.values()) {
                System.out.println("static final int " + nt.name + " = " + nt.id + ";");
            }
            System.out.println();
            
            System.out.println("terms:");
            for (Terminal t : terminals.values()) {
                System.out.println("static final int " + t.name + " = " + t.id + ";");
            }
            System.out.println();
            
            generateBURM();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void generateBURM() {
        CodeBuilder code = new CodeBuilder();
        
        String pkg = "burm";
        
        code.appendStmtln("package " + pkg);
        
        code.appendln("public class BURM_GENERATED {");
        code.increaseIndent();
        
        /*
         * terms / nonterms
         */
        code.appendCommentln("non terms");
        for (NonTerminal nt : nonterminals.values()) {
            code.appendStmtln("static final int " + nt.name + " = " + nt.id);
        }
        code.appendln();
        
        code.appendCommentln("terms");
        for (Terminal t : terminals.values()) {
            code.appendStmtln("static final int " + t.name + " = " + t.id);
        }
        code.appendln();
        
        /*
         * State and record() and first few lines of state()
         */
        Scanner hardcode = null;
        try {
            hardcode = new Scanner(new FileInputStream(HARDCODE_PIECES));
            while (hardcode.hasNextLine()) {
                code.appendlnNoIndent(hardcode.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            error("Error when copying from hard coded pieces: " + HARDCODE_PIECES); 
        } finally {
            if (hardcode != null)
                hardcode.close();
        }
        
        /*
         * rest of state()
         */
        code.increaseIndent();
        code.append("p.cost = new short[]{");
        int ntCount = nonterminals.size();
        for (int i = 0; i <= ntCount; i++)
            if (i == 0)
                code.appendNoIndent("0,");
            else if (i != 0 && i != ntCount)
                code.appendNoIndent("State.INFINITE,");
            else code.appendlnNoIndent("State.INFINITE};");
        code.append("p.rule = new short[]{");
        for (int i = 0; i <= ntCount; i++)
            if (i != ntCount)
                code.appendNoIndent("0,");
            else code.appendlnNoIndent("0};");
        
        code.appendln();
        code.appendStmtln("int c");
        code.appendln();
        
        code.appendln("switch (op) {");
        
        // for every terminal
        for (Terminal term : terminals.values()) {
            code.appendln("case " + term.name + ":");
            code.increaseIndent();
            
            // for every rule, this terminal is rhs
            for (Rule rule : rules) {
                if (rule.rhs.id == term.id) {
                    code.append("if (");
                    for (int i = 0; i < term.children.size(); i++) {
                        code.appendNoIndent(String.format("leaves[%d].rule[%s] != 0", i, term.children.get(i).name));
                        if (i != term.children.size() - 1)
                            code.appendNoIndent(" && ");
                    }
                    code.appendNoIndent(") {");
                    code.appendlnNoIndent();
                    
                    code.increaseIndent();
                    
                    // current cost
                    code.appendCommentln(rule.prettyPrint());
                    code.append("c = ");
                    for (int i = 0; i < term.children.size(); i++) {
                        code.appendNoIndent(String.format("leaves[%d].cost[%s] + ", i, term.children.get(i).name));
                    }
                    code.appendNoIndent(Integer.toString(rule.cost));
                    code.appendNoIndent(";\n");
                    code.appendStmtln(String.format("record(p, %s, c, %d)", rule.lhs.name, rule.ruleno));
                    
                    // chain cost
                    code.appendln();
                    code.appendCommentln("chain cost");
                    genChainCost(code, rule.lhs, "c");
                    
                    code.decreaseIndent();
                    code.appendln("}");
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
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(output)));
            writer.write(code.toString());
        } catch (IOException e) {
            System.err.println("Error when writing BURM to file:" + output);
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
    
    public static void genChainCost(CodeBuilder code, NonTerminal nt, String cost) {
        for (Rule rule : rules) {
            if (rule.rhs.id == nt.id) {
                String chainCost = cost + "+" + rule.cost;
                code.appendCommentln(rule.prettyPrint());
                code.appendStmtln(
                        String.format("record(p, %s, %s, %d)", 
                        rule.lhs.name,
                        chainCost,
                        rule.ruleno));
                genChainCost(code, rule.lhs, chainCost);
            }
        }
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
    
    
    static final int MAX_NT = 100;
    static class Terminal extends TreeNode {
        static int nextId = MAX_NT + 1;
        
        Terminal(String name) {
            super(name);
            this.id = nextId;
            nextId ++;
        }        
    }
    
    static class NonTerminal extends TreeNode {
        static int nextId = 1;
        NonTerminal(String name) {
            super(name + "_NT");
            this.id = nextId;
            nextId ++;
            
            if (nextId > MAX_NT)
                error("Exceeds maximum of non terminal");
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
        if (nonterminals.containsKey(name))
            return nonterminals.get(name);
        
        NonTerminal nt = new NonTerminal(name);
        nonterminals.put(name, nt);
        return nt;
    }
    
    public static Terminal findOrCreateTerm(String name) {
        if (terminals.containsKey(name))
            return terminals.get(name);
        
        Terminal t = new Terminal(name);
        terminals.put(name, t);
        return t;
    }
    
    public static List<Rule> rules = new ArrayList<Rule>();    
    public static void newRule(Rule r) {
        rules.add(r);
    }
    
    public static void error(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
