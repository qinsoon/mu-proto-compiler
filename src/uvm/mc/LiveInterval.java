package uvm.mc;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import uvm.mc.*;

public class LiveInterval {
    static final boolean DEBUG = true;
    
    MCRegister reg;
    
    int begin;
    int end;
    HashMap<MCBasicBlock, List<Range>> ranges = new HashMap<MCBasicBlock, List<Range>>();
    
    public LiveInterval(MCRegister reg) {
        this.reg = reg;
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append(reg.prettyPrint());
        ret.append(" beg=" + begin);
        ret.append(" end=" + end);
        ret.append(" ");
        for (MCBasicBlock bb : ranges.keySet()) {
            ret.append("{" + bb.getName() + ":");
            for (Range r : ranges.get(bb))
                ret.append(r.prettyPrint() + ",");
            ret.append("}");
        }
        return ret.toString();
    }
    
    /**
     * do two intervals overlap any other intervals than this mc
     * @param another
     * @param mc
     * @return
     */
    public boolean overlapOtherThan(LiveInterval another, int mc) {
        for (MCBasicBlock bb : ranges.keySet()) {
            if (!another.ranges.containsKey(bb))
                continue;
            else {
                List<Range> mine = ranges.get(bb);
                List<Range> his = another.ranges.get(bb);
                for (Range r1 : mine)
                    for (Range r2 : his) {
                        if (r1.overlap(r2) && !r1.contains(mc))
                            return true;
                    }
            }
            
        }
        
        return false;
    }
    
    public boolean hasValidRanges() {
        for (List<Range> list : ranges.values())
            if (!list.isEmpty())
                return true;
        
        return false;
    }
    
    public boolean isLiveAt(int mc) {
        return overlap(mc);
    }
    
    public boolean overlap(int mc) {
        if (!hasValidRanges())
            return false;
        
        for (List<Range> list : ranges.values()) {
            for (Range r : list)
                if (r.contains(mc))
                    return true;
        }
        
        return false;
    }
    
    public boolean overlap(LiveInterval another) {
        for (MCBasicBlock bb : ranges.keySet()) {
            if (!another.ranges.containsKey(bb))
                continue;
            else {
                List<Range> mine = ranges.get(bb);
                List<Range> his = another.ranges.get(bb);
                for (Range r1 : mine)
                    for (Range r2 : his) {
                        if (r1.overlap(r2))
                            return true;
                    }
            }
            
        }
        
        return false;
    }
    
    public static class Range{
        public static final int UNKNOWN_START = -1;
        public static final int UNKNOWN_END   = -1;
        int start, end;
        
        public Range(int start, int end) {            
            if (start != UNKNOWN_START && end != UNKNOWN_END && start > end) {
                System.out.print("creating range ");
                System.out.println(" [" + start + "," + end + "[");
                
                Thread.dumpStack();
                System.exit(5);                
            }
            
            this.start = start;
            this.end = end;
        }
        
        public String prettyPrint() {
            return "[" + start + "," + end + "[";
        }
        
        public boolean contains(int mc) {
            if (start <= mc && end >= mc)
                return true;
             return false;
        }
        
        public boolean contains(Range another) {
            if (start <= another.start && end >= another.end)
                return true;
            
            return false;
        }
        
        public void merge(int start, int end) {
            if (DEBUG)
                System.out.print(" merge range [" + this.start + "," + this.end + "[ with [" + start + ", " + end + "[");
            
            if (this.start == UNKNOWN_START)
                this.start = start;
            else if (start == UNKNOWN_START) {
                // do nothing
            }
            else
                this.start = Math.min(this.start, start);
            
            if (this.end == UNKNOWN_END)
                this.end = end;
            else if (end == UNKNOWN_END) {
                // do nothing
            }
            else 
                this.end = Math.max(this.end, end);
            
            if (DEBUG)
                System.out.println(" => [" + this.start + ", " + this.end + "[");
        }
        
