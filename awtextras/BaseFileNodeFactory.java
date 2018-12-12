package awtextras;

public abstract class BaseFileNodeFactory implements FileNodeFactory,FileNodeInputOutput {
	public FileNode createRoot(String name) {
		return new BaseFileNode(this,null,name);
	}
	public FileNode createFolder(FileNode parent,String name) {
		BaseFileNode node=new BaseFileNode(this,parent,name);
		((BaseFileNode)parent).addFolder(node);
		return node;
	}
	public FileNode createFile(FileNode parent,String name) {
		BaseFileNodeChild node=new BaseFileNodeChild(this,(BaseFileNode)parent,name);
		((BaseFileNode)parent).addFile(node);
		return node;
	}
}
