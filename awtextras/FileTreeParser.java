// $ANTLR 2.7.1: "awtextras/filetree.g" -> "FileTreeParser.java"$

package awtextras;

import java.io.*;
import java.net.*;

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

public class FileTreeParser extends antlr.LLkParser
       implements FileTreeParserTokenTypes
 {

		FileNodeFactory factory;

		FileTreeParser(FileNodeFactory factory,TokenBuffer buffer) {
			this(buffer);
			this.factory=factory;
		}
		public static FileNode buildFileTree(FileNodeFactory factory,InputStream input) throws IOException,ANTLRException {
			FileTreeLexer fileTreeLexer=new FileTreeLexer(new InputStreamReader(input));
			TokenBuffer buffer = new TokenBuffer(fileTreeLexer);
			FileTreeParser fileTreeParser=new FileTreeParser(factory,buffer);
			return fileTreeParser.xmlroot();
		}
	
protected FileTreeParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public FileTreeParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected FileTreeParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public FileTreeParser(TokenStream lexer) {
  this(lexer,2);
}

public FileTreeParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final FileNode  xmlroot() throws RecognitionException, TokenStreamException, IOException {
		FileNode root;
		
		
				root=null;
			
		
		try {      // for error handling
			root=dir();
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return root;
	}
	
	public final FileNode  dir() throws RecognitionException, TokenStreamException, IOException {
		FileNode root;
		
		Token  n = null;
		
				root=null;
			
		
		try {      // for error handling
			match(LT);
			match(DIR);
			match(NAME);
			match(EQ);
			n = LT(1);
			match(QUOTED);
			match(GT);
			
					root=factory.createRoot(n.getText());
				
			contents(root);
			match(LT);
			match(DIV);
			match(DIR);
			match(GT);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return root;
	}
	
	public final void contents(
		FileNode folder
	) throws RecognitionException, TokenStreamException, IOException {
		
		
		try {      // for error handling
			{
			_loop8:
			do {
				if ((LA(1)==LT) && (LA(2)==DIR)) {
					subdir(folder);
				}
				else if ((LA(1)==LT) && (LA(2)==FILE)) {
					file(folder);
				}
				else {
					break _loop8;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
	}
	
	public final void file(
		FileNode folder
	) throws RecognitionException, TokenStreamException, IOException {
		
		Token  n = null;
		
		try {      // for error handling
			match(LT);
			match(FILE);
			match(NAME);
			match(EQ);
			n = LT(1);
			match(QUOTED);
			match(DIV);
			match(GT);
			
					FileNode file=factory.createFile(folder,n.getText());
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
	}
	
	public final void subdir(
		FileNode folder
	) throws RecognitionException, TokenStreamException, IOException {
		
		Token  n = null;
		
		try {      // for error handling
			match(LT);
			match(DIR);
			match(NAME);
			match(EQ);
			n = LT(1);
			match(QUOTED);
			
					FileNode subfolder=factory.createFolder(folder,n.getText());
				
			{
			switch ( LA(1)) {
			case DIV:
			{
				match(DIV);
				match(GT);
				break;
			}
			case GT:
			{
				match(GT);
				contents(subfolder);
				match(LT);
				match(DIV);
				match(DIR);
				match(GT);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"LT",
		"DIR",
		"NAME",
		"EQ",
		"QUOTED",
		"GT",
		"DIV",
		"FILE",
		"HEADER",
		"WHITESPACE"
	};
	
	private static final long _tokenSet_0_data_[] = { 2L, 0L };
	public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
	private static final long _tokenSet_1_data_[] = { 16L, 0L };
	public static final BitSet _tokenSet_1 = new BitSet(_tokenSet_1_data_);
	
	}
