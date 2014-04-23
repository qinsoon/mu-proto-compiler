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
import compiler.phase.MachineCodeEmission;
import compiler.phase.mc.AllocateParamRetRegister;
import compiler.phase.mc.BBReconstruction;
import compiler.phase.mc.ComputeLiveInterval;
import compiler.phase.mc.GenMovForPhi;
import compiler.phase.mc.InstructionNumbering;
import compiler.phase.mc.LinearScan;
import compiler.phase.mc.RegisterCoalescing;
import parser.uIRLexer;
import parser.uIRListenerImpl;
import parser.uIRParser;
import uvm.BasicBlock;
import uvm.Function;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.mc.AbstractMCDriver;

public class UVMCompiler {
    
    public static final String file = "tests/micro-bm/int-prime-number/prime-number.uir";
    
    public static AbstractMCDriver MCDriver = new X64Driver();
    public static final int MC_REG_SIZE = 64;
    
    public static void main(String[] args) {

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
            
            // see the tree
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
            
            // generating IR tree
            new DefUseGeneration("defusegen").execute();            
            new IRTreeGeneration("treegen").execute();
            
            // instruction selection
            new InstructionSelection("instsel").execute();
            new MachineCodeEmission("mcemit").execute();
            
            // mc level
            new BBReconstruction("reconstbb").execute();
            new GenMovForPhi("genmovforphi").execute();
            new InstructionNumbering("instnumbering").execute();
            new AllocateParamRetRegister("allocparamret").execute();
            new ComputeLiveInterval("compinterval").execute();
            new RegisterCoalescing("regcoalesc").execute();
            new LinearScan("linearscan").execute();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public static final void error(String message) {
        System.err.print(message);
        Thread.dumpStack();
        System.exit(1);
    }
    
    public static final void _assert(boolean cond, String message) {
        if (!cond)
            error(message);
    }
}
