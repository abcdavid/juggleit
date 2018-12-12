import java.awt.*;
import java.awt.event.*;

import awtextras.*;

class CommentDialog extends Dialog {

	PatternController patternController;
	TextArea commentText;

	CommentDialog(Frame parent,String title,PatternController controller,boolean editable) {
		super(parent,title,true);
		this.patternController=controller;
		initComponents(editable);
		String txt=controller.getPattern().getComment();
		if (txt!=null) commentText.setText(txt);
		Rectangle bounds=parent.getBounds();
		Dimension size=getSize();
		setLocation(Math.max(0,bounds.x+bounds.width/2-size.width/2),Math.max(0,bounds.y+bounds.height/2-size.height/2));
	}
	private void initComponents(boolean editable) {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();
		
		commentText=new TextArea(10,50);
		commentText.setEditable(editable);
		gbConstraints.gridx=1;
		gbConstraints.gridy=1;
		add(commentText,gbConstraints);
/*		commentText.addTextListener(new TextListener() {
			public void textValueChanged(TextEvent e) {
				wordWrap();
			}
		});
*/		
		Button okButton=new Button(Globals.getString(Globals.OK));
		gbConstraints.gridx=1;
		gbConstraints.gridy=2;
		gbConstraints.anchor=gbConstraints.CENTER;
		add(okButton,gbConstraints);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		pack();
		setResizable(false);
	}
/*	class Wrapper {
		int cols,caret,extraCaret;
		String text,nl;
		StringBuffer buffer;
		
		Wrapper(String text,int cols,int caret) {
			this.text=text;
			this.cols=cols;
			this.caret=caret;
			extraCaret=0;
			buffer=new StringBuffer();
			nl=System.getProperty("line.separator");
			wrap(0);
		}
		public String getText() {
			return buffer.toString();
		}
		public int getCaretPosition() {
			return caret+extraCaret;
		}
		private void wrap(int startIndex) {
			if (startIndex>=text.length()) return; // end recursion
			int index=text.indexOf(nl,startIndex);
			if (index<0) index=text.length();  // no more new lines
			if ((index-startIndex>cols)) {
				int spacePos=text.substring(startIndex,startIndex+cols).lastIndexOf(" ");
				if (spacePos<0) spacePos=text.substring(startIndex,index).indexOf(" ",cols);
				if (spacePos<0) {
					// no spaces so append the lot
					System.out.println("No spaces");
					buffer.append(text.substring(startIndex,index));
					buffer.append(nl);
					wrap(index+nl.length());
				} else {
					spacePos+=startIndex;
					// replace the space with a nl
					if (spacePos<=caret) extraCaret+=nl.length()-1;
					System.out.println("Replacing space:"+spacePos);
					buffer.append(text.substring(startIndex,spacePos));
					buffer.append(nl);
					wrap(spacePos+" ".length());
				}
			} else {
				// fine just append
				System.out.println("Just append");
				buffer.append(text.substring(startIndex,index));
				buffer.append(nl);
				wrap(index+nl.length());
			}
		}
	}
	private void wordWrap() {
		Wrapper wrapper=new Wrapper(commentText.getText(),commentText.getColumns(),commentText.getCaretPosition());
		commentText.setText(wrapper.getText());
		commentText.setCaretPosition(wrapper.getCaretPosition());
	}
	*/
	private void doClose() {
		String txt=commentText.getText();
		if (txt.indexOf("/*")>=0 || txt.indexOf("*/")>=0) {
			MessageDialog.showMessageDialog(CommentDialog.this,"Bad comment","Comment cannot contain /* or */",MessageDialog.OK);
		} else {
			String oldText=patternController.getPattern().getComment();
			if ((oldText!=null && !oldText.equals(txt)) || (oldText==null && !txt.equals(""))) {
				patternController.setComment(txt);
			}
			setVisible(false);
			dispose();
		}
	}
} 
