package uvm.mc.linearscan;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uvm.mc.MCMemoryOperand;
import uvm.mc.MCRegister;

public class Interval {
    int begin;
    int end;
    
    LivenessRange liveness;
    
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
    
    public Interval(int length, int type) {
        liveness = new LivenessRange(length);
        this.dataType = type;
    }
    
    /**
     * Split this interval at a certain position. Return the rest as another interval
     * @param pos
     * @return
     */
    public Interval splitAt(int pos) {
        if (pos > end)
            return null;
        
        // new Interval
        LivenessRange newRange = liveness.getSubrange(pos);
        Interval newInterval = new Interval(liveness.size(), dataType);
        newInterval.liveness = newRange;
        newInterval.physical = this.physical;
        newInterval.spill = this.spill;
        newInterval.begin = newRange.firstAlive();
        newInterval.end = newRange.lastAlive();
        
        // cut current interval
        liveness.cropCurrentRange(pos);
        end = liveness.lastAlive();
        
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
    
    public void calcLiveness() {
        liveness.calculateLivenessFromPositions();
    }

    public boolean hasValidRange() {
        return liveness.firstAlive() != -1;
    }

    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("Interval([");
        ret.append(begin);
        ret.append(",");
        ret.append(end);
        ret.append("],type=");
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
        this.begin = liveness.firstAlive();
        this.end = liveness.lastAlive();
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
}
