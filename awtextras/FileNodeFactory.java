package awtextras;

import java.io.*;

public interface FileNodeFactory {
	public FileNode createRoot(String name) throws IOException,SecurityException;
	public FileNode createFolder(FileNode parent,String name) throws IOException,SecurityException;
	public FileNode createFile(FileNode parent,String name) throws IOException,SecurityException;
}
