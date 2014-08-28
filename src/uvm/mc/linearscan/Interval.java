package uvm.mc.linearscan;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uvm.mc.MCMemoryOperand;
import uvm.mc.MCRegister;

public class Interval {
    LivenessRange liveness;
    
    MCRegister orig;
    boolean fixed = false;
    
    int dataType;
    MCRegister physical;
    MCMemoryOperand spill;
    
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
     * Split this interval at a certain position. Return the rest as another interval
     * @param pos
     * @return
     */
    public Interval splitAt(int pos) {
        if (pos > liveness.size()) {
        	System.out.println("trying to split an interval at " + pos + " which is bigger than its size " + liveness.size());
        	System.exit(1);
        	return null;
        }
        
        // new Interval
        LivenessRange newRange = liveness.getSubrange(pos);
        Interval newInterval = new Interval(liveness.size(), dataType, orig);
        newInterval.liveness = newRange;
        newInterval.physical = this.physical;
        newInterval.spill = this.spill;
        
        // cut current interval
        liveness.cropCurrentRange(pos);
        
        return newInterval;
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
    
    public boolean isLiveAt(int index) {
        return liveness.isLiveAt(index);
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
        ret.append("Interval([");
        ret.append(getBegin());
        ret.append(",");
        ret.append(getEnd());
        ret.append("],orig=");
        ret.append(orig.prettyPrint());
        ret.append(",type=");
        ret.append(dataType);
        ret.append(",reg=");
        ret.append(physical == null ? "null" : physical.prettyPrint());
        ret.append(",spill=");
        ret.append(spill == null ? "null" : spill.prettyPrint());
        ret.append(")");
        ret.append(liveness.prettyPrint());
        return ret.toString();
    }
    
    public void addPosition(Position pos) {
        liveness.addPosition(pos);
    }
    
    public boolean doesIntersectWith(Interval another) {
        return nextIntersectionWith(another) != -1;
    }
    
    public int nextIntersectionWith(Interval another) {
        return liveness.firstIntersect(another.liveness);
    }
    
    public boolean intersectOtherThan(Interval interval, int sequence) {
        return liveness.firstIntersectOtherThan(interval.liveness, sequence) != -1;
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
}
