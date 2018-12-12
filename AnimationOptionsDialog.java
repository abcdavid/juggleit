import java.awt.*;
import java.awt.event.*;

import awtextras.MessageDialog;

public class AnimationOptionsDialog extends Dialog {
	JugglingAnimation animation;
	TextField textFramesPerSecond;
	TextField textBeatTime;

	AnimationOptionsDialog(Frame parent,JugglingAnimation animation) {
		super(parent,"Animation Options",true);
		this.animation=animation;
		initComponents();
		if (parent!=null) {
			Rectangle bounds=parent.getBounds();
			Dimension size=getSize();
			setLocation(bounds.x+bounds.width/2-size.width/2,bounds.y+bounds.height/2-size.height/2);
		}
	}
	private void initComponents() {
		setBackground(Color.white);
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();

		Label labelFramesPerSecond=new Label("Frames per Second",Label.CENTER);
		gbConstraints.gridx=1;
		gbConstraints.gridy=1;
		add(labelFramesPerSecond,gbConstraints);

		textFramesPerSecond=new TextField(Integer.toString(animation.getFramesPerSecond()));
		gbConstraints.gridx=2;
		add(textFramesPerSecond,gbConstraints);

		Label labelBeatTime=new Label("Beat Time (milliseconds)");
		gbConstraints.gridx=1;
		gbConstraints.gridy=2;
		add(labelBeatTime,gbConstraints);

		textBeatTime=new TextField(Long.toString(animation.getBeatTime()));
		gbConstraints.gridx=2;
		add(textBeatTime,gbConstraints);

		Panel buttonPanel=new Panel();

		Button btnOK=new Button("OK");
		buttonPanel.add(btnOK);

		Button btnCancel=new Button("Cancel");
		buttonPanel.add(btnCancel);

		gbConstraints.gridx=1;
		gbConstraints.gridy=3;
		gbConstraints.gridwidth=2;
		add(buttonPanel,gbConstraints);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doChange();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		pack();
	}
	private void doClose() {
		setVisible(false);
		dispose();
	}
	private void doChange() {
		int framesPerSecond;
		long beatTime;
		try {
			framesPerSecond=Integer.valueOf(textFramesPerSecond.getText()).intValue();
		} catch (NumberFormatException e) {
			MessageDialog.showMessageDialog(this,"Frames per Second","Enter a number",MessageDialog.OK);
			return;
		}
		if (framesPerSecond>20 || framesPerSecond<1) {
			MessageDialog.showMessageDialog(this,"Frames per Second","Enter a value between 1 to 20",MessageDialog.OK);
			return;
		}
		try {
			beatTime=Long.valueOf(textBeatTime.getText()).longValue();
		} catch (NumberFormatException e) {
			MessageDialog.showMessageDialog(this,"Beat Time","Enter a number",MessageDialog.OK);
			return;
		}
		if (beatTime<1) {
			MessageDialog.showMessageDialog(this,"Beat Time","Enter a positive number",MessageDialog.OK);
			return;
		}
		animation.setFramesPerSecond(framesPerSecond);
		animation.setBeatTime(beatTime);
		doClose();
	}
}
