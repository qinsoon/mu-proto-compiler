package compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import burm.mc.X64Driver;
import compiler.phase.DefUseGeneration;
import compiler.phase.ExpandRuntimeServices;
import compiler.phase.IRTreeGeneration;
import compiler.phase.InstructionSelection;
import compiler.phase.MCRepresentationGeneration;
import compiler.phase.ReplaceConsts;
import compiler.phase.RuntimeCodeEmission;
import compiler.phase.mc.*;
import compiler.phase.mc.linearscan.AddingJumpInstruction;
import compiler.phase.mc.linearscan.ComputeLiveInterval;
import compiler.phase.mc.linearscan.GenMovForPhi;
import compiler.phase.mc.linearscan.InstructionNumbering;
import compiler.phase.mc.linearscan.JoinPhiNode;
import compiler.phase.mc.linearscan.LinearScan;
import compiler.phase.mc.linearscan.RegisterCoalescing;
import compiler.phase.mc.linearscan.ReplaceRegisterOperand;
import compiler.phase.mc.linearscan.SimpleLinearScan;
import compiler.phase.mc.x64.X64AllocateParamRetRegister;
import compiler.phase.mc.x64.X64ExpandCallSequence;
import compiler.phase.mc.x64.X64MachineCodeExpansion;
import compiler.phase.mc.x64.X64PostRegisterAllocPatching;
import parser.uIRLexer;
import parser.uIRListenerImpl;
import parser.uIRParser;
import uvm.BasicBlock;
import uvm.Function;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.mc.AbstractMCDriver;
import uvm.util.RecordUVMStats;
import compiler.util.Pair;

public class UVMCompiler {
	public static final boolean TIMING_COMPILATION = true;
	
	public static final List<Pair<String, Long>> ELAPSE_TIME = new ArrayList<Pair<String, Long>>();
	public static long startTime;
	public static long endTime;
	
	public static String BASE_DIR = ".";

    public static AbstractMCDriver MCDriver = new X64Driver();
    public static final int MC_REG_SIZE = 64;
    public static final int MC_REG_SIZE_IN_BYTES = MC_REG_SIZE / 8;
    public static final int MC_FP_REG_SIZE = 64;
    public static final int MC_FP_REG_SIZE_IN_BYTES = MC_FP_REG_SIZE / 8;
    
    public static final boolean ALWAYS_DUMP_INFO = true;
    
    public static void main(String[] args) {
        if (args.length == 0)
            UVMCompiler.error("Missing source file name in arguments");
        
        String file = null;
        
        for (int i = 0; i < args.length; i++) {
        	if (args[i].equals("-base")) {
        		BASE_DIR = args[i+1];
        		i++;
        	} else {
        		if (file == null)
        			file = args[i];
        		else error("Only one source file allowed.");
        	}
        }
        
        try {
        	compile(file, TIMING_COMPILATION, ALWAYS_DUMP_INFO);
        	System.out.println("======= Compilation Succeed! ======");
        } catch (Exception e) {
        	e.printStackTrace();
        	error(e.getMessage());
        }

    }

	public static void compile(String file, boolean timeCompilation, boolean dumpInfo) throws IOException,
			FileNotFoundException {
		long parsingStart = 0;
		
		RecordUVMStats.clearPreviousStats(ERROR_DUMP_DIR);
		
		if (timeCompilation) {
			parsingStart = System.currentTimeMillis();
			startTime = parsingStart;
		}
		
		// create a CharStream that reads from standard input
		ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(file));
		// create a lexer that feeds off of input CharStream
		uIRLexer lexer = new uIRLexer(input);
		// create a buffer of tokens pulled from the lexer
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// create a parser that feeds off the tokens buffer
		uIRParser parser = new uIRParser(tokens);
		ParseTree tree = parser.ir(); // begin parsing at init rule
		
		System.out.println("Parsing Tree:");
		System.out.println(tree.toStringTree(parser));
		System.out.println();
		
		// Create a generic parse tree walker that can trigger callbacks
		ParseTreeWalker walker = new ParseTreeWalker();
		// Walk the tree created during the parse, trigger callbacks
		walker.walk(new uIRListenerImpl(), tree);
		System.out.println(); // print a \n after translation
		
		// see the IR
		for (Function f : MicroVM.v.funcs.values()) {
		    System.out.println("function " + f.getName() + " of " + f.getSig());
		    for (BasicBlock bb : f.getBBs()) {
		        System.out.println("BB[" + bb.getName() + "]:");
		        for (Instruction inst : bb.getInsts()) {
		            System.out.println(inst.prettyPrint());
		        }
		        System.out.println();
		    }
		    System.out.println();
		}
		
		if (timeCompilation) {
			ELAPSE_TIME.add(new Pair<String, Long>("parsing", System.currentTimeMillis() - parsingStart));
		}
		
		/*
		 *  generating IR tree
		 */
		new ReplaceConsts("replaceconsts", true).execute();
		new ExpandRuntimeServices("expandruntime", Verbose.EXPAND_RT_SERVICE).execute();
		new DefUseGeneration("defusegen", Verbose.DEF_USE_GEN).execute();
		new IRTreeGeneration("treegen", Verbose.TREE_GEN).execute();		
		
