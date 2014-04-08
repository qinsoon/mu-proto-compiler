package uvm.mc;

import java.util.LinkedList;
import java.util.List;

import compiler.UVMCompiler;

import uvm.mc.*;

public class LiveInterval {
    MCRegister i;
    
    LinkedList<Range> ranges = new LinkedList<Range>();
    
    public LiveInterval(MCRegister reg) {
        this.i = reg;
    }
    
    public static class Range{
        public static final int UNKNOWN_START = Integer.MAX_VALUE;
        public static final int UNKNOWN_END   = Integer.MIN_VALUE;
        int start, end;
        
        MCBasicBlock bb;
        
        public Range(MCBasicBlock bb, int start, int end) {
            this.bb = bb;
            this.start = start;
            this.end = end;
        }
        
        public void merge(int start, int end) {
            System.out.print(" merge range [" + this.start + "," + this.end + "[ with [" + start + ", " + end + "[");
            this.start = Math.min(this.start, start);
            this.end = Math.max(this.end, end);
            System.out.println(" => [" + this.start + ", " + this.end + "[");
        }
        
        public boolean contains(int start, int end) {
            if (start >= this.start && end <= this.end)
                return true;
            else return false;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public MCBasicBlock getBb() {
            return bb;
        }

        public void setStart(int start) {
            if (start < bb.getFirst().sequence)
                UVMCompiler.error("underflow. Want to set range start as " + start + " but the bb starts with " + bb.getFirst().sequence);
            this.start = start;
        }

        public void setEnd(int end) {
            if (end > bb.getLast().sequence)
                UVMCompiler.error("overflow. Want to set range end as " + end + " but the bb starts with " + bb.getLast().sequence);
            
            this.end = end;
        }
    }
    
    public List<Range> getRanges() {
        return ranges;
    }
    
    public void addRange(MCBasicBlock bb, int start, int end) {
        boolean addNewRange = true;
        for (Range r : ranges) {
            if (r.bb.equals(bb)) {
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
}
