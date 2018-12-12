header {
package awtextras;

import java.io.*;
import java.net.*;
}

options {
language="Java";
}

class FileTreeParser extends Parser;
options {
k=2;
}
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
	}

xmlroot returns [FileNode root] throws IOException
	{
		root=null;
	}
: root=dir EOF;

dir returns [FileNode root] throws IOException
	{
		root=null;
	}
: LT DIR NAME EQ n:QUOTED GT
	{
		root=factory.createRoot(n.getText());
	}
contents[root] LT DIV DIR GT
;

file[FileNode folder] throws IOException
: LT FILE NAME EQ n:QUOTED DIV GT
	{
		FileNode file=factory.createFile(folder,n.getText());
	}
; 
subdir[FileNode folder] throws IOException
: LT DIR NAME EQ n:QUOTED
	{
		FileNode subfolder=factory.createFolder(folder,n.getText());
	}
(DIV GT | GT contents[subfolder] LT DIV DIR GT) ;
contents[FileNode folder] throws IOException : (subdir[folder] | file[folder])*;

class FileTreeLexer extends Lexer;
options {
charVocabulary='\u0000'..'\uFFFE';
k=2;
}

HEADER : "<?" (~'?')* "?>" { $setType(Token.SKIP); };
WHITESPACE : (' ' | '\r' | '\n' | '\t')+ { $setType(Token.SKIP); };
LT : '<' ;
GT : '>' ;
DIV : '/' ;
DIR : "dir" ;
FILE : "file" ;
NAME : "name" ;
EQ : '=' ;
QUOTED : '"'! (~'"')* '"'! ;

