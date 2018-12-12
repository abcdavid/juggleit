header {package juggling;

import java.io.*;
}
options {
language="Java";
}

class SiteswapParser extends Parser;
options {
	defaultErrorHandler=false;
}
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
	
}
siteswap[Pattern pattern,int startTime] : comments (simple[pattern,startTime]|synchro[pattern,startTime]|passing[pattern,startTime]/*|app*/) EOF;
comments : (SL_COMMENT | ML_COMMENT )*;
simple[Pattern pattern,int startTime]
	{
		Juggler juggler=pattern.addJuggler();
		int time=startTime;
	}
 : (d:DIGIT
	{
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
)+
;

synchro[Pattern pattern,int startTime]
	{
		int time=startTime;
		int leftBeats,rightBeats;
		boolean leftCrossed,rightCrossed;
		Juggler juggler=pattern.addJuggler();
		try {
			juggler.getLeftHand().setRhythm(juggler.getRightHand().getRhythm());
		} catch (PatternException e) {}
	}
: (
	{
		leftCrossed=false;
		rightCrossed=false;
	}
LP dR:DIGIT	{rightBeats=Integer.valueOf(dR.getText()).intValue();}
(X	{rightCrossed=true;}
 )?
(COMMA)?
dL:DIGIT	{leftBeats=Integer.valueOf(dL.getText()).intValue();}
(X	{leftCrossed=true;}
 )? 
 RP
	 {
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
 )+
;
passing[Pattern pattern,int startTime]
	{
		int time=startTime;
		Juggler aJuggler=pattern.addJuggler();
		Juggler bJuggler=pattern.addJuggler();
		int beats;
		Juggler passingJuggler,catchingJuggler;
		EndPoint passPoint,catchPoint;
		boolean passed=false;
	}
:
LT 
 	{	passingJuggler=aJuggler;
	}
(
 	{
		passed=false;
	}
dA:DIGIT
	{
		beats=Integer.valueOf(dA.getText()).intValue();
	}
(P	{passed=true;}
 )?
	{
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
)+
PIPE
 	{	passingJuggler=bJuggler;
		time=startTime;
	}
(
 	{
		passed=false;
	}
dB:DIGIT
	{
		beats=Integer.valueOf(dB.getText()).intValue();
	}
(P	{passed=true;}
 )?
	{
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
)+
GT
;

class SiteswapLexer extends Lexer;
options {
k=2;
}

DIGIT : '0' .. '9';

LT : '<';
GT : '>';

LP : '(';
RP : ')';

PIPE : '|';
COMMA : ',';
X : ( 'x' | 'X') ; // switched i.e. (odd number and (R->R or L->L)) or (even number and (R->L or L->R))
P : ( 'p' | 'P' ) ; // pass
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
	;
ML_COMMENT
	:	"/*"
		(	
			options {
				generateAmbigWarnings=false;
			}
		:
			{ LA(2)!='/' }? '*'
		|	'\r' '\n'		{newline();}
		|	'\r'			{newline();}
		|	'\n'			{newline();}
		|	~('*'|'\n'|'\r')
		)*
		"*/"
	;

WS : (' '|'\r'|'\n'|'\t') {$setType(Token.SKIP);};

