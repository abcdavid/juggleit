import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import awtextras.MessageDialog;
import awtextras.Globals;
import juggling.*;

class TimingDialog extends Dialog {
	
	public static void showTimingDialog(Frame parent,PatternController patternController,ResourceBundle resourceBundle) {
		TimingDialog timingDialog=new TimingDialog(parent,patternController,resourceBundle);
		timingDialog.setVisible(true);
		timingDialog.dispose();
	}
	
	static final int SIMPLE=0;
	static final int SYNCHRO=1;
	static final int ADVANCED=2;

	ResourceBundle resourceBundle;
	int mode=SIMPLE;
	PatternController patternController;
	Rhythm[] leftHandRhythms;
	Rhythm[] rightHandRhythms;
	int juggler=-1;
	Rhythm leftHandRhythm;
	Rhythm rightHandRhythm;
	
	Choice jugglerChoice;
	Checkbox simpleCheck,synchroCheck,advancedCheck;
	HandPanel leftHandPanel,rightHandPanel;
	Button okButton;

	class SequenceItem {
		int beats;
		int repeats;
		SequenceItem(int beats,int repeats) {
			this.beats=beats;
			this.repeats=repeats;
		}
		public String toString() {
			return beats+"#"+repeats;
		}
	}
	class NumberInputField extends TextField {
		NumberInputField(int value,int cols,final int minimumValue) {
			super(Integer.toString(value),cols);
			addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (!(e.getKeyChar()>=e.VK_0 && e.getKeyChar()<=e.VK_9)  && e.getKeyChar()!=e.VK_BACK_SPACE) {
						e.consume();
					}
				}
			});
			addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					try {
						if (Integer.valueOf(getText()).intValue()<minimumValue) {
							showMessage(minimumValue);
							setText(Integer.toString(minimumValue));
						}
					} catch (NumberFormatException ex) {
						showMessage(minimumValue);
						setText(Integer.toString(minimumValue));
					}
				}
			});
		}
		private void showMessage(final int minimumValue) {
			Thread thread=new Thread(new Runnable() {
				public void run() {
					MessageDialog.showMessageDialog(TimingDialog.this,resourceBundle.getString("dialogTimingInvalidInput"),MessageFormat.format(resourceBundle.getString("dialogTimingMinimum"),new Object[] {new Integer(minimumValue)}),MessageDialog.OK);
				}
			});
			thread.start();
		}
	}
	class HandPanel extends Panel {
		Rhythm rhythm;
		TextField introWaitTxt,beatTimeTxt,repeatTxt;
		Checkbox sequenceCheck;
		SequencePanel sequencePanel;
		
		class SequencePanel extends Panel {
			Button addSequenceButton,removeSequenceButton;
			List sequenceList;
			SequencePanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbConstraints=new GridBagConstraints();
				Label repeatLabel=new Label(resourceBundle.getString("dialogTimingRepeats"),Label.RIGHT);
				gbConstraints.gridx=1;
				gbConstraints.gridy=1;
				gbConstraints.gridwidth=1;
				add(repeatLabel,gbConstraints);
				
				repeatTxt=new NumberInputField(1,2,1);
				gbConstraints.gridx=2;
				gbConstraints.gridy=1;
				gbConstraints.gridwidth=1;
				add(repeatTxt,gbConstraints);
				
				Label sequencesLabel=new Label(resourceBundle.getString("dialogTimingSequence"),Label.RIGHT);
				gbConstraints.gridx=1;
				gbConstraints.gridy=2;
				gbConstraints.gridwidth=1;
				add(sequencesLabel,gbConstraints);
				
				addSequenceButton=new Button(resourceBundle.getString("dialogTimingAddSequence"));
				gbConstraints.gridx=2;
				gbConstraints.gridy=2;
				gbConstraints.gridwidth=1;
				add(addSequenceButton,gbConstraints);
				addSequenceButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addSequence(getBeatTime(),getRepeats());
					}
				});
				
				sequenceList=new List(5);
				gbConstraints.gridx=1;
				gbConstraints.gridy=3;
				gbConstraints.gridwidth=1;
				add(sequenceList,gbConstraints);
				sequenceList.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange()==e.SELECTED) {
							removeSequenceButton.setEnabled(true);
						}
					}
				});
				
				removeSequenceButton=new Button(resourceBundle.getString("dialogTimingRemoveSequence"));
				gbConstraints.gridx=2;
				gbConstraints.gridy=3;
				gbConstraints.gridwidth=1;
				removeSequenceButton.setEnabled(false);
				add(removeSequenceButton,gbConstraints);
				removeSequenceButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeSequence(sequenceList.getSelectedIndex());
						removeSequenceButton.setEnabled(false);
					}
				});
			}
			private void updateRhythm(RhythmSequence rhythm) {
				sequenceList.removeAll();
				introWaitTxt.setText(Integer.toString(rhythm.getIntro()));
				Iterator it=rhythm.getSequences();
				while (it.hasNext()) {
					RhythmSequence.SequenceItem item=(RhythmSequence.SequenceItem)it.next();
					sequenceList.add(Integer.toString(item.getBeats())+"#"+Integer.toString(item.getRepeats()));
				}
			}
			private int getRepeats() {
				return Integer.valueOf(repeatTxt.getText()).intValue();
			}
			private void addSequence(int beats,int repeats) {
				((RhythmSequence)rhythm).addSequence(beats,repeats);
				sequenceList.add(Integer.toString(beats)+"#"+Integer.toString(repeats));
				timingChanged();
			}
			private void removeSequence(int index) {
				if (index<0) return;
				((RhythmSequence)rhythm).removeSequence(index);
				sequenceList.remove(index);
				timingChanged();
			}
		}
		HandPanel(int introWait,String labelTxt) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbConstraints=new GridBagConstraints();
			
			Label handLabel=new Label(labelTxt,Label.CENTER);
			gbConstraints.gridx=1;
			gbConstraints.gridy=1;
			gbConstraints.gridwidth=2;
			add(handLabel,gbConstraints);
			
			Label introWaitLabel=new Label(resourceBundle.getString("dialogTimingIntroWait"),Label.RIGHT);
			gbConstraints.gridx=1;
			gbConstraints.gridy=2;
			gbConstraints.gridwidth=1;
			add(introWaitLabel,gbConstraints);
			
			introWaitTxt=new NumberInputField(introWait,2,0);
			gbConstraints.gridx=2;
			gbConstraints.gridy=2;
			gbConstraints.gridwidth=1;
			add(introWaitTxt,gbConstraints);
			introWaitTxt.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					updateRhythm();
				}
			});

			Label beatTimeLabel=new Label(resourceBundle.getString("dialogTimingBeatTime"),Label.RIGHT);
			gbConstraints.gridx=1;
			gbConstraints.gridy=3;
			gbConstraints.gridwidth=1;
			add(beatTimeLabel,gbConstraints);
			
			beatTimeTxt=new NumberInputField(2,2,1);
			gbConstraints.gridx=2;
			gbConstraints.gridy=3;
			gbConstraints.gridwidth=1;
			add(beatTimeTxt,gbConstraints);
			beatTimeTxt.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					updateRhythm();
				}
			});
			
			sequenceCheck=new Checkbox(resourceBundle.getString("dialogTimingSequence"),false);
			gbConstraints.gridx=1;
			gbConstraints.gridy=4;
			gbConstraints.gridwidth=2;
			add(sequenceCheck,gbConstraints);
			sequenceCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange()==e.SELECTED) {
						setRhythm(new RhythmSequence(getIntro()));
						/*
						rhythm=new RhythmSequence(getIntro());
						sequencePanel.setVisible(true);
						TimingDialog.this.doLayout();
						TimingDialog.this.validate();
						TimingDialog.this.pack();
						timingChanged();
						*/
					} else if (e.getStateChange()==e.DESELECTED) {
						setRhythm(new SimpleRhythm(getIntro(),getBeatTime()));
						/*
						rhythm=new SimpleRhythm(getIntro(),getBeatTime());
						sequencePanel.setVisible(false);
						TimingDialog.this.doLayout();
						TimingDialog.this.validate();
						TimingDialog.this.pack();
						timingChanged();
						*/
					}
				}
			});
			
			sequencePanel=new SequencePanel();
			gbConstraints.gridx=1;
			gbConstraints.gridy=5;
			gbConstraints.gridwidth=2;
			add(sequencePanel,gbConstraints);
			sequencePanel.setVisible(false);
		}
		private boolean isTimingValid() {
			return (!sequenceCheck.getState() || sequencePanel.sequenceList.getItemCount()>1);
		}
		private int getBeatTime() {
			return Integer.valueOf(beatTimeTxt.getText()).intValue();
		}
		private Rhythm getRhythm() {
			return rhythm;
/*		if (!sequenceCheck.getState()) {
				return HandPanel.this.rhythm;
			} else {
				return sequencePanel.getRhythm();
			}
*/
		}
		private int getIntro() {
			return Integer.valueOf(introWaitTxt.getText()).intValue();
		}
		// called when beat text or intro text changes
		private void updateRhythm() {
			if (rhythm instanceof SimpleRhythm) {
				rhythm=new SimpleRhythm(getIntro(),getBeatTime());
			} else if (rhythm instanceof RhythmSequence) {
				((RhythmSequence)rhythm).setIntro(getIntro());
			}
		}
		private void setRhythm(Rhythm rhythm) {
			this.rhythm=rhythm;
			if (rhythm instanceof SimpleRhythm) {
				SimpleRhythm simpleRhythm=(SimpleRhythm)rhythm;
				sequenceCheck.setState(false);
				introWaitTxt.setText(Integer.toString(simpleRhythm.getIntro()));
				beatTimeTxt.setText(Integer.toString(simpleRhythm.getBeats()));
				if (sequencePanel.isVisible()) {
					sequencePanel.setVisible(false);
					doLayout();
					validate();
					pack();
				}
			} else if (rhythm instanceof RhythmSequence) {
				sequenceCheck.setState(true);
				sequencePanel.updateRhythm((RhythmSequence)rhythm);
				if (!sequencePanel.isVisible()) {
					sequencePanel.setVisible(true);
					doLayout();
					validate();
					pack();
				}
			}
			timingChanged();
		}
		/*
		private String getRhythm() {
			StringBuffer buffer=new StringBuffer("");
			int intro=getIntro();
			int count=0;
			for (;count<intro;count++) buffer.append("_");
			if (!sequenceCheck.getState()) {
				buffer.append("x");
				int beats=getBeatTime();
				for (count=0;count<(beats-1);count++) buffer.append("_");
			} else {
				for (int i=0;i<sequencePanel.sequenceVector.size();i++) {
					SequenceItem item=(SequenceItem)sequencePanel.sequenceVector.elementAt(i);
					for (int r=0;r<item.repeats;r++) {
						buffer.append("x");
						for (count=0;count<(item.beats-1);count++) buffer.append("_");
					}
				}
			}
			return buffer.toString();
		}
		*/
	}

	TimingDialog(Frame parent,PatternController patternController,ResourceBundle resourceBundle) {
		super(parent,resourceBundle.getString("dialogTimingTitle"),true);
		this.resourceBundle=resourceBundle;
		initComponents();
		setController(patternController);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		Rectangle b=parent.getBounds();
		Dimension size=getSize();
		setLocation(Math.max(0,b.x+b.width/2-size.width/2),Math.max(0,b.y+b.height/2-size.height/2));
	}
	
	private void setController(PatternController controller) {
		this.patternController=controller;
		Pattern pattern=patternController.getPattern();
		int count=pattern.getJugglerCount();
		leftHandRhythms=new Rhythm[count];
		rightHandRhythms=new Rhythm[count];
		for (int j=0;j<count;j++) {
			String jLabel=(new Character((char)('A'+j))).toString();
			jugglerChoice.add(jLabel);
			leftHandRhythms[j]=copyRhythm(pattern.getJuggler(j).getLeftHand().getRhythm());
			rightHandRhythms[j]=copyRhythm(pattern.getJuggler(j).getRightHand().getRhythm());
		}
		setJuggler(0);
	}
	private Rhythm copyRhythm(Rhythm rhythm) {
		if (rhythm instanceof SimpleRhythm) return new SimpleRhythm((SimpleRhythm)rhythm);
		else if (rhythm instanceof RhythmSequence) return new RhythmSequence((RhythmSequence)rhythm);
		throw new RuntimeException();
	}
	// keep changes for possible update
	private void trackChanges(int number) {
		if (number>=0) {
		leftHandRhythms[number]=getLHRhythm();
		rightHandRhythms[number]=getRHRhythm();
		}
	}
	private void setJuggler(int number) {
		trackChanges(juggler);
		juggler=number;
		Rhythm leftHandRhythm=leftHandRhythms[number];
		Rhythm rightHandRhythm=rightHandRhythms[number];
		Rhythm basicL=new SimpleRhythm(1,2);
		Rhythm basicR=new SimpleRhythm(0,2);
		// init panel whatever else happens
		leftHandPanel.setRhythm(leftHandRhythm);
		rightHandPanel.setRhythm(rightHandRhythm);
		if (leftHandRhythm.equals(basicL) && rightHandRhythm.equals(basicR)) {
			simpleCheck.setState(true);
			setSimple();
		} else if (leftHandRhythm.equals(basicR) && rightHandRhythm.equals(basicR)) {
			synchroCheck.setState(true);
			setSynchro();
		} else {
			advancedCheck.setState(true);
			setAdvanced();
		}
	}
	private void initComponents() {
		setBackground(Color.white);
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints=new GridBagConstraints();
		
		Label jugglerLabel=new Label(resourceBundle.getString("dialogTimingJuggler"),Label.RIGHT);
		gbConstraints.gridx=1;
		gbConstraints.gridy=1;
		gbConstraints.gridwidth=1;
		add(jugglerLabel,gbConstraints);

		jugglerChoice=new Choice();
		gbConstraints.gridx=2;
		gbConstraints.gridy=1;
		gbConstraints.gridwidth=1;
		add(jugglerChoice,gbConstraints);
		jugglerChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==e.SELECTED) {
					setJuggler(jugglerChoice.getSelectedIndex());
				}
			}
		});
		
		CheckboxGroup checkGroup=new CheckboxGroup();
		simpleCheck=new Checkbox(resourceBundle.getString("dialogTimingSimple"),checkGroup,true);
		gbConstraints.gridx=1;
		gbConstraints.gridy=2;
		gbConstraints.gridwidth=2;
		add(simpleCheck,gbConstraints);
		simpleCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==e.SELECTED) setSimple();
			}
		});
		
		synchroCheck=new Checkbox(resourceBundle.getString("dialogTimingSynchro"),checkGroup,false);
		gbConstraints.gridx=1;
		gbConstraints.gridy=3;
		gbConstraints.gridwidth=2;
		add(synchroCheck,gbConstraints);
		synchroCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==e.SELECTED) setSynchro();
			}
		});
		
		advancedCheck=new Checkbox(resourceBundle.getString("dialogTimingAdvanced"),checkGroup,false);
		gbConstraints.gridx=1;
		gbConstraints.gridy=4;
		gbConstraints.gridwidth=2;
		add(advancedCheck,gbConstraints);
		advancedCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==e.SELECTED) {
					setAdvanced();
				}
			}
		});

		leftHandPanel=new HandPanel(1,resourceBundle.getString("dialogTimingLeftHand"));
		gbConstraints.anchor=gbConstraints.NORTH;
		gbConstraints.gridx=1;
		gbConstraints.gridy=5;
		gbConstraints.gridwidth=1;
		add(leftHandPanel,gbConstraints);
		leftHandPanel.setVisible(false);

		rightHandPanel=new HandPanel(0,resourceBundle.getString("dialogTimingRightHand"));
		gbConstraints.anchor=gbConstraints.NORTH;
		gbConstraints.gridx=2;
		gbConstraints.gridy=5;
		gbConstraints.gridwidth=1;
		add(rightHandPanel,gbConstraints);
		rightHandPanel.setVisible(false);
		
		okButton=new Button(Globals.getString(Globals.OK));
		gbConstraints.gridx=1;
		gbConstraints.gridy=6;
		gbConstraints.gridwidth=1;
		add(okButton,gbConstraints);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean success=true;
				trackChanges(jugglerChoice.getSelectedIndex());
				Pattern pattern=patternController.getPattern();
				for (int j=0;j<pattern.getJugglerCount();j++) {
					Juggler juggler=pattern.getJuggler(j);
					try {
						patternController.setRhythm(juggler.getLeftHand(),leftHandRhythms[j]);
						patternController.setRhythm(juggler.getRightHand(),rightHandRhythms[j]);
					} catch (PatternException ex) {
						success=false;
					}
				}
				if (success) doClose();
				else MessageDialog.showMessageDialog(TimingDialog.this,resourceBundle.getString("dialogTimingWarning"),resourceBundle.getString("dialogTimingChangeFail"),MessageDialog.OK);					
 
			}
		});
		
		Button cancelButton=new Button(Globals.getString(Globals.CANCEL));
		gbConstraints.gridx=2;
		gbConstraints.gridy=6;
		gbConstraints.gridwidth=1;
		add(cancelButton,gbConstraints);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		pack();
	}
	private void setSimple() {
		mode=SIMPLE;
		if (leftHandPanel.isVisible()) {
			leftHandPanel.setVisible(false);
			rightHandPanel.setVisible(false);
			doLayout();
			validate();
			pack();
		}
		okButton.setEnabled(true);
	}
	private void setSynchro() {
		mode=SYNCHRO;
		if (leftHandPanel.isVisible()) {
			leftHandPanel.setVisible(false);
			rightHandPanel.setVisible(false);
			doLayout();
			validate();
			pack();
		}
		okButton.setEnabled(true);
	}
	private void setAdvanced() {
		mode=ADVANCED;
		leftHandPanel.setVisible(true);
		rightHandPanel.setVisible(true);
		doLayout();
		validate();
		pack();
		timingChanged();
	}
	private void timingChanged() {
		okButton.setEnabled(leftHandPanel.isTimingValid() && rightHandPanel.isTimingValid());
	}
	private void doClose() {
		setVisible(false);
		dispose();
	}
	public Rhythm getLHRhythm() {
		switch (mode) {
			case SIMPLE :
				return new SimpleRhythm(1,2);
			case SYNCHRO :
				return new SimpleRhythm(0,2);
			case ADVANCED :
				return leftHandPanel.getRhythm();
			default :
				throw new RuntimeException("Unexpected exception");
		}
	}
	public Rhythm getRHRhythm() {
		switch (mode) {
			case SIMPLE :
				return new SimpleRhythm(0,2);
			case SYNCHRO :
				return new SimpleRhythm(0,2);
			case ADVANCED :
				return rightHandPanel.getRhythm();
			default :
				throw new RuntimeException("Unexpected exception");
		}
	}
}
