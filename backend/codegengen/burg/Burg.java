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
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;

public class Burg {
    public static final Map<String, Integer> termNames = new HashMap<String, Integer>();
    public static final Map<String, Integer> ntNames = new HashMap<String, Integer>();
    
    public static final List<Terminal> terms = new ArrayList<Terminal>();
    public static final List<NonTerminal> nonterms = new ArrayList<NonTerminal>();
    
    public static String ruleFile;
    public static String output;
    
    public static final String BURM_FILE = "BURM_GENERATED.java";
    
    public static boolean debug = false;
    
    public static final List<String>    MC_COND_JUMP = new ArrayList<String>();
    public static final List<String>    MC_UNCOND_JUMP = new ArrayList<String>();
    public static final String          MC_PHI = "mcphi";
    public static final List<String>    MC_RET = new ArrayList<String>();
    public static final List<String>    MC_MOV = new ArrayList<String>();
    public static final List<String>    MC_NOP = new ArrayList<String>();
    
    public static final List<String>    REG_GPR = new ArrayList<String>();
    public static final List<String>    REG_GPR_PARAM = new ArrayList<String>();
    public static final List<String>    REG_GPR_RET = new ArrayList<String>();
    
//    public static final Map<String, MCDefine> mcDefines = new HashMap<String, MCDefine>();
//    public static final Map<String, String> mcEmit = new HashMap<String, String>();
    
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
            
            checkCorrectness();
            
            // gen MC layer first, it creates a few things that we need
            generateMCLayer();
            generateBURM();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void checkCorrectness() {
        if (MC_MOV.isEmpty())
            error("need to define mov instruction by using .mc_mov in .target file. ");
        
        for (MCOp op : mcOps.values()) {
            if (!op.defined) {
                error("undefined mc op: " + op.name);
            }
        }
    }
    
    public static String targetName = null;
    
    private static String curCost = null;
    
    private static final String OPERAND_FROM_NODE = "operandFromNode";
    
    public static void generateBURM() {
        CodeBuilder code = new CodeBuilder();
        
        String pkg = "burm";
        
        code.appendStmtln("package " + pkg);
        code.appendStmtln("import static burm.BurmState.INFINITE");
        code.appendStmtln("import uvm.IRTreeNode");
        code.appendStmtln("import java.util.*");
        code.appendStmtln("import burm.mc.*");
        code.appendStmtln("import uvm.mc.*");
        code.appendStmtln("import uvm.inst.*");
        code.appendStmtln("import uvm.OpCode");
        code.appendln();
        
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
         * has code to emit
         */
        code.appendCommentln("rule code emission");
        code.append("static final boolean[] hasCodeEmission = {");
        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);
            
            if (r.mcEmissionRules != null && !r.mcEmissionRules.isEmpty())
                code.appendNoIndent("true");
            else code.appendNoIndent("false");
            
