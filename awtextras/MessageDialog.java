package awtextras;

import java.awt.*;
import java.awt.event.*;

public class MessageDialog extends Dialog {

	public static int OK=1;
	public static int YES=2;
	public static int NO=4;
	public static int CANCEL=8;

	int result;

	public static int showMessageDialog(Component component,String title,String message,int buttons) {
		Component root=component;
		while (root!=null && !(root instanceof Frame || root instanceof Dialog)) {
			root=root.getParent();
		}
		if (root==null) throw new RuntimeException();
		if (root instanceof Frame) {
			return showMessageDialogF((Frame)root,title,message,buttons);
		}
		return showMessageDialogD((Dialog)root,title,message,buttons);
	}
	private static int showMessageDialogF(Frame parent,String title,String message,int buttons) {
		MessageDialog dialog=new MessageDialog(parent,title,message,buttons);
		dialog.setVisible(true);
		int result=dialog.result;
		dialog.dispose();
		return result;
	}
	private static int showMessageDialogD(Dialog parent,String title,String message,int buttons) {
		MessageDialog dialog=new MessageDialog(parent,title,message,buttons);
		dialog.setVisible(true);
		int result=dialog.result;
		dialog.dispose();
		return result;
	}
	MessageDialog(Frame parent,String title,String message,int buttons) {
		super(parent,title,true);
		init(message,buttons);
	}
	MessageDialog(Dialog parent,String title,String message,int buttons) {
		super(parent,title,true);
		init(message,buttons);
	}
	private void init(String message,int buttons) {
		setBackground(Color.white);
		setLayout(new GridBagLayout());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				result=CANCEL;
				doCloseWindow();
			}
		});

		GridBagConstraints gridBagConstraints;
		gridBagConstraints=new GridBagConstraints();
		// add buttons
		Button button;
		int buttonCount=0;
		Insets insets=new Insets(5,5,5,5);
		
		if ((buttons & OK)>0) {
			button=new Button(Globals.getString(Globals.OK));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result=OK;
					doOK();
				}
			});
			gridBagConstraints.gridx=buttonCount; buttonCount++;
			gridBagConstraints.gridy=1;
			gridBagConstraints.insets=insets;
			add(button,gridBagConstraints);
		}
		if ((buttons & YES)>0) {
			button=new Button(Globals.getString(Globals.YES));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result=YES;
					doYes();
				}
			});
			gridBagConstraints.gridx=buttonCount; buttonCount++;
			gridBagConstraints.gridy=1;
			gridBagConstraints.insets=insets;
			add(button,gridBagConstraints);
		}
		if ((buttons & NO)>0) {
			button=new Button(Globals.getString(Globals.NO));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result=NO;
					doNo();
				}
			});
			gridBagConstraints.gridx=buttonCount; buttonCount++;
			gridBagConstraints.gridy=1;
			gridBagConstraints.insets=insets;
			add(button,gridBagConstraints);
		}
		if ((buttons & CANCEL)>0) {
			button=new Button(Globals.getString(Globals.CANCEL));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result=CANCEL;
					doCancel();
				}
			});
			gridBagConstraints.gridx=buttonCount; buttonCount++;
			gridBagConstraints.gridy=1;
			gridBagConstraints.insets=insets;
			add(button,gridBagConstraints);
		}
		Label labelMessage=new Label(message);
		gridBagConstraints.gridx=0;
		gridBagConstraints.gridy=0;
		gridBagConstraints.gridwidth=buttonCount;
		gridBagConstraints.insets=insets;
		add(labelMessage,gridBagConstraints);
		pack();
		setResizable(false);
		// centre the dialog on parent
		if (getParent()!=null) {
			Dimension parentSize=getParent().getSize();
			Dimension size=getSize();
			Point location=getParent().getLocation();
			location.translate(parentSize.width/2-size.width/2,parentSize.height/2-size.height/2);
			setLocation(location);
		}
	}
	protected void doOK() {
		setVisible(false);
	}
	protected void doYes() {
		setVisible(false);
	}
	protected void doNo() {
		setVisible(false);
	}
	protected void doCancel() {
		setVisible(false);
	}
	protected void doCloseWindow() {
		doCancel();
	}
}
