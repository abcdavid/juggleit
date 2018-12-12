import java.util.*;
import java.io.*;
import java.awt.Color;

import juggling.*;
import awtextras.FileNode;

public class PatternController extends Observable {

	static PatternFileFormat fileFormat=new PatternFileFormat();
	static int untitledCount=0;
	
	String filename;
	FileNode fileNode;
	Pattern pattern;
	Stack undoStack=new Stack();
	Stack redoStack=new Stack();
	int changeCount=0;
	boolean modified=false;
	boolean multipleChanges=false;
	static Color[] defaultColors=new Color[] {Color.red,Color.orange,Color.yellow,Color.green,Color.blue,Color.cyan,Color.magenta};
	Hashtable ballColors=new Hashtable();	

	abstract class Change {
		int number;
		
		Change(int number) {
			this.number=number;
		}
		protected abstract void undo();
		protected abstract void redo();
	}
	class AddPassChange extends Change {
		EndPoint passPoint;
		EndPoint catchPoint;
		AddPassChange(int number,EndPoint passPoint,EndPoint catchPoint) {
			super(number);
			this.passPoint=passPoint;
			this.catchPoint=catchPoint;
		}
		protected void undo() {
			passPoint.removePass();
		}
		protected void redo() {
			passPoint.makePass(catchPoint);
		}
	}
	class SetPassChange extends Change {
		Juggler juggler;
		int time;
		Pass pass;
		SetPassChange(int number,Juggler juggler,int time,Pass pass) {
			super(number);
			this.juggler=juggler;
			this.time=time;
			this.pass=pass;
		}
		protected void undo() {
			juggler.removePass(time);
		}
		protected void redo() {
			juggler.setPass(time,pass);
		}
	}
	class RemovePassChange extends Change {
		EndPoint passPoint;
		EndPoint catchPoint;
		Pass pass;
		RemovePassChange(int number,EndPoint passPoint,EndPoint catchPoint) {
			super(number);
			this.passPoint=passPoint;
			this.catchPoint=catchPoint;
		}
		protected void undo() {
			passPoint.makePass(catchPoint);
		}
		protected void redo() {
			passPoint.removePass();
		}
	}
	
