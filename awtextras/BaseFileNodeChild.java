package awtextras;

import java.io.*;

public class BaseFileNodeChild implements FileNode {
	FileNodeInputOutput inputOutput;
	BaseFileNode parent;
	String name;

	BaseFileNodeChild(FileNodeInputOutput inputOutput,BaseFileNode parent,String name) {
		this.inputOutput=inputOutput;
		this.parent=parent;
		this.name=name;
	}
	public FileNode getParent() {
		return parent;
	}
	public boolean isRoot() {
		return false;
	}
	public boolean isFolder() {
		return false;
	}
	public FileNode addFile(String name) {
		throw new RuntimeException("File cannot have children");
	}
	public FileNode getChild(String name) throws FileNotFoundException {
		throw new FileNotFoundException(name);
	}
	public String[] getFolderNames() {
		throw new RuntimeException("File cannot have children");
	}
	public String[] getFileNames() {
		throw new RuntimeException("File cannot have children");
	}
	public String getName() {
		return name;
	}
	public FileNode createFile(String name) {
		throw new RuntimeException("File cannot have children");
	}
	public Reader getInputReader() throws IOException {
		return inputOutput.getReader(this);
	}
	public Writer getOutputWriter() throws IOException {
		return inputOutput.getWriter(this);
	}
}
