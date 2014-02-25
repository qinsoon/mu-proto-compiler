// Generated from uIR.g4 by ANTLR 4.2
package parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link uIRParser}.
 */
public interface uIRListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link uIRParser#funcSig}.
	 * @param ctx the parse tree
	 */
	void enterFuncSig(@NotNull uIRParser.FuncSigContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#funcSig}.
	 * @param ctx the parse tree
	 */
	void exitFuncSig(@NotNull uIRParser.FuncSigContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(@NotNull uIRParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(@NotNull uIRParser.TypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstPhi}.
	 * @param ctx the parse tree
	 */
	void enterInstPhi(@NotNull uIRParser.InstPhiContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstPhi}.
	 * @param ctx the parse tree
	 */
	void exitInstPhi(@NotNull uIRParser.InstPhiContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#FloatType}.
	 * @param ctx the parse tree
	 */
	void enterFloatType(@NotNull uIRParser.FloatTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#FloatType}.
	 * @param ctx the parse tree
	 */
	void exitFloatType(@NotNull uIRParser.FloatTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstSgt}.
	 * @param ctx the parse tree
	 */
	void enterInstSgt(@NotNull uIRParser.InstSgtContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstSgt}.
	 * @param ctx the parse tree
	 */
	void exitInstSgt(@NotNull uIRParser.InstSgtContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstAlloca}.
	 * @param ctx the parse tree
	 */
	void enterInstAlloca(@NotNull uIRParser.InstAllocaContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstAlloca}.
	 * @param ctx the parse tree
	 */
	void exitInstAlloca(@NotNull uIRParser.InstAllocaContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#IntType}.
	 * @param ctx the parse tree
	 */
	void enterIntType(@NotNull uIRParser.IntTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#IntType}.
	 * @param ctx the parse tree
	 */
	void exitIntType(@NotNull uIRParser.IntTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(@NotNull uIRParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(@NotNull uIRParser.ValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#typeDef}.
	 * @param ctx the parse tree
	 */
	void enterTypeDef(@NotNull uIRParser.TypeDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#typeDef}.
	 * @param ctx the parse tree
	 */
	void exitTypeDef(@NotNull uIRParser.TypeDefContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#DoubleType}.
	 * @param ctx the parse tree
	 */
	void enterDoubleType(@NotNull uIRParser.DoubleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#DoubleType}.
	 * @param ctx the parse tree
	 */
	void exitDoubleType(@NotNull uIRParser.DoubleTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstAdd}.
	 * @param ctx the parse tree
	 */
	void enterInstAdd(@NotNull uIRParser.InstAddContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstAdd}.
	 * @param ctx the parse tree
	 */
	void exitInstAdd(@NotNull uIRParser.InstAddContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#metaData}.
	 * @param ctx the parse tree
	 */
	void enterMetaData(@NotNull uIRParser.MetaDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#metaData}.
	 * @param ctx the parse tree
	 */
	void exitMetaData(@NotNull uIRParser.MetaDataContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#constDef}.
	 * @param ctx the parse tree
	 */
	void enterConstDef(@NotNull uIRParser.ConstDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#constDef}.
	 * @param ctx the parse tree
	 */
	void exitConstDef(@NotNull uIRParser.ConstDefContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#ir}.
	 * @param ctx the parse tree
	 */
	void enterIr(@NotNull uIRParser.IrContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#ir}.
	 * @param ctx the parse tree
	 */
	void exitIr(@NotNull uIRParser.IrContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#ArrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(@NotNull uIRParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#ArrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(@NotNull uIRParser.ArrayTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#funcBodyInst}.
	 * @param ctx the parse tree
	 */
	void enterFuncBodyInst(@NotNull uIRParser.FuncBodyInstContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#funcBodyInst}.
	 * @param ctx the parse tree
	 */
	void exitFuncBodyInst(@NotNull uIRParser.FuncBodyInstContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstParam}.
	 * @param ctx the parse tree
	 */
	void enterInstParam(@NotNull uIRParser.InstParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstParam}.
	 * @param ctx the parse tree
	 */
	void exitInstParam(@NotNull uIRParser.InstParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstShl}.
	 * @param ctx the parse tree
	 */
	void enterInstShl(@NotNull uIRParser.InstShlContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstShl}.
	 * @param ctx the parse tree
	 */
	void exitInstShl(@NotNull uIRParser.InstShlContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstLoad}.
	 * @param ctx the parse tree
	 */
	void enterInstLoad(@NotNull uIRParser.InstLoadContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstLoad}.
	 * @param ctx the parse tree
	 */
	void exitInstLoad(@NotNull uIRParser.InstLoadContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#fpImmediate}.
	 * @param ctx the parse tree
	 */
	void enterFpImmediate(@NotNull uIRParser.FpImmediateContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#fpImmediate}.
	 * @param ctx the parse tree
	 */
	void exitFpImmediate(@NotNull uIRParser.FpImmediateContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstStore}.
	 * @param ctx the parse tree
	 */
	void enterInstStore(@NotNull uIRParser.InstStoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstStore}.
	 * @param ctx the parse tree
	 */
	void exitInstStore(@NotNull uIRParser.InstStoreContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(@NotNull uIRParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(@NotNull uIRParser.LabelContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstBranch2}.
	 * @param ctx the parse tree
	 */
	void enterInstBranch2(@NotNull uIRParser.InstBranch2Context ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstBranch2}.
	 * @param ctx the parse tree
	 */
	void exitInstBranch2(@NotNull uIRParser.InstBranch2Context ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#funcDef}.
	 * @param ctx the parse tree
	 */
	void enterFuncDef(@NotNull uIRParser.FuncDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#funcDef}.
	 * @param ctx the parse tree
	 */
	void exitFuncDef(@NotNull uIRParser.FuncDefContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#intImmediate}.
	 * @param ctx the parse tree
	 */
	void enterIntImmediate(@NotNull uIRParser.IntImmediateContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#intImmediate}.
	 * @param ctx the parse tree
	 */
	void exitIntImmediate(@NotNull uIRParser.IntImmediateContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#VoidType}.
	 * @param ctx the parse tree
	 */
	void enterVoidType(@NotNull uIRParser.VoidTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#VoidType}.
	 * @param ctx the parse tree
	 */
	void exitVoidType(@NotNull uIRParser.VoidTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstBranch}.
	 * @param ctx the parse tree
	 */
	void enterInstBranch(@NotNull uIRParser.InstBranchContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstBranch}.
	 * @param ctx the parse tree
	 */
	void exitInstBranch(@NotNull uIRParser.InstBranchContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#immediate}.
	 * @param ctx the parse tree
	 */
	void enterImmediate(@NotNull uIRParser.ImmediateContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#immediate}.
	 * @param ctx the parse tree
	 */
	void exitImmediate(@NotNull uIRParser.ImmediateContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#InstRet2}.
	 * @param ctx the parse tree
	 */
	void enterInstRet2(@NotNull uIRParser.InstRet2Context ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#InstRet2}.
	 * @param ctx the parse tree
	 */
	void exitInstRet2(@NotNull uIRParser.InstRet2Context ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#IRefType}.
	 * @param ctx the parse tree
	 */
	void enterIRefType(@NotNull uIRParser.IRefTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#IRefType}.
	 * @param ctx the parse tree
	 */
	void exitIRefType(@NotNull uIRParser.IRefTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#StructType}.
	 * @param ctx the parse tree
	 */
	void enterStructType(@NotNull uIRParser.StructTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#StructType}.
	 * @param ctx the parse tree
	 */
	void exitStructType(@NotNull uIRParser.StructTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#funcBody}.
	 * @param ctx the parse tree
	 */
	void enterFuncBody(@NotNull uIRParser.FuncBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#funcBody}.
	 * @param ctx the parse tree
	 */
	void exitFuncBody(@NotNull uIRParser.FuncBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link uIRParser#RefType}.
	 * @param ctx the parse tree
	 */
	void enterRefType(@NotNull uIRParser.RefTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#RefType}.
	 * @param ctx the parse tree
	 */
	void exitRefType(@NotNull uIRParser.RefTypeContext ctx);
}