            if (i != rules.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendStmtln("}");
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
        
        /*
         * gen mc emission: List<MC> emitCode(IRTreeNode node)
         */
        code.appendln();
        code.appendln(String.format("public static List<AbstractMachineCode> emitCode(%s node) {", IR_NODE_TYPE));
        code.increaseIndent();
        code.appendStmtln("int leastCostRuleNo = -1");
        code.appendStmtln("short leastCost = Short.MAX_VALUE");
        code.appendStmtln("List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>()");
        code.appendln();
        
        code.appendStmtln("boolean unmatchedNode = true");
        code.appendln();
        
        code.appendln("for (int i = 0; i < node.state.cost.length; i++)");
        code.increaseIndent();
        code.appendln("if (leastCost > node.state.cost[i]) {");
        code.increaseIndent();
        code.appendStmtln("unmatchedNode = false");
        code.appendln("if (hasCodeEmission[node.state.rule[i]]) {");
        code.increaseIndent();
        code.appendStmtln("leastCost = node.state.cost[i]");
        code.appendStmtln("leastCostRuleNo = node.state.rule[i]");
        code.decreaseIndent();
        code.appendln("}");
        code.decreaseIndent();
        code.appendln("}");
        code.decreaseIndent();
        
        code.appendln();
        code.appendln("if (unmatchedNode) {");
        code.increaseIndent();
        code.appendStmtln("System.out.println(\"node: \" + node.prettyPrint() + \"doesnt have a match in instr selection\")");
        code.appendStmtln("System.exit(-1)");
        code.decreaseIndent();
        code.appendln("}");
        
        code.appendln();
        code.appendln("for (int i = 0; i < node.getArity(); i++) ");
        code.increaseIndent();
        code.appendStmtln("ret.addAll(emitCode(node.getChild(i)))");
        code.decreaseIndent();
        
        code.appendln();
        code.appendStmtln("ret.addAll(emitCode(node, leastCostRuleNo))");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        code.appendln();
        
        /*
         * gen mc emission: List<MC> emitCode(IRTreeNode node, int rule) 
         */
        code.appendln(String.format(
                "public static List<AbstractMachineCode> emitCode(%s node, int rule) {",
                IR_NODE_TYPE));
        
        code.increaseIndent();
        code.appendStmtln(String.format("List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>()"));
        code.appendStmtln("if (rule == -1) return ret");
        code.appendln();
        
        code.appendln("switch (rule) {");
        for (Rule rule : rules) {
            code.appendln(String.format("case %d:", rule.ruleno));
            code.increaseIndent();
            code.appendCommentln(rule.prettyPrint());
            
            /*
             * machine code gen from rules
             */
            if (rule.mcEmissionRules != null && !rule.mcEmissionRules.isEmpty()) {
                for (int i = 0; i < rule.mcEmissionRules.size(); i++) {
                    MCRule mc = rule.mcEmissionRules.get(i);
                    System.out.println("emit " + mc.op.name);
                    String opClass = targetName + mc.op.name;
                    String var = "mc" + rule.ruleno + "_" + i;
                    code.appendStmtln(String.format(
                            "%s %s = new %s()", opClass, var, opClass));
                    
                    for (int j = 0; j < mc.operands.size(); j++) {
                        CCTOperand operand = mc.operands.get(j);
                        String newOperandStr;
                        if (mc.op.opDataType != null)
                            newOperandStr = getOperandCreation(operand, mc.op.opDataType[j]);
                        else newOperandStr = getOperandCreation(operand, -1);   // -1 will be ignored
                        
                        code.appendStmtln(String.format(
                                "%s.setOperand%d(%s)", var, j, newOperandStr));
                    }
                    
                    code.appendStmtln(String.format("%s.setNodeIndex(node.getId())", var));
                    
                    if (mc.reg != null)
                        code.appendStmtln(String.format("%s.setReg((MCRegister)%s)", var, getOperandCreation(mc.reg, mc.op.resDataType)));
                    
                    code.appendStmtln(String.format(
                            "ret.add(%s)", var));
                    
                    code.appendln();
                }
            }
            
            code.appendStmtln("return ret");
            code.decreaseIndent();
        }
        
        code.appendln("default:");
        code.increaseIndent();
        code.appendStmtln("System.out.println(\"unmatched node.\")");
        code.appendStmtln("System.exit(3)");
        code.decreaseIndent();
        
        code.appendln("}");
        code.appendStmtln("return null");
        
        code.appendln("}");
        
        /*
         * operandFromNode()
         */
        code.appendln(String.format(
                "public static MCOperand operandFromNode(%s node, int dataType) {", IR_NODE_TYPE));
        code.increaseIndent();
        code.appendln("switch(node.getOpcode()) {");
        code.appendln("case OpCode.INT_IMM:");
        code.increaseIndent();
        code.appendln("return new uvm.mc.MCIntImmediate(((uvm.IntImmediate)node).getValue());");
        code.decreaseIndent();
        code.appendln("case OpCode.REG:");
        code.increaseIndent();
        code.appendln("return uvm.mc.MCRegister.findOrCreate(((uvm.Register)node).getName(), uvm.mc.MCRegister.OTHER_SYMBOL_REG, dataType);");
        code.decreaseIndent();
        code.appendln("case OpCode.LABEL:");
        code.increaseIndent();
        code.appendln("return new uvm.mc.MCLabel(((uvm.Label)node).getName());");
        code.decreaseIndent();
        code.appendln("default:");
        code.increaseIndent();
        code.appendln("return uvm.mc.MCRegister.findOrCreate(\"res_reg\"+node.getId(), uvm.mc.MCRegister.RES_REG, dataType);");
        code.decreaseIndent();
        code.appendln("}");
        code.decreaseIndent();
        code.appendln("}");

        // end of class
        code.decreaseIndent();
        code.appendln("}");
        
        System.out.println("========BURM Generated========");
        System.out.println(code.toString());
        
        writeTo(output + BURM_FILE, code.toString());
    }
    
