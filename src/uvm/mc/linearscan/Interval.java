package uvm.mc.linearscan;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import compiler.util.Pair;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;

public class Interval {
    LivenessRange liveness;
    
    MCRegister orig;
    boolean fixed = false;
    
    int dataType;
    MCRegister physical;
    MCMemoryOperand spill;
    
    Interval next = null;
    
    public int getBegin() {
        return liveness.firstAlive();
    }
    
    public int getEnd() {
        return liveness.lastAlive();
    }
    
    public void setPhysicalReg(MCRegister reg) {
        this.physical = reg;
    }
    
    public MCRegister getPhysicalReg() {
        return physical;
    }
    
    public Interval(int length, int type, MCRegister orig) {
        this.orig = orig;
        if (orig.getType() == MCRegister.MACHINE_REG) {
            this.fixed = true;
            this.physical = orig;
        }
        this.liveness = new LivenessRange(length);
        this.dataType = type;
    }
    
    /**
     * Split this interval at a certain position. Return the rest as another interval.
     * the new interval has its physical reg and spill set to 'null'
     * @param pos
     * @return
     */
    public Interval splitAt(int pos) {
        if (pos > liveness.size()) {
        	System.out.println("trying to split an interval at " + pos + " which is bigger than its size " + liveness.size());
        	System.exit(1);
        	return null;
        }
        
        if (pos == getBegin()) {
        	return null;
        }
        
        // new Interval
        LivenessRange newRange = liveness.getSubrange(pos);
        Interval newInterval = new Interval(liveness.size(), dataType, orig);
        newInterval.liveness = newRange;
        if (!this.fixed)
        	newInterval.physical = null;
        newInterval.spill = null;
        if (next != null)
        	newInterval.next = next;
        
        // cut current interval
        liveness.cropCurrentRange(pos);
        next = newInterval;
        
        System.out.println("after split:");
        System.out.println(prettyPrint());
        
        return newInterval;
    }
    
    public Interval getNext() {
    	return this.next;
    }
    
    public int nextUseAfter(int pos) {
        Position ret = liveness.nextUseAfter(pos);
        if (ret != null)
            return ret.index;
        else return -1;
    }
    
    public int skipLifetimeHole(int pos) {
        return liveness.nextAlive(pos);
    }
    
    public int firstUse() {
        return nextUseAfter(0);
    }
    
    public int firstRegOnlyUse() {
        Position ret = liveness.nextRegOnlyUseAfter(0);
        if (ret != null)
            return ret.index;
        else return -1;
    }
    
    public int regOnlyUses(int pos) {
    	return liveness.regOnlyUses(pos);
    }
    
    public boolean isRegOnlyUseAt(int pos) {
    	Position p = liveness.getPosition(pos);
    	if (p == null)
    		return false;
    	else if (p.regOnly)
    		return true;
    	else return false;
    }
    
    public boolean isLiveAt(int index) {
        return liveness.isLiveAt(index);
    }
    
    public boolean hasDefineAt(int index) {
    	return liveness.hasDefineAt(index);
    }
    
    public LivenessRange getLiveness() {
        return liveness;
    }
    
    public boolean calcLiveness() {
        liveness.calculateLivenessFromPositions();
        
        if (getBegin() == -1 || getEnd() == -1)
            return false;
        return true;
    }

    public boolean hasValidRange() {
        return liveness.firstAlive() != -1;
    }

    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("Interval(");
        ret.append(orig.prettyPrint());
        ret.append(")[");
        ret.append(getBegin());
        ret.append(",");
        ret.append(getEnd());
        ret.append("]");
        ret.append(" type=");
        ret.append(dataType);
        ret.append(" reg=");
        ret.append(physical == null ? "null" : physical.prettyPrint());
        ret.append(" spill=");
        ret.append(spill == null ? "null" : spill.prettyPrint());
        ret.append(")");
        ret.append(liveness.prettyPrint());
        ret.append("\n");
        
        if (next != null)
        	ret.append(next.prettyPrint());
        
        return ret.toString();
    }
    
    public void addPosition(Position pos) {
        liveness.addPosition(pos);
    }
    
    public boolean doesIntersectWith(Interval another) {
    	return liveness.firstIntersect(another.liveness) != -1;
    }
    
    public int nextIntersectionWith(Interval another) {
        return liveness.firstIntersect(another.liveness);
    }
    
    public boolean intersectOtherThan(Interval interval, int sequence) {
        return liveness.firstIntersectOtherThan(interval.liveness, sequence) != -1;
    }
    
    public boolean isDefinedDuring(Interval interval) {
    	return liveness.isDefinedDuring(interval.liveness);
    }
    
    public void setLivenessRange(LivenessRange lr) {
        this.liveness = lr;
    }

    public int getDataType() {
        return dataType;
    }

    public MCMemoryOperand getSpill() {
        return spill;
    }

    public void setSpill(MCMemoryOperand spill) {
        this.spill = spill;
    }

    public MCRegister getOrig() {
        return orig;
    }

    public void setOrig(MCRegister orig) {
        this.orig = orig;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    
	public static Pair<Interval, Interval> firstAndLastEncountingInterval(Interval i, int start, int end) {
		Interval cur = i;
		
		Pair<Interval, Interval> ret = new Pair<Interval, Interval>();
		
		while(cur != null) {
			// check if this is the first one
			if (ret.getFirst() == null) {
				if (cur.isLiveAt(start))
					ret.setFirst(cur);
				else if (cur.getBegin() > start && cur.getBegin() < end) 
					ret.setFirst(cur);
			}
			
			// check if this is the last one
			if (ret.getFirst() != null) {
				if (cur.isLiveAt(end))
					ret.setSecond(cur);
				else if (cur.getEnd() > start && cur.getEnd() < end)
					ret.setSecond(cur);
			}
			
			cur = cur.next;
		}
		
		return ret;
	}
	
	public MCOperand getPhysicalLocation() {
		if (spill != null)
			return spill;
		else return physical;
	}
}
