package uvm.mc.linearscan;

import java.util.BitSet;
import compiler.util.OrderedList;

import java.util.Comparator;

public class LivenessRange {
    private BitSet bitset;
    private OrderedList<Position> positions = new OrderedList<Position>(new Comparator<Position>() {
        @Override
        public int compare(Position o1, Position o2) {
            if (o1.index < o2.index)
                return -1;
            else if (o1.index == o2.index) {
                if (o1.isUse() && o2.isDefine())
                    return 1;
                else if (o1.isDefine() && o2.isUse())
                    return -1;
                else return 0;
            }
            else return 1;
        }
        
    });    
    
    public LivenessRange(int length) {
        bitset = new BitSet(length);
    }
    
    public void calculateLivenessFromPositions() {
        Position rangeStart = null;
        
        for (int i = 0; i < positions.size(); i++) {
            Position p = positions.get(i);
            if (rangeStart == null) {
                // first position
                if (p.isUse()) {
                	// commenting out the warning
                	// some insts such as call, implicity uses all the parameter registers
                	// if those registers are not used somewhere else, they only have a USE position
                	// and we are supposed to ignore them and do not create intervals for them
                	
//                    System.out.println(p.prettyPrint());
//                    System.err.println("warning: first position in liveness range is USE");
                } else if (p.isDefine()) {
                    rangeStart = p;
                }
            } else {
                if (p.isUse()) {
                    bitset.set(rangeStart.index, p.index + 1);
                } else if (p.isDefine()) {
                    rangeStart = p;
                }
            }
        }
    }
    
    public void addPosition(Position pos) {
        for (Position p : positions)
            if (p.equals(pos))
                return;
        
        positions.add(pos);
    }
    
    public int firstAlive() {
        return bitset.nextSetBit(0);
    }
    
    public int lastAlive() {
        for (int i = bitset.size() - 1; i >= 0; i--) {
            if (bitset.get(i))
                return i;
        }
        return -1;
    }
    
    public int nextAlive(int pos) {
        return bitset.nextSetBit(pos);
    }
    
    public LivenessRange getSubrange(int fromIndex) {
        LivenessRange subrange = new LivenessRange(bitset.size());
        
        // deal with bitset
        for (int i = fromIndex; i < bitset.size(); i++)
            if (isLiveAt(i))
                subrange.setLive(i);
        
        // deal with positions
        for (Position p : positions) {
            if (p.index >= fromIndex)
                subrange.positions.add(p);
        }
        
        return subrange;
    }
    
    public void cropCurrentRange(int toIndex) {
        bitset.clear(toIndex, bitset.size());
        for (int i = 0; i < positions.size();) {
            Position p = positions.get(i);
            if (p.index > toIndex) {
                positions.remove(i);
            } else {
                i++;
            }
        }
    }
    
    public Position nextUseAfter(int pos) {
        for (int i = 0; i < positions.size(); i++) {
            Position p = positions.get(i);
            if (p.index > pos && p.isUse())
                return p;
        }
        
        return null;
    }
    
    public Position nextRegOnlyUseAfter(int pos) {
        for (int i = 0; i < positions.size(); i++) {
            Position p = positions.get(i);
            if (p.index > pos && p.isUse() && p.regOnly)
                return p;
        }
        
        return null;
    }
    
    public int firstIntersect(LivenessRange another) {
        for (int i = 0; i < bitset.size(); i++) {
            if (bitset.get(i) && i < another.bitset.size() && another.bitset.get(i))
                return i;
        }
        
        return -1;
    }
    
    public int firstIntersectOtherThan(LivenessRange another, int pos) {
        for (int i = 0; i < bitset.size(); i++) {
            if (i != pos && bitset.get(i) && i < another.bitset.size() && another.bitset.get(i))
                return i;
        }
        
        return -1;
    }
    
    public boolean isLiveAt(int index) {
        return bitset.get(index);
    }
    
    public void setLive(int index) {
        bitset.set(index);
    }
    
    public void setNotLive(int index) {
        bitset.set(index, false);
    }
    
    public int size() {
        return bitset.size();
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        int rangeStart = -1;
        for (int i = 0; i < bitset.size(); i++) {
            if (rangeStart == -1) {
                if (bitset.get(i))
                    rangeStart = i;
            } else {
                if (!bitset.get(i)) {
                    ret.append(rangeStart + "-" + (i-1) + ",");
                    rangeStart = -1;
                }
            }
        }
        
        ret.append("\n");
        for (Position p : positions) {
            ret.append(p.prettyPrint() + ", ");
        }
        return ret.toString();
    }

    public static LivenessRange union(LivenessRange i, LivenessRange j) {
        LivenessRange ret = null;
        
        if (i.size() < j.size()) {
            ret = new LivenessRange(i.size());
            ret.bitset = i.bitset;
            ret.bitset.or(j.bitset);
        } else {
            ret = new LivenessRange(j.size());
            ret.bitset = j.bitset;
            ret.bitset.or(i.bitset);
        }
        
        ret.positions.addAll(i.positions);
        for (Position p : j.positions)
            if (!ret.positions.contains(p))
                ret.positions.add(p);
        
        return ret;
    }
}
