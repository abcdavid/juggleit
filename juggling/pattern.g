header {package juggling;

import java.io.*;
}
options {
language="Java";
}

class PatternParser extends Parser;
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
	
}

pattern returns [Pattern pattern]
{
	pattern=new Pattern();
	int hand=0;
	int time;
	Rhythm rhythm;
}
:
(c:ML_COMMENT {pattern.setComment(c.getText());} )?
JUGGLERS 
	LC n:NUMBER RC
	{  pattern.setJugglerCount(Integer.valueOf(n.getText()).intValue()); }
HANDS
	(
		LC
		intro:NUMBER baseBeats:NUMBER 
			(HASH repeats:NUMBER 
			{
				rhythm=new RhythmSequence(Integer.valueOf(intro.getText()).intValue());
				((RhythmSequence)rhythm).addSequence(Integer.valueOf(baseBeats.getText()).intValue(),Integer.valueOf(repeats.getText()).intValue());
			}
				(baseBeats2:NUMBER HASH repeats2:NUMBER
				{((RhythmSequence)rhythm).addSequence(Integer.valueOf(baseBeats2.getText()).intValue(),Integer.valueOf(repeats2.getText()).intValue());}
				)*
			|
				{rhythm=new SimpleRhythm(Integer.valueOf(intro.getText()).intValue(),Integer.valueOf(baseBeats.getText()).intValue());}
			)
		{
			try {
				pattern.getHand(hand).setRhythm(rhythm);
			} catch (PatternException e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			}
			hand++;
		}
		RC
	)+ // left and right hands for every juggler
PASSES
{
	hand=0;
}
	(
		LC
		{ time=getNextBeat(0,pattern.getHand(hand)); }
		(
			b:NUMBER (
				{!b.getText().equals("0")}?
					(
						COMMA h:NUMBER 
						{
							int beats=Integer.valueOf(b.getText()).intValue();
							int toHand=Integer.valueOf(h.getText()).intValue();
							if (!pattern.getHand(hand).makePass(time,beats,pattern.getHand(toHand))) System.err.println("Failed to make pass: time="+time+",beats="+beats+",fromHand="+hand+",toHand="+toHand);
						}
					)
				|
				/* no pass - nothing to do*/
			)
			{ time=getNextBeat(time+1,pattern.getHand(hand));}
		)* RC
		{ hand++; }
	)+ // left and right hands for every juggler
(
	{
	int j=0;
	Token degrees;
	}
	POSITIONS LC
	(
		OB x:NUMBER COMMA y:NUMBER CB (d:NUMBER {degrees=d;}|nd:NEGATIVE_NUMBER{degrees=nd;})
		{
			Location location=pattern.getJuggler(j).getLocation();
			location.setPosition(Integer.valueOf(x.getText()).intValue(),Integer.valueOf(y.getText()).intValue());
			location.setOrientation(Integer.valueOf(degrees.getText()).intValue());
			j++;
		}
	)+
	RC
)?
EOF ;

class PatternLexer extends Lexer;
options {
charVocabulary='\u0000'..'\uFFFE';
k=2;
}
JUGGLERS : "jugglers";
BALLS : "balls";
HANDS : "hands";
PASSES : "passes";
POSITIONS : "positions";
NUMBER : ('0' .. '9')+;
NEGATIVE_NUMBER : '-' NUMBER;
COMMA : ',';
HASH : '#' ;
RHYTHM : ( 'x' | 'X' | '_' | '-' )+;
OB : '(' ;
CB : ')' ;
LC : '{' ;
RC : '}' ;
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
	{ $setType(Token.SKIP); }
	;
ML_COMMENT
	:	"/*"!
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
		"*/"!
	;

WS : (' '|'\r'|'\n'|'\t') {$setType(Token.SKIP);};

