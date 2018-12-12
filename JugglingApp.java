import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.MessageFormat;

import awtextras.*;
import juggling.*;
import animation.*;

public class JugglingApp extends Frame {
	private static ResourceBundle loadResourceBundle() {
		if (resourceB==null) {
			try {
				resourceB=ResourceBundle.getBundle(JugglingApp.class.getName());
				Globals.updateDefaults(resourceB);
			} catch (MissingResourceException e) {
				System.err.println(e.getMessage());
			}
		}
		return resourceB;
	}
	protected static ResourceBundle getResourceBundle() {
		return resourceB;
	}
	private static ResourceBundle resourceB=loadResourceBundle();
	
	static int MAX_JUGGLERS=7;
	boolean exitOnClose;
	FileNode dataFolder;
	ScrollPane scrollPane;
	JugglingFrame animationFrame;
	LadderDiagram ladderDiagram;
	PatternController patternController;
	Observer patternObserver=new Observer() {
		public void update(Observable ob,Object o) {
			patternUpdate((PatternController)ob);
		}
	};
	Hashtable controllers=new Hashtable();

	MenuBar menuBar=new MenuBar();
	
	Menu menuFile=new Menu(getResourceBundle().getString("menuFile"));
	MenuItem menuNewPattern=new MenuItem(getResourceBundle().getString("menuFileNewPattern"));
	MenuItem menuSiteswap=new MenuItem(getResourceBundle().getString("menuFileNewSiteswap"));
	MenuItem menuOpen=new MenuItem(getResourceBundle().getString("menuFileOpen"));
	MenuItem menuSave=new MenuItem(getResourceBundle().getString("menuFileSave"));
	MenuItem menuSaveAs=new MenuItem(getResourceBundle().getString("menuFileSaveAs"));
	MenuItem menuClose=new MenuItem(getResourceBundle().getString("menuFileClose"));
	MenuItem menuExit=new MenuItem(getResourceBundle().getString("menuFileExit"));
	
	Menu menuEdit=new Menu(getResourceBundle().getString("menuEdit"));
	MenuItem menuUndo=new MenuItem(getResourceBundle().getString("menuEditUndo"));
	MenuItem menuRedo=new MenuItem(getResourceBundle().getString("menuEditRedo"));
	MenuItem menuCut=new MenuItem(getResourceBundle().getString("menuEditCut"));
	MenuItem menuCopy=new MenuItem(getResourceBundle().getString("menuEditCopy"));
	MenuItem menuPaste=new MenuItem(getResourceBundle().getString("menuEditPaste"));
	MenuItem menuRemove=new MenuItem(getResourceBundle().getString("menuEditRemove"));
	MenuItem menuAddJuggler=new MenuItem(getResourceBundle().getString("menuEditAddJuggler"));
	MenuItem menuRemoveJuggler=new MenuItem(getResourceBundle().getString("menuEditRemoveJuggler"));
	MenuItem menuEditTiming=new MenuItem(getResourceBundle().getString("menuEditTiming"));
	MenuItem menuEditComment=new MenuItem(getResourceBundle().getString("menuEditComment"));

	Menu menuPattern=new Menu(getResourceBundle().getString("menuPattern"));
	String[] toggleLabels=new String[] {getResourceBundle().getString("menuPatternCausal"),getResourceBundle().getString("menuPatternLadder")};
	MenuItem menuPatternCausal=new MenuItem(toggleLabels[0]);
//	MenuItem menuPatternProperties=new MenuItem("Properties...");
//	MenuItem menuPatternColors=new MenuItem("Colours...");
	MenuItem menuPatternAnimate=new MenuItem(getResourceBundle().getString("menuPatternAnimate"));

	Menu menuHelp=new Menu(getResourceBundle().getString("menuHelp"));
	MenuItem menuHelpAbout=new MenuItem(getResourceBundle().getString("menuHelpAbout"));
	
