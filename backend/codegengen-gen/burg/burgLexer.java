// Generated from /Users/apple/uvm-compiler-antlr/burg.g4 by ANTLR 4.2
package burg;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class burgLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__3=1, T__2=2, T__1=3, T__0=4, NONTERM=5, TERM=6, COST=7, WS=8, LINE_COMMENT=9;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'.target'", "':'", "'('", "NONTERM", "TERM", "COST", "WS", "LINE_COMMENT"
	};
	public static final String[] ruleNames = {
		"T__3", "T__2", "T__1", "T__0", "NONTERM", "TERM", "COST", "CHAR", "DIGIT", 
		"LOWERCHAR", "UPPERCHAR", "WS", "LINE_COMMENT"
	};


	public burgLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "burg.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\13Z\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\4\3\4\3\5\3\5\3\6\3\6\6\6.\n\6\r\6\16\6/\3\7\3\7\6\7\64\n\7\r\7\16"+
		"\7\65\3\b\6\b9\n\b\r\b\16\b:\3\t\3\t\3\t\3\t\5\tA\n\t\3\n\3\n\3\13\3\13"+
		"\3\f\3\f\3\r\6\rJ\n\r\r\r\16\rK\3\r\3\r\3\16\3\16\3\16\3\16\7\16T\n\16"+
		"\f\16\16\16W\13\16\3\16\3\16\2\2\17\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\2"+
		"\23\2\25\2\27\2\31\n\33\13\3\2\b\4\2//aa\3\2\62;\3\2c|\3\2C\\\5\2\13\f"+
		"\17\17\"\"\4\2\f\f\17\17]\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\3"+
		"\35\3\2\2\2\5\37\3\2\2\2\7\'\3\2\2\2\t)\3\2\2\2\13+\3\2\2\2\r\61\3\2\2"+
		"\2\178\3\2\2\2\21@\3\2\2\2\23B\3\2\2\2\25D\3\2\2\2\27F\3\2\2\2\31I\3\2"+
		"\2\2\33O\3\2\2\2\35\36\7+\2\2\36\4\3\2\2\2\37 \7\60\2\2 !\7v\2\2!\"\7"+
		"c\2\2\"#\7t\2\2#$\7i\2\2$%\7g\2\2%&\7v\2\2&\6\3\2\2\2\'(\7<\2\2(\b\3\2"+
		"\2\2)*\7*\2\2*\n\3\2\2\2+-\5\25\13\2,.\5\21\t\2-,\3\2\2\2./\3\2\2\2/-"+
		"\3\2\2\2/\60\3\2\2\2\60\f\3\2\2\2\61\63\5\27\f\2\62\64\5\21\t\2\63\62"+
		"\3\2\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\66\16\3\2\2\2\679\5"+
		"\23\n\28\67\3\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2;\20\3\2\2\2<A\5\25\13"+
		"\2=A\5\27\f\2>A\5\23\n\2?A\t\2\2\2@<\3\2\2\2@=\3\2\2\2@>\3\2\2\2@?\3\2"+
		"\2\2A\22\3\2\2\2BC\t\3\2\2C\24\3\2\2\2DE\t\4\2\2E\26\3\2\2\2FG\t\5\2\2"+
		"G\30\3\2\2\2HJ\t\6\2\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3\2\2\2LM\3\2\2"+
		"\2MN\b\r\2\2N\32\3\2\2\2OP\7\61\2\2PQ\7\61\2\2QU\3\2\2\2RT\n\7\2\2SR\3"+
		"\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WU\3\2\2\2XY\b\16\2\2Y\34"+
		"\3\2\2\2\t\2/\65:@KU\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}