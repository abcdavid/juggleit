import java.awt.*;
import java.awt.event.*;

import juggling.*;
import awtextras.Globals;

public class SiteswapDialog extends Dialog {
    
    public SiteswapDialog(Frame parent,String title,String prompt,boolean modal) {
        super(parent,title,modal);
        initComponents(prompt);
	if (parent!=null) {
		Point location=parent.getLocation();
		Dimension parentSize=parent.getSize();
		Dimension size=getSize();
		location.translate(parentSize.width/2-size.width/2,parentSize.height/2-size.height/2);
		setLocation(location);
	}
    }
    
    private void initComponents(String prompt) {
	setBackground(Color.white);
        GridBagConstraints gridBagConstraints;

        labelSiteswap = new Label();
        txtSiteswap = new TextField();
        labelMessages = new Label();
        panelButtons = new Panel();
        btnOK = new Button();
        btnCancel = new Button();

        setLayout(new GridBagLayout());

        setModal(true);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                SiteswapDialog.this.setVisible(false);
            }
        });

	txtSiteswap.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			readSiteswap();
		}
	});

	btnOK.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			readSiteswap();
		}
	});

	btnCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	});

        labelSiteswap.setText(prompt);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(labelSiteswap, gridBagConstraints);

        txtSiteswap.setColumns(30);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(txtSiteswap, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(labelMessages, gridBagConstraints);

        btnOK.setLabel(Globals.getString(Globals.OK));
        panelButtons.add(btnOK);

        btnCancel.setLabel(Globals.getString(Globals.CANCEL));
        panelButtons.add(btnCancel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        add(panelButtons, gridBagConstraints);

        pack();
    }
    
    private void readSiteswap() {
	try {
		pattern=SiteswapParser.readSiteswap(txtSiteswap.getText());
		if (pattern!=null) setVisible(false);
	} catch (antlr.RecognitionException ex) {
		labelMessages.setText("Cannot read siteswap (check character "+ex.getColumn()+")");
	} catch (Exception ex) {
		labelMessages.setText(ex.getMessage());
	}
    }
    public String getSiteswap() {
	    return txtSiteswap.getText();
    }
    public Pattern getPattern() {
	    return pattern;
    }
    
    
    private Button btnOK;
    private Button btnCancel;
    private Label labelSiteswap;
    private TextField txtSiteswap;
    private Label labelMessages;
    private Panel panelButtons;
    

    private Pattern pattern;    
    
}
