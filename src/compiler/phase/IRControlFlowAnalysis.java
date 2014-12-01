package compiler.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import uvm.BasicBlock;
import uvm.Function;
import uvm.Instruction;
import uvm.Label;
import uvm.Value;

@Deprecated
public class IRControlFlowAnalysis extends AbstractCompilationPhase {

	public IRControlFlowAnalysis(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitFunction(Function f) {
		for (BasicBlock bb : f.getBBs()) {
			Instruction branch = bb.getInsts().get(bb.getInsts().size() - 1);
			
			if (!branch.isBranching())
				continue;
			
			for (Value v : branch.getOperands()) {
				if (v instanceof Label) {
					BasicBlock targetBB = f.getBB(((Label) v).getName());
					
					bb.addSuccessor(targetBB);
					targetBB.addPredecessor(bb);
				}
			}
		}
		
		// TODO: is it true?
		f.setCFGEntry(f.getBBs().get(0));
		
		buildBackEdges(f);
	}

	Stack<BasicBlock> dfsStack;
	List<BasicBlock> visited;
	
	private void buildBackEdges(Function f) {
		visited  = new ArrayList<BasicBlock>();
		dfsStack = new Stack<BasicBlock>();
		
		dfs(f.getCFG());
	}

	private void dfs(BasicBlock cur) {
		dfsStack.push(cur);
		visited.add(cur);
		
		for (BasicBlock succ : cur.getSuccessors()) {
			if (dfsStack.contains(succ)) {
				cur.addBackEdge(succ);
			}
			
			if (!visited.contains(succ))
				dfs(succ);
		}
		
		dfsStack.pop();
	}
}
