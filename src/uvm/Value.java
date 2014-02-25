package uvm;

public class Value {
    boolean isGlobal;
    String name;
    
    public Value (String name, boolean global) {
        this.name = name;
        this.isGlobal = global;
    }
}
