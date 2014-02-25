package parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncSigContext;
import parser.uIRParser.ImmediateContext;
import parser.uIRParser.InstContext;
import parser.uIRParser.TypeContext;
import parser.uIRParser.TypeDescriptorContext;
import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.Type;
import uvm.metadata.Const;
import uvm.type.Int;

public abstract class ASTHelper {
    private ASTHelper() {}
    
    public static String getIdentifierName(TerminalNode id, boolean expectGlobal) 
            throws ASTParsingException {
        String text = id.getText();
        
        if (expectGlobal && text.charAt(0) != '@')
            throw new ASTParsingException("Met identifier" + id.getText() + 
                    " while expecting a global identifier");
        
        if (text.charAt(0) == '@' || text.charAt(0) == '%')
            return text.substring(1);

        throw new ASTParsingException("Invalid identifier " + id.getText() + 
                ", expecting @ or % prefix");
    }
    
    public static FunctionSignature getFunctionSignature(FuncSigContext ctx) throws ASTParsingException{
        List<TypeContext> types = ctx.type();
        
        Type returnType = null;
        List<Type> paramTypes = new ArrayList<Type>();
        
        for (int i = 0; i < types.size(); i++) {
            if (i == 0) {
                // return type
                returnType = getType(types.get(i));
            } else  
                // param type 
                paramTypes.add(getType(types.get(i)));
        }
        
        return new FunctionSignature(returnType, paramTypes);
    }

    public static Type getType(TypeContext typeContext) throws ASTParsingException {
        if (typeContext.typeDescriptor() != null) {
            // defining a type via type descriptor
            TypeDescriptorContext ctx = typeContext.typeDescriptor();
            if (ctx instanceof parser.uIRParser.IntTypeContext) {
                int size = Integer.parseInt(
                        ((parser.uIRParser.IntTypeContext) ctx).intImmediate().getText());
                return Int.findOrCreate(size);
            } else {
                throw new ASTParsingException("Missing implementation on " + ctx.getClass().toString());
            }
        } else {
            // referring a type via IDENTIFIER
            throw new ASTParsingException("Missing implementation on type by IDENTIFIER");
        }
    }
    
    public static Const getConst(ConstDefContext ctx) throws ASTParsingException {
        String name = getIdentifierName(ctx.IDENTIFIER(), false);
        Type type = getType(ctx.type());
        Number imm = getImmediateValue(ctx.immediate(), type);
        
        return new Const(name, type, imm);
    }
    
    public static Number getImmediateValue(ImmediateContext ctx, Type t) throws ASTParsingException {
        Number ret;
        if (t instanceof uvm.type.Int) {
            ret = Long.parseLong(ctx.getText());
        } else if (t instanceof uvm.type.Float) {
            ret = Float.parseFloat(ctx.getText());
        } else if (t instanceof uvm.type.Double) {
            ret = Double.parseDouble(ctx.getText());
        } else throw new ASTParsingException("Invalid immediate type: " + t);
        
        return ret;
    }
    
    public static Instruction getInstruction(InstContext ctx) throws ASTParsingException {
        return null;
    }
}
