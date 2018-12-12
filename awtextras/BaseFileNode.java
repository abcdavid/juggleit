package awtextras;

import java.util.*;
import java.net.*;
import java.io.*;

class BaseFileNode implements FileNode {
	BaseFileNodeFactory factory;
	FileNode parent;
	Hashtable children=new Hashtable();
	Vector folderNames=new Vector();
	Vector fileNames=new Vector();
	String name;

	BaseFileNode(BaseFileNodeFactory factory,FileNode parent,String name) {
		this.factory=factory;
		this.parent=parent;
		this.name=name;
	}
	protected void addFolder(BaseFileNode subfolder) {
		folderNames.addElement(subfolder.getName());
		children.put(subfolder.getName(),subfolder);
	}
	protected void addFile(BaseFileNodeChild fileNode) {
		fileNames.addElement(fileNode.getName());
		children.put(fileNode.getName(),fileNode);
	}
/*
	Reader getInputReader(String path) throws IOException {
		return parent.getInputReader(name+"/"+path);
	}
	Writer getOutputWriter(String path) throws IOException {
		return parent.getOutputWriter(name+"/"+path);
	}
*/
	public Reader getInputReader() throws IOException {
		throw new IOException("Cannot read from folder");
	}
	public Writer getOutputWriter() throws IOException {
		throw new IOException("Cannot write to folder");
	}
	public boolean isRoot() {
		return (parent==null);
	}
	public boolean isFolder() {
		return true;
	}
	public FileNode getParent() {
		return parent;
	}
	public FileNode createFile(String name) throws SecurityException {
		if (children.containsKey(name)) throw new SecurityException("File already exists");
		return factory.createFile(this,name);
	}
	public FileNode getChild(String name) throws FileNotFoundException {
		if (!children.containsKey(name)) throw new FileNotFoundException(name);
		return (FileNode)children.get(name);
	}
	public String getName() {
		return name;
	}
	public String[] getFolderNames() {
		String[] names=new String[folderNames.size()];
		folderNames.copyInto(names);
		return names;
	}
	public String[] getFileNames() {
		String[] names=new String[fileNames.size()];
		fileNames.copyInto(names);
		return names;
	}
}
