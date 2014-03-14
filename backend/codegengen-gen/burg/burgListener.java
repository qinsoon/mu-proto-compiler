// Generated from /Users/apple/uvm-compiler-antlr/burg.g4 by ANTLR 4.2
package burg;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link burgParser}.
 */
public interface burgListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link burgParser#node}.
	 * @param ctx the parse tree
	 */
	void enterNode(@NotNull burgParser.NodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#node}.
	 * @param ctx the parse tree
	 */
	void exitNode(@NotNull burgParser.NodeContext ctx);

	/**
	 * Enter a parse tree produced by {@link burgParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(@NotNull burgParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(@NotNull burgParser.StartContext ctx);

	/**
	 * Enter a parse tree produced by {@link burgParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull burgParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull burgParser.StringContext ctx);

	/**
	 * Enter a parse tree produced by {@link burgParser#targetDecl}.
	 * @param ctx the parse tree
	 */
	void enterTargetDecl(@NotNull burgParser.TargetDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#targetDecl}.
	 * @param ctx the parse tree
	 */
	void exitTargetDecl(@NotNull burgParser.TargetDeclContext ctx);

	/**
	 * Enter a parse tree produced by {@link burgParser#treerule}.
	 * @param ctx the parse tree
	 */
	void enterTreerule(@NotNull burgParser.TreeruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#treerule}.
	 * @param ctx the parse tree
	 */
	void exitTreerule(@NotNull burgParser.TreeruleContext ctx);

	/**
	 * Enter a parse tree produced by {@link burgParser#declare}.
	 * @param ctx the parse tree
	 */
	void enterDeclare(@NotNull burgParser.DeclareContext ctx);
	/**
	 * Exit a parse tree produced by {@link burgParser#declare}.
	 * @param ctx the parse tree
	 */
	void exitDeclare(@NotNull burgParser.DeclareContext ctx);
}