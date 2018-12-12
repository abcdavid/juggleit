package awtextras;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.FileNotFoundException;

public class FileChooser extends Dialog {
	// statics used in FileList
	public static int FILE=0;
	public static int FOLDER=1;
	public static int PARENT=2;
	static String openButtonLabel=Globals.getString(Globals.OPEN);
	static String saveButtonLabel=Globals.getString(Globals.SAVE);
	
	
	FileNode chosenNode;

	boolean isOpen=true;
	ScrollPane scrollPane;
	FileList nodeList;
	TextField chosenFileText;
	Button openSaveButton,cancelButton;

	public static FileNode showFileOpenDialog(Frame parent,String title,FileNode rootFolder) {
		FileChooser fileChooser=new FileChooser(parent,title,true);
		fileChooser.setCurrentFolder(rootFolder);
		fileChooser.setVisible(true);
		FileNode result=fileChooser.getChosenFile();
		fileChooser.dispose();
		return result;
	}
	public static FileNode showFileSaveDialog(Frame parent,String title,FileNode rootFolder) {
		FileChooser fileChooser=new FileChooser(parent,title,false);
		fileChooser.setCurrentFolder(rootFolder);
		fileChooser.setVisible(true);
		FileNode result=fileChooser.getChosenFile();
		fileChooser.dispose();
		return result;
	}

	class FileList extends Canvas implements ItemSelectable {
		Image fileImage,folderImage,parentImage;
		Vector actionListeners=new Vector();
		Vector itemListeners=new Vector();
		Image image;
		Graphics imageG;
		Dimension minimumSize=new Dimension(150,120);
		int entryHeight=15; // start with an estimate, uses fontmetrics when graphics available
		// positioning text entries for each file
		int iconWidth=20;
		int rightOffset=5;

		FileNode currentFileNode; // change of this affects following variables
		boolean showUpDir=false;
		String[] fileNames;
		String[] folderNames;
		int itemCount=0;

		Rectangle selectedEntryBounds;
		int selectedIndex=-1;


