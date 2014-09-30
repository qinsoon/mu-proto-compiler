package compiler.phase.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import uvm.CompiledFunction;
import uvm.mc.MCBasicBlock;

public class DetectBackEdge extends AbstractMCCompilationPhase {

	public DetectBackEdge(String name, boolean verbose) {
		super(name, verbose);
	}
	
	Stack<MCBasicBlock> dfsStack;
	List<MCBasicBlock> visited;

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		verboseln("--- detect backedge for " + cf.getOriginFunction().getName() + " ---");
		visited = new ArrayList<MCBasicBlock>();
		dfsStack = new Stack<MCBasicBlock>();
		
		dfs(cf.entryBB);
	}

	private void dfs(MCBasicBlock cur) {
		dfsStack.push(cur);
		visited.add(cur);
		
		for (MCBasicBlock succ : cur.getSuccessor()) {
			if (dfsStack.contains(succ)) {
				verboseln("BB #" + cur.getName() + " has a backedge to #" + succ.getName());
				cur.addBackEdge(succ);
			}
			
			if (!visited.contains(succ))
				dfs(succ);
		}
		
		dfsStack.pop();
	}
}
