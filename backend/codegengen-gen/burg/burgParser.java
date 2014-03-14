// Generated from /Users/apple/uvm-compiler-antlr/burg.g4 by ANTLR 4.2
package burg;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class burgParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__3=1, T__2=2, T__1=3, T__0=4, NONTERM=5, TERM=6, COST=7, WS=8, LINE_COMMENT=9;
	public static final String[] tokenNames = {
		"<INVALID>", "')'", "'.target'", "':'", "'('", "NONTERM", "TERM", "COST", 
		"WS", "LINE_COMMENT"
	};
	public static final int
		RULE_start = 0, RULE_declare = 1, RULE_targetDecl = 2, RULE_string = 3, 
		RULE_treerule = 4, RULE_node = 5;
	public static final String[] ruleNames = {
		"start", "declare", "targetDecl", "string", "treerule", "node"
	};

	@Override
	public String getGrammarFileName() { return "burg.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public burgParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StartContext extends ParserRuleContext {
		public TreeruleContext treerule(int i) {
			return getRuleContext(TreeruleContext.class,i);
		}
		public List<TreeruleContext> treerule() {
			return getRuleContexts(TreeruleContext.class);
		}
		public DeclareContext declare(int i) {
			return getRuleContext(DeclareContext.class,i);
		}
		public List<DeclareContext> declare() {
			return getRuleContexts(DeclareContext.class);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitStart(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==2 || _la==NONTERM) {
				{
				setState(14);
				switch (_input.LA(1)) {
				case 2:
					{
					setState(12); declare();
					}
					break;
				case NONTERM:
					{
					setState(13); treerule();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclareContext extends ParserRuleContext {
		public TargetDeclContext targetDecl() {
			return getRuleContext(TargetDeclContext.class,0);
		}
		public DeclareContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declare; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterDeclare(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitDeclare(this);
		}
	}

	public final DeclareContext declare() throws RecognitionException {
		DeclareContext _localctx = new DeclareContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_declare);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); targetDecl();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TargetDeclContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TargetDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_targetDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterTargetDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitTargetDecl(this);
		}
	}

	public final TargetDeclContext targetDecl() throws RecognitionException {
		TargetDeclContext _localctx = new TargetDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_targetDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21); match(2);
			setState(22); string();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public TerminalNode TERM() { return getToken(burgParser.TERM, 0); }
		public TerminalNode NONTERM() { return getToken(burgParser.NONTERM, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			_la = _input.LA(1);
			if ( !(_la==NONTERM || _la==TERM) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TreeruleContext extends ParserRuleContext {
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public TerminalNode COST() { return getToken(burgParser.COST, 0); }
		public TerminalNode NONTERM() { return getToken(burgParser.NONTERM, 0); }
		public TreeruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_treerule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterTreerule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitTreerule(this);
		}
	}

	public final TreeruleContext treerule() throws RecognitionException {
		TreeruleContext _localctx = new TreeruleContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_treerule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26); match(NONTERM);
			setState(27); match(3);
			setState(28); node();
			setState(29); match(COST);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public TerminalNode NONTERM() { return getToken(burgParser.NONTERM, 0); }
		public TerminalNode TERM() { return getToken(burgParser.TERM, 0); }
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).enterNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof burgListener ) ((burgListener)listener).exitNode(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_node);
		int _la;
		try {
			setState(43);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(31); match(TERM);
				setState(40);
				_la = _input.LA(1);
				if (_la==4) {
					{
					setState(32); match(4);
					setState(34); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(33); node();
						}
						}
						setState(36); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==NONTERM || _la==TERM );
					setState(38); match(1);
					}
				}

				}
				break;
			case NONTERM:
				enterOuterAlt(_localctx, 2);
				{
				setState(42); match(NONTERM);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\13\60\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\7\2\21\n\2\f\2\16\2\24\13"+
		"\2\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\6\7%\n"+
		"\7\r\7\16\7&\3\7\3\7\5\7+\n\7\3\7\5\7.\n\7\3\7\2\2\b\2\4\6\b\n\f\2\3\3"+
		"\2\7\b.\2\22\3\2\2\2\4\25\3\2\2\2\6\27\3\2\2\2\b\32\3\2\2\2\n\34\3\2\2"+
		"\2\f-\3\2\2\2\16\21\5\4\3\2\17\21\5\n\6\2\20\16\3\2\2\2\20\17\3\2\2\2"+
		"\21\24\3\2\2\2\22\20\3\2\2\2\22\23\3\2\2\2\23\3\3\2\2\2\24\22\3\2\2\2"+
		"\25\26\5\6\4\2\26\5\3\2\2\2\27\30\7\4\2\2\30\31\5\b\5\2\31\7\3\2\2\2\32"+
		"\33\t\2\2\2\33\t\3\2\2\2\34\35\7\7\2\2\35\36\7\5\2\2\36\37\5\f\7\2\37"+
		" \7\t\2\2 \13\3\2\2\2!*\7\b\2\2\"$\7\6\2\2#%\5\f\7\2$#\3\2\2\2%&\3\2\2"+
		"\2&$\3\2\2\2&\'\3\2\2\2\'(\3\2\2\2()\7\3\2\2)+\3\2\2\2*\"\3\2\2\2*+\3"+
		"\2\2\2+.\3\2\2\2,.\7\7\2\2-!\3\2\2\2-,\3\2\2\2.\r\3\2\2\2\7\20\22&*-";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}