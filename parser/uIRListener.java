// Generated from uIR.g4 by ANTLR 4.2
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link uIRParser}.
 */
public interface uIRListener extends ParseTreeListener {
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
	 * Enter a parse tree produced by {@link uIRParser#typeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterTypeDescriptor(@NotNull uIRParser.TypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#typeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitTypeDescriptor(@NotNull uIRParser.TypeDescriptorContext ctx);

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
	 * Enter a parse tree produced by {@link uIRParser#inst}.
	 * @param ctx the parse tree
	 */
	void enterInst(@NotNull uIRParser.InstContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#inst}.
	 * @param ctx the parse tree
	 */
	void exitInst(@NotNull uIRParser.InstContext ctx);

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
	 * Enter a parse tree produced by {@link uIRParser#metaData}.
	 * @param ctx the parse tree
	 */
	void enterMetaData(@NotNull uIRParser.MetaDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link uIRParser#metaData}.
	 * @param ctx the parse tree
	 */
	void exitMetaData(@NotNull uIRParser.MetaDataContext ctx);
}