package compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import burm.mc.X64Driver;
import compiler.phase.DefUseGeneration;
import compiler.phase.IRTreeGeneration;
import compiler.phase.InstructionSelection;
import compiler.phase.MCRepresentationGeneration;
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
import compiler.phase.mc.x64.X64PostRegisterAllocPatching;
import parser.uIRLexer;
import parser.uIRListenerImpl;
import parser.uIRParser;
import uvm.BasicBlock;
import uvm.Function;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.mc.AbstractMCDriver;

public class UVMCompiler {
	public static String BASE_DIR = ".";

    public static AbstractMCDriver MCDriver = new X64Driver();
    public static final int MC_REG_SIZE = 64;
    public static final int MC_REG_SIZE_IN_BYTES = MC_REG_SIZE / 8;
    public static final int MC_FP_REG_SIZE = 80;
    public static final int MC_FP_REG_SIZE_IN_BYTES = MC_FP_REG_SIZE / 8;
    
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
            
            /*
             *  generating IR tree
             */
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
            new CombineReturns("combineret", Verbose.COMBINE_RET).execute();
            new BBReconstruction("reconstbb", Verbose.RECONSTRUCT_BB).execute();
            new RetainHighLevelDataType("retainhlltype", Verbose.RETAIN_HLL_TYPE).execute();            

            
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
            
            /*
             *  code emission
             */
            new SimpleBranchAlignment("simplebralign", Verbose.SIMPLE_BRANCH_ALIGN).execute();
            new MachineCodeCleanup("mccleanup", Verbose.MC_CLEANUP).execute();
            
            /*
             * machine dependent transformation
             */
            new compiler.phase.mc.SpillConstantsToMemory("spillconstant", Verbose.SPILL_CONSTANT).execute();
            new CodeEmission("codeemit", "emit", Verbose.CODE_EMIT).execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public static final void error(String message) {
        System.err.print(message);
        Thread.dumpStack();
        System.exit(1);
    }
    
    public static final void exit() {
        System.exit(1);
    }
    
    public static final void _assert(boolean cond, String message) {
        if (!cond)
            error(message);
    }
}
