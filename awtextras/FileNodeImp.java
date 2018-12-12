package awtextras;

import java.io.*;
//import java.util.*;

public class FileNodeImp implements FileNode {
	FileNodeImp parent;
//	Hashtable children=new Hashtable();
	File file;

	FileFilter fileFilter=new FileFilter() {
		public boolean accept(File file) {
			return file.isFile();
		}
	};
	FileFilter dirFilter=new FileFilter() {
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
/*
	class FileArrayEnumeration implements Enumeration {
		File[] files;
		int pos=0;
		FileArrayEnumeration(File[] files) {
			this.files=files;
		}
		public boolean hasMoreElements() {
			return (files!=null && pos<files.length);
		}
		public Object nextElement() {
			String fileName=files[pos].getName();
			pos++;
			return fileName;
		}
	}
*/
	public FileNodeImp(File file) {
		this.file=file;
	}
	FileNodeImp(FileNodeImp parent,File file) {
		this.parent=parent;
		this.file=file;
	}
	public boolean isRoot() {
		return (parent==null);
	}
	public boolean isFolder() {
		return file.isDirectory();
	}
	public FileNode getParent() {
		return parent;
	}
	public synchronized FileNode createFile(String name) throws SecurityException {
		File newFile=new File(file,name);
		if (newFile.exists() || !newFile.getParentFile().equals(file)/* || children.containsKey(name)*/) throw new SecurityException("File already exists");
		if (!newFile.getParentFile().equals(file)) throw new SecurityException("Invalid name"+name);
		// ok this is allowed
		FileNode childNode=new FileNodeImp(this,newFile);
//		children.put(name,childNode);
		return childNode;
	}
	public synchronized FileNode getChild(String name) throws FileNotFoundException {
//		if (!children.containsKey(name)) {
			File child=new File(file,name);
			if (!child.getParentFile().equals(file)) throw new SecurityException("Invalid name"+name);
			if (!child.exists()) throw new FileNotFoundException(name);
			FileNode childNode=new FileNodeImp(this,child);
//			children.put(name,childNode);
			return childNode;
//		} else {
//			return (FileNode)children.get(name);
//		}
	}
	public String getName() {
		return file.getName();
	}
	public String[] getFolderNames() {
		File[] folders=file.listFiles(dirFilter);
		String[] names=new String[folders.length];
		for (int i=0;i<folders.length;i++) {
			names[i]=folders[i].getName();
		}
		return names;
//		return new FileArrayEnumeration(folders);
	}
	public String[] getFileNames() {
		File[] files=file.listFiles(fileFilter);
		String[] names=new String[files.length];
		for (int i=0;i<files.length;i++) {
			names[i]=files[i].getName();
		}
		return names;
//		return new FileArrayEnumeration(files);
	}
/*
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}
*/
	public Writer getOutputWriter() throws IOException {
		return new FileWriter(file);
	}
	public Reader getInputReader() throws IOException {
		return new FileReader(file);
	}
	public boolean equals(Object o) {
		if (o instanceof FileNodeImp) {
			return ((FileNodeImp)o).file.equals(file);
		}
		return false;
	}
}
