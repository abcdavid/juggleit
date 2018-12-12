import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import awtextras.Globals;

class AboutDialog extends Dialog {
	AboutDialog(Frame parent,ResourceBundle resourceBundle) {
		super(parent,resourceBundle.getString("dialogAboutTitle"),true);
		initComponents(resourceBundle);
		if (parent!=null) {
			Rectangle bounds=parent.getBounds();
			Dimension size=getSize();
			setLocation(bounds.x+bounds.width/2-size.width/2,bounds.y+bounds.height/2-size.height/2);
		}
	}
	private void initComponents(ResourceBundle resourceBundle) {
		setBackground(Color.white);
		int y=1;
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();

		String commaSeparated=resourceBundle.getString("dialogAboutInfo");
		StringTokenizer tokenizer=new StringTokenizer(commaSeparated,"\\");
		while (tokenizer.hasMoreTokens()) {
			Label label=new Label(tokenizer.nextToken(),Label.CENTER);
			gbConstraints.anchor=gbConstraints.CENTER;
			gbConstraints.gridy=y++;
			gbConstraints.fill=gbConstraints.BOTH;
			add(label,gbConstraints);
		}
		
		Button okButton=new Button(Globals.getString(Globals.OK));
		gbConstraints.anchor=gbConstraints.CENTER;
		gbConstraints.fill=gbConstraints.NONE;
		gbConstraints.gridy=y;
		add(okButton,gbConstraints);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		pack();
	}
	private void doClose() {
		setVisible(false);
		dispose();
	}
}