    public static String getOperandCreation(CCTOperand operand, int dataType) {
        String newOperandStr = null;
                
        if (operand instanceof OpdIntImmediate) {
            newOperandStr = "new MCIntImmediate(" + ((OpdIntImmediate) operand).value + ")";
        } else if (operand instanceof OpdLabel) {
            newOperandStr = "new MCLabel(" + ((OpdLabel)operand).name + ")";
        } else if (operand instanceof OpdRegister) {
            switch (((OpdRegister) operand).type) {
            case OpdRegister.MACHINE_REG:
            case OpdRegister.OTHER_SYMBOL_REG:
                newOperandStr = String.format(
                        "MCRegister.findOrCreate(\"%s\", %d, %d)",  
                        ((OpdRegister) operand).name, 
                        ((OpdRegister) operand).type,
                        dataType);
                break;
            case OpdRegister.RES_REG:
                newOperandStr = String.format(
                        "MCRegister.findOrCreate(\"%s\"+%s, %d, %d)", 
                        ((OpdRegister) operand).name,
                        "node.getId()",
                        ((OpdRegister) operand).type,
                        dataType);
                break;
                
            // temp registers are also indexed
            case OpdRegister.TMP_REG:
                newOperandStr = String.format(
                        "MCRegister.findOrCreate(\"%s%s_\"+%s, %d, %d)", 
                        ((OpdRegister) operand).name,
                        getCompileTimeOperand(((OpdRegister) operand).index, dataType),
                        "node.getId()",
                        MCRegister.OTHER_SYMBOL_REG,
                        dataType
                        );
                break;
            
            // these two can be indexed
            case OpdRegister.PARAM_REG:
            case OpdRegister.RET_REG:
                newOperandStr = String.format(
                        "MCRegister.findOrCreate(\"%s\"+%s, %d, %d)",
                        ((OpdRegister) operand).name,
                        getCompileTimeOperand(((OpdRegister) operand).index, dataType),
                        ((OpdRegister) operand).type,
                        dataType);
                break;
            }
        } else {
            newOperandStr = getCompileTimeOperand(operand, dataType);
        }
        
        return newOperandStr;
    }
    
    public static String getCompileTimeOperand(CCTOperand operand, int dataType) {
        if (operand instanceof OpdIntImmediate)
            return Long.toString(((OpdIntImmediate) operand).value);
        else if (operand instanceof OpdNode) {
            StringBuilder ret = new StringBuilder();
            ret.append("operandFromNode(node");
            for (CCTOperand i : ((OpdNode) operand).index) {
                ret.append(".getChild(" + getCompileTimeOperand(i, dataType) + ")");
            }
            ret.append(", " + dataType + ")");
            return ret.toString();
        } else if (operand instanceof OpdNodeFunc) {
            if (((OpdNodeFunc) operand).receiver == null)
                return "node." + ((OpdNodeFunc)operand).funcName + "()";
            else
                return String.format(
                        "((%s)node).%s()", 
                        ((OpdNodeFunc) operand).receiver, 
                        ((OpdNodeFunc) operand).funcName);
        } else {
            error("illegal compile-time operand: " + operand.getClass().toString());
            return null;
        }
    }
    