		FileList() {
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					requestFocus();
				}
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount()>1 && selectedIndex>=0) notifyActionPerformed(e);
				}	
				public void mouseReleased(MouseEvent e) {
					select(e.getPoint());
				}
			});
			addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					if (selectedIndex<0) selectItem(0);
				}
			});
			addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==e.VK_UP) {
						selectItem(selectedIndex-1);
					} else if (e.getKeyCode()==e.VK_DOWN) {
						selectItem(selectedIndex+1);
					} else if (e.getKeyCode()==e.VK_ENTER || e.getKeyCode()==e.VK_SPACE) {
						notifyActionPerformed(e);
					}
				}
			});
			loadImages();
		}
		public boolean isFocusTraversable() {
			return true;
		}
		public boolean imageUpdate(Image image,int flags,int x,int y,int width,int height) {
			if ((flags&java.awt.image.ImageObserver.ALLBITS)>0) {
				paintEntries(imageG);
				repaint();
				return false;
			}
			return true;
		}
		private void loadImages() {
			try {
				java.net.URL url=FileChooser.class.getResource("images/file.gif");
				fileImage=Toolkit.getDefaultToolkit().getImage(url);
				url=FileChooser.class.getResource("images/folder.gif");
				folderImage=Toolkit.getDefaultToolkit().getImage(url);
				url=FileChooser.class.getResource("images/upfolder.gif");
				parentImage=Toolkit.getDefaultToolkit().getImage(url);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		private Image getImage(int entryType) {
			if (entryType==PARENT) {
				return parentImage;
			} else if (entryType==FOLDER) {
				return folderImage;
			} else { // if (entryType==FILE) {
				return fileImage;
			}
		}
		public Dimension getMinimumSize() {
			return minimumSize;
		}
		public void addActionListener(ActionListener listener) {
			actionListeners.addElement(listener);
		}
		public void removeActionListener(ActionListener listener) {
			actionListeners.removeElement(listener);
		}
		public void addItemListener(ItemListener listener) {
			itemListeners.addElement(listener);
		}
		public void removeItemListener(ItemListener listener) {
			itemListeners.removeElement(listener);
		}
		public Object[] getSelectedObjects() {
			if (selectedIndex<0) return null;
			return new Object[] { getSelectedFile() };
		}
		private void notifyActionPerformed(AWTEvent e) {
			ActionEvent event=new ActionEvent(this,e.getID(),e.paramString());
			Enumeration enu=actionListeners.elements();
			while (enu.hasMoreElements()) {
				ActionListener listener=(ActionListener)enu.nextElement();
				listener.actionPerformed(event);
			}
		}
		private void notifyItemStateChanged(ItemEvent event) {
			Enumeration enu=itemListeners.elements();
			while (enu.hasMoreElements()) {
				ItemListener listener=(ItemListener)enu.nextElement();
				listener.itemStateChanged(event);
			}
		}
		public void clearSelection() {
			if (selectedIndex>=0) {
				selectedIndex=-1;
				ItemEvent deselectEvent=new ItemEvent(this,selectedIndex,getSelectedFile(),ItemEvent.DESELECTED);
				notifyItemStateChanged(deselectEvent);
				paintEntries(imageG);
				repaint();
			}
		}
		public int getItemIndex(String entry) {
			int index=Arrays.binarySearch(folderNames,entry);
			if (index>=0) {
				if (showUpDir) return index+1;
				return index;
			}
			index=Arrays.binarySearch(fileNames,entry);
			if (index>=0) {
				index+=folderNames.length;
				if (showUpDir) return index+1;
				return index;
			}
			return -1;
		}
		public synchronized int getItemCount() {
			return itemCount;
		}
		public synchronized boolean selectItem(int index) {
			if (index>=0 && index<getItemCount() && index!=selectedIndex) {
				// notify deselect
				if (selectedIndex>=0) {
					ItemEvent deselectEvent=new ItemEvent(this,selectedIndex,getSelectedFile(),ItemEvent.DESELECTED);
					notifyItemStateChanged(deselectEvent);
				}
				selectedIndex=index;
				paintEntries(imageG);
				repaint();
				// nofify select
				if (selectedIndex>=0) {
					ItemEvent selectEvent=new ItemEvent(this,selectedIndex,getSelectedFile(),ItemEvent.SELECTED);
					notifyItemStateChanged(selectEvent);
				}
				return true;
			}
			return false;
		}
		private synchronized void select(Point p) {
			selectItem(p.y/entryHeight);
		}

		public synchronized FileNode getSelectedFile() {
			int index=selectedIndex;
			if (selectedIndex<0) return null;
			if (showUpDir) {
				if (selectedIndex==0) return currentFileNode.getParent();
				index--;
			}
			try {
				if (folderNames!=null) {
						if (index<folderNames.length) return currentFileNode.getChild(folderNames[index]);
					index-=folderNames.length;
				}
				if (fileNames!=null && index<fileNames.length) return currentFileNode.getChild(fileNames[index]);
				return null;
			} catch (FileNotFoundException e) {
				// unexpected
				System.err.println(e.getMessage());
				return null;
			}
		}
		public FileNode getCurrentFolder() {
			return currentFileNode;
		}
		public synchronized void setCurrentFolder(FileNode fileNode) {
			if (currentFileNode==null || !fileNode.equals(currentFileNode)) {
				clearSelection();
				this.currentFileNode=fileNode;
				// get file names
				if (fileNode==null) {
					fileNames=null;
					folderNames=null;
					itemCount=0;
				} else {
					showUpDir=(!fileNode.isRoot());
					folderNames=fileNode.getFolderNames();
					fileNames=fileNode.getFileNames();
					itemCount=folderNames.length+fileNames.length;
					if (showUpDir) itemCount++;
				}
				Dimension size=new Dimension(getEntryListWidth(),getEntryListHeight());
				if (!getSize().equals(size)) {
					setSize(size);
					Container parent=getParent();
					parent.validate();
					parent.doLayout();
				} else {
					paintEntries(imageG);
					repaint();
				}
			}
		}
		private int getEntryListHeight() {
			int min=Math.max(getMinimumSize().height,entryHeight*itemCount);
			Graphics g=imageG;
			if (g==null) return min;
			entryHeight=g.getFontMetrics().getHeight();
			return Math.max(entryHeight*itemCount,min);
		}
		private int getEntryListWidth() {
			int max=getMinimumSize().width;
			Graphics g=imageG;
			if (g==null) return max; 
			FontMetrics fm=g.getFontMetrics();
			if (folderNames!=null) {
				for (int i=0;i<folderNames.length;i++) {
					max=Math.max(iconWidth+rightOffset+fm.stringWidth(folderNames[i]),max);
				}
			}
			if (fileNames!=null) {
				for (int i=0;i<fileNames.length;i++) {
					max=Math.max(iconWidth+rightOffset+fm.stringWidth(fileNames[i]),max);
				}
			}
			return max;
		}
		public void update(Graphics g) {
			if (g==null) return;
			Dimension size=getSize();
			if (image==null || image.getWidth(this)!=size.width || image.getHeight(this)!=size.height) {
				image=createImage(size.width,size.height);
				imageG=image.getGraphics();
				paintEntries(imageG);
			}
			g.drawImage(image,0,0,this);
		}
		private void paintEntries(Graphics g) {
			if (g==null) return;
			int index=0;
			g.setColor(Color.white);
			Dimension size=getSize();
			g.fillRect(0,0,size.width,size.height);
			if (showUpDir) {
				paintEntry(g,"..",index,PARENT,index==selectedIndex);
				index++;
			}
			for (int j=0;folderNames!=null && j<folderNames.length;j++) {
				paintEntry(g,folderNames[j],index,FOLDER,index==selectedIndex);
				index++;
			}
			for (int i=0;fileNames!=null && i<fileNames.length;i++) {
				paintEntry(g,fileNames[i],index,FILE,index==selectedIndex);
				index++;
			}
		}
		public void checkSelectionVisible() {
			if (selectedEntryBounds!=null) {
				Rectangle scrolledEntryBounds=new Rectangle(selectedEntryBounds);
				scrolledEntryBounds.translate(0,-scrollPane.getScrollPosition().y);
				Dimension viewportSize=scrollPane.getViewportSize();
				if (viewportSize.height<(scrolledEntryBounds.y+scrolledEntryBounds.height)) {
					scrollPane.setScrollPosition(selectedEntryBounds.getLocation());
				} else if (scrolledEntryBounds.y<0) {
					scrollPane.setScrollPosition(0,selectedEntryBounds.y-viewportSize.height+selectedEntryBounds.height);
				}
			}
		}
		private void paintEntry(Graphics g,String name,int index,int entryType,boolean isSelected) {
			if (isSelected) {
				selectedEntryBounds=new Rectangle(0,index*entryHeight,getSize().width,entryHeight);
				g.setColor(Color.lightGray);
				g.fillRect(selectedEntryBounds.x,selectedEntryBounds.y,selectedEntryBounds.width,selectedEntryBounds.height);
			}
			Image image=getImage(entryType);
			if (image!=null) g.drawImage(image,0,index*entryHeight,this);
			g.setColor(Color.black);
			FontMetrics fm=g.getFontMetrics();
			g.drawString(name,iconWidth,index*entryHeight+fm.getAscent());
		}
		public void paint(Graphics g) {
			update(g);
		}
	}

	FileChooser(Frame parent,String title,boolean isOpen) {
		super(parent,title,true);
		this.isOpen=isOpen;
		initComponents();
		if (parent!=null) {
			Rectangle rect=parent.getBounds();
			Dimension size=getSize();
			setLocation(rect.x+rect.width/2-size.width/2,rect.y+rect.height/2-size.height/2);
		}
	}
	FileChooser(Dialog parent,String title,boolean isOpen) {
		super(parent,title,true);
		this.isOpen=isOpen;
		initComponents();
		if (parent!=null) {
			Rectangle rect=parent.getBounds();
			Dimension size=getSize();
			setLocation(rect.x+rect.width/2-size.width/2,rect.y+rect.height/2-size.height/2);
		}
	}
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		setBackground(Color.white);
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();

		nodeList=new FileList();
		scrollPane=new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		scrollPane.add(nodeList);
		gbConstraints.gridx=1;
		gbConstraints.gridy=1;
		gbConstraints.gridwidth=2;
		gbConstraints.gridheight=2;
		gbConstraints.fill=gbConstraints.BOTH;
		add(scrollPane,gbConstraints);
		nodeList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				doSelectNode();
			}
		});
		nodeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpenSave();
			}
		});

		chosenFileText=new TextField(15);
		gbConstraints.gridx=1;
		gbConstraints.gridy=3;
		gbConstraints.gridwidth=1;
		gbConstraints.gridheight=1;
		gbConstraints.fill=gbConstraints.NONE;
		add(chosenFileText,gbConstraints);
		chosenFileText.addTextListener(new TextListener() {
			public void textValueChanged(TextEvent e) {
				if (e.getID()==e.TEXT_VALUE_CHANGED) doTextChanged();
			}
		});
		chosenFileText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpenSave();
			}
		});

		openSaveButton=new Button(isOpen?openButtonLabel:saveButtonLabel);
		gbConstraints.gridx=2;
		gbConstraints.gridy=3;
		gbConstraints.gridwidth=1;
		add(openSaveButton,gbConstraints);
		openSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpenSave();
			}
		});

		cancelButton=new Button("Cancel");
		gbConstraints.gridx=2;
		gbConstraints.gridy=4;
		gbConstraints.gridwidth=1;
		add(cancelButton,gbConstraints);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});

		pack();
		setResizable(false);
	}
	public synchronized void setCurrentFolder(FileNode currentNode) {
		nodeList.setCurrentFolder(currentNode);
	}
	public FileNode getChosenFile() {
		return chosenNode;
	}
	private void setChosenFile(FileNode fileNode) {
		chosenNode=fileNode;
	}
	private void doClose() {
		setVisible(false);
	}
	private void doOpenSave() {
		FileNode chosen=nodeList.getSelectedFile();
		if (chosen!=null) {
			if (chosen.isFolder()) {
				nodeList.setCurrentFolder(chosen);
				nodeList.selectItem(0);
			} else {
				if (isOpen || MessageDialog.showMessageDialog(this,"Warning","File already exists.  Overwrite?",MessageDialog.YES|MessageDialog.NO)==MessageDialog.YES) { 
				setChosenFile(chosen);
				setVisible(false);
				}
			}
		} else {
			if (!chosenFileText.getText().equals("")) {
				// todo throw an exception for invalid filenames
				FileNode folder=nodeList.getCurrentFolder();
				if (isOpen) {
					try {
						FileNode child=folder.getChild(chosenFileText.getText());
						chosenFileText.setText("");
						if (child.isFolder()) {
							setCurrentFolder(child);
						} else {
							setChosenFile(child);
							setVisible(false);
						}
					} catch (FileNotFoundException e) {
						MessageDialog.showMessageDialog(this,"File not found",e.getMessage(),MessageDialog.OK);
					}
				} else {
					// saving
					// does this file already exist
					if (Arrays.binarySearch(folder.getFileNames(),chosenFileText.getText())>=0) {
						// warning overwrite
						if (MessageDialog.showMessageDialog(this,"Warning","File already exists.  Overwrite?",MessageDialog.YES|MessageDialog.NO)==MessageDialog.YES) {
							try {
								setChosenFile(folder.getChild(chosenFileText.getText()));
								setVisible(false);
							} catch (FileNotFoundException e) {
								// unexpected behaviour
								System.err.println(e.getMessage());
							}
						}
					} else {
						// create new file
						try {
							setChosenFile(folder.createFile(chosenFileText.getText()));
							setVisible(false);
						} catch (SecurityException e) {
							MessageDialog.showMessageDialog(this,"Failed","You are not allowed to create this file",MessageDialog.OK);
						}
						
					}
				}
			}
		}
	}
	private void doTextChanged() {
		// does the text correspond with an existing entry
		String text=chosenFileText.getText();
		if (!text.equals("")) {
			int index=nodeList.getItemIndex(text);
			if (index>=0) {
				nodeList.selectItem(index);
				FileNode fileNode=nodeList.getSelectedFile();
				if (fileNode.isFolder()) {
					nodeList.setCurrentFolder(fileNode);
				}
			} else {
				nodeList.clearSelection();
			}
		}
	}
	private void doSelectNode() {
		FileNode selectedFile=nodeList.getSelectedFile();
		if (selectedFile!=null) {
			nodeList.checkSelectionVisible();
			if (selectedFile.isFolder()) {
				if (!isOpen) openSaveButton.setLabel(openButtonLabel);
				chosenFileText.setText("");
			} else {
				// file selected
				if (!isOpen) openSaveButton.setLabel(saveButtonLabel);
				chosenFileText.setText(selectedFile.getName());
			}
		} else {
			// no selection
			if (!isOpen) openSaveButton.setLabel(saveButtonLabel);
		}
	}
/*	public static void main(String[] args) {
		FileNode chosen=FileChooser.showFileOpenDialog(new Frame(),"Open file",new FileNodeImp(new java.io.File(".")));
		System.out.println("Chosen:"+chosen);
	}
*/
}
