package awtextras;

import java.io.*;

public interface FileNode {
	public boolean isFolder();
	public boolean isRoot();
	public FileNode getParent();
	public FileNode createFile(String name) throws SecurityException;
	public FileNode getChild(String name) throws FileNotFoundException;
	public String[] getFolderNames();
	public String[] getFileNames();
	public String getName();
	public Reader getInputReader() throws IOException;
	public Writer getOutputWriter() throws IOException;
//	public InputStream getInputStream() throws IOException;
//	public OutputStream getOutputStream() throws IOException;
}
