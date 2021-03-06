package burg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uvm.OpCode;
import uvm.mc.MCLabel;
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
    public static final List<String>    MC_PHI = new ArrayList<String>();
    public static final List<String>    MC_RET = new ArrayList<String>();
    public static final List<String>    MC_MOV = new ArrayList<String>();
    public static final List<String>    MC_NOP = new ArrayList<String>();
    public static final List<String>    MC_DPMOV = new ArrayList<String>();
    public static final List<String>    MC_SPMOV = new ArrayList<String>();
    public static String                INST_PTR;
    public static String                STACK_PTR;
    public static String                FRAME_PTR;
    public static final List<String>    MC_CALL = new ArrayList<String>();
    public static final List<String>	MC_CALL_EXP = new ArrayList<String>();
    public static final List<String>	MC_TAILCALL = new ArrayList<String>();
    public static final List<String>	MC_CMP = new ArrayList<String>();
    
    public static final List<String>    REG_GPR = new ArrayList<String>();
    public static final List<String>    REG_GPR_PARAM = new ArrayList<String>();
    public static final List<String>    REG_GPR_RET = new ArrayList<String>();
    
    public static final List<String>    REG_FP = new ArrayList<String>();
    public static final List<String>    REG_FP_PARAM = new ArrayList<String>();
    public static final List<String>    REG_FP_RET = new ArrayList<String>();
    
    public static final List<String>    REG_CALLEE_SAVE = new ArrayList<String>();
    
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
        if (targetName == null)
            error("need to define target name using .target");
        
        if (INST_PTR == null)
            error("need to declare instruction pointer register for the target");
        
        if (MC_MOV.isEmpty())
            error("need to define mov instruction by using .mc_mov in .target file. ");
        
        if (MC_DPMOV.isEmpty() || MC_SPMOV.isEmpty())
            error("need to define double/single-precition fp mov instruction by using .mc_dpmov/.mc_spmov in .target file");
        
        if (MC_CALL.isEmpty())
            error("need to define call mc");
        
        for (MCOp op : mcOps.values()) {
            if (!op.defined) {
                error("undefined mc op: " + op.name);
            }
        }
    }
    
    public static String targetName = null;
    
    private static String curCost = null;
    private static ArrayList<String> curChildren = null;
    
    private static String genChildrenArray(ArrayList<String> children) {
    	if (children == null)
    		return "null";
    	
    	StringBuilder ret = new StringBuilder();
    	ret.append("new Integer[]{");
    	for (int i = 0; i < children.size(); i++) {
    		ret.append(children.get(i));
    		if (i != children.size() - 1)
    			ret.append(',');
    	}
    	ret.append('}');
    	return ret.toString();
    }
    
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
        
        generateBURM_f_state(code);
        
        /*
         * gen mc emission: List<MC> emitCode(IRTreeNode node, int supposedNT)
         */
        generateBURM_f_emitCode(code);
        
        /*
         * gen mc emission: List<MC> emitCode(IRTreeNode node, int rule) 
         */
        generateBURM_f_emitCodeByRule(code);
        
        /*
         * TODO shouldnt hard-code it here
         * operandFromNode()
         * 
         * there is another copy of this code in X64CallConvention
         */
        generateBURM_f_operandFromNode(code);

        // end of class
        code.decreaseIndent();
        code.appendln("}");
        
        System.out.println("========BURM Generated========");
        System.out.println(code.toString());
        
        writeTo(output + BURM_FILE, code.toString());
    }

	private static void generateBURM_f_operandFromNode(CodeBuilder code) {
		code.appendln(String.format(
                "public static MCOperand operandFromNode(%s node, int dataType) {", IR_NODE_TYPE));
        code.increaseIndent();
        code.appendln("MCOperand ret;");
        code.appendln("switch(node.getOpcode()) {");
        code.appendln("case OpCode.INT_IMM:");
        code.increaseIndent();
        code.appendln("ret = new uvm.mc.MCIntImmediate(((uvm.IntImmediate)node).getValue()); break;");
        code.decreaseIndent();
        code.appendln("case OpCode.FP_SP_IMM:");
        code.increaseIndent();
        code.appendln("ret = new uvm.mc.MCSPImmediate(((uvm.FPImmediate)node).getFloat()); break;");
        code.decreaseIndent();
        code.appendln("case OpCode.FP_DP_IMM:");
        code.increaseIndent();
        code.appendln("ret = new uvm.mc.MCDPImmediate(((uvm.FPImmediate)node).getDouble()); break;");
        code.decreaseIndent();
        code.appendln("case OpCode.REG_I1:");
        code.appendln("case OpCode.REG_I8:");
        code.appendln("case OpCode.REG_I16:");
        code.appendln("case OpCode.REG_I32:");
        code.appendln("case OpCode.REG_I64:");
        code.increaseIndent();
        code.appendln("ret = uvm.mc.MCRegister.findOrCreate(((uvm.Register)node).getName(), uvm.mc.MCRegister.OTHER_SYMBOL_REG, dataType);");
        code.appendln("break;");
        code.decreaseIndent();
        code.appendln("case OpCode.LABEL:");
        code.increaseIndent();
        code.appendln("ret = new uvm.mc.MCLabel(((uvm.Label)node).getName()); break;");
        code.decreaseIndent();
        code.appendln("default:");
        code.increaseIndent();
        code.appendln("ret = uvm.mc.MCRegister.findOrCreate(\"res_reg\"+node.getId(), uvm.mc.MCRegister.RES_REG, dataType);");
        code.appendln("break;");
        code.decreaseIndent();
        code.appendln("}");
        
        code.appendStmtln("node.setMCOp(ret)");
        code.appendStmtln("ret.highLevelOp = node");
        code.appendStmtln("return ret");
        
        code.decreaseIndent();
        code.appendln("}");
	}

	private static void generateBURM_f_emitCodeByRule(CodeBuilder code) {
		code.appendln(String.format(
                "public static List<AbstractMachineCode> emitCodeByRule(%s node, int rule) {",
                IR_NODE_TYPE));
        
        code.increaseIndent();
        code.appendStmtln("node.pickedRule = rule");
        code.appendStmtln(String.format("List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>()"));
        code.appendStmtln("if (rule == -1) return ret");
        code.appendln();
        
        code.appendln("switch (rule) {");
        for (Rule rule : rules) {
            code.appendln(String.format("case %d:", rule.ruleno));
            code.increaseIndent();
            code.appendCommentln(rule.prettyPrint());
            
            System.out.println("---Rule:" + rule.prettyPrint() + "---");
            
            /*
             * machine code gen from rules
             */
            if (rule.mcEmissionRules != null && !rule.mcEmissionRules.isEmpty()) {            	
                for (int i = 0; i < rule.mcEmissionRules.size(); i++) {
                    MCRule mc = rule.mcEmissionRules.get(i);
                    System.out.println("emit " + mc.prettyPrint());
                    String opClass = targetName + mc.op.name;
                    String var = "mc" + rule.ruleno + "_" + i;
                    code.appendStmtln(String.format(
                            "%s %s = new %s()", opClass, var, opClass));
                    
                    code.appendStmtln(String.format("%s.setHighLevelIR(node)", var));
                    
                    for (int j = 0; j < mc.operands.size(); j++) {
                        CCTOperand operand = mc.operands.get(j);
                    	System.out.println("  op" + j + ": " + operand.toString());
                    	
                        String newOperandStr;
                        if (mc.op.opDataType != null)
                            newOperandStr = getOperandCreation(operand, mc.op.opDataType[j]);
                        else newOperandStr = getOperandCreation(operand, -1);   // -1 will be ignored
                        
                        code.appendStmtln(String.format(
                                "%s.setOperand%d(%s)", var, j, newOperandStr));
                    }
                    
                    code.appendStmtln(String.format("%s.setNodeIndex(node.getId())", var));
                    
                    if (mc.reg != null)
                        code.appendStmtln(String.format("%s.setDefine(%s)", var, getOperandCreation(mc.reg, mc.op.resDataType)));
                    
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
	}

	private static void generateBURM_f_emitCode(CodeBuilder code) {
		code.appendln();
        code.appendln(String.format("public static List<AbstractMachineCode> emitCode(%s node, int supposedNT) {", IR_NODE_TYPE));
        code.increaseIndent();
        code.appendStmtln("int leastCostRuleNo = -1");
        code.appendStmtln("short leastCost = Short.MAX_VALUE");
        code.appendStmtln("int leastCostNT = -1");
        code.appendStmtln("List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>()");
        code.appendln();
        
        code.appendStmtln("boolean unmatchedNode = true");
        code.appendln();
        
        code.appendln("for (int i = 0; i < node.state.cost.length; i++)");
        code.increaseIndent();
        code.appendln("if (leastCost > node.state.cost[i]) {");
        code.increaseIndent();
        code.appendStmtln("unmatchedNode = false");
        code.appendln("if (hasCodeEmission[node.state.rule[i]] && (supposedNT == -1 || supposedNT == i)) {");
        code.increaseIndent();
        code.appendStmtln("leastCost = node.state.cost[i]");
        code.appendStmtln("leastCostRuleNo = node.state.rule[i]");
        code.decreaseIndent();
        code.appendln("}");
        code.appendStmtln("leastCostNT = i");
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
        code.appendStmtln("ret.addAll(emitCode(node.getChild(i), node.state.childNodes.get(leastCostNT).get(i)))");
        code.decreaseIndent();
        
        code.appendln();
        code.appendStmtln("ret.addAll(emitCodeByRule(node, leastCostRuleNo))");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        code.appendln();
	}

	private static void generateBURM_f_state(CodeBuilder code) {
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
        code.appendStmtln("p.childNodes = new ArrayList<List<Integer>>()");
        for (int i = 0; i <= ntCount; i++)
        	code.appendStmtln("p.childNodes.add(null)");
        
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
                        curChildren = new ArrayList<String>();
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
                    code.appendStmtln(String.format("p.record(%s, c, %d, %s)", rule.lhs.name, rule.ruleno, genChildrenArray(curChildren)));
                    
                    // chain cost
                    code.appendln();
                    code.appendCommentln("chain cost");
                    genChainCost(code, rule.ruleno, rule.lhs, "c");
                    
                    if (needToMatchChildren) {
                        code.decreaseIndent();
                        code.appendln("}");
                        curCost = null;
                        curChildren = null;
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
	}
    
    public static String getOperandCreation(CCTOperand operand, int dataType) {        
        switch(dataType) {
        case MCRegister.DATA_DP:
        case OpdRegister.DATA_DP_OR_MEM:
        	dataType = MCRegister.DATA_DP;
        	break;
        case MCRegister.DATA_SP:
        case OpdRegister.DATA_SP_OR_MEM:
        	dataType = MCRegister.DATA_SP;
        	break;
        case MCRegister.DATA_GPR:
        case OpdRegister.DATA_GPR_OR_MEM:
        	dataType = MCRegister.DATA_GPR;
        	break;
        case MCRegister.DATA_MEM:
            break;
        case -1:
            break;
        default:
            System.out.println("unexpected data type: " + dataType);
            Thread.dumpStack();
            System.exit(6);
        }
        
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
        }
        else if (operand instanceof OpdMemOperand) {
        	CCTOperand index = ((OpdMemOperand) operand).index;
        	
        	if (index instanceof OpdIntImmediate && ((OpdIntImmediate)index).value == 0) {
        		newOperandStr = String.format(
        				"new MCDispMemoryOperand((MCRegister) %s, %s)",
        				getOperandCreation(((OpdMemOperand) operand).base, dataType),
        				getCompileTimeOperand(((OpdMemOperand) operand).disp, 0));
        	} else {        	
	    		newOperandStr = String.format(
	    				"new MCIndexedDispMemoryOperand((MCRegister) %s, %s, (MCRegister) %s, %s)",
	    				getOperandCreation(((OpdMemOperand) operand).base, dataType),
	    				getCompileTimeOperand(((OpdMemOperand) operand).disp, 0),
	    				index == null ? "null" : getOperandCreation(((OpdMemOperand) operand).index, MCRegister.DATA_GPR),
	    				getCompileTimeOperand(((OpdMemOperand) operand).scale, 0));
        	}
        }
        
        else {
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
            return genChainCallFromOpdNodeFunc((OpdNodeFunc) operand);
        } else {
        	if (operand == null) 
        		error("met CCTOperand as null");
        	else error("illegal compile-time operand: " + operand.getClass().toString());
            return null;
        }
    }

    private static String genChainCallFromOpdNodeFunc(OpdNodeFunc operand) {
        String chainedCalls = "";
        for (String f : ((OpdNodeFunc)operand).funcName) {
            chainedCalls += "." + f + "()";
        }
        
        if (((OpdNodeFunc) operand).receiver == null)
            return "node" + chainedCalls;
        else
            return String.format(
                    "((%s)node)%s", 
                    ((OpdNodeFunc) operand).receiver, 
                    chainedCalls);
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
                curChildren.add(cur.name);
            } else {
                // cur is terminal
                ret.append(String.format(base + "leaves[%d].node.getOpcode() == %s", i, cur.name));
                curChildren.add("-1");
            }
            
            if (cur.children.size() != 0)
                ret.append(" && " + matchChildren(cur, String.format(base+"leaves[%d].", i)));
            
            if (i != node.children.size() - 1)
                ret.append(" && ");
        }
        
        return ret.toString();
    }
    
    public static void genChainCost(CodeBuilder code, int ruleno, NonTerminal nt, String cost) {
        for (Rule rule : rules) {
            if (rule.rhs instanceof NonTerminal && rule.rhs.id == nt.id) {
                String chainCost = cost + "+" + rule.cost;
                code.appendCommentln(rule.prettyPrint());
                code.appendStmtln(
                        String.format("p.record(%s, %s, %d, %s)", 
                        rule.lhs.name,
                        chainCost,
                        ruleno,
                        genChildrenArray(curChildren)));
                genChainCost(code, ruleno, rule.lhs, chainCost);
            }
        }
    }
    
    /*
     * Machine Code Layer
     */
    static class MCOp {
        String name;
        int operands = 0;
        String emit;
        int resDataType = -1;
        int[] opDataType;
        boolean defined = false;
        List<String> implicitUses = new ArrayList<String>();
        List<Integer> implicitUsesDataTypes = new ArrayList<Integer>();
        List<String> implicitDefines = new ArrayList<String>();
        List<Integer> implicitDefinesDataTypes = new ArrayList<Integer>();
    }
    
    static class OperandEmit {
        String operandName;
        String format;
        List<OpdNodeFunc> funcCalls = new ArrayList<OpdNodeFunc>();
    }
    
    public static final HashMap<String, OperandEmit> operandEmit = new HashMap<String, OperandEmit>();
    
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
        
        boolean isZero() {
        	return value == 0;
        }
    }
    
    static class OpdMemOperand extends CCTOperand {
    	CCTOperand base;
    	CCTOperand disp;
    	CCTOperand index;
    	CCTOperand scale;
    	
    	OpdMemOperand(CCTOperand base, CCTOperand disp, CCTOperand index, CCTOperand scale) {
    		this.base = base;
    		this.disp = disp;
    		this.index = index;
    		this.scale = scale;
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
        public static final int DATA_DP_OR_MEM = 200;
        public static final int DATA_SP = 101;
        public static final int DATA_SP_OR_MEM = 201;
        public static final int DATA_GPR = 102;
        public static final int DATA_GPR_OR_MEM = 202;
        public static final int DATA_OTH = 103;
        
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
        List<String> funcName;
        String receiver;
        
        OpdNodeFunc(List<String> name) {
            this.funcName = name;
        }
        
        OpdNodeFunc(List<String> name, String receiver) {
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
        // set name
        code.appendStmtln("this.name = \"" + op.name + "\"");
        // create operand list
        code.appendStmtln(String.format("this.operands = Arrays.asList(new MCOperand[%d])", op.operands));
        // set opRegOnly
        if (op.opDataType != null) {
	        code.append("this.opRegOnly = Arrays.asList(");
	        for (int i = 0; i < op.opDataType.length; i++) {
	        	int dataType = op.opDataType[i];
	        	if (dataType == OpdRegister.DATA_DP 
	        			|| dataType == OpdRegister.DATA_SP
	        			|| dataType == OpdRegister.DATA_GPR) {
	        		code.appendNoIndent("true");
	        	} else {
	        		code.appendNoIndent("false");
	        	}
	        	
	        	if (i != op.opDataType.length - 1)
	        		code.appendNoIndent(",");
	        }
	        code.appendNoIndent(");\n");
        }
        // set defineRegOnly
        if (op.resDataType == OpdRegister.DATA_DP 
        		|| op.resDataType == OpdRegister.DATA_SP 
        		|| op.resDataType == OpdRegister.DATA_GPR) {
        	code.appendStmtln("this.defineRegOnly = true");
        } else {
        	code.appendStmtln("this.defineRegOnly = false");
        }
        // implicit uses
        for (int i = 0; i < op.implicitUses.size(); i++) {
            code.appendStmtln(
                    String.format("this.implicitUses.add(MCRegister.findOrCreate(\"%s\", MCRegister.MACHINE_REG, %d))", 
                            op.implicitUses.get(i), op.implicitUsesDataTypes.get(i)));
        }
        // implicit defines
        for (int i = 0; i < op.implicitDefines.size(); i++) {
            code.appendStmtln(
                    String.format("this.implicitDefines.add(MCRegister.findOrCreate(\"%s\", MCRegister.MACHINE_REG, %d))", 
                            op.implicitDefines.get(i), op.implicitDefinesDataTypes.get(i)));
        }
        code.decreaseIndent();
        code.appendln("}");
        
        // set operands
        code.appendln();
        for (int i = 0; i < op.operands; i++) {
            code.appendln(String.format(
                    "public void setOperand%d(MCOperand op) {operands.set(%d, op);}", i, i));
        }
        
        // is mc phi?
        if (MC_PHI.contains(op.name)) {
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
        
        if (MC_CALL.contains(op.name)) {
            code.appendln();
            code.appendln("@Override public boolean isCall() {return true;}");
        }
        
        if (MC_CALL_EXP.contains(op.name)) {
        	code.appendln();
        	code.appendln("@Override public boolean isCallWithExp() {return true;}");
        }
        
        if (MC_TAILCALL.contains(op.name)) {
        	code.appendln();
        	code.appendln("@Override public boolean isTailCall() {return true;}");
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
        
        // an instance
        code.appendStmtln(String.format("public static %s v = new %s()", driver, driver));
        code.appendln();
        
        // mov
        code.appendln("@Override public AbstractMachineCode genMove(MCRegister dest, MCOperand src) {");
        code.increaseIndent();        
        String mov = targetName + MC_MOV.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", mov, mov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
        code.appendStmtln("return ret");        
        code.decreaseIndent();
        code.appendln("}");
        
        // mov
        code.appendln("@Override public AbstractMachineCode genMove(MCOperand dest, MCOperand src) {");
        code.increaseIndent();        
        code.appendStmtln(String.format("%s ret = new %s()", mov, mov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
        code.appendStmtln("return ret");        
        code.decreaseIndent();
        code.appendln("}");
        
        // dp mov
        code.appendln("@Override public AbstractMachineCode genDPMove(MCRegister dest, MCOperand src) {");
        code.increaseIndent();
        String dpmov = targetName + MC_DPMOV.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", dpmov, dpmov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // dp mov
        code.appendln("@Override public AbstractMachineCode genDPMove(MCOperand dest, MCOperand src) {");
        code.increaseIndent();
        code.appendStmtln(String.format("%s ret = new %s()", dpmov, dpmov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // sp mov
        code.appendln("@Override public AbstractMachineCode genSPMove(MCRegister dest, MCOperand src) {");
        code.increaseIndent();
        String spmov = targetName + MC_SPMOV.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", spmov, spmov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // sp mov
        code.appendln("@Override public AbstractMachineCode genSPMove(MCOperand dest, MCOperand src) {");
        code.increaseIndent();
        code.appendStmtln(String.format("%s ret = new %s()", spmov, spmov));
        code.appendStmtln("ret.setOperand0(src)");
        code.appendStmtln("ret.setDefine(dest)");
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
            code.appendStmtln("ret.setDefine(dest)");
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
        
        // call
        code.appendln();
        code.appendln("@Override public AbstractMachineCode genCall(MCLabel func) {");
        code.increaseIndent();
        String callMC = targetName + MC_CALL.get(0);
        code.appendStmtln(String.format("%s ret = new %s()", callMC, callMC));
        code.appendStmtln("ret.setOperand(0, func)");
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
        
        // genCompareAndJE
        // cmp op1 op2
        // jne #pass
        // call func
        // pass: nop
        code.appendln("@Override public AbstractMachineCode[] genCallIfEqual(MCOperand op1, MCOperand op2, MCLabel func, int id) {");
        code.increaseIndent();
        code.appendStmtln("AbstractMachineCode[] ret = new AbstractMachineCode[4]");
        // cmp op1 op2
        code.appendCommentln("cmp op1 op2");
        String cmpMC = targetName + MC_CMP.get(0);
        code.appendStmtln(String.format("%s i0 = new %s()", cmpMC, cmpMC));
        code.appendStmtln("i0.setOperand0(op1)");
        code.appendStmtln("i0.setOperand1(op2)");
        code.appendStmtln("ret[0] = i0");
        // jne #pass
        code.appendCommentln("jne #pass");
        code.appendStmtln("MCLabel pass = new MCLabel(\"yp_pass_\" + id)");
        String jneMC = targetName + MC_COND_JUMP.get(1);
        code.appendStmtln(String.format("%s i1 = new %s()", jneMC, jneMC));
        code.appendStmtln("i1.setOperand0(pass)");
        code.appendStmtln("ret[1] = i1");
        // call func
        code.appendCommentln("call func");
        code.appendStmtln("ret[2] = genCall(func)");
        // pass: nop
        code.appendCommentln("#pass: nop");
        code.appendStmtln("ret[3] = genNop()");
        code.appendStmtln("ret[3].setLabel(pass)");
        code.appendStmtln("return ret");
        code.decreaseIndent();
        code.appendln("}");
        
        // opposite jmp
        code.appendln("@Override public AbstractMachineCode genOppositeCondJump(AbstractMachineCode orig) {");
        code.increaseIndent();
        code.appendStmtln("String name = orig.getName()");
        code.appendStmtln("AbstractMachineCode ret = null");
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
        
        // inst ptr
        code.appendln();
        code.appendln("@Override public String getInstPtrReg() {");
        code.increaseIndent();
        code.appendStmtln("return \"" + INST_PTR + "\"");
        code.decreaseIndent();
        code.appendln("}");
        
        code.appendln();
        code.appendln("@Override public String getStackPtrReg() {");
        code.increaseIndent();
        code.appendStmtln("return \"" + STACK_PTR + "\"");
        code.decreaseIndent();
        code.appendln("}");
        
        code.appendln();
        code.appendln("@Override public String getFramePtrReg() {");
        code.increaseIndent();
        code.appendStmtln("return \"" + FRAME_PTR + "\"");
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
        
        // FP reg
        code.appendln();
        code.append("public static final String[] FP_REG = {");
        for (int i = 0; i < Burg.REG_FP.size(); i++) {
            String s = Burg.REG_FP.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_FP.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfFPR() {return FP_REG.length;}");
        code.appendln("@Override public String getFPRName(int i) {return FP_REG[i];}");
        
        // FP param reg
        code.appendln();
        code.append("public static final String[] FP_REG_PARAM = {");
        for (int i = 0; i < Burg.REG_FP_PARAM.size(); i++) {
            String s = Burg.REG_FP_PARAM.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_FP_PARAM.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfFPRParam() {return FP_REG_PARAM.length;}");
        code.appendln("@Override public String getFPRParamName(int i) {return FP_REG_PARAM[i];}");
        
        // FP ret reg
        code.appendln();
        code.append("public static final String[] FP_REG_RET = {");
        for (int i = 0; i < Burg.REG_FP_RET.size(); i++) {
            String s = Burg.REG_FP_RET.get(i);
            code.appendNoIndent(String.format("\"%s\"", s));
            if (i != Burg.REG_FP_RET.size() - 1)
                code.appendNoIndent(",");
        }
        code.appendlnNoIndent("};");
        
        code.appendln("@Override public int getNumberOfFPRRet() {return FP_REG_RET.length;}");
        code.appendln("@Override public String getFPRRetName(int i) {return FP_REG_RET[i];}");
        
        // callee save
        code.appendln();
        code.appendln("@Override public boolean isCalleeSave(String reg) {");
        code.increaseIndent();
        code.appendln("if (");
        for (int i = 0; i < REG_CALLEE_SAVE.size(); i++) {
            code.append(String.format("(reg.equals(\"%s\"))", REG_CALLEE_SAVE.get(i)));
            if (i != REG_CALLEE_SAVE.size() - 1)
                code.appendln(" || ");
        }
        code.appendln(")");
        code.increaseIndent();
        code.appendStmtln("return true");
        code.decreaseIndent();
        code.appendStmtln("return false");
        code.decreaseIndent();
        code.appendln("}");
        
        // MC op emit
        code.appendln();
        code.appendln("@Override public String emitOp(MCOperand node) {");
        code.increaseIndent();
        for (String op : operandEmit.keySet()) {
            OperandEmit operand = operandEmit.get(op);            

            code.appendln("if (node.getClass().getName().contains(\"" + operand.operandName + "\"))");
            
            StringBuilder str = new StringBuilder();
            str.append("return String.format(");
            str.append(operand.format);
            
            for (OpdNodeFunc funcNode : operand.funcCalls) {
                str.append(',');
                str.append(genChainCallFromOpdNodeFunc(funcNode));
            }
            
            str.append(");");
            
            code.appendln(str.toString());
        }
        code.appendStmtln("return \"error\"");
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
            builder.append(ruleno + " - ");
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
        Thread.dumpStack();
        System.exit(1);
    }
}
