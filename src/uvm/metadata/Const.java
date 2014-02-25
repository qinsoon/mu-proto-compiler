package uvm.metadata;

import uvm.Type;

public class Const {
    Type t;
    Number immValue;
    String name;
    
    public Const(String name, Type t, Number immValue) {
        this.name = name;
        this.t = t;
        this.immValue = immValue;
    }

    public Type getType() {
        return t;
    }

    public Number getImmValue() {
        return immValue;
    }

    public String getName() {
        return name;
    }
}
