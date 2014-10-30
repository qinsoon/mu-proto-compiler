package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uvm.objectmodel.SimpleObjectModel;

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
     * OBJECT MODEL
     */
    public SimpleObjectModel objectModel = new SimpleObjectModel();
}
