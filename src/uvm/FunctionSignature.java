package uvm;

import java.util.List;

public class FunctionSignature {
    Type returnType;
    List<Type> paramTypes;
    
    public FunctionSignature(Type returnType, List<Type> paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(returnType);
        str.append(" (");
        for (int i = 0; i < paramTypes.size(); i++) {
            str.append(paramTypes.get(i));
            if (i != paramTypes.size() - 1)
                str.append(", ");
        }
        str.append(")");
            
        return str.toString();
    }
    
    public String prettyPrint() {
        return toString();
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }
}