    public static void writeTo(String file, String code) {
        BufferedWriter writer = null;
        try {
            File outFile = new File(file);
            outFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(code);
        } catch (IOException e) {
            error("Error when writing to file:" + file);
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
        String emit;
        int resDataType;
        int[] opDataType;
        boolean defined = false;
    }
    
    public static final HashMap<String, MCOp> mcOps = new HashMap<String, MCOp>();
    
    static class MCRule {
        MCOp op;
        List<CCTOperand> operands = new ArrayList<CCTOperand>();
        CCTOperand reg;
        
        MCRule(MCOp op, List<CCTOperand> operands, CCTOperand reg) {
            this.op = op;
            this.operands = operands;
            this.reg = reg;
        }

        String prettyPrint() {
            return op.name + "(" + op.operands + ")";
        }
    }
    
    /*
     * compiler-compile time operands
     */
    static abstract class CCTOperand {
        
    }
    
    static class OpdLabel extends CCTOperand {
        String name;
        OpdLabel(String name) {
            this.name = name;
        }
    }
    
    static class OpdIntImmediate extends CCTOperand {
        long value;
        OpdIntImmediate(long value) {
            this.value = value;
        }
    }
    
    static class OpdRegister extends CCTOperand {   
        public static final int RES_REG     = 0;
        public static final int RET_REG     = 1;
        public static final int PARAM_REG   = 2;
        public static final int MACHINE_REG = 3;
        public static final int OTHER_SYMBOL_REG  = 4;
        public static final int TMP_REG     = 5;
        
        public static final int DATA_DP = 100;
        public static final int DATA_SP = 101;
        public static final int DATA_GPR = 102;
        public static final int DATA_MEM = 103;
        
        int type;
        int dataType;
        String name;
        
        CCTOperand index;
        
        private OpdRegister(String name, int type, int dataType) {
            this.name = name;
            this.type = type;
            this.dataType = dataType;
        }
        
        public void setIndex(CCTOperand index) {
            this.index = index;
        }
        
        public CCTOperand getIndex() {
            return index;
        }
        
        static final HashMap<String, OpdRegister> regs = new HashMap<String, OpdRegister>();
        
        public static OpdRegister findOrCreate(String name, int type) {
            return findOrCreate(name, type, DATA_GPR);
        }
        
        public static OpdRegister findOrCreate(String name, int type, int dataType) {
            if (regs.containsKey(name))
                return regs.get(name);
            
            OpdRegister ret = new OpdRegister(name, type, dataType);
            regs.put(name, ret);
            return ret;
        }
        
        public static void clearRegisters() {
            regs.clear();
        }
    }
    
    static class OpdNode extends CCTOperand {
        List<CCTOperand> index;
        
        OpdNode(int i) {
            index = new ArrayList<CCTOperand>();
            index.add(new OpdIntImmediate(i));
        }
        
        OpdNode(List<CCTOperand> index) {
            this.index = index;
        }
    }
    
    static class OpdNodeFunc extends CCTOperand {
        String funcName;
        String receiver;
        
        OpdNodeFunc(String name) {
            this.funcName = name;
        }
        
        OpdNodeFunc(String name, String receiver) {
            this.funcName = name;
            this.receiver = receiver;
        }
    }
    
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
        
        generateMCDriver();
    }
    
    public static void generateMCOpAbstractClass() {
        abstractOpClass = targetName + abstractOpClass;
        
        CodeBuilder code = new CodeBuilder();
        
        code.appendStmtln("package burm.mc");
        code.appendStmtln("import uvm.mc.*");
        code.appendStmtln("import java.util.*");
        
        code.appendln("public abstract class " + abstractOpClass + " extends AbstractMachineCode {");
//        code.increaseIndent();
//        
//        code.appendStmtln("public String name");
//        code.appendStmtln("public List<Operand> operands;");
//        code.decreaseIndent();
        code.appendln("}");
        
        writeTo(output + "mc/" + abstractOpClass + ".java", code.toString());
    }
    
