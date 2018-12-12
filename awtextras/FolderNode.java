package awtextras;

import java.util.Enumeration;

public interface FolderNode {
	public boolean isRoot();
	public FolderNode getParent();
	public Enumeration getFolders();
	public Enumeration getFiles();
	public String getName();
}