        public static List<Range> union(Range a, Range b) {
            List<Range> ret = new LinkedList<Range>();
            
            // one is null
            
            if (a == null) {
                ret.add(b);
                return ret;
            }
            
            if (b == null) {
                ret.add(a);
                return ret;
            }
            
            // one range contains another
            
            if (a.contains(b)) {
                ret.add(a);
                return ret;
            }
            
            if (b.contains(a)) {
                ret.add(b);
                return ret;
            }
            
            // two ranges has intersection
            
            if (b.contains(a.start)) {
                Range r = new Range(b.start, a.end);
                ret.add(r);
                return ret;
            }
            
            if (a.contains(b.start)) {
                Range r = new Range(a.start, b.end);
                ret.add(r);
                return ret;
            }
            
            // two discontiguous ranges
            ret.add(a);
            ret.add(b);
            return ret;
        }
        
        public boolean adjacent(Range another) {
            if (another.start == this.end + 1)
                return true;
            
            if (this.start == another.end + 1)
                return true;
            
            return false;
        }
        
        public boolean overlap(Range another) {
            if (another.start <= this.end && another.end >= this.start)
                return true;
            
            return false;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {            
            this.end = end;
        }
    }  
    
//  public Range getRange(int mc) {
//      for (int i = 0; i < ranges.size(); i++) {
//          if (ranges.get(i).contains(mc))
//              return ranges.get(i);
//      }
//      
//      error("cannot find range for " + mc + " for reg " + i.prettyPrint());
//      return null;
//  }
  
    /**
     * 
     * @param bb
     * @return may return null
     */
    public List<Range> getRange(MCBasicBlock bb) {
        return ranges.get(bb);
    }
    
    public Range getRange(MCBasicBlock bb, int n) {
        List<Range> list = ranges.get(bb);
        
        if (list == null)
            return null;
        
        for (Range r : list)
            if (r.contains(n))
                return r;
        
        return null;
    }
    
    public void removeRange(MCBasicBlock bb) {
        ranges.remove(bb);
    }
    
    public void replaceRange(MCBasicBlock bb, Range oldRange, int newStart, int newEnd) {
        List<Range> list = ranges.get(bb);
        
        Range newRange = new Range(newStart, newEnd);
        Range overlappingRange = null;
        
        for (Range r : list) {
            if (r != oldRange && r.overlap(newRange)) {
                if (overlappingRange != null) {
                    System.out.println("trying to replace " + oldRange.prettyPrint() + " with new range: " + newStart + "," + newEnd);
                    System.out.println("checking if new range overlap with other ranges");
                    System.out.println("other ranges include " + overlappingRange.prettyPrint() + " and " + r.prettyPrint());
                    error("we have more than 2 ranges for bb " + bb.getName());
                }
                overlappingRange = r;
            }
        }
        
        if (overlappingRange == null || !overlappingRange.overlap(newRange)) {
            oldRange.setStart(newStart);
            oldRange.setEnd(newEnd);
            return;
        } else {
            // we need to merge
            overlappingRange.merge(newStart, newEnd);
            list.remove(oldRange);
        } 
    }
    
    public void replaceRange(MCBasicBlock bb, List<Range> b) {
        ranges.put(bb, b);
    }
    
    public void addRange(MCBasicBlock bb, int start, int end) {

        if (!ranges.containsKey(bb)) {
            Range r = new Range(start, end);
            List<Range> list = new LinkedList<Range>();
            list.add(r);
            ranges.put(bb, list);
            
            if (DEBUG)
                System.out.println(" create new range for bb " + bb.getName());
        }
        else {
            if (DEBUG)
                System.out.println(" found range for bb " + bb.getName());
            
            List<Range> iterList = ranges.get(bb);
            List<Range> list = new LinkedList<Range>();
            
            Range newRange = new Range(start, end);
            for (Range r : iterList) {
                if (r.overlap(newRange)) {
                    newRange.merge(r.start, r.end);
                } else {
                    list.add(r);
                }
            }
            list.add(newRange);
            ranges.put(bb, list);
        }
    }
    
    public void addRange(MCBasicBlock bb, Range r) {
        if (r != null)
            addRange(bb, r.getStart(), r.getEnd());
    }
    
    public void addRange(MCBasicBlock bb, List<Range> list) {
        for (Range r : list)
            addRange(bb, r);
    }
    
    private static void error(String message) {
        System.err.println(message);
        Thread.dumpStack();
        System.exit(4);
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public MCRegister getReg() {
        return reg;
    }

    public void setReg(MCRegister reg) {
        this.reg = reg;
    }
    
    public HashMap<MCBasicBlock, List<Range>> getRanges() {
        return ranges;
    }
}
