import java.awt.*;
import java.awt.event.*;
import animation.AnimationPlayControl;
import java.util.*;

public class JugglingFrame extends Frame {
	Dimension minimumSize;
	PatternController patternController;
	JugglingAnimation animation;
	String[] showBeatPassesLabel;
	Observer patternObserver=new Observer() {
		public void update(Observable ob,Object o) {
			setTitle(patternController.getFilename());
		}
	};

	JugglingFrame(PatternController patternController,ResourceBundle resourceBundle) {
		super(patternController.getFilename());
		setController(patternController);

		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();

		showBeatPassesLabel=new String[] {resourceBundle.getString("menuAnimationEditPasses"),resourceBundle.getString("menuAnimationFinishEdit")};
		MenuBar bar=new MenuBar();
		setMenuBar(bar);
		Menu menuAnimation=new Menu(resourceBundle.getString("menuAnimation"));
		bar.add(menuAnimation);

		if (patternController.getPattern().getJugglerCount()==1) {
			animation=new SoloJugglerAnimation(patternController);
			minimumSize=new Dimension(300,450);
			animation.setSize(minimumSize);
		} else {
			animation=new PassingAnimation(patternController);
			minimumSize=new Dimension(400,400);
			animation.setSize(minimumSize);
			MenuItem menuAnimationShowBeatPasses=new MenuItem(animation.getShowBeatPasses()?showBeatPassesLabel[1]:showBeatPassesLabel[0]);
			menuAnimationShowBeatPasses.setShortcut(new MenuShortcut(KeyEvent.VK_E));
			menuAnimationShowBeatPasses.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean alreadyEditing=animation.getShowBeatPasses();
					animation.setShowBeatPasses(!alreadyEditing);
					((MenuItem)e.getSource()).setLabel(alreadyEditing?showBeatPassesLabel[0]:showBeatPassesLabel[1]);
					if (!animation.isStopped()|| !animation.isPaused()) {
						animation.stop();
					}
					if (!alreadyEditing) animation.setElapsedTime(0);
				}
			});
			menuAnimation.add(menuAnimationShowBeatPasses);
		}
		gbConstraints.gridy=1;
		gbConstraints.fill=gbConstraints.BOTH;
		add(animation,gbConstraints);
		AnimationPlayControl playControl=new AnimationPlayControl(animation);
		gbConstraints.gridy=2;
		gbConstraints.fill=gbConstraints.BOTH;
		add(playControl,gbConstraints);
		
		MenuItem menuAnimationOptions=new MenuItem(resourceBundle.getString("menuAnimationOptions"));
		menuAnimationOptions.setShortcut(new MenuShortcut(KeyEvent.VK_O));
		menuAnimationOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOptions();
			}
		});

		menuAnimation.add(menuAnimationOptions);
		MenuItem menuAnimationClose=new MenuItem(resourceBundle.getString("menuAnimationClose"));
		menuAnimationClose.setShortcut(new MenuShortcut(KeyEvent.VK_C));
		menuAnimationClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		menuAnimation.add(menuAnimationClose);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
//		Panel animationPanel=new AnimationPanel(animation);
//		add(animationPanel);
		
		pack();
		setResizable(false);
	}
	public Dimension getMinimumSize() {
		Insets insets=getInsets();
		return new Dimension(minimumSize.width+insets.left+insets.right,minimumSize.height+insets.top+insets.bottom);
	}
/*	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) animation.play();
	}*/
	public void setController(PatternController controller) {
		if (patternController!=null) {
			patternController.deleteObserver(patternObserver);
		}
		if (controller!=null) {
			controller.addObserver(patternObserver);
		}
		patternController=controller;
	}
	private void doOptions() {
		AnimationOptionsDialog optionsDialog=new AnimationOptionsDialog(this,animation);
		optionsDialog.setVisible(true);
	}
	private void doClose() {
		animation.stop();
		setVisible(false);
	}
	public void play() {
		if (animation.isStopped() || animation.isPaused()) animation.play();
	}
	public void dispose() {
		setController(null);
		animation.setController(null);
		super.dispose();
	}
}