    public static void generateMCOpClass(MCOp op) {
        CodeBuilder code = new CodeBuilder();
        
        String opFullName = targetName + op.name;
        
        code.appendStmtln("package burm.mc");
        code.appendStmtln("import uvm.mc.*");
        code.appendStmtln("import java.util.*");
        
        code.appendln(String.format("public class %s extends %s{", opFullName, abstractOpClass));
        
        code.increaseIndent();
        code.appendln(String.format("public %s() {", opFullName));
        code.increaseIndent();
        code.appendStmtln("this.name = \"" + op.name + "\"");
        code.appendStmtln(String.format("this.operands = Arrays.asList(new MCOperand[%d])", op.operands));
        code.decreaseIndent();
        code.appendln("}");
        
        // set operands
        code.appendln();
        for (int i = 0; i < op.operands; i++) {
            code.appendln(String.format(
                    "public void setOperand%d(MCOperand op) {operands.set(%d, op);}", i, i));
        }
        
        // is mc phi?
        if (op.name.equals(MC_PHI)) {
            code.appendln();
            code.appendln("@Override public boolean isPhi() {return true; }");
        }
        
        // is mc jump?
        if (MC_COND_JUMP.contains(op.name)) {
            code.appendln();
            code.appendln("@Override public boolean isCondJump() {return true; }");
        }
        
        if (MC_UNCOND_JUMP.contains(op.name)) {
            code.appendln();
            code.appendln("@Override public boolean isUncondJump() {return true; }");
        }
        
        // is mc ret?
        if (MC_RET.contains(op.name)) {
            code.appendln();
            code.appendln("@Override public boolean isRet() {return true;}");
        }
        
        // is mc mov?
        if (MC_MOV.contains(op.name)) {
            code.appendln();
            code.appendln("@Override public boolean isMov() {return true;}");
        }
        
        // emit code?
        if (mcOps.containsKey(op.name)) {
            code.appendln();
            code.appendln("@Override public String emit() {");
            code.increaseIndent();
            code.appendStmtln("return String.format(" + mcOps.get(op.name).emit + ")");
            code.decreaseIndent();
            code.appendln("}");
        }
        
        code.decreaseIndent();
        code.appendln();
        code.appendln("}");
        
        writeTo(output + "mc/" + opFullName + ".java", code.toString());
    }
    
    public static void generateMCDriver() {
        CodeBuilder code = new CodeBuilder();
        
        code.appendStmtln("package burm.mc");
        code.appendStmtln("import uvm.mc.*");
        
        String driver = targetName + "Driver";
        code.appendln(String.format("public class %s extends AbstractMCDriver {", driver));
        code.increaseIndent();
        
        // mov
        code.appendln("public AbstractMachineCode genMove(MCRegister dest, MCOperand src) {");
        code.increaseIndent();
        
        String mov = targetName + MC_MOV.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", mov, mov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setReg(dest)");
        code.appendStmtln("return ret");
        
        code.decreaseIndent();
        code.appendln("}");
        
        // all movs
        for (int i = 0; i < MC_MOV.size(); i++) {
            String s = targetName + MC_MOV.get(i);
            code.appendln(String.format("public AbstractMachineCode gen%s(MCRegister dest, MCOperand src) {", s));
            
            code.increaseIndent();
            code.appendStmtln(String.format("%s ret = new %s()", s, s));
            code.appendStmtln("ret.setOperand0(src)");
            code.appendStmtln("ret.setReg(dest)");
            code.appendStmtln("return ret");
            
            code.decreaseIndent();
            code.appendln("}");
        }
        
