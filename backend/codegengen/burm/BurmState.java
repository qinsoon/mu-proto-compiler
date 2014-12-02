package burm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uvm.IRTreeNode;

public class BurmState {
    IRTreeNode node;
    BurmState[] leaves;
    static final short INFINITE = Short.MAX_VALUE;
    short[] cost;
    short[] rule; 
    List<List<Integer>> childNodes;

	public void record(int nt, int cost, int ruleno, Integer[] child) {
      if (cost < this.cost[nt]) {
        this.cost[nt] = (short) cost;
        this.rule[nt] = (short) ruleno;
        if (child != null)
        	childNodes.set(nt, Arrays.asList(child));
      }
    }
    
    public IRTreeNode getNode() {
      return node;
    }
    
    public BurmState getLeaf(int i) {
      return leaves[i];
    }
    
    public short getCost(int i) {
      return cost[i];
    }
    
    public short getRule(int i) {
      return rule[i];
    }
    
    public String prettyPrint() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < cost.length; i++) {
            if (rule[i] == -1 && cost[i] == INFINITE)
                continue;
            builder.append("rule" + rule[i] + "=" + cost[i]);
            if (i != cost.length - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }
}