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
		T__37=1, T__36=2, T__35=3, T__34=4, T__33=5, T__32=6, T__31=7, T__30=8, 
		T__29=9, T__28=10, T__27=11, T__26=12, T__25=13, T__24=14, T__23=15, T__22=16, 
		T__21=17, T__20=18, T__19=19, T__18=20, T__17=21, T__16=22, T__15=23, 
		T__14=24, T__13=25, T__12=26, T__11=27, T__10=28, T__9=29, T__8=30, T__7=31, 
		T__6=32, T__5=33, T__4=34, T__3=35, T__2=36, T__1=37, T__0=38, DIGITS=39, 
		IDENTIFIER=40, GLOBAL_ID_PREFIX=41, LOCAL_ID_PREFIX=42, WS=43, LINE_COMMENT=44;
	public static final String[] tokenNames = {
		"<INVALID>", "'-'", "'ref'", "':'", "'('", "'<'", "'int'", "'array'", 
		"'RET2'", "'EQ'", "'void'", "'double'", "'{'", "'ALLOCA'", "'LOAD'", "'SLT'", 
		"'SGT'", "'PARAM'", "'}'", "'float'", "'struct'", "'STORE'", "'BRANCH'", 
		"'SREM'", "')'", "'.'", "'+'", "'='", "'ADD'", "'e'", "'.label'", "'.typedef'", 
		"'SHL'", "'>'", "'.const'", "'BRANCH2'", "'iref'", "'PHI'", "'.funcdef'", 
		"DIGITS", "IDENTIFIER", "'@'", "'%'", "WS", "LINE_COMMENT"
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
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 30) | (1L << 31) | (1L << 34) | (1L << 38))) != 0)) {
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
			case 34:
				enterOuterAlt(_localctx, 1);
				{
				setState(38); constDef();
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 2);
				{
				setState(39); funcDef();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 3);
				{
				setState(40); label();
				}
				break;
			case 31:
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
			setState(44); match(34);
			setState(45); match(IDENTIFIER);
			setState(46); match(27);
			setState(47); match(5);
			setState(48); type();
			setState(49); match(33);
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
			setState(52); match(38);
			setState(53); match(IDENTIFIER);
			setState(54); match(5);
			setState(55); funcSig();
			setState(56); match(33);
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
			setState(59); match(12);
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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 8) | (1L << 21) | (1L << 22) | (1L << 30) | (1L << 34) | (1L << 35) | (1L << IDENTIFIER))) != 0) );
			setState(65); match(18);
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
			case 34:
				enterOuterAlt(_localctx, 1);
				{
				setState(67); constDef();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 2);
				{
				setState(68); label();
				}
				break;
			case 8:
			case 21:
			case 22:
			case 35:
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
			setState(72); match(30);
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
			setState(76); match(31);
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
			case 6:
			case 7:
			case 10:
			case 11:
			case 19:
			case 20:
			case 36:
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
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 6) | (1L << 7) | (1L << 10) | (1L << 11) | (1L << 19) | (1L << 20) | (1L << 36) | (1L << IDENTIFIER))) != 0)) {
				{
				{
				setState(90); type();
				}
				}
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(96); match(24);
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
			case 6:
				_localctx = new IntTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(98); match(6);
				setState(99); match(5);
				setState(100); intImmediate();
				setState(101); match(33);
				}
				break;
			case 19:
				_localctx = new FloatTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(103); match(19);
				}
				break;
			case 11:
				_localctx = new DoubleTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(104); match(11);
				}
				break;
			case 20:
				_localctx = new StructTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(105); match(20);
				setState(106); match(5);
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
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 6) | (1L << 7) | (1L << 10) | (1L << 11) | (1L << 19) | (1L << 20) | (1L << 36) | (1L << IDENTIFIER))) != 0) );
				setState(112); match(33);
				}
				break;
			case 7:
				_localctx = new ArrayTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(114); match(7);
				setState(115); match(5);
				setState(116); type();
				setState(117); intImmediate();
				setState(118); match(33);
				}
				break;
			case 2:
				_localctx = new RefTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(120); match(2);
				setState(121); match(5);
				setState(122); type();
				setState(123); match(33);
				}
				break;
			case 36:
				_localctx = new IRefTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(125); match(36);
				setState(126); match(5);
				setState(127); type();
				setState(128); match(33);
				}
				break;
			case 10:
				_localctx = new VoidTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(130); match(10);
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
	public static class InstEqContext extends InstContext {
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
		public InstEqContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstEq(this);
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
	public static class InstSremContext extends InstContext {
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
		public InstSremContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstSrem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstSrem(this);
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
	public static class InstSltContext extends InstContext {
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
		public InstSltContext(InstContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).enterInstSlt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof uIRListener ) ((uIRListener)listener).exitInstSlt(this);
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

	public final InstContext inst() throws RecognitionException {
		InstContext _localctx = new InstContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_inst);
		try {
			setState(235);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				_localctx = new InstParamContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(133); match(IDENTIFIER);
				setState(134); match(27);
				setState(135); match(17);
				setState(136); intImmediate();
				}
				break;

			case 2:
				_localctx = new InstBranchContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(137); match(22);
				setState(138); match(IDENTIFIER);
				}
				break;

			case 3:
				_localctx = new InstBranch2Context(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(139); match(35);
				setState(140); value();
				setState(141); match(IDENTIFIER);
				setState(142); match(IDENTIFIER);
				}
				break;

			case 4:
				_localctx = new InstShlContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(144); match(IDENTIFIER);
				setState(145); match(27);
				setState(146); match(32);
				setState(147); match(5);
				setState(148); type();
				setState(149); match(33);
				setState(150); value();
				setState(151); value();
				}
				break;

			case 5:
				_localctx = new InstAddContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(153); match(IDENTIFIER);
				setState(154); match(27);
				setState(155); match(28);
				setState(156); match(5);
				setState(157); type();
				setState(158); match(33);
				setState(159); value();
				setState(160); value();
				}
				break;

			case 6:
				_localctx = new InstSremContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(162); match(IDENTIFIER);
				setState(163); match(27);
				setState(164); match(23);
				setState(165); match(5);
				setState(166); type();
				setState(167); match(33);
				setState(168); value();
				setState(169); value();
				}
				break;

			case 7:
				_localctx = new InstBranchContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(171); match(22);
				setState(172); match(IDENTIFIER);
				}
				break;

			case 8:
				_localctx = new InstPhiContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(173); match(IDENTIFIER);
				setState(174); match(27);
				setState(175); match(37);
				setState(176); match(5);
				setState(177); type();
				setState(178); match(33);
				setState(179); value();
				setState(180); match(IDENTIFIER);
				setState(181); value();
				setState(182); match(IDENTIFIER);
				}
				break;

			case 9:
				_localctx = new InstRet2Context(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(184); match(8);
				setState(185); value();
				}
				break;

			case 10:
				_localctx = new InstAllocaContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(186); match(IDENTIFIER);
				setState(187); match(27);
				setState(188); match(13);
				setState(189); match(5);
				setState(190); type();
				setState(191); match(33);
				}
				break;

			case 11:
				_localctx = new InstStoreContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(193); match(21);
				setState(194); match(5);
				setState(195); type();
				setState(196); match(33);
				setState(197); match(IDENTIFIER);
				setState(198); value();
				}
				break;

			case 12:
				_localctx = new InstLoadContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(200); match(IDENTIFIER);
				setState(201); match(27);
				setState(202); match(14);
				setState(203); match(5);
				setState(204); type();
				setState(205); match(33);
				setState(206); match(IDENTIFIER);
				}
				break;

			case 13:
				_localctx = new InstSgtContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(208); match(IDENTIFIER);
				setState(209); match(27);
				setState(210); match(16);
				setState(211); match(5);
				setState(212); type();
				setState(213); match(33);
				setState(214); value();
				setState(215); value();
				}
				break;

			case 14:
				_localctx = new InstEqContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(217); match(IDENTIFIER);
				setState(218); match(27);
				setState(219); match(9);
				setState(220); match(5);
				setState(221); type();
				setState(222); match(33);
				setState(223); value();
				setState(224); value();
				}
				break;

			case 15:
				_localctx = new InstSltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(226); match(IDENTIFIER);
				setState(227); match(27);
				setState(228); match(15);
				setState(229); match(5);
				setState(230); type();
				setState(231); match(33);
				setState(232); value();
				setState(233); value();
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
			setState(239);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(237); match(IDENTIFIER);
				}
				break;
			case 1:
			case 26:
			case DIGITS:
				enterOuterAlt(_localctx, 2);
				{
				setState(238); immediate();
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
			setState(242);
			_la = _input.LA(1);
			if (_la==1 || _la==26) {
				{
				setState(241);
				_la = _input.LA(1);
				if ( !(_la==1 || _la==26) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
			}

			setState(244); match(DIGITS);
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
			setState(261);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(247);
				_la = _input.LA(1);
				if (_la==1 || _la==26) {
					{
					setState(246);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==26) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(249); match(DIGITS);
				setState(250); match(29);
				setState(252);
				_la = _input.LA(1);
				if (_la==1 || _la==26) {
					{
					setState(251);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==26) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(254); match(DIGITS);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(256);
				_la = _input.LA(1);
				if (_la==1 || _la==26) {
					{
					setState(255);
					_la = _input.LA(1);
					if ( !(_la==1 || _la==26) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(258); match(DIGITS);
				setState(259); match(25);
				setState(260); match(DIGITS);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3.\u010a\4\2\t\2\4"+
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
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\5\16\u00ee\n\16\3\17\3\17\5\17\u00f2\n\17\3"+
		"\20\5\20\u00f5\n\20\3\20\3\20\3\21\5\21\u00fa\n\21\3\21\3\21\3\21\5\21"+
		"\u00ff\n\21\3\21\3\21\5\21\u0103\n\21\3\21\3\21\3\21\5\21\u0108\n\21\3"+
		"\21\2\2\22\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \2\3\4\2\3\3\34\34\u011f"+
		"\2%\3\2\2\2\4,\3\2\2\2\6.\3\2\2\2\b\66\3\2\2\2\n=\3\2\2\2\fH\3\2\2\2\16"+
		"J\3\2\2\2\20N\3\2\2\2\22T\3\2\2\2\24X\3\2\2\2\26Z\3\2\2\2\30\u0085\3\2"+
		"\2\2\32\u00ed\3\2\2\2\34\u00f1\3\2\2\2\36\u00f4\3\2\2\2 \u0107\3\2\2\2"+
		"\"$\5\4\3\2#\"\3\2\2\2$\'\3\2\2\2%#\3\2\2\2%&\3\2\2\2&\3\3\2\2\2\'%\3"+
		"\2\2\2(-\5\6\4\2)-\5\b\5\2*-\5\16\b\2+-\5\20\t\2,(\3\2\2\2,)\3\2\2\2,"+
		"*\3\2\2\2,+\3\2\2\2-\5\3\2\2\2./\7$\2\2/\60\7*\2\2\60\61\7\35\2\2\61\62"+
		"\7\7\2\2\62\63\5\22\n\2\63\64\7#\2\2\64\65\5\24\13\2\65\7\3\2\2\2\66\67"+
		"\7(\2\2\678\7*\2\289\7\7\2\29:\5\26\f\2:;\7#\2\2;<\5\n\6\2<\t\3\2\2\2"+
		"=?\7\16\2\2>@\5\f\7\2?>\3\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2BC\3\2\2"+
		"\2CD\7\24\2\2D\13\3\2\2\2EI\5\6\4\2FI\5\16\b\2GI\5\32\16\2HE\3\2\2\2H"+
		"F\3\2\2\2HG\3\2\2\2I\r\3\2\2\2JK\7 \2\2KL\7*\2\2LM\7\5\2\2M\17\3\2\2\2"+
		"NO\7!\2\2OP\7*\2\2PQ\5\30\r\2Q\21\3\2\2\2RU\7*\2\2SU\5\30\r\2TR\3\2\2"+
		"\2TS\3\2\2\2U\23\3\2\2\2VY\5\36\20\2WY\5 \21\2XV\3\2\2\2XW\3\2\2\2Y\25"+
		"\3\2\2\2Z[\5\22\n\2[_\7\6\2\2\\^\5\22\n\2]\\\3\2\2\2^a\3\2\2\2_]\3\2\2"+
		"\2_`\3\2\2\2`b\3\2\2\2a_\3\2\2\2bc\7\32\2\2c\27\3\2\2\2de\7\b\2\2ef\7"+
		"\7\2\2fg\5\36\20\2gh\7#\2\2h\u0086\3\2\2\2i\u0086\7\25\2\2j\u0086\7\r"+
		"\2\2kl\7\26\2\2ln\7\7\2\2mo\5\22\n\2nm\3\2\2\2op\3\2\2\2pn\3\2\2\2pq\3"+
		"\2\2\2qr\3\2\2\2rs\7#\2\2s\u0086\3\2\2\2tu\7\t\2\2uv\7\7\2\2vw\5\22\n"+
		"\2wx\5\36\20\2xy\7#\2\2y\u0086\3\2\2\2z{\7\4\2\2{|\7\7\2\2|}\5\22\n\2"+
		"}~\7#\2\2~\u0086\3\2\2\2\177\u0080\7&\2\2\u0080\u0081\7\7\2\2\u0081\u0082"+
		"\5\22\n\2\u0082\u0083\7#\2\2\u0083\u0086\3\2\2\2\u0084\u0086\7\f\2\2\u0085"+
		"d\3\2\2\2\u0085i\3\2\2\2\u0085j\3\2\2\2\u0085k\3\2\2\2\u0085t\3\2\2\2"+
		"\u0085z\3\2\2\2\u0085\177\3\2\2\2\u0085\u0084\3\2\2\2\u0086\31\3\2\2\2"+
		"\u0087\u0088\7*\2\2\u0088\u0089\7\35\2\2\u0089\u008a\7\23\2\2\u008a\u00ee"+
		"\5\36\20\2\u008b\u008c\7\30\2\2\u008c\u00ee\7*\2\2\u008d\u008e\7%\2\2"+
		"\u008e\u008f\5\34\17\2\u008f\u0090\7*\2\2\u0090\u0091\7*\2\2\u0091\u00ee"+
		"\3\2\2\2\u0092\u0093\7*\2\2\u0093\u0094\7\35\2\2\u0094\u0095\7\"\2\2\u0095"+
		"\u0096\7\7\2\2\u0096\u0097\5\22\n\2\u0097\u0098\7#\2\2\u0098\u0099\5\34"+
		"\17\2\u0099\u009a\5\34\17\2\u009a\u00ee\3\2\2\2\u009b\u009c\7*\2\2\u009c"+
		"\u009d\7\35\2\2\u009d\u009e\7\36\2\2\u009e\u009f\7\7\2\2\u009f\u00a0\5"+
		"\22\n\2\u00a0\u00a1\7#\2\2\u00a1\u00a2\5\34\17\2\u00a2\u00a3\5\34\17\2"+
		"\u00a3\u00ee\3\2\2\2\u00a4\u00a5\7*\2\2\u00a5\u00a6\7\35\2\2\u00a6\u00a7"+
		"\7\31\2\2\u00a7\u00a8\7\7\2\2\u00a8\u00a9\5\22\n\2\u00a9\u00aa\7#\2\2"+
		"\u00aa\u00ab\5\34\17\2\u00ab\u00ac\5\34\17\2\u00ac\u00ee\3\2\2\2\u00ad"+
		"\u00ae\7\30\2\2\u00ae\u00ee\7*\2\2\u00af\u00b0\7*\2\2\u00b0\u00b1\7\35"+
		"\2\2\u00b1\u00b2\7\'\2\2\u00b2\u00b3\7\7\2\2\u00b3\u00b4\5\22\n\2\u00b4"+
		"\u00b5\7#\2\2\u00b5\u00b6\5\34\17\2\u00b6\u00b7\7*\2\2\u00b7\u00b8\5\34"+
		"\17\2\u00b8\u00b9\7*\2\2\u00b9\u00ee\3\2\2\2\u00ba\u00bb\7\n\2\2\u00bb"+
		"\u00ee\5\34\17\2\u00bc\u00bd\7*\2\2\u00bd\u00be\7\35\2\2\u00be\u00bf\7"+
		"\17\2\2\u00bf\u00c0\7\7\2\2\u00c0\u00c1\5\22\n\2\u00c1\u00c2\7#\2\2\u00c2"+
		"\u00ee\3\2\2\2\u00c3\u00c4\7\27\2\2\u00c4\u00c5\7\7\2\2\u00c5\u00c6\5"+
		"\22\n\2\u00c6\u00c7\7#\2\2\u00c7\u00c8\7*\2\2\u00c8\u00c9\5\34\17\2\u00c9"+
		"\u00ee\3\2\2\2\u00ca\u00cb\7*\2\2\u00cb\u00cc\7\35\2\2\u00cc\u00cd\7\20"+
		"\2\2\u00cd\u00ce\7\7\2\2\u00ce\u00cf\5\22\n\2\u00cf\u00d0\7#\2\2\u00d0"+
		"\u00d1\7*\2\2\u00d1\u00ee\3\2\2\2\u00d2\u00d3\7*\2\2\u00d3\u00d4\7\35"+
		"\2\2\u00d4\u00d5\7\22\2\2\u00d5\u00d6\7\7\2\2\u00d6\u00d7\5\22\n\2\u00d7"+
		"\u00d8\7#\2\2\u00d8\u00d9\5\34\17\2\u00d9\u00da\5\34\17\2\u00da\u00ee"+
		"\3\2\2\2\u00db\u00dc\7*\2\2\u00dc\u00dd\7\35\2\2\u00dd\u00de\7\13\2\2"+
		"\u00de\u00df\7\7\2\2\u00df\u00e0\5\22\n\2\u00e0\u00e1\7#\2\2\u00e1\u00e2"+
		"\5\34\17\2\u00e2\u00e3\5\34\17\2\u00e3\u00ee\3\2\2\2\u00e4\u00e5\7*\2"+
		"\2\u00e5\u00e6\7\35\2\2\u00e6\u00e7\7\21\2\2\u00e7\u00e8\7\7\2\2\u00e8"+
		"\u00e9\5\22\n\2\u00e9\u00ea\7#\2\2\u00ea\u00eb\5\34\17\2\u00eb\u00ec\5"+
		"\34\17\2\u00ec\u00ee\3\2\2\2\u00ed\u0087\3\2\2\2\u00ed\u008b\3\2\2\2\u00ed"+
		"\u008d\3\2\2\2\u00ed\u0092\3\2\2\2\u00ed\u009b\3\2\2\2\u00ed\u00a4\3\2"+
		"\2\2\u00ed\u00ad\3\2\2\2\u00ed\u00af\3\2\2\2\u00ed\u00ba\3\2\2\2\u00ed"+
		"\u00bc\3\2\2\2\u00ed\u00c3\3\2\2\2\u00ed\u00ca\3\2\2\2\u00ed\u00d2\3\2"+
		"\2\2\u00ed\u00db\3\2\2\2\u00ed\u00e4\3\2\2\2\u00ee\33\3\2\2\2\u00ef\u00f2"+
		"\7*\2\2\u00f0\u00f2\5\24\13\2\u00f1\u00ef\3\2\2\2\u00f1\u00f0\3\2\2\2"+
		"\u00f2\35\3\2\2\2\u00f3\u00f5\t\2\2\2\u00f4\u00f3\3\2\2\2\u00f4\u00f5"+
		"\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f7\7)\2\2\u00f7\37\3\2\2\2\u00f8"+
		"\u00fa\t\2\2\2\u00f9\u00f8\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fb\3\2"+
		"\2\2\u00fb\u00fc\7)\2\2\u00fc\u00fe\7\37\2\2\u00fd\u00ff\t\2\2\2\u00fe"+
		"\u00fd\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0108\7)"+
		"\2\2\u0101\u0103\t\2\2\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2\u0103"+
		"\u0104\3\2\2\2\u0104\u0105\7)\2\2\u0105\u0106\7\33\2\2\u0106\u0108\7)"+
		"\2\2\u0107\u00f9\3\2\2\2\u0107\u0102\3\2\2\2\u0108!\3\2\2\2\22%,AHTX_"+
		"p\u0085\u00ed\u00f1\u00f4\u00f9\u00fe\u0102\u0107";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}