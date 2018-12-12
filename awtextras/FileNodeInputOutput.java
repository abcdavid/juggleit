package awtextras;

import java.io.*;

public interface FileNodeInputOutput {
	public Reader getReader(FileNode file) throws IOException,SecurityException;
	public Writer getWriter(FileNode file) throws IOException,SecurityException;
}
