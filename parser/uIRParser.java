// Generated from uIR.g4 by ANTLR 4.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class uIRParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__34=1, T__33=2, T__32=3, T__31=4, T__30=5, T__29=6, T__28=7, T__27=8, 
		T__26=9, T__25=10, T__24=11, T__23=12, T__22=13, T__21=14, T__20=15, T__19=16, 
		T__18=17, T__17=18, T__16=19, T__15=20, T__14=21, T__13=22, T__12=23, 
		T__11=24, T__10=25, T__9=26, T__8=27, T__7=28, T__6=29, T__5=30, T__4=31, 
		T__3=32, T__2=33, T__1=34, T__0=35, DIGITS=36, IDENTIFIER=37, GLOBAL_ID_PREFIX=38, 
		LOCAL_ID_PREFIX=39, WS=40, LINE_COMMENT=41;
	public static final String[] tokenNames = {
		"<INVALID>", "'-'", "'ref'", "':'", "'('", "'int'", "'<'", "'array'", 
		"'RET2'", "'void'", "'double'", "'{'", "'ALLOCA'", "'LOAD'", "'SGT'", 
		"'PARAM'", "'}'", "'float'", "'struct'", "'STORE'", "'BRANCH'", "')'", 
		"'.'", "'+'", "'='", "'ADD'", "'e'", "'.label'", "'.typedef'", "'SHL'", 
		"'>'", "'.const'", "'BRANCH2'", "'iref'", "'PHI'", "'.funcdef'", "DIGITS", 
		"IDENTIFIER", "'@'", "'%'", "WS", "LINE_COMMENT"
	};
	public static final int
		RULE_ir = 0, RULE_metaData = 1, RULE_constDef = 2, RULE_funcDef = 3, RULE_funcBody = 4, 
		RULE_funcBodyInst = 5, RULE_label = 6, RULE_typeDef = 7, RULE_type = 8, 
		RULE_immediate = 9, RULE_funcSig = 10, RULE_typeDescriptor = 11, RULE_inst = 12, 
		RULE_value = 13, RULE_intImmediate = 14, RULE_fpImmediate = 15;
	public static final String[] ruleNames = {
		"ir", "metaData", "constDef", "funcDef", "funcBody", "funcBodyInst", "label", 
		"typeDef", "type", "immediate", "funcSig", "typeDescriptor", "inst", "value", 
		"intImmediate", "fpImmediate"
	};

	@Override
	public String getGrammarFileName() { return "uIR.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public uIRParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class IrContext extends ParserRuleContext {
		public MetaDataContext metaData() {
			return getRuleContext(MetaDataContext.class,0);
		}
		public InstContext inst() {
			return getRuleContext(InstContext.class,0);
		}
		public IrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ir; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterIr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitIr(this);
		}
	}

	public final IrContext ir() throws RecognitionException {
		IrContext _localctx = new IrContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_ir);
		try {
			setState(34);
			switch (_input.LA(1)) {
			case 27:
			case 28:
			case 31:
			case 35:
				enterOuterAlt(_localctx, 1);
				{
				setState(32); metaData();
				}
				break;
			case 8:
			case 19:
			case 20:
			case 32:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(33); inst();
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

	public static class MetaDataContext extends ParserRuleContext {
		public FuncDefContext funcDef() {
			return getRuleContext(FuncDefContext.class,0);
		}
		public ConstDefContext constDef() {
			return getRuleContext(ConstDefContext.class,0);
		}
		public LabelContext label() {
			return getRuleContext(LabelContext.class,0);
		}
		public TypeDefContext typeDef() {
			return getRuleContext(TypeDefContext.class,0);
		}
		public MetaDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterMetaData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitMetaData(this);
		}
	}

	public final MetaDataContext metaData() throws RecognitionException {
		MetaDataContext _localctx = new MetaDataContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_metaData);
		try {
			setState(40);
			switch (_input.LA(1)) {
			case 31:
				enterOuterAlt(_localctx, 1);
				{
				setState(36); constDef();
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 2);
				{
				setState(37); funcDef();
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 3);
				{
				setState(38); label();
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 4);
				{
				setState(39); typeDef();
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

	public static class ConstDefContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ImmediateContext immediate() {
			return getRuleContext(ImmediateContext.class,0);
		}
		public ConstDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterConstDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitConstDef(this);
		}
	}

	public final ConstDefContext constDef() throws RecognitionException {
		ConstDefContext _localctx = new ConstDefContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_constDef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42); match(31);
			setState(43); match(IDENTIFIER);
			setState(44); match(24);
			setState(45); match(6);
			setState(46); type();
			setState(47); match(30);
			setState(48); immediate();
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

	public static class FuncDefContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public FuncSigContext funcSig() {
			return getRuleContext(FuncSigContext.class,0);
		}
		public FuncBodyContext funcBody() {
			return getRuleContext(FuncBodyContext.class,0);
		}
		public FuncDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFuncDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFuncDef(this);
		}
	}

	public final FuncDefContext funcDef() throws RecognitionException {
		FuncDefContext _localctx = new FuncDefContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_funcDef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50); match(35);
			setState(51); match(IDENTIFIER);
			setState(52); match(6);
			setState(53); funcSig();
			setState(54); match(30);
			setState(55); funcBody();
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

	public static class FuncBodyContext extends ParserRuleContext {
		public FuncBodyInstContext funcBodyInst() {
			return getRuleContext(FuncBodyInstContext.class,0);
		}
		public FuncBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFuncBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFuncBody(this);
		}
	}

	public final FuncBodyContext funcBody() throws RecognitionException {
		FuncBodyContext _localctx = new FuncBodyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_funcBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57); match(11);
			setState(58); funcBodyInst();
			setState(59); match(16);
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

	public static class FuncBodyInstContext extends ParserRuleContext {
		public InstContext inst(int i) {
			return getRuleContext(InstContext.class,i);
		}
		public ConstDefContext constDef(int i) {
			return getRuleContext(ConstDefContext.class,i);
		}
		public List<ConstDefContext> constDef() {
			return getRuleContexts(ConstDefContext.class);
		}
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public List<InstContext> inst() {
			return getRuleContexts(InstContext.class);
		}
		public FuncBodyInstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcBodyInst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFuncBodyInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFuncBodyInst(this);
		}
	}

	public final FuncBodyInstContext funcBodyInst() throws RecognitionException {
		FuncBodyInstContext _localctx = new FuncBodyInstContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_funcBodyInst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 8) | (1L << 19) | (1L << 20) | (1L << 27) | (1L << 31) | (1L << 32) | (1L << IDENTIFIER))) != 0)) {
				{
				setState(64);
				switch (_input.LA(1)) {
				case 31:
					{
					setState(61); constDef();
					}
					break;
				case 27:
					{
					setState(62); label();
					}
					break;
				case 8:
				case 19:
				case 20:
				case 32:
				case IDENTIFIER:
					{
					setState(63); inst();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(68);
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

	public static class LabelContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitLabel(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); match(27);
			setState(70); match(IDENTIFIER);
			setState(71); match(3);
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

	public static class TypeDefContext extends ParserRuleContext {
		public TypeDescriptorContext typeDescriptor() {
			return getRuleContext(TypeDescriptorContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterTypeDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitTypeDef(this);
		}
	}

	public final TypeDefContext typeDef() throws RecognitionException {
		TypeDefContext _localctx = new TypeDefContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_typeDef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73); match(28);
			setState(74); match(IDENTIFIER);
			setState(75); typeDescriptor();
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

	public static class TypeContext extends ParserRuleContext {
		public TypeDescriptorContext typeDescriptor() {
			return getRuleContext(TypeDescriptorContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitType(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_type);
		try {
			setState(79);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(77); match(IDENTIFIER);
				}
				break;
			case 2:
			case 5:
			case 7:
			case 9:
			case 10:
			case 17:
			case 18:
			case 33:
				enterOuterAlt(_localctx, 2);
				{
				setState(78); typeDescriptor();
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

	public static class ImmediateContext extends ParserRuleContext {
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public FpImmediateContext fpImmediate() {
			return getRuleContext(FpImmediateContext.class,0);
		}
		public ImmediateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_immediate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterImmediate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitImmediate(this);
		}
	}

	public final ImmediateContext immediate() throws RecognitionException {
		ImmediateContext _localctx = new ImmediateContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_immediate);
		try {
			setState(83);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(81); intImmediate();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(82); fpImmediate();
				}
				break;
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

	public static class FuncSigContext extends ParserRuleContext {
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public FuncSigContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcSig; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFuncSig(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFuncSig(this);
		}
	}

	public final FuncSigContext funcSig() throws RecognitionException {
		FuncSigContext _localctx = new FuncSigContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_funcSig);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85); type();
			setState(86); match(4);
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 5) | (1L << 7) | (1L << 9) | (1L << 10) | (1L << 17) | (1L << 18) | (1L << 33) | (1L << IDENTIFIER))) != 0)) {
				{
				{
				setState(87); type();
				}
				}
				setState(92);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(93); match(21);
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

	public static class TypeDescriptorContext extends ParserRuleContext {
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeDescriptorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDescriptor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterTypeDescriptor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitTypeDescriptor(this);
		}
	}

	public final TypeDescriptorContext typeDescriptor() throws RecognitionException {
		TypeDescriptorContext _localctx = new TypeDescriptorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_typeDescriptor);
		int _la;
		try {
			setState(128);
			switch (_input.LA(1)) {
			case 5:
				enterOuterAlt(_localctx, 1);
				{
				setState(95); match(5);
				setState(96); match(6);
				setState(97); intImmediate();
				setState(98); match(30);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 2);
				{
				setState(100); match(17);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 3);
				{
				setState(101); match(10);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 4);
				{
				setState(102); match(18);
				setState(103); match(6);
				setState(105); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(104); type();
					}
					}
					setState(107); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 5) | (1L << 7) | (1L << 9) | (1L << 10) | (1L << 17) | (1L << 18) | (1L << 33) | (1L << IDENTIFIER))) != 0) );
				setState(109); match(30);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 5);
				{
				setState(111); match(7);
				setState(112); match(6);
				setState(113); type();
				setState(114); intImmediate();
				setState(115); match(30);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 6);
				{
				setState(117); match(2);
				setState(118); match(6);
				setState(119); type();
				setState(120); match(30);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 7);
				{
				setState(122); match(33);
				setState(123); match(6);
				setState(124); type();
				setState(125); match(30);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 8);
				{
				setState(127); match(9);
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

	public static class InstContext extends ParserRuleContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public TerminalNode IDENTIFIER(int i) {
			return getToken(uIRParser.IDENTIFIER, i);
		}
		public List<TerminalNode> IDENTIFIER() { return getTokens(uIRParser.IDENTIFIER); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInst(this);
		}
	}

	public final InstContext inst() throws RecognitionException {
		InstContext _localctx = new InstContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_inst);
		try {
			setState(203);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(130); match(IDENTIFIER);
				setState(131); match(24);
				setState(132); match(15);
				setState(133); intImmediate();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(134); match(IDENTIFIER);
				setState(135); match(24);
				setState(136); match(14);
				setState(137); match(6);
				setState(138); type();
				setState(139); match(30);
				setState(140); value();
				setState(141); value();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(143); match(32);
				setState(144); value();
				setState(145); match(IDENTIFIER);
				setState(146); match(IDENTIFIER);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(148); match(IDENTIFIER);
				setState(149); match(24);
				setState(150); match(29);
				setState(151); match(6);
				setState(152); type();
				setState(153); match(30);
				setState(154); value();
				setState(155); value();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(157); match(IDENTIFIER);
				setState(158); match(24);
				setState(159); match(25);
				setState(160); match(6);
				setState(161); type();
				setState(162); match(30);
				setState(163); value();
				setState(164); value();
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(166); match(20);
				setState(167); match(IDENTIFIER);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(168); match(IDENTIFIER);
				setState(169); match(24);
				setState(170); match(34);
				setState(171); match(6);
				setState(172); type();
				setState(173); match(30);
				setState(174); value();
				setState(175); match(IDENTIFIER);
				setState(176); value();
				setState(177); match(IDENTIFIER);
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(179); match(8);
				setState(180); value();
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(181); match(IDENTIFIER);
				setState(182); match(24);
				setState(183); match(12);
				setState(184); match(6);
				setState(185); type();
				setState(186); match(30);
				}
				break;

			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(188); match(19);
				setState(189); match(6);
				setState(190); type();
				setState(191); match(30);
				setState(192); match(IDENTIFIER);
				setState(193); value();
				}
				break;

			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(195); match(IDENTIFIER);
				setState(196); match(24);
				setState(197); match(13);
				setState(198); match(6);
				setState(199); type();
				setState(200); match(30);
				setState(201); match(IDENTIFIER);
				}
				break;
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

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public ImmediateContext immediate() {
			return getRuleContext(ImmediateContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_value);
		try {
			setState(207);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(205); match(IDENTIFIER);
				}
				break;
			case 1:
			case 23:
			case DIGITS:
				enterOuterAlt(_localctx, 2);
				{
				setState(206); immediate();
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

	public static class IntImmediateContext extends ParserRuleContext {
		public TerminalNode DIGITS() { return getToken(uIRParser.DIGITS, 0); }
		public IntImmediateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intImmediate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterIntImmediate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitIntImmediate(this);
		}
	}

	public final IntImmediateContext intImmediate() throws RecognitionException {
		IntImmediateContext _localctx = new IntImmediateContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_intImmediate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			_la = _input.LA(1);
			if (_la==1 || _la==23) {
				{
				setState(209);
				_la = _input.LA(1);
				if ( !(_la==1 || _la==23) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
			}

			setState(212); match(DIGITS);
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

	public static class FpImmediateContext extends ParserRuleContext {
		public List<TerminalNode> DIGITS() { return getTokens(uIRParser.DIGITS); }
		public TerminalNode DIGITS(int i) {
			return getToken(uIRParser.DIGITS, i);
		}
		public FpImmediateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fpImmediate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFpImmediate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFpImmediate(this);
		}
	}

	public final FpImmediateContext fpImmediate() throws RecognitionException {
		FpImmediateContext _localctx = new FpImmediateContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_fpImmediate);
		int _la;
		try {
			setState(229);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(214);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(217); match(DIGITS);
				setState(218); match(26);
				setState(220);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(219);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(222); match(DIGITS);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(223);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(226); match(DIGITS);
				setState(227); match(22);
				setState(228); match(DIGITS);
				}
				break;
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3+\u00ea\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\5"+
		"\2%\n\2\3\3\3\3\3\3\3\3\5\3+\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\7\7C\n\7\f\7\16\7F"+
		"\13\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\5\nR\n\n\3\13\3\13\5\13"+
		"V\n\13\3\f\3\f\3\f\7\f[\n\f\f\f\16\f^\13\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\6\rl\n\r\r\r\16\rm\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u0083\n\r\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\5\16\u00ce\n\16\3\17\3\17\5\17\u00d2\n\17\3\20\5\20\u00d5\n\20\3"+
		"\20\3\20\3\21\5\21\u00da\n\21\3\21\3\21\3\21\5\21\u00df\n\21\3\21\3\21"+
		"\5\21\u00e3\n\21\3\21\3\21\3\21\5\21\u00e8\n\21\3\21\2\2\22\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \2\3\4\2\3\3\31\31\u00fb\2$\3\2\2\2\4*\3"+
		"\2\2\2\6,\3\2\2\2\b\64\3\2\2\2\n;\3\2\2\2\fD\3\2\2\2\16G\3\2\2\2\20K\3"+
		"\2\2\2\22Q\3\2\2\2\24U\3\2\2\2\26W\3\2\2\2\30\u0082\3\2\2\2\32\u00cd\3"+
		"\2\2\2\34\u00d1\3\2\2\2\36\u00d4\3\2\2\2 \u00e7\3\2\2\2\"%\5\4\3\2#%\5"+
		"\32\16\2$\"\3\2\2\2$#\3\2\2\2%\3\3\2\2\2&+\5\6\4\2\'+\5\b\5\2(+\5\16\b"+
		"\2)+\5\20\t\2*&\3\2\2\2*\'\3\2\2\2*(\3\2\2\2*)\3\2\2\2+\5\3\2\2\2,-\7"+
		"!\2\2-.\7\'\2\2./\7\32\2\2/\60\7\b\2\2\60\61\5\22\n\2\61\62\7 \2\2\62"+
		"\63\5\24\13\2\63\7\3\2\2\2\64\65\7%\2\2\65\66\7\'\2\2\66\67\7\b\2\2\67"+
		"8\5\26\f\289\7 \2\29:\5\n\6\2:\t\3\2\2\2;<\7\r\2\2<=\5\f\7\2=>\7\22\2"+
		"\2>\13\3\2\2\2?C\5\6\4\2@C\5\16\b\2AC\5\32\16\2B?\3\2\2\2B@\3\2\2\2BA"+
		"\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2E\r\3\2\2\2FD\3\2\2\2GH\7\35\2\2"+
		"HI\7\'\2\2IJ\7\5\2\2J\17\3\2\2\2KL\7\36\2\2LM\7\'\2\2MN\5\30\r\2N\21\3"+
		"\2\2\2OR\7\'\2\2PR\5\30\r\2QO\3\2\2\2QP\3\2\2\2R\23\3\2\2\2SV\5\36\20"+
		"\2TV\5 \21\2US\3\2\2\2UT\3\2\2\2V\25\3\2\2\2WX\5\22\n\2X\\\7\6\2\2Y[\5"+
		"\22\n\2ZY\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]_\3\2\2\2^\\\3\2\2\2"+
		"_`\7\27\2\2`\27\3\2\2\2ab\7\7\2\2bc\7\b\2\2cd\5\36\20\2de\7 \2\2e\u0083"+
		"\3\2\2\2f\u0083\7\23\2\2g\u0083\7\f\2\2hi\7\24\2\2ik\7\b\2\2jl\5\22\n"+
		"\2kj\3\2\2\2lm\3\2\2\2mk\3\2\2\2mn\3\2\2\2no\3\2\2\2op\7 \2\2p\u0083\3"+
		"\2\2\2qr\7\t\2\2rs\7\b\2\2st\5\22\n\2tu\5\36\20\2uv\7 \2\2v\u0083\3\2"+
		"\2\2wx\7\4\2\2xy\7\b\2\2yz\5\22\n\2z{\7 \2\2{\u0083\3\2\2\2|}\7#\2\2}"+
		"~\7\b\2\2~\177\5\22\n\2\177\u0080\7 \2\2\u0080\u0083\3\2\2\2\u0081\u0083"+
		"\7\13\2\2\u0082a\3\2\2\2\u0082f\3\2\2\2\u0082g\3\2\2\2\u0082h\3\2\2\2"+
		"\u0082q\3\2\2\2\u0082w\3\2\2\2\u0082|\3\2\2\2\u0082\u0081\3\2\2\2\u0083"+
		"\31\3\2\2\2\u0084\u0085\7\'\2\2\u0085\u0086\7\32\2\2\u0086\u0087\7\21"+
		"\2\2\u0087\u00ce\5\36\20\2\u0088\u0089\7\'\2\2\u0089\u008a\7\32\2\2\u008a"+
		"\u008b\7\20\2\2\u008b\u008c\7\b\2\2\u008c\u008d\5\22\n\2\u008d\u008e\7"+
		" \2\2\u008e\u008f\5\34\17\2\u008f\u0090\5\34\17\2\u0090\u00ce\3\2\2\2"+
		"\u0091\u0092\7\"\2\2\u0092\u0093\5\34\17\2\u0093\u0094\7\'\2\2\u0094\u0095"+
		"\7\'\2\2\u0095\u00ce\3\2\2\2\u0096\u0097\7\'\2\2\u0097\u0098\7\32\2\2"+
		"\u0098\u0099\7\37\2\2\u0099\u009a\7\b\2\2\u009a\u009b\5\22\n\2\u009b\u009c"+
		"\7 \2\2\u009c\u009d\5\34\17\2\u009d\u009e\5\34\17\2\u009e\u00ce\3\2\2"+
		"\2\u009f\u00a0\7\'\2\2\u00a0\u00a1\7\32\2\2\u00a1\u00a2\7\33\2\2\u00a2"+
		"\u00a3\7\b\2\2\u00a3\u00a4\5\22\n\2\u00a4\u00a5\7 \2\2\u00a5\u00a6\5\34"+
		"\17\2\u00a6\u00a7\5\34\17\2\u00a7\u00ce\3\2\2\2\u00a8\u00a9\7\26\2\2\u00a9"+
		"\u00ce\7\'\2\2\u00aa\u00ab\7\'\2\2\u00ab\u00ac\7\32\2\2\u00ac\u00ad\7"+
		"$\2\2\u00ad\u00ae\7\b\2\2\u00ae\u00af\5\22\n\2\u00af\u00b0\7 \2\2\u00b0"+
		"\u00b1\5\34\17\2\u00b1\u00b2\7\'\2\2\u00b2\u00b3\5\34\17\2\u00b3\u00b4"+
		"\7\'\2\2\u00b4\u00ce\3\2\2\2\u00b5\u00b6\7\n\2\2\u00b6\u00ce\5\34\17\2"+
		"\u00b7\u00b8\7\'\2\2\u00b8\u00b9\7\32\2\2\u00b9\u00ba\7\16\2\2\u00ba\u00bb"+
		"\7\b\2\2\u00bb\u00bc\5\22\n\2\u00bc\u00bd\7 \2\2\u00bd\u00ce\3\2\2\2\u00be"+
		"\u00bf\7\25\2\2\u00bf\u00c0\7\b\2\2\u00c0\u00c1\5\22\n\2\u00c1\u00c2\7"+
		" \2\2\u00c2\u00c3\7\'\2\2\u00c3\u00c4\5\34\17\2\u00c4\u00ce\3\2\2\2\u00c5"+
		"\u00c6\7\'\2\2\u00c6\u00c7\7\32\2\2\u00c7\u00c8\7\17\2\2\u00c8\u00c9\7"+
		"\b\2\2\u00c9\u00ca\5\22\n\2\u00ca\u00cb\7 \2\2\u00cb\u00cc\7\'\2\2\u00cc"+
		"\u00ce\3\2\2\2\u00cd\u0084\3\2\2\2\u00cd\u0088\3\2\2\2\u00cd\u0091\3\2"+
		"\2\2\u00cd\u0096\3\2\2\2\u00cd\u009f\3\2\2\2\u00cd\u00a8\3\2\2\2\u00cd"+
		"\u00aa\3\2\2\2\u00cd\u00b5\3\2\2\2\u00cd\u00b7\3\2\2\2\u00cd\u00be\3\2"+
		"\2\2\u00cd\u00c5\3\2\2\2\u00ce\33\3\2\2\2\u00cf\u00d2\7\'\2\2\u00d0\u00d2"+
		"\5\24\13\2\u00d1\u00cf\3\2\2\2\u00d1\u00d0\3\2\2\2\u00d2\35\3\2\2\2\u00d3"+
		"\u00d5\t\2\2\2\u00d4\u00d3\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\3\2"+
		"\2\2\u00d6\u00d7\7&\2\2\u00d7\37\3\2\2\2\u00d8\u00da\t\2\2\2\u00d9\u00d8"+
		"\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00dc\7&\2\2\u00dc"+
		"\u00de\7\34\2\2\u00dd\u00df\t\2\2\2\u00de\u00dd\3\2\2\2\u00de\u00df\3"+
		"\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e8\7&\2\2\u00e1\u00e3\t\2\2\2\u00e2"+
		"\u00e1\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e5\7&"+
		"\2\2\u00e5\u00e6\7\30\2\2\u00e6\u00e8\7&\2\2\u00e7\u00d9\3\2\2\2\u00e7"+
		"\u00e2\3\2\2\2\u00e8!\3\2\2\2\22$*BDQU\\m\u0082\u00cd\u00d1\u00d4\u00d9"+
		"\u00de\u00e2\u00e7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}