		/*
		 *  instruction selection (use BURM)
		 */
		new InstructionSelection("instsel", Verbose.INST_SEL).execute();
		new MCRepresentationGeneration("mcrepgen", Verbose.MC_REP_GEN).execute();
		
		/*
		 *  mc code transform
		 */
		new compiler.phase.mc.x64.X64SpillConstantsToMemory("spillconstant", Verbose.SPILL_CONSTANT).execute();
		new CombineReturns("combineret", Verbose.COMBINE_RET).execute();
		new MCControlFlowAnalysis("mccfa", Verbose.RECONSTRUCT_BB).execute();
		new RetainHighLevelDataType("retainhlltype", Verbose.RETAIN_HLL_TYPE).execute();            
		new AddCallRegisterArguments("callregargs", true).execute();
		new X64MachineCodeExpansion("x64mcexp", true).execute();
		
		/*
		 *  register allocation
		 *  
		 *  the parts below with a * implement the paper from CC02: http://dl.acm.org/citation.cfm?id=647478.727924
		 *  
		 *  alternative (more recent works are):
		 *  - Vivek Sarkar's work in CC07: http://dl.acm.org/citation.cfm?id=1759937.1759950
		 *  - Christian Wimmer's work in CGO10: http://doi.acm.org/10.1145/1772954.1772979
		 */
		new X64AllocateParamRetRegister("allocparamret", Verbose.ALLOC_PARAM_RET_REG).execute();
		new GenMovForPhi("genmovforphi", Verbose.GEN_MOV_FOR_PHI).execute();                    //*
		new JoinPhiNode("joinphi", false).execute();
		new AddingJumpInstruction("addingjmp", false).execute();
		new DetectBackEdge("detectbedge", Verbose.DETECT_BACK_EDGE).execute();
		new InstructionNumbering("instnumbering", Verbose.INST_NUMBERING).execute();            //*
		new ComputeLiveInterval("compinterval", Verbose.COMPUTE_INTERVAL).execute();            //*
		new RegisterCoalescing("regcoalesc", Verbose.REG_COALESC).execute();                    //*
//            new LinearScan("linearscan", Verbose.LINEAR_SCAN).execute();                            //*
//            new ReplaceRegisterOperand("replaceregop", Verbose.REPLACE_MEM_OP).execute();
		new X64ExpandCallSequence("expandcallseq", Verbose.EXPAND_CALL_SEQ).execute();
		new SimpleLinearScan("simplelinearscan", Verbose.LINEAR_SCAN).execute();
		new X64PostRegisterAllocPatching("postregallocpatching", false).execute();
		
//            dumpInfo("AfterRegAlloc");
		
		/*
		 * post register allocation code transform (be careful of using any registers, and concern about calling convention)
		 */
		new InsertYieldpoint("insertYP", Verbose.INSERT_YIELDPOINT).execute();
		new SimpleBranchAlignment("simplebralign", Verbose.SIMPLE_BRANCH_ALIGN).execute();
		
//            dumpInfo("AfterBranchAlign");
		
		/*
		 *  code emission
		 */
		new MachineCodeCleanup("mccleanup", Verbose.MC_CLEANUP).execute();
		
		/*
		 * machine dependent transformation
		 */
		new CodeEmission("codeemit", "emit", Verbose.CODE_EMIT).execute();
		new RuntimeCodeEmission("rtemit", "emit", false).execute();
		
		if (timeCompilation) {
			endTime = System.currentTimeMillis();
			
			reportElapseTime();
		}
		
		if (dumpInfo) {
			dumpInfo();
		}
	}
    
    private static final void reportElapseTime() {
    	long total = endTime - startTime;
    	reportTime("Total", total, total);
    	
    	System.out.println();
    	System.out.println();
    	
    	for (Pair<String, Long> p : ELAPSE_TIME) {
    		reportTime(p.getFirst(), p.getSecond(), total);
    	}
    }
    
    private static void reportTime(String category, long curTime, long totalTime) {
    	String m = String.format("%30s\t\t\t\t\t%d(%f)", category, curTime, ((double)curTime)/totalTime);
    	System.out.println(m);
    }
    
    public static final void error(String message) {
        System.err.println(message);
        Thread.dumpStack();
        dumpInfo();
        
        System.exit(1);
    }
    
    public static final String ERROR_DUMP_DIR = "errordump";
    
    public static final void dumpInfo() {
    	dumpInfo(null);
    }
    
    public static final void dumpInfo(String suffix) {
        String errorDumpDir = BASE_DIR + "/" + ERROR_DUMP_DIR + (suffix != null ? suffix : "")+ "/";
        System.out.println("dump info to " + errorDumpDir);
        
        File dir = new File(errorDumpDir);
        if (!dir.exists()) {
        	dir.mkdirs();
        } else {
        	// clear its content
        	for (File f : dir.listFiles()) {
        		f.delete();
        	}
        }
        
        try {
        	new RecordUVMStats(errorDumpDir).output();
        } catch (Exception e) {
        	System.err.println("Error during dumping stats");
        	e.printStackTrace();
        	System.exit(1);
        }
    }
    
    public static final void exit() {
        System.exit(1);
    }
    
    public static final void _assert(boolean cond, String message) {
        if (!cond)
            error(message);
    }
    
    public static final void unimplemented(String message) {
    	error(message);
    }
}
