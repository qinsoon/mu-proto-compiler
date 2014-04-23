package uvm.mc;

import java.util.LinkedList;
import java.util.List;
import uvm.mc.*;

public class LiveInterval {
    MCRegister reg;
    
    int begin = Integer.MAX_VALUE;
    int end = Integer.MIN_VALUE;
    LinkedList<Range> ranges = new LinkedList<Range>();
    
    public LiveInterval(MCRegister reg) {
        this.reg = reg;
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append(reg.prettyPrint());
        ret.append(" beg=" + begin);
        ret.append(" end=" + end);
        ret.append(" ");
        for (Range r : ranges) {
            ret.append(r.prettyPrint() + ",");
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
        for (Range range : ranges) {
            for (Range range2 : another.ranges) {
                if (range.overlap(range2) && !range.contains(mc))
                    return true;
            }
        }
        
        return false;
    }
    
    public boolean overlap(int mc) {
        for (Range range : ranges)
            if (range.contains(mc))
                return false;
        
        return true;
    }
    
    public boolean overlap(LiveInterval another) {
        for (Range range : ranges) {
            for (Range range2 : another.ranges) {
                if (range.overlap(range2))
                    return true;
            }
        }
        
        return false;
    }
    
    public static class Range{
        public static final int UNKNOWN_START = Integer.MAX_VALUE;
        public static final int UNKNOWN_END   = Integer.MIN_VALUE;
        int start, end;
        
        private MCBasicBlock bb;
        
        public Range(MCBasicBlock bb, int start, int end) {
            if (bb == null) {
                System.out.println("setting a range's bb to null");
                Thread.dumpStack();
                System.exit(5);
            }
            
            if (start != UNKNOWN_START && end != UNKNOWN_END && start > end) {
                System.out.print("creating range for " + bb.getName());
                System.out.println(" [" + start + "," + end + "[");
                
                Thread.dumpStack();
                System.exit(5);                
            }
            
            this.bb = bb;
            this.start = start;
            this.end = end;
        }
        
        public MCBasicBlock getBB() {
            return bb;
        }
        
        public void setBB(MCBasicBlock b) {
            if (b == null) {
                System.out.println("setBB() bb == null");
                Thread.dumpStack();
                System.exit(5);
            }
            this.bb = b;
        }
        
        public String prettyPrint() {
            return "[" + start + "," + end + "[";
        }
        
        public boolean contains(int mc) {
            if (start <= mc && end >= mc)
                return true;
             return false;
        }
        
        public void merge(int start, int end) {
            System.out.print(" merge range [" + this.start + "," + this.end + "[ with [" + start + ", " + end + "[");
            this.start = Math.min(this.start, start);
            this.end = Math.max(this.end, end);
            System.out.println(" => [" + this.start + ", " + this.end + "[");
        }
        
        public static Range union(Range a, Range b) {
            if (a == null)
                return b;
            
            if (b == null)
                return a;
            
            if (!a.bb.equals(b.bb))
                error("trying to get a union of ranges from different BBs: 1)" + a.bb.getName() + " 2)" + b.bb.getName());
            
            if (!a.adjacent(b) && !a.overlap(b))
                error("trying to get a union of two non-overlaping/adjancent ranges: 1)" + a.prettyPrint() + " 2)" + b.prettyPrint());
            
            int start = Math.min(a.start, b.start);
            int end = Math.max(a.end, b.end);
            
            return new Range(a.bb, start, end);
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
            if (start < bb.getFirst().sequence)
                error("underflow. Want to set range start as " + start + " but the bb starts with " + bb.getFirst().sequence);
            this.start = start;
        }

        public void setEnd(int end) {
            if (end > bb.getLast().sequence)
                error("overflow. Want to set range end as " + end + " but the bb starts with " + bb.getLast().sequence);
            
            this.end = end;
        }
    }
    
    public List<Range> getRanges() {
        return ranges;
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
  public Range getRange(MCBasicBlock bb) {
      for (Range r : ranges)
          if (r.getBB().equals(bb))
              return r;
      
//      error("cannot find range for BB " + bb.getName() + " for reg " + i.prettyPrint());
      return null;
  }
    
    public void removeRange(MCBasicBlock bb) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).getBB().equals(bb)) {
                ranges.remove(i);
                return;
            }
        }
        
        error("cannot find range for BB " + bb.getName() + " for reg " + reg.prettyPrint());
    }
    
    public void replaceRange(MCBasicBlock bb, Range b) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).getBB().equals(bb)) {
                ranges.set(i, b);
                return;
            }
        }
        
        error("cannot find range for BB " + bb.getName() + " for reg " + reg.prettyPrint());
    }
    
    public void addRange(MCBasicBlock bb, int start, int end) {
        boolean addNewRange = true;
        for (Range r : ranges) {
            if (r.getBB().equals(bb)) {
                System.out.println(" found range for bb " + bb.getName());
                r.merge(start, end);
                addNewRange = false;
            }
        }
        
        if (addNewRange) {
            System.out.println(" create new range for bb " + bb.getName());
            ranges.add(new Range(bb, start, end));
        }
    }
    
    public void addRange(Range r) {
        if (r != null)
            addRange(r.getBB(), r.getStart(), r.getEnd());
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
}