	JugglingApp(FileNode dataFolder,boolean exitOnClose) {
		super(getResourceBundle().getString("appName"));
		this.dataFolder=dataFolder;
		this.exitOnClose=exitOnClose;
		Pattern pattern=new Pattern();
		pattern.addJuggler();
		PatternController controller=new PatternController(pattern);
		ladderDiagram=new LadderDiagram(/*patternController*/);	
		setLayout(new GridLayout(1,1));
		scrollPane=new ScrollPane();
		scrollPane.add(ladderDiagram);
		add(scrollPane);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doQuit();
			}
		});
		menuBar.add(menuFile);
		menuFile.add(menuNewPattern);
		menuNewPattern.setShortcut(new MenuShortcut(KeyEvent.VK_N));
		menuNewPattern.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doNewPattern();
			}
		});
		menuFile.add(menuSiteswap);
		menuSiteswap.setShortcut(new MenuShortcut(KeyEvent.VK_M));
		menuSiteswap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpenSiteswap();
			}
		});
		menuFile.add(menuOpen);
		menuOpen.setShortcut(new MenuShortcut(KeyEvent.VK_O));
		menuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		menuFile.add(menuSave);
		menuSave.setShortcut(new MenuShortcut(KeyEvent.VK_S));
		menuSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		menuFile.add(menuSaveAs);
		menuSaveAs.setShortcut(new MenuShortcut(KeyEvent.VK_A));
		menuSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveAs();
			}
		});
		menuFile.add(menuClose);
		menuClose.setShortcut(new MenuShortcut(KeyEvent.VK_W));
		menuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		menuFile.add(menuExit);
		menuExit.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
		menuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doQuit();
			}
		});
		menuUndo.setShortcut(new MenuShortcut(KeyEvent.VK_U));
		menuUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				patternController.undo();
			}
		});
		menuRedo.setShortcut(new MenuShortcut(KeyEvent.VK_R));
		menuRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				patternController.redo();
			}
		});
		menuCut.setShortcut(new MenuShortcut(KeyEvent.VK_X));
		menuCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ladderDiagram.getSelectionModel().cut();
			}
		});
		menuCopy.setShortcut(new MenuShortcut(KeyEvent.VK_C));
		menuCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ladderDiagram.getSelectionModel().copy();
			}
		});
		menuPaste.setShortcut(new MenuShortcut(KeyEvent.VK_V));
		menuPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!ladderDiagram.getSelectionModel().paste()) {
					MessageDialog.showMessageDialog(JugglingApp.this,getResourceBundle().getString("menuEditPaste"),getResourceBundle().getString("msgPasteFail"),MessageDialog.OK);
				}
			}
		});
		menuRemove.setShortcut(new MenuShortcut(KeyEvent.VK_D));
		menuRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ladderDiagram.getSelectionModel().remove();
			}
		});
		menuAddJuggler.setShortcut(new MenuShortcut(KeyEvent.VK_G));
		menuAddJuggler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeAnimationFrame(); // in case changing between 1 and many jugglers
				patternController.addJuggler();
			}
		});
		menuRemoveJuggler.setShortcut(new MenuShortcut(KeyEvent.VK_F));
		menuRemoveJuggler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int startJ=ladderDiagram.getSelectionModel().getFirstSelectedJuggler();
				int endJ=ladderDiagram.getSelectionModel().getLastSelectedJuggler();
				for (int j=endJ;j>=startJ;j--) {
					String jLabel=patternController.getPattern().getJuggler(j).getLabel();
					int result;
					if ((result=MessageDialog.showMessageDialog(JugglingApp.this,getResourceBundle().getString("menuEditRemoveJuggler"),MessageFormat.format(getResourceBundle().getString("msgRemoveJuggler"),new Object[] {jLabel}),MessageDialog.YES|MessageDialog.NO|MessageDialog.CANCEL))==MessageDialog.YES) {
						removeAnimationFrame(); // in case changing between 1 and many jugglers
						patternController.removeJuggler(j);
					} else if (result==MessageDialog.CANCEL) {
						break;
					}
				}
			}
		});
		menuEditTiming.setShortcut(new MenuShortcut(KeyEvent.VK_T));
		menuEditTiming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimingDialog.showTimingDialog(JugglingApp.this,patternController,getResourceBundle());
			}
		});
		menuEditComment.setShortcut(new MenuShortcut(KeyEvent.VK_I));
		menuEditComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommentDialog commentDialog=new CommentDialog(JugglingApp.this,patternController.getFilename(),patternController,true);
				commentDialog.setVisible(true);
			}
		});
		menuBar.add(menuEdit);
		menuEdit.add(menuUndo);
		menuEdit.add(menuRedo);
		menuEdit.add(menuCut);
		menuEdit.add(menuCopy);
		menuEdit.add(menuPaste);
		menuEdit.add(menuRemove);
		menuEdit.addSeparator();
		menuEdit.add(menuAddJuggler);
		menuEdit.add(menuRemoveJuggler);
		menuEdit.addSeparator();
		menuEdit.add(menuEditTiming);
		menuEdit.add(menuEditComment);
		
		menuBar.add(menuPattern);
		menuPattern.add(menuPatternCausal);
		menuPatternCausal.setShortcut(new MenuShortcut(KeyEvent.VK_L));
		menuPatternCausal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuItem menuItem=(MenuItem)e.getSource();
				if (ladderDiagram.isCausal()) {
					ladderDiagram.setShowCausal(false);
					menuItem.setLabel(toggleLabels[0]);
				} else {
					ladderDiagram.setShowCausal(true);
					menuItem.setLabel(toggleLabels[1]);
				}
			}
		});
