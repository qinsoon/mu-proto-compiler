// Generated from uIR.g4 by ANTLR 4.2
package parser;
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
		public MetaDataContext metaData(int i) {
			return getRuleContext(MetaDataContext.class,i);
		}
		public List<MetaDataContext> metaData() {
			return getRuleContexts(MetaDataContext.class);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 27) | (1L << 28) | (1L << 31) | (1L << 35))) != 0)) {
				{
				{
				setState(32); metaData();
				}
				}
				setState(37);
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
			setState(42);
			switch (_input.LA(1)) {
			case 31:
				enterOuterAlt(_localctx, 1);
				{
				setState(38); constDef();
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 2);
				{
				setState(39); funcDef();
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 3);
				{
				setState(40); label();
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 4);
				{
				setState(41); typeDef();
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
			setState(44); match(31);
			setState(45); match(IDENTIFIER);
			setState(46); match(24);
			setState(47); match(6);
			setState(48); type();
			setState(49); match(30);
			setState(50); immediate();
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
			setState(52); match(35);
			setState(53); match(IDENTIFIER);
			setState(54); match(6);
			setState(55); funcSig();
			setState(56); match(30);
			setState(57); funcBody();
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
		public List<FuncBodyInstContext> funcBodyInst() {
			return getRuleContexts(FuncBodyInstContext.class);
		}
		public FuncBodyInstContext funcBodyInst(int i) {
			return getRuleContext(FuncBodyInstContext.class,i);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59); match(11);
			setState(61); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(60); funcBodyInst();
				}
				}
				setState(63); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 8) | (1L << 19) | (1L << 20) | (1L << 27) | (1L << 31) | (1L << 32) | (1L << IDENTIFIER))) != 0) );
			setState(65); match(16);
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
		public ConstDefContext constDef() {
			return getRuleContext(ConstDefContext.class,0);
		}
		public LabelContext label() {
			return getRuleContext(LabelContext.class,0);
		}
		public InstContext inst() {
			return getRuleContext(InstContext.class,0);
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
		try {
			setState(70);
			switch (_input.LA(1)) {
			case 31:
				enterOuterAlt(_localctx, 1);
				{
				setState(67); constDef();
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 2);
				{
				setState(68); label();
				}
				break;
			case 8:
			case 19:
			case 20:
			case 32:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 3);
				{
				setState(69); inst();
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
			setState(72); match(27);
			setState(73); match(IDENTIFIER);
			setState(74); match(3);
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
			setState(76); match(28);
			setState(77); match(IDENTIFIER);
			setState(78); typeDescriptor();
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
			setState(82);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(80); match(IDENTIFIER);
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
				setState(81); typeDescriptor();
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
			setState(86);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(84); intImmediate();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(85); fpImmediate();
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
			setState(88); type();
			setState(89); match(4);
			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 5) | (1L << 7) | (1L << 9) | (1L << 10) | (1L << 17) | (1L << 18) | (1L << 33) | (1L << IDENTIFIER))) != 0)) {
				{
				{
				setState(90); type();
				}
				}
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(96); match(21);
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
		public TypeDescriptorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDescriptor; }
	 
		public TypeDescriptorContext() { }
		public void copyFrom(TypeDescriptorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class VoidTypeContext extends TypeDescriptorContext {
		public VoidTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterVoidType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitVoidType(this);
		}
	}
	public static class ArrayTypeContext extends TypeDescriptorContext {
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ArrayTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterArrayType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitArrayType(this);
		}
	}
	public static class IRefTypeContext extends TypeDescriptorContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IRefTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterIRefType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitIRefType(this);
		}
	}
	public static class IntTypeContext extends TypeDescriptorContext {
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public IntTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterIntType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitIntType(this);
		}
	}
	public static class DoubleTypeContext extends TypeDescriptorContext {
		public DoubleTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterDoubleType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitDoubleType(this);
		}
	}
	public static class StructTypeContext extends TypeDescriptorContext {
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public StructTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterStructType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitStructType(this);
		}
	}
	public static class RefTypeContext extends TypeDescriptorContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public RefTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterRefType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitRefType(this);
		}
	}
	public static class FloatTypeContext extends TypeDescriptorContext {
		public FloatTypeContext(TypeDescriptorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterFloatType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitFloatType(this);
		}
	}

	public final TypeDescriptorContext typeDescriptor() throws RecognitionException {
		TypeDescriptorContext _localctx = new TypeDescriptorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_typeDescriptor);
		int _la;
		try {
			setState(131);
			switch (_input.LA(1)) {
			case 5:
				_localctx = new IntTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(98); match(5);
				setState(99); match(6);
				setState(100); intImmediate();
				setState(101); match(30);
				}
				break;
			case 17:
				_localctx = new FloatTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(103); match(17);
				}
				break;
			case 10:
				_localctx = new DoubleTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(104); match(10);
				}
				break;
			case 18:
				_localctx = new StructTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(105); match(18);
				setState(106); match(6);
				setState(108); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(107); type();
					}
					}
					setState(110); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 5) | (1L << 7) | (1L << 9) | (1L << 10) | (1L << 17) | (1L << 18) | (1L << 33) | (1L << IDENTIFIER))) != 0) );
				setState(112); match(30);
				}
				break;
			case 7:
				_localctx = new ArrayTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(114); match(7);
				setState(115); match(6);
				setState(116); type();
				setState(117); intImmediate();
				setState(118); match(30);
				}
				break;
			case 2:
				_localctx = new RefTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(120); match(2);
				setState(121); match(6);
				setState(122); type();
				setState(123); match(30);
				}
				break;
			case 33:
				_localctx = new IRefTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(125); match(33);
				setState(126); match(6);
				setState(127); type();
				setState(128); match(30);
				}
				break;
			case 9:
				_localctx = new VoidTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(130); match(9);
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
		public InstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inst; }
	 
		public InstContext() { }
		public void copyFrom(InstContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InstBranchContext extends InstContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public InstBranchContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstBranch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstBranch(this);
		}
	}
	public static class InstRet2Context extends InstContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public InstRet2Context(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstRet2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstRet2(this);
		}
	}
	public static class InstAllocaContext extends InstContext {
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstAllocaContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstAlloca(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstAlloca(this);
		}
	}
	public static class InstParamContext extends InstContext {
		public IntImmediateContext intImmediate() {
			return getRuleContext(IntImmediateContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public InstParamContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstParam(this);
		}
	}
	public static class InstShlContext extends InstContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstShlContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstShl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstShl(this);
		}
	}
	public static class InstLoadContext extends InstContext {
		public TerminalNode IDENTIFIER(int i) {
			return getToken(uIRParser.IDENTIFIER, i);
		}
		public List<TerminalNode> IDENTIFIER() { return getTokens(uIRParser.IDENTIFIER); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstLoadContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstLoad(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstLoad(this);
		}
	}
	public static class InstBranch2Context extends InstContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode IDENTIFIER(int i) {
			return getToken(uIRParser.IDENTIFIER, i);
		}
		public List<TerminalNode> IDENTIFIER() { return getTokens(uIRParser.IDENTIFIER); }
		public InstBranch2Context(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstBranch2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstBranch2(this);
		}
	}
	public static class InstStoreContext extends InstContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstStoreContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstStore(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstStore(this);
		}
	}
	public static class InstAddContext extends InstContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstAddContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstAdd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstAdd(this);
		}
	}
	public static class InstPhiContext extends InstContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
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
		public InstPhiContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstPhi(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstPhi(this);
		}
	}
	public static class InstSgtContext extends InstContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public TerminalNode IDENTIFIER() { return getToken(uIRParser.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public InstSgtContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstSgt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstSgt(this);
		}
	}

	public final InstContext inst() throws RecognitionException {
		InstContext _localctx = new InstContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_inst);
		try {
			setState(206);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				_localctx = new InstParamContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(133); match(IDENTIFIER);
				setState(134); match(24);
				setState(135); match(15);
				setState(136); intImmediate();
				}
				break;

			case 2:
				_localctx = new InstSgtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(137); match(IDENTIFIER);
				setState(138); match(24);
				setState(139); match(14);
				setState(140); match(6);
				setState(141); type();
				setState(142); match(30);
				setState(143); value();
				setState(144); value();
				}
				break;

			case 3:
				_localctx = new InstBranch2Context(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(146); match(32);
				setState(147); value();
				setState(148); match(IDENTIFIER);
				setState(149); match(IDENTIFIER);
				}
				break;

			case 4:
				_localctx = new InstShlContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(151); match(IDENTIFIER);
				setState(152); match(24);
				setState(153); match(29);
				setState(154); match(6);
				setState(155); type();
				setState(156); match(30);
				setState(157); value();
				setState(158); value();
				}
				break;

			case 5:
				_localctx = new InstAddContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(160); match(IDENTIFIER);
				setState(161); match(24);
				setState(162); match(25);
				setState(163); match(6);
				setState(164); type();
				setState(165); match(30);
				setState(166); value();
				setState(167); value();
				}
				break;

			case 6:
				_localctx = new InstBranchContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(169); match(20);
				setState(170); match(IDENTIFIER);
				}
				break;

			case 7:
				_localctx = new InstPhiContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(171); match(IDENTIFIER);
				setState(172); match(24);
				setState(173); match(34);
				setState(174); match(6);
				setState(175); type();
				setState(176); match(30);
				setState(177); value();
				setState(178); match(IDENTIFIER);
				setState(179); value();
				setState(180); match(IDENTIFIER);
				}
				break;

			case 8:
				_localctx = new InstRet2Context(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(182); match(8);
				setState(183); value();
				}
				break;

			case 9:
				_localctx = new InstAllocaContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(184); match(IDENTIFIER);
				setState(185); match(24);
				setState(186); match(12);
				setState(187); match(6);
				setState(188); type();
				setState(189); match(30);
				}
				break;

			case 10:
				_localctx = new InstStoreContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(191); match(19);
				setState(192); match(6);
				setState(193); type();
				setState(194); match(30);
				setState(195); match(IDENTIFIER);
				setState(196); value();
				}
				break;

			case 11:
				_localctx = new InstLoadContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(198); match(IDENTIFIER);
				setState(199); match(24);
				setState(200); match(13);
				setState(201); match(6);
				setState(202); type();
				setState(203); match(30);
				setState(204); match(IDENTIFIER);
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
			setState(210);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(208); match(IDENTIFIER);
				}
				break;
			case 1:
			case 23:
			case DIGITS:
				enterOuterAlt(_localctx, 2);
				{
				setState(209); immediate();
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
			setState(213);
			_la = _input.LA(1);
			if (_la==1 || _la==23) {
				{
				setState(212);
				_la = _input.LA(1);
				if ( !(_la==1 || _la==23) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
			}

			setState(215); match(DIGITS);
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
			setState(232);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(218);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(217);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(220); match(DIGITS);
				setState(221); match(26);
				setState(223);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(222);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(225); match(DIGITS);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(227);
				_la = _input.LA(1);
				if (_la==1 || _la==23) {
					{
					setState(226);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==23) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(229); match(DIGITS);
				setState(230); match(22);
				setState(231); match(DIGITS);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3+\u00ed\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\7\2$\n"+
		"\2\f\2\16\2\'\13\2\3\3\3\3\3\3\3\3\5\3-\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\6\6@\n\6\r\6\16\6A\3\6\3\6"+
		"\3\7\3\7\3\7\5\7I\n\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\5\nU\n\n"+
		"\3\13\3\13\5\13Y\n\13\3\f\3\f\3\f\7\f^\n\f\f\f\16\fa\13\f\3\f\3\f\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\6\ro\n\r\r\r\16\rp\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u0086"+
		"\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u00d1\n\16\3\17\3\17\5\17\u00d5\n\17\3\20\5"+
		"\20\u00d8\n\20\3\20\3\20\3\21\5\21\u00dd\n\21\3\21\3\21\3\21\5\21\u00e2"+
		"\n\21\3\21\3\21\5\21\u00e6\n\21\3\21\3\21\3\21\5\21\u00eb\n\21\3\21\2"+
		"\2\22\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \2\3\4\2\3\3\31\31\u00fe"+
		"\2%\3\2\2\2\4,\3\2\2\2\6.\3\2\2\2\b\66\3\2\2\2\n=\3\2\2\2\fH\3\2\2\2\16"+
		"J\3\2\2\2\20N\3\2\2\2\22T\3\2\2\2\24X\3\2\2\2\26Z\3\2\2\2\30\u0085\3\2"+
		"\2\2\32\u00d0\3\2\2\2\34\u00d4\3\2\2\2\36\u00d7\3\2\2\2 \u00ea\3\2\2\2"+
		"\"$\5\4\3\2#\"\3\2\2\2$\'\3\2\2\2%#\3\2\2\2%&\3\2\2\2&\3\3\2\2\2\'%\3"+
		"\2\2\2(-\5\6\4\2)-\5\b\5\2*-\5\16\b\2+-\5\20\t\2,(\3\2\2\2,)\3\2\2\2,"+
		"*\3\2\2\2,+\3\2\2\2-\5\3\2\2\2./\7!\2\2/\60\7\'\2\2\60\61\7\32\2\2\61"+
		"\62\7\b\2\2\62\63\5\22\n\2\63\64\7 \2\2\64\65\5\24\13\2\65\7\3\2\2\2\66"+
		"\67\7%\2\2\678\7\'\2\289\7\b\2\29:\5\26\f\2:;\7 \2\2;<\5\n\6\2<\t\3\2"+
		"\2\2=?\7\r\2\2>@\5\f\7\2?>\3\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2BC\3\2"+
		"\2\2CD\7\22\2\2D\13\3\2\2\2EI\5\6\4\2FI\5\16\b\2GI\5\32\16\2HE\3\2\2\2"+
		"HF\3\2\2\2HG\3\2\2\2I\r\3\2\2\2JK\7\35\2\2KL\7\'\2\2LM\7\5\2\2M\17\3\2"+
		"\2\2NO\7\36\2\2OP\7\'\2\2PQ\5\30\r\2Q\21\3\2\2\2RU\7\'\2\2SU\5\30\r\2"+
		"TR\3\2\2\2TS\3\2\2\2U\23\3\2\2\2VY\5\36\20\2WY\5 \21\2XV\3\2\2\2XW\3\2"+
		"\2\2Y\25\3\2\2\2Z[\5\22\n\2[_\7\6\2\2\\^\5\22\n\2]\\\3\2\2\2^a\3\2\2\2"+
		"_]\3\2\2\2_`\3\2\2\2`b\3\2\2\2a_\3\2\2\2bc\7\27\2\2c\27\3\2\2\2de\7\7"+
		"\2\2ef\7\b\2\2fg\5\36\20\2gh\7 \2\2h\u0086\3\2\2\2i\u0086\7\23\2\2j\u0086"+
		"\7\f\2\2kl\7\24\2\2ln\7\b\2\2mo\5\22\n\2nm\3\2\2\2op\3\2\2\2pn\3\2\2\2"+
		"pq\3\2\2\2qr\3\2\2\2rs\7 \2\2s\u0086\3\2\2\2tu\7\t\2\2uv\7\b\2\2vw\5\22"+
		"\n\2wx\5\36\20\2xy\7 \2\2y\u0086\3\2\2\2z{\7\4\2\2{|\7\b\2\2|}\5\22\n"+
		"\2}~\7 \2\2~\u0086\3\2\2\2\177\u0080\7#\2\2\u0080\u0081\7\b\2\2\u0081"+
		"\u0082\5\22\n\2\u0082\u0083\7 \2\2\u0083\u0086\3\2\2\2\u0084\u0086\7\13"+
		"\2\2\u0085d\3\2\2\2\u0085i\3\2\2\2\u0085j\3\2\2\2\u0085k\3\2\2\2\u0085"+
		"t\3\2\2\2\u0085z\3\2\2\2\u0085\177\3\2\2\2\u0085\u0084\3\2\2\2\u0086\31"+
		"\3\2\2\2\u0087\u0088\7\'\2\2\u0088\u0089\7\32\2\2\u0089\u008a\7\21\2\2"+
		"\u008a\u00d1\5\36\20\2\u008b\u008c\7\'\2\2\u008c\u008d\7\32\2\2\u008d"+
		"\u008e\7\20\2\2\u008e\u008f\7\b\2\2\u008f\u0090\5\22\n\2\u0090\u0091\7"+
		" \2\2\u0091\u0092\5\34\17\2\u0092\u0093\5\34\17\2\u0093\u00d1\3\2\2\2"+
		"\u0094\u0095\7\"\2\2\u0095\u0096\5\34\17\2\u0096\u0097\7\'\2\2\u0097\u0098"+
		"\7\'\2\2\u0098\u00d1\3\2\2\2\u0099\u009a\7\'\2\2\u009a\u009b\7\32\2\2"+
		"\u009b\u009c\7\37\2\2\u009c\u009d\7\b\2\2\u009d\u009e\5\22\n\2\u009e\u009f"+
		"\7 \2\2\u009f\u00a0\5\34\17\2\u00a0\u00a1\5\34\17\2\u00a1\u00d1\3\2\2"+
		"\2\u00a2\u00a3\7\'\2\2\u00a3\u00a4\7\32\2\2\u00a4\u00a5\7\33\2\2\u00a5"+
		"\u00a6\7\b\2\2\u00a6\u00a7\5\22\n\2\u00a7\u00a8\7 \2\2\u00a8\u00a9\5\34"+
		"\17\2\u00a9\u00aa\5\34\17\2\u00aa\u00d1\3\2\2\2\u00ab\u00ac\7\26\2\2\u00ac"+
		"\u00d1\7\'\2\2\u00ad\u00ae\7\'\2\2\u00ae\u00af\7\32\2\2\u00af\u00b0\7"+
		"$\2\2\u00b0\u00b1\7\b\2\2\u00b1\u00b2\5\22\n\2\u00b2\u00b3\7 \2\2\u00b3"+
		"\u00b4\5\34\17\2\u00b4\u00b5\7\'\2\2\u00b5\u00b6\5\34\17\2\u00b6\u00b7"+
		"\7\'\2\2\u00b7\u00d1\3\2\2\2\u00b8\u00b9\7\n\2\2\u00b9\u00d1\5\34\17\2"+
		"\u00ba\u00bb\7\'\2\2\u00bb\u00bc\7\32\2\2\u00bc\u00bd\7\16\2\2\u00bd\u00be"+
		"\7\b\2\2\u00be\u00bf\5\22\n\2\u00bf\u00c0\7 \2\2\u00c0\u00d1\3\2\2\2\u00c1"+
		"\u00c2\7\25\2\2\u00c2\u00c3\7\b\2\2\u00c3\u00c4\5\22\n\2\u00c4\u00c5\7"+
		" \2\2\u00c5\u00c6\7\'\2\2\u00c6\u00c7\5\34\17\2\u00c7\u00d1\3\2\2\2\u00c8"+
		"\u00c9\7\'\2\2\u00c9\u00ca\7\32\2\2\u00ca\u00cb\7\17\2\2\u00cb\u00cc\7"+
		"\b\2\2\u00cc\u00cd\5\22\n\2\u00cd\u00ce\7 \2\2\u00ce\u00cf\7\'\2\2\u00cf"+
		"\u00d1\3\2\2\2\u00d0\u0087\3\2\2\2\u00d0\u008b\3\2\2\2\u00d0\u0094\3\2"+
		"\2\2\u00d0\u0099\3\2\2\2\u00d0\u00a2\3\2\2\2\u00d0\u00ab\3\2\2\2\u00d0"+
		"\u00ad\3\2\2\2\u00d0\u00b8\3\2\2\2\u00d0\u00ba\3\2\2\2\u00d0\u00c1\3\2"+
		"\2\2\u00d0\u00c8\3\2\2\2\u00d1\33\3\2\2\2\u00d2\u00d5\7\'\2\2\u00d3\u00d5"+
		"\5\24\13\2\u00d4\u00d2\3\2\2\2\u00d4\u00d3\3\2\2\2\u00d5\35\3\2\2\2\u00d6"+
		"\u00d8\t\2\2\2\u00d7\u00d6\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00d9\3\2"+
		"\2\2\u00d9\u00da\7&\2\2\u00da\37\3\2\2\2\u00db\u00dd\t\2\2\2\u00dc\u00db"+
		"\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\7&\2\2\u00df"+
		"\u00e1\7\34\2\2\u00e0\u00e2\t\2\2\2\u00e1\u00e0\3\2\2\2\u00e1\u00e2\3"+
		"\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00eb\7&\2\2\u00e4\u00e6\t\2\2\2\u00e5"+
		"\u00e4\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e8\7&"+
		"\2\2\u00e8\u00e9\7\30\2\2\u00e9\u00eb\7&\2\2\u00ea\u00dc\3\2\2\2\u00ea"+
		"\u00e5\3\2\2\2\u00eb!\3\2\2\2\22%,AHTX_p\u0085\u00d0\u00d4\u00d7\u00dc"+
		"\u00e1\u00e5\u00ea";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}