	public PatternController(Pattern pattern) {
		this.pattern=pattern;
	}
	public Pattern getPattern() {
		return pattern;
	}
	public Color getBallColor(Ball ball) {
		Integer number=new Integer(ball.getNumber());
		if (!ballColors.containsKey(number)) {
			return defaultColors[number.intValue()%defaultColors.length];
		}
		return (Color)ballColors.get(number);
	}
	public void setBallColor(Ball ball,Color color) {
		Integer number=new Integer(ball.getNumber());
		if (color==null) {
			ballColors.remove(ball);
		} else {
			if (!getBallColor(ball).equals(color)) {
				ballColors.put(number,color);
				setChanged();
				notifyObservers();
			}
		}
	}
	public void setFilename(String filename) {
		if (filename!=null) this.filename=filename;
	}
	public String getFilename() {
		if (filename==null) {
			untitledCount++;
			filename="untitled"+untitledCount;
		}
		return filename;
	}
	public FileNode getFile() {
		return fileNode;
	}
	private void setFile(FileNode fileNode) {
		this.fileNode=fileNode;
		this.filename=fileNode.getName();
		undoStack.removeAllElements();
		redoStack.removeAllElements();
		changeCount=0;
		modified=false;
		setChanged();
		notifyObservers();
	}
	public void openPattern(FileNode fileNode) throws IOException {
		Pattern pattern=fileFormat.readPattern(fileNode.getInputReader());
		this.pattern=pattern;
		setFile(fileNode);
	}
	public void savePattern(FileNode fileNode) throws IOException {
		fileFormat.writePattern(fileNode.getOutputWriter(),pattern);
		setFile(fileNode);
	}
	public void savePattern() throws IOException {
		savePattern(fileNode);
	}
	public boolean isModified() {
		return (changeCount>0 || modified);
	}
	protected void startMultipleChanges() {
		getNextChangeCount();
		multipleChanges=true;
	}
	protected void finishMultipleChanges() {
		multipleChanges=false;
		notifyObservers();
	}
	protected void abortMultipleChanges() {
		Change change=(Change)undoStack.pop();
		int n=change.number;
		change.undo();
		while (!undoStack.isEmpty() && ((Change)undoStack.peek()).number==n) {
			change=(Change)undoStack.pop();
			change.undo();
		}
		changeCount=n-1;
		multipleChanges=false;
	}
	protected synchronized int getNextChangeCount() {
		if (!multipleChanges) changeCount++;
		return changeCount;
	}
	public boolean canUndo() {
		return !undoStack.isEmpty();
	}
	public boolean canRedo() {
		return !redoStack.isEmpty();
	}
	public void undo() {
		Change change=(Change)undoStack.pop();
		int n=change.number;
		change.undo();
		redoStack.push(change);
		while (!undoStack.isEmpty() && ((Change)undoStack.peek()).number==n) {
			change=(Change)undoStack.pop();
			change.undo();
			redoStack.push(change);
		}
		changeCount=n-1;
		setChanged();
		notifyObservers();
	}
	public void redo() {
		Change change=(Change)redoStack.pop();
		int n=change.number;
		change.redo();
		undoStack.push(change);
		while (!redoStack.isEmpty() && ((Change)redoStack.peek()).number==n) {
			change=(Change)redoStack.pop();
			change.redo();
			undoStack.push(change);
		}
		changeCount=n;
		setChanged();
		notifyObservers();
	}
	protected void registerChange(Change change) {
		if (!redoStack.isEmpty()) redoStack.clear();
		undoStack.push(change);
		setChanged();
	}
	public boolean removePass(EndPoint endPoint) {
		EndPoint catchPoint=endPoint.getPassDestination();
		if (catchPoint.isValid()) {
			if (endPoint.removePass()) {
				registerChange(new RemovePassChange(getNextChangeCount(),endPoint,catchPoint));
				if (!multipleChanges) notifyObservers();
				return true;
			}
		}
		return false;
	}
	public boolean removePass(int time,int j) {
		Juggler juggler=pattern.getJuggler(j);
		if (juggler.getRightHand().isBeat(time)) removePass(juggler.getRightHand().getEndPoint(time));
		if (juggler.getLeftHand().isBeat(time)) removePass(juggler.getLeftHand().getEndPoint(time));
		return true;
	}
	public boolean addPass(EndPoint from,EndPoint to) {
		if (from.makePass(to)) {
			registerChange(new AddPassChange(getNextChangeCount(),from,to));
			if (!multipleChanges) notifyObservers();
			return true;
		}
		return false;
	}
	public boolean makePass(EndPoint passPoint,EndPoint catchPoint) {
		EndPoint passerOldDestination=passPoint.getPassDestination();
		if (passerOldDestination.isValid()) {
			EndPoint catcherOldOrigin=catchPoint.getCatchOrigin();
			if (catcherOldOrigin.isValid()) {
				if (catcherOldOrigin.getTime()<passerOldDestination.getTime()) {
					startMultipleChanges();
					// remove old passes
					removePass(passPoint);
					removePass(catcherOldOrigin);
					// make new pass
					addPass(passPoint,catchPoint);
					// link old incoming and outgoing passes
					addPass(catcherOldOrigin,passerOldDestination);
					finishMultipleChanges();
					return true;
				}
				return false;
			} else {
				startMultipleChanges();
				// remove pass and add
				removePass(passPoint);
				addPass(passPoint,catchPoint);
				finishMultipleChanges();
				return true;
			}
		} else {
			// no existing pass
			EndPoint catcherOldOrigin=catchPoint.getCatchOrigin();
			if (catcherOldOrigin.isValid()) {
				// catcher's incoming pass must go to passer
				EndPoint catcherNewDestination=passPoint.getHand().getEndPoint(catchPoint.getTime());
				if (!catcherNewDestination.isValid() || catcherNewDestination.getCatchOrigin().isValid() || catcherNewDestination.equals(catchPoint)) catcherNewDestination=passPoint.getHand().getOther().getEndPoint(catchPoint.getTime());
				if (!catcherNewDestination.isValid() || catcherNewDestination.getCatchOrigin().isValid() || catcherNewDestination.equals(catchPoint)) return false;
				// remove catcher's incoming
				startMultipleChanges();
				removePass(catcherOldOrigin);
				// send to passer instead
				addPass(catcherOldOrigin,catcherNewDestination);
				addPass(passPoint,catchPoint);
				finishMultipleChanges();
				return true;
			} else {
				// empty hand just add
				addPass(passPoint,catchPoint);
				return true;
			}
		}
	}
	public boolean setPass(Juggler juggler,int time,Pass pass) {
		if (juggler.setPass(time,pass)) {
			registerChange(new SetPassChange(getNextChangeCount(),juggler,time,pass));
			if (!multipleChanges) notifyObservers();
			return true;
		}
		return false;
	}
	public boolean swapOutgoing(EndPoint endPointA,EndPoint endPointB) {
		// try to swap passes
		EndPoint catchPointA=endPointA.getPassDestination();
		EndPoint catchPointB=endPointB.getPassDestination();
		if (catchPointA.isValid() && catchPointB.isValid()) {
			if (catchPointA.getTime()>endPointB.getTime() && catchPointB.getTime()>endPointA.getTime()) {
				// we can swap these throws
				startMultipleChanges();
				removePass(endPointA);
				removePass(endPointB);
				addPass(endPointA,catchPointB);
				addPass(endPointB,catchPointA);
				finishMultipleChanges();
				return true;
			}
		}
		return false;
	}
	public boolean swapIncoming(EndPoint endPointA,EndPoint endPointB) {
		// try to swap passes
		EndPoint passPointA=endPointA.getCatchOrigin();
		EndPoint passPointB=endPointB.getCatchOrigin();
		if (passPointA.isValid() && passPointB.isValid()) {
			if (passPointA.getTime()<endPointB.getTime() && passPointB.getTime()<endPointA.getTime()) {
				// we can swap these throws
				startMultipleChanges();
				removePass(passPointA);
				removePass(passPointB);
				addPass(passPointA,endPointB);
				addPass(passPointB,endPointA);
				finishMultipleChanges();
				return true;
			}
		}
		return false;
	}
	public void addJuggler() {
		Juggler juggler=pattern.addJuggler();
/*		try {
			juggler.getRightHand().setBallCount(2);
			juggler.getLeftHand().setBallCount(1);
		} catch (PatternException e) {}
*/
		modified=true;
		setChanged();
		notifyObservers();
	}
	public boolean removeJuggler(int n) {
		Juggler juggler=pattern.getJuggler(n);
		if (juggler!=null) {
			pattern.removeJuggler(juggler);
			modified=true;
			setChanged();
			notifyObservers();
			return true;
		}
		return false;
	}
	public void setComment(String comment) {
		pattern.setComment(comment);
		modified=true;
		setChanged();
		notifyObservers();
	}
	public void setRhythm(Hand hand,Rhythm rhythm) throws PatternException {
		if (!hand.getRhythm().equals(rhythm)) {
			hand.setRhythm(rhythm);
			modified=true;
			setChanged();
			notifyObservers();
		}
	}
	public void setLocation(Juggler juggler,java.awt.Point pos,double orientation) {
		int degrees=(int)Math.toDegrees(orientation);
		Location location=juggler.getLocation();
		location.setPosition(pos.x,pos.y);
		location.setOrientation(degrees);
		// no notify yet required for this change
	}
	public java.awt.Point getPosition(Juggler juggler) {
		Location location=juggler.getLocation();
		if (location.getX()==0 && location.getY()==0) return null;
		return new java.awt.Point(location.getX(),location.getY());
	}
	public double getOrientation(Juggler juggler) {
		Location location=juggler.getLocation();
		return Math.toRadians(location.getOrientation());
	}
	/*
	// todo remove this method when no longer used
	public void patternChanged() {
		setChanged();
		notifyObservers();
	}
	*/
}