/*		menuPattern.add(menuPatternProperties);
		menuPatternProperties.setShortcut(new MenuShortcut(KeyEvent.VK_P));
		menuPatternProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeAnimationFrame(); // in case changing between 1 and many jugglers
				showPatternProperties();
			}
		});
*/
/*		menuPattern.add(menuPatternColors);
		menuPatternColors.setShortcut(new MenuShortcut(KeyEvent.VK_I));
		menuPatternColors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
*/		
		menuPattern.add(menuPatternAnimate);
		menuPatternAnimate.setShortcut(new MenuShortcut(KeyEvent.VK_J));
		menuPatternAnimate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				animate();
			}
		});
		menuPattern.addSeparator();
		addController(controller);
		
		menuBar.add(menuHelp);
		menuHelp.add(menuHelpAbout);
		menuHelpAbout.setShortcut(new MenuShortcut(KeyEvent.VK_H));
		menuHelpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				about();
			}
		});
		
		setMenuBar(menuBar);
		setController(controller);
		selectionUpdate(ladderDiagram.getSelectionModel());
		Dimension size=getPreferredSize();
		setSize(size);
		setResizable(false);
		Dimension maxSize=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(maxSize.width/2-size.width/2,maxSize.height/2-size.height/2);
		ladderDiagram.getSelectionModel().addObserver(new Observer() {
			public void update(Observable ob,Object o) {
				selectionUpdate((LadderDiagram.SelectionModel)ob);
			}
		});
		ladderDiagram.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Dimension newSize=getPreferredSize();
				Dimension frameSize=getSize();
				if (Math.abs(newSize.width-frameSize.width)>50 || Math.abs(newSize.height-frameSize.height)>50) {
//				if (!getSize().equals(newSize)) {
					setSize(newSize);
					doLayout();
					validate();
				}
				// scrollPane must always be notified
				scrollPane.doLayout();
				scrollPane.validate();
				// do we need to reposition
				Dimension maxSize=Toolkit.getDefaultToolkit().getScreenSize();
				Point newLocation=new Point(maxSize.width/2-newSize.width/2,maxSize.height/2-newSize.height/2);
				if (!getLocation().equals(newLocation)) setLocation(newLocation);					
			}
		});
	}
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	public Dimension getPreferredSize() {
		// try to size nicely
		Dimension ladderSize=ladderDiagram.getSize();
		Dimension maxSize=Toolkit.getDefaultToolkit().getScreenSize();
		addNotify(); // ensure peer is created to get correct insets
		Insets insets=getInsets();
		return new Dimension(Math.min(ladderSize.width+insets.left+insets.right+4,maxSize.width),Math.min(ladderSize.height+insets.top+insets.bottom+4,maxSize.height));
	}
	public void patternUpdate(PatternController patternController) {
		menuSave.setEnabled(patternController.isModified());
		menuUndo.setEnabled(patternController.canUndo());
		menuRedo.setEnabled(patternController.canRedo());
		menuAddJuggler.setEnabled(patternController.getPattern().getJugglerCount()<MAX_JUGGLERS);
	}
	public void selectionUpdate(LadderDiagram.SelectionModel selectionModel) {
		boolean selected=selectionModel.isSelected();
		menuCut.setEnabled(selected);
		menuCopy.setEnabled(selected);
		menuPaste.setEnabled(selected);
		menuRemove.setEnabled(selected);
		int startJ=selectionModel.getFirstSelectedJuggler();
		int endJ=selectionModel.getLastSelectedJuggler();
		menuRemoveJuggler.setEnabled(startJ>=0 && (endJ-startJ+1)!=patternController.getPattern().getJugglerCount());
	}
	protected PatternController getController() {
		return patternController;
	}
	protected void setController(PatternController controller) {
		if (patternController==null || !patternController.equals(controller)) {
			removeAnimationFrame();
			if (patternController!=null) patternController.deleteObserver(patternObserver);
			patternController=controller;
			if (patternController!=null) {
				patternController.addObserver(patternObserver);
				setTitle(getResourceBundle().getString("appName")+" - "+patternController.getFilename());
			}
			ladderDiagram.setController(controller);
			patternUpdate(patternController);
		}
	}
	class PatternMenuItem extends MenuItem implements ActionListener {
		PatternController controller;
		PatternMenuItem(PatternController controller) {
			this.controller=controller;
			updateLabel();
			addActionListener(PatternMenuItem.this);
		}
		public void actionPerformed(ActionEvent e) {
			setController(controller);
		}
		public void updateLabel() {
			setLabel(controller.getFilename());
		}
	}
	protected void updatePatternName(PatternController controller) {
		PatternMenuItem menuItem=(PatternMenuItem)controllers.get(controller);
		menuItem.updateLabel();
		setTitle(getResourceBundle().getString("appName")+" - "+controller.getFilename());
	}
	protected MenuShortcut getMenuShortcut(int no) {
		if (no<9)
			return new MenuShortcut(KeyEvent.VK_1+no);
		if (no<18)
			return new MenuShortcut(KeyEvent.VK_1+no-9,true);
		return null;
	}
	protected void addController(PatternController controller) {
		MenuItem patternMenuItem=new PatternMenuItem(controller);
		MenuShortcut menuShortcut=getMenuShortcut(controllers.size());
		if (menuShortcut!=null) patternMenuItem.setShortcut(menuShortcut);
		controllers.put(controller,patternMenuItem);
		menuPattern.add(patternMenuItem);
	}
	private void doNewPattern() {
		Pattern pattern=new Pattern();
		Juggler juggler=pattern.addJuggler();
/*		try {
			juggler.getRightHand().setBallCount(2);
			juggler.getLeftHand().setBallCount(1);
		} catch (PatternException e) {}
*/
		PatternController controller=new PatternController(pattern);
		addController(controller);
		setController(controller);
//		showPatternProperties();
	}
	private void doOpenSiteswap() {
		SiteswapDialog dialog=new SiteswapDialog(JugglingApp.this,getResourceBundle().getString("dialogSiteswapTitle"),getResourceBundle().getString("dialogSiteswapPrompt"),true);
		dialog.setVisible(true);
		Pattern pattern=dialog.getPattern();
		if (pattern!=null) {
			PatternController controller=new PatternController(pattern);
			controller.setFilename(dialog.getSiteswap());
			addController(controller);
			setController(controller);
		}
		dialog.dispose();
	}
	private boolean isOpen(FileNode fileNode) {
		Enumeration enu=controllers.keys();
		while (enu.hasMoreElements()) {
			PatternController controller=(PatternController)enu.nextElement();
			if (controller.getFile()!=null && fileNode.equals(controller.getFile())) return true;
		}
		return false;
	}
	private void doOpen() {
		final FileNode fileNode=FileChooser.showFileOpenDialog(this,getResourceBundle().getString("menuFileOpen"),dataFolder);
		if (fileNode!=null) {
			if (isOpen(fileNode)) {
				MessageDialog.showMessageDialog(this,getResourceBundle().getString("menuFileOpen"),MessageFormat.format(getResourceBundle().getString("msgFileAlreadyOpen"),new Object[] {fileNode.getName()}),MessageDialog.OK);
				return;
			}
			Runnable runnable=new Runnable() {
				public void run() {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {
						PatternController patternController=new PatternController(new Pattern());
						patternController.openPattern(fileNode);
						addController(patternController);
						setController(patternController);
						// show comment on opening pattern
						String comment=patternController.getPattern().getComment();
						if (comment!=null) {
							CommentDialog commentDialog=new CommentDialog(JugglingApp.this,patternController.getFilename(),patternController,false);
							commentDialog.setVisible(true);
						}

					} catch (IOException e) {
						MessageDialog.showMessageDialog(JugglingApp.this,getResourceBundle().getString("menuFileOpen"),MessageFormat.format(getResourceBundle().getString("msgFileAlreadyOpen"),new Object[] {fileNode.getName()}),MessageDialog.OK);
						System.err.println(e.getMessage());
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
			Thread thread=new Thread(runnable);
			thread.start();
		}
	}
	private void doSave() {
		final FileNode fileNode=patternController.getFile();
		if (fileNode!=null) {
			Runnable runnable=new Runnable() {
				public void run() {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {
						patternController.savePattern();
					} catch (IOException e) {
						MessageDialog.showMessageDialog(JugglingApp.this,getResourceBundle().getString("menuFileSave"),MessageFormat.format(getResourceBundle().getString("msgFileSaveError"),new Object[] {fileNode.getName()}),MessageDialog.OK);
						System.err.println(e.getMessage());
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
			Thread thread=new Thread(runnable);
			thread.start();
		} else {
			doSaveAs();
		}
	}
	private void doSaveAs() {
		final FileNode fileNode=FileChooser.showFileSaveDialog(this,getResourceBundle().getString("menuFileSave"),dataFolder);
		if (fileNode!=null) {
			if (isOpen(fileNode)) {
				MessageDialog.showMessageDialog(this,getResourceBundle().getString("menuFileSaveAs"),MessageFormat.format(getResourceBundle().getString("msgFileAlreadyOpen"),new Object[] {fileNode.getName()}),MessageDialog.OK);
				return;
			}
			Runnable runnable=new Runnable() {
				public void run() {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {
						patternController.savePattern(fileNode);
						updatePatternName(patternController); // filename changed in menu
					} catch (IOException e) {
						MessageDialog.showMessageDialog(JugglingApp.this,getResourceBundle().getString("menuFileSaveAs"),MessageFormat.format(getResourceBundle().getString("msgFileSaveError"),new Object[] {fileNode.getName()}),MessageDialog.OK);
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
			Thread thread=new Thread(runnable);
			thread.start();
		}
	}
	private boolean doClose() {
		if (patternController.isModified()) {
			int result;
			int buttons=MessageDialog.YES|MessageDialog.NO;
			if (exitOnClose) buttons=buttons|MessageDialog.CANCEL;
			if ((result=MessageDialog.showMessageDialog(this,getResourceBundle().getString("menuFileClose"),MessageFormat.format(getResourceBundle().getString("msgPromptSave"),new Object[]{patternController.getFilename()}),buttons))==MessageDialog.YES) {
				doSave();
			} else if (result==MessageDialog.CANCEL) {
				return false;
			}
		}
		// remove from shortcut
		MenuItem patternShortcut=(MenuItem)controllers.get(patternController);
		menuPattern.remove(patternShortcut);
		controllers.remove(patternController);
		if (!controllers.isEmpty()) {
			setController((PatternController)controllers.keys().nextElement());
		} else {
			// exit if no more patterns
			doExit();
		}
		return true;
	}
	private void doQuit() {
		if (!exitOnClose || MessageDialog.showMessageDialog(this,getResourceBundle().getString("menuFileExit"),getResourceBundle().getString("msgPromptExit"),MessageDialog.YES | MessageDialog.NO)==MessageDialog.YES) {
			while (getController()!=null) {
				if (!doClose()) return;
			}
		}
	}
	public void doExit() {
		setVisible(false);
		patternController=null;
		dispose();
		if (exitOnClose) System.exit(0);
	}
	private void removeAnimationFrame() {
		if (animationFrame!=null) {
			if (animationFrame.isVisible()) {
				animationFrame.setVisible(false);
			}
			animationFrame.dispose();
			animationFrame=null;
		}
	}
	private void animate() {
		if (animationFrame==null) {
			animationFrame=new JugglingFrame(patternController,getResourceBundle());
			Rectangle b=getBounds();
			Dimension size=animationFrame.getSize();
			animationFrame.setLocation(b.x+b.width/2-size.width/2,Math.max(0,b.y+b.height/2-size.height/2));
			animationFrame.setVisible(true);
		} else {
			if (!animationFrame.isVisible()) {
				animationFrame.setVisible(true);
			} else {
				animationFrame.toFront();
			}
		}
		animationFrame.play();
	}
	/*
	private void showPatternProperties() {
		PatternProperties patternProperties=new PatternProperties(this,patternController);
		patternProperties.setVisible(true);
	}
	*/
	private void about() {
		AboutDialog dialog=new AboutDialog(this,getResourceBundle());
		dialog.setVisible(true);
	}
}
