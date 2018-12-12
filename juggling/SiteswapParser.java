// $ANTLR 2.7.1: "juggling/siteswap.g" -> "SiteswapParser.java"$
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

public class SiteswapParser extends antlr.LLkParser
       implements SiteswapParserTokenTypes
 {
	
	public static Pattern readSiteswap(String siteswap) throws ANTLRException {
		SiteswapLexer lexer = new SiteswapLexer(new StringReader(siteswap));
		TokenBuffer buffer = new TokenBuffer(lexer);
		SiteswapParser parser = new SiteswapParser(buffer);
		Pattern pattern=new Pattern();
		parser.siteswap(pattern,0);
		return pattern;
	}
	public static void openPattern(InputStream input,Pattern pattern,int startTime) throws ANTLRException {
		SiteswapLexer lexer = new SiteswapLexer(new InputStreamReader(input));
		TokenBuffer buffer = new TokenBuffer(lexer);
		SiteswapParser parser = new SiteswapParser(buffer);
		parser.siteswap(pattern,startTime);
	}
	

protected SiteswapParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public SiteswapParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected SiteswapParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public SiteswapParser(TokenStream lexer) {
  this(lexer,1);
}

public SiteswapParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final void siteswap(
		Pattern pattern,int startTime
	) throws RecognitionException, TokenStreamException {
		
		
		comments();
		{
		switch ( LA(1)) {
		case DIGIT:
		{
			simple(pattern,startTime);
			break;
		}
		case LP:
		{
			synchro(pattern,startTime);
			break;
		}
		case LT:
		{
			passing(pattern,startTime);
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
	
	public final void comments() throws RecognitionException, TokenStreamException {
		
		
		{
		_loop5:
		do {
			switch ( LA(1)) {
			case SL_COMMENT:
			{
				match(SL_COMMENT);
				break;
			}
			case ML_COMMENT:
			{
				match(ML_COMMENT);
				break;
			}
			default:
			{
				break _loop5;
			}
			}
		} while (true);
		}
	}
	
	public final void simple(
		Pattern pattern,int startTime
	) throws RecognitionException, TokenStreamException {
		
		Token  d = null;
		
				Juggler juggler=pattern.addJuggler();
				int time=startTime;
			
		
		{
		int _cnt8=0;
		_loop8:
		do {
			if ((LA(1)==DIGIT)) {
				d = LT(1);
				match(DIGIT);
				
						int beats=Integer.valueOf(d.getText()).intValue();
						Hand fromHand;
						if (time%2==0) fromHand=pattern.getJuggler(0).getRightHand();
						else fromHand=pattern.getJuggler(0).getLeftHand();
						Hand toHand;
						if (beats%2==0) toHand=fromHand;
						else toHand=fromHand.getOther();
						EndPoint passPoint=fromHand.getEndPoint(time);
						EndPoint catchPoint=toHand.getEndPoint(time+beats);
						if (beats>0 && !passPoint.makePass(catchPoint)) System.err.println("Failed to pass:time="+time+",beats="+beats);
						time++;
					
			}
			else {
				if ( _cnt8>=1 ) { break _loop8; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt8++;
		} while (true);
		}
	}
	
	public final void synchro(
		Pattern pattern,int startTime
	) throws RecognitionException, TokenStreamException {
		
		Token  dR = null;
		Token  dL = null;
		
				int time=startTime;
				int leftBeats,rightBeats;
				boolean leftCrossed,rightCrossed;
				Juggler juggler=pattern.addJuggler();
				try {
					juggler.getLeftHand().setRhythm(juggler.getRightHand().getRhythm());
				} catch (PatternException e) {}
			
		
		{
		int _cnt14=0;
		_loop14:
		do {
			if ((LA(1)==LP)) {
				
						leftCrossed=false;
						rightCrossed=false;
					
				match(LP);
				dR = LT(1);
				match(DIGIT);
				rightBeats=Integer.valueOf(dR.getText()).intValue();
				{
				switch ( LA(1)) {
				case X:
				{
					match(X);
					rightCrossed=true;
					break;
				}
				case DIGIT:
				case COMMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					break;
				}
				case DIGIT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				dL = LT(1);
				match(DIGIT);
				leftBeats=Integer.valueOf(dL.getText()).intValue();
				{
				switch ( LA(1)) {
				case X:
				{
					match(X);
					leftCrossed=true;
					break;
				}
				case RP:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RP);
				
						if (rightBeats>0) {
							EndPoint passPoint=juggler.getRightHand().getEndPoint(time);
							EndPoint catchPoint;
							if (rightCrossed) {
								catchPoint=juggler.getLeftHand().getEndPoint(time+rightBeats);
							} else {
								catchPoint=juggler.getRightHand().getEndPoint(time+rightBeats);
							}
							if (!passPoint.makePass(catchPoint)) System.err.println("Failed to pass:time="+time+",rightBeats="+rightBeats);
						}
						if (leftBeats>0) {
							EndPoint passPoint=juggler.getLeftHand().getEndPoint(time);
							EndPoint catchPoint;
							if (leftCrossed) {
								catchPoint=juggler.getRightHand().getEndPoint(time+leftBeats);
							} else {
								catchPoint=juggler.getLeftHand().getEndPoint(time+leftBeats);
							}
							if (!passPoint.makePass(catchPoint)) System.err.println("Failed to pass:time="+time+",leftBeats="+leftBeats);
						}
						time+=2;
					
			}
			else {
				if ( _cnt14>=1 ) { break _loop14; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt14++;
		} while (true);
		}
	}
	
	public final void passing(
		Pattern pattern,int startTime
	) throws RecognitionException, TokenStreamException {
		
		Token  dA = null;
		Token  dB = null;
		
				int time=startTime;
				Juggler aJuggler=pattern.addJuggler();
				Juggler bJuggler=pattern.addJuggler();
				int beats;
				Juggler passingJuggler,catchingJuggler;
				EndPoint passPoint,catchPoint;
				boolean passed=false;
			
		
		match(LT);
			passingJuggler=aJuggler;
			
		{
		int _cnt18=0;
		_loop18:
		do {
			if ((LA(1)==DIGIT)) {
				
						passed=false;
					
				dA = LT(1);
				match(DIGIT);
				
						beats=Integer.valueOf(dA.getText()).intValue();
					
				{
				switch ( LA(1)) {
				case P:
				{
					match(P);
					passed=true;
					break;
				}
				case DIGIT:
				case PIPE:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				
						if (time%2==0) {
							passPoint=passingJuggler.getRightHand().getEndPoint(time);
						} else {
							passPoint=passingJuggler.getLeftHand().getEndPoint(time);
						}
						if (passed) catchingJuggler=bJuggler;
						else catchingJuggler=aJuggler;
						if ((time+beats)%2==0) {
							catchPoint=catchingJuggler.getRightHand().getEndPoint(time+beats);
						} else {
							catchPoint=catchingJuggler.getLeftHand().getEndPoint(time+beats);
						}
						if (!passPoint.makePass(catchPoint)) System.err.println("Juggler A failed to pass at time="+time);
						time++;
					
			}
			else {
				if ( _cnt18>=1 ) { break _loop18; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt18++;
		} while (true);
		}
		match(PIPE);
			passingJuggler=bJuggler;
				time=startTime;
			
		{
		int _cnt21=0;
		_loop21:
		do {
			if ((LA(1)==DIGIT)) {
				
						passed=false;
					
				dB = LT(1);
				match(DIGIT);
				
						beats=Integer.valueOf(dB.getText()).intValue();
					
				{
				switch ( LA(1)) {
				case P:
				{
					match(P);
					passed=true;
					break;
				}
				case DIGIT:
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				
						if (time%2==0) {
							passPoint=passingJuggler.getRightHand().getEndPoint(time);
						} else {
							passPoint=passingJuggler.getLeftHand().getEndPoint(time);
						}
						if (passed) catchingJuggler=aJuggler;
						else catchingJuggler=bJuggler;
						if ((time+beats)%2==0) {
							catchPoint=catchingJuggler.getRightHand().getEndPoint(time+beats);
						} else {
							catchPoint=catchingJuggler.getLeftHand().getEndPoint(time+beats);
						}
						if (!passPoint.makePass(catchPoint)) System.err.println("Juggler B failed to pass at time="+time);
						time++;
					
			}
			else {
				if ( _cnt21>=1 ) { break _loop21; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt21++;
		} while (true);
		}
		match(GT);
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"SL_COMMENT",
		"ML_COMMENT",
		"DIGIT",
		"LP",
		"X",
		"COMMA",
		"RP",
		"LT",
		"P",
		"PIPE",
		"GT",
		"WS"
	};
	
	
	}