        // unconditional jump
        code.appendln("public AbstractMachineCode genJmp(MCLabel target) {");
        code.increaseIndent();
        String uncondJmp = targetName + MC_UNCOND_JUMP.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", uncondJmp, uncondJmp));
        code.appendStmtln("ret.setOperand0(target)");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // ret
        code.appendln("@Override public AbstractMachineCode genRet() {");
        code.increaseIndent();
        String retMC = targetName + MC_RET.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", retMC, retMC));
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // nop
        code.appendln("@Override public AbstractMachineCode genNop() {");
        code.increaseIndent();
        String nopMC = targetName + MC_NOP.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", nopMC, nopMC));
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // opposite jmp
        code.appendln("@Override public AbstractMachineCode genOppositeCondJump(AbstractMachineCode orig) {");
        code.increaseIndent();
        code.appendStmtln("String name = orig.getName()");
        code.appendStmtln("AbstractMachineCode ret = null;");
        code.appendln();
        for (int i = 0; i < MC_COND_JUMP.size() - 1; i += 2) {
            String jmp1 = MC_COND_JUMP.get(i);
            String jmp2 = MC_COND_JUMP.get(i + 1);
            
            String elseStr = i == 0 ? "" : "else ";
            code.appendln(
                    String.format("%sif (name.equals(\"%s\")) ret = new %s%s();", 
                            elseStr,
                            jmp1, targetName, jmp2));
            code.appendln(String.format("else if (name.equals(\"%s\")) ret = new %s%s();",
                            jmp2, targetName, jmp1));
        }
        code.appendln();
        code.appendStmtln("ret.setOperand(0, orig.getOperand(0))");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // GPRs
        code.appendln();
        code.append("public static final String[] GPR = {");
        for (int i = 0; i < Burg.REG_GPR.size(); i++) {
            String s = Burg.REG_GPR.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_GPR.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfGPR() {return GPR.length;}");
        code.appendln("@Override public String getGPRName(int i) {return GPR[i];}");
        
        // GPR param
        code.appendln();
        code.append("public static final String[] GPR_PARAM = {");
        for (int i = 0; i < Burg.REG_GPR_PARAM.size(); i++) {
            String s = Burg.REG_GPR_PARAM.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_GPR_PARAM.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfGPRParam() {return GPR_PARAM.length;}");
        code.appendln("@Override public String getGPRParamName(int i) {return GPR_PARAM[i];}");
        
        // GPR ret
        code.appendln();
        code.append("public static final String[] GPR_RET = {");
        for (int i = 0; i < Burg.REG_GPR_RET.size(); i++) {
            String s = Burg.REG_GPR_RET.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_GPR_RET.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfGPRRet() {return GPR_RET.length;}");
        code.appendln("@Override public String getGPRRetName(int i) {return GPR_RET[i];}");
        
        // MC op emit
        // TODO this should go to target description file instead of hard coded here
        code.appendln();
        code.appendln("public static String emitOp(MCOperand op) {");
        code.increaseIndent();
        code.appendln("if (op instanceof MCLabel)");
        code.appendln("return ((MCLabel) op).getName();");
        code.appendln("if (op instanceof MCIntImmediate)");
        code.appendln("return \"$\"+((MCIntImmediate) op).getValue();");
        code.appendln("if (op instanceof MCRegister)");
        code.appendln("return \"%\"+((MCRegister) op).REP().getName();");
        code.appendln("return \"error\";");
        code.decreaseIndent();
        code.appendln("}");
        
        // end of class
        code.decreaseIndent();
        code.appendln("}");
        
        writeTo(output + "mc/" + driver + ".java", code.toString());
    }
    
    /*
     * code string builder wrapper
     */
    
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
    
    /*
     * pattern tree node
     */
    
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
    
    /*
     * pattern rules
     */
    
    static class Rule {
        static int count = 0;
        
        int ruleno;
        NonTerminal lhs;
        TreeNode rhs;
        int cost;
        
        List<MCRule> mcEmissionRules;
        
        String asOperand;
        
        Rule(NonTerminal lhs, TreeNode rhs, int cost) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.cost = cost;
            
            this.ruleno = count;
            count++;
        }
        
        void setMCEmissionRules(List<MCRule> rules) {
            this.mcEmissionRules = rules;
        }
        
        String prettyPrint() {
            StringBuilder builder = new StringBuilder();
            builder.append(lhs.prettyPrint() + " := " + rhs.prettyPrint() + 
                    " (cost:" + cost + ", ruleno:" + ruleno + ")");
            
            if (mcEmissionRules != null) {
                builder.append(" > ");
                for (MCRule mr : mcEmissionRules)
                    builder.append(mr.prettyPrint() + ",");
            }
            
            return builder.toString();
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
