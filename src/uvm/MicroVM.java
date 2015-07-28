package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.UVMCompiler;
import uvm.objectmodel.SimpleObjectModel;
import uvm.runtime.UVMRuntime;

public class MicroVM {
    public static final int POINTER_SIZE = 64;
    
    public static final MicroVM v = new MicroVM();
    
    private MicroVM() {}
    
    /*
     * TYPES
     */
    public HashMap<String, Type> types = new HashMap<String, Type>();
    
    public void declareType(String name, Type t) {
        types.put(name, t);
        System.out.println("declared type: " + t.prettyPrint());
    }
    
    /*
     * FUNCTIONS
     */
    public HashMap<String, Function> funcs = new HashMap<String, Function>();
    
    public void declareFunc(String name, Function f) {
        funcs.put(name, f);
        System.out.println("declared func: " + f);
    }
    
    public Function getFunction(String name) {
        return funcs.get(name);
    }
    
    /*
     * COMPILED FUNCTIONS
     */    
    public List<CompiledFunction> compiledFuncs = new ArrayList<CompiledFunction>();
    
    public void compiledFunc(CompiledFunction cf) {
        compiledFuncs.add(cf);
    }
    
    /*
     * GLOBAL LABELS
     */
    public HashMap<String, Label> globalLabels = new HashMap<String, Label>();
    
    public Label findOrCreateGlobalLabel(String label) {
    	Label res = globalLabels.get(label);
    	if (res == null) {
    		res = new Label(label);
    		globalLabels.put(label, res);
    	}
    	
    	return res;
    }
    
    /*
     * GLOBAL CONSTS
     */
    public HashMap<String, ImmediateValue> globalConsts = new HashMap<String, ImmediateValue>();
    
    public void defineGlobalConsts(String id, ImmediateValue v) {
    	ImmediateValue res = globalConsts.get(id);
    	if (res == null) {
    		globalConsts.put(id, v);
    		System.out.println("declared const: " + id + " = " + v.prettyPrint());
    	} else {
    		if (!res.equals(v))
    			UVMCompiler.error(String.format("redefining const %s: OLD=%s, NEW=%s", id, res.prettyPrint(), v.prettyPrint()));
    	}
    }
    
    /**
     * may return null
     * @param id
     * @return
     */
    public ImmediateValue getGlobalConsts(String id) {
    	return globalConsts.get(id);
    }
    
    /*
     * OBJECT MODEL
     */
    public SimpleObjectModel objectModel = new SimpleObjectModel();
    
    /*
     * RUNTIME
     */
    public UVMRuntime runtime = new UVMRuntime();
}
