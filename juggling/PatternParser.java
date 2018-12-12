// $ANTLR 2.7.1: "juggling/pattern.g" -> "PatternParser.java"$
package juggling;

import java.io.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class PatternParser extends antlr.LLkParser
       implements PatternParserTokenTypes
 {

	public static Pattern readPattern(Reader input) throws IOException {
		try {
			PatternLexer lexer = new PatternLexer(input);
			TokenBuffer buffer = new TokenBuffer(lexer);
			PatternParser parser = new PatternParser(buffer);
			return parser.pattern();
		} catch(Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	public static Pattern readPattern(InputStream input) throws IOException {
		try {
			PatternLexer lexer = new PatternLexer(new InputStreamReader(input));
			TokenBuffer buffer = new TokenBuffer(lexer);
			PatternParser parser = new PatternParser(buffer);
			return parser.pattern();
		} catch(Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	private int getNextBeat(int time,Hand hand) {
		while (!hand.isBeat(time)) time++;
		return time;
	}
	

protected PatternParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public PatternParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected PatternParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public PatternParser(TokenStream lexer) {
  this(lexer,1);
}

public PatternParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final Pattern  pattern() throws RecognitionException, TokenStreamException {
		Pattern pattern;
		
		Token  c = null;
		Token  n = null;
		Token  intro = null;
		Token  baseBeats = null;
		Token  repeats = null;
		Token  baseBeats2 = null;
		Token  repeats2 = null;
		Token  b = null;
		Token  h = null;
		Token  x = null;
		Token  y = null;
		Token  d = null;
		Token  nd = null;
		
			pattern=new Pattern();
			int hand=0;
			int time;
			Rhythm rhythm;
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ML_COMMENT:
			{
				c = LT(1);
				match(ML_COMMENT);
				pattern.setComment(c.getText());
				break;
			}
			case JUGGLERS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(JUGGLERS);
			match(LC);
			n = LT(1);
			match(NUMBER);
			match(RC);
			pattern.setJugglerCount(Integer.valueOf(n.getText()).intValue());
			match(HANDS);
			{
			int _cnt7=0;
			_loop7:
			do {
				if ((LA(1)==LC)) {
					match(LC);
					intro = LT(1);
					match(NUMBER);
					baseBeats = LT(1);
					match(NUMBER);
					{
					switch ( LA(1)) {
					case HASH:
					{
						match(HASH);
						repeats = LT(1);
						match(NUMBER);
						
										rhythm=new RhythmSequence(Integer.valueOf(intro.getText()).intValue());
										((RhythmSequence)rhythm).addSequence(Integer.valueOf(baseBeats.getText()).intValue(),Integer.valueOf(repeats.getText()).intValue());
									
						{
						_loop6:
						do {
							if ((LA(1)==NUMBER)) {
								baseBeats2 = LT(1);
								match(NUMBER);
								match(HASH);
								repeats2 = LT(1);
								match(NUMBER);
								((RhythmSequence)rhythm).addSequence(Integer.valueOf(baseBeats2.getText()).intValue(),Integer.valueOf(repeats2.getText()).intValue());
							}
							else {
								break _loop6;
							}
							
						} while (true);
						}
						break;
					}
					case RC:
					{
						rhythm=new SimpleRhythm(Integer.valueOf(intro.getText()).intValue(),Integer.valueOf(baseBeats.getText()).intValue());
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					
								try {
									pattern.getHand(hand).setRhythm(rhythm);
								} catch (PatternException e) {
									System.err.println(e.getMessage());
									e.printStackTrace(System.err);
								}
								hand++;
							
					match(RC);
				}
				else {
					if ( _cnt7>=1 ) { break _loop7; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt7++;
			} while (true);
			}
			match(PASSES);
			
				hand=0;
			
			{
			int _cnt13=0;
			_loop13:
			do {
				if ((LA(1)==LC)) {
					match(LC);
					time=getNextBeat(0,pattern.getHand(hand));
					{
					_loop12:
					do {
						if ((LA(1)==NUMBER)) {
							b = LT(1);
							match(NUMBER);
							{
							if (((LA(1)==COMMA))&&(!b.getText().equals("0"))) {
								{
								match(COMMA);
								h = LT(1);
								match(NUMBER);
								
															int beats=Integer.valueOf(b.getText()).intValue();
															int toHand=Integer.valueOf(h.getText()).intValue();
															if (!pattern.getHand(hand).makePass(time,beats,pattern.getHand(toHand))) System.err.println("Failed to make pass: time="+time+",beats="+beats+",fromHand="+hand+",toHand="+toHand);
														
								}
							}
							else if ((LA(1)==NUMBER||LA(1)==RC)) {
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
							}
							time=getNextBeat(time+1,pattern.getHand(hand));
						}
						else {
							break _loop12;
						}
						
					} while (true);
					}
					match(RC);
					hand++;
				}
				else {
					if ( _cnt13>=1 ) { break _loop13; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt13++;
			} while (true);
			}
			{
			switch ( LA(1)) {
			case POSITIONS:
			{
				
					int j=0;
					Token degrees;
					
				match(POSITIONS);
				match(LC);
				{
				int _cnt17=0;
				_loop17:
				do {
					if ((LA(1)==OB)) {
						match(OB);
						x = LT(1);
						match(NUMBER);
						match(COMMA);
						y = LT(1);
						match(NUMBER);
						match(CB);
						{
						switch ( LA(1)) {
						case NUMBER:
						{
							d = LT(1);
							match(NUMBER);
							degrees=d;
							break;
						}
						case NEGATIVE_NUMBER:
						{
							nd = LT(1);
							match(NEGATIVE_NUMBER);
							degrees=nd;
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						
									Location location=pattern.getJuggler(j).getLocation();
									location.setPosition(Integer.valueOf(x.getText()).intValue(),Integer.valueOf(y.getText()).intValue());
									location.setOrientation(Integer.valueOf(degrees.getText()).intValue());
									j++;
								
					}
					else {
						if ( _cnt17>=1 ) { break _loop17; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt17++;
				} while (true);
				}
				match(RC);
				break;
			}
			case EOF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return pattern;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"ML_COMMENT",
		"JUGGLERS",
		"LC",
		"NUMBER",
		"RC",
		"HANDS",
		"HASH",
		"PASSES",
		"COMMA",
		"POSITIONS",
		"OB",
		"CB",
		"NEGATIVE_NUMBER",
		"BALLS",
		"RHYTHM",
		"SL_COMMENT",
		"WS"
	};
	
	private static final long _tokenSet_0_data_[] = { 2L, 0L };
	public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
	
	}
