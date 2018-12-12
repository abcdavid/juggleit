import java.awt.*;
import java.awt.event.*;
import java.awt.Image;
import java.util.*;

import juggling.*;
import awtextras.MessageDialog;

public class LadderDiagram extends Canvas {

	static int DEFAULT=0;
	static int PASSING=1;
	static int SELECTING=2;

	int mode=DEFAULT;
	boolean showCausal=false;

	/* Allows selection of range of times and jugglers
	but cannot select EndPoints which are not adjacent
	*/
	class SelectionModel extends Observable {
		int timeA=-1,timeB=-1,jugglerA=-1,jugglerB=-1;
		
		Rectangle selectionBounds;
		
		protected Rectangle getSelectionBounds() {
			return selectionBounds;
		}
		private void updateSelectionBounds() {
			if (isSelected()) {
				int x1,y1,x2,y2;
				if (timeA==-1 || timeB==-1) {
					// select juggler range
					x1=getLeftGridX(0);
					x2=getRightGridX(getTotalTime(getPattern())-1);
					y1=getTopGridY(Math.min(jugglerA,jugglerB)*2);
					y2=getBottomGridY(Math.max(jugglerA,jugglerB)*2+1);
				} else if (jugglerA==-1 || jugglerB==-1) {
					// select time range
					x1=getLeftGridX(Math.min(timeA,timeB));
					x2=getRightGridX(Math.max(timeA,timeB));
					y1=getTopGridY(0);
					y2=getBottomGridY(getPattern().getHandCount()-1);
				} else {
					// juggler time range
					x1=getLeftGridX(Math.min(timeA,timeB));
					x2=getRightGridX(Math.max(timeA,timeB));
					y1=getTopGridY(Math.min(jugglerA,jugglerB)*2);
					y2=getBottomGridY(Math.max(jugglerA,jugglerB)*2+1);
				}
				selectionBounds=new Rectangle(x1,y1,x2-x1,y2-y1);
			} else {
				selectionBounds=null;
			}
		}
		protected void startSelection(int time,int juggler) {
			timeA=time;
			jugglerA=juggler;
			timeB=time;
			jugglerB=juggler;
			updateSelectionBounds();
			setChanged();
			notifyObservers();
		}
		protected void updateSelection(int time,int juggler) {
			if (!(time==-1 && juggler==-1) && !(time==timeB && jugglerB==juggler)) {
				timeB=time;
				jugglerB=juggler;
				updateSelectionBounds();
				setChanged();
				notifyObservers();
			}
		}
		public void clearSelection() {
			if (isSelected()) startSelection(-1,-1);
		}
		public boolean isSelected() {
			return !(timeA==-1 && jugglerA==-1);
		}
		protected Pass[][] getSelection(boolean isCut) {
			if (!isSelected()) return null;
			int tBegin,tEnd,jBegin,jEnd;
			if (timeA==-1 || timeB==-1) {
				// all beats
				tBegin=0;
				tEnd=getTotalTime(getPattern());
				jBegin=Math.min(jugglerA,jugglerB);
				jEnd=Math.max(jugglerA,jugglerB)+1;
			} else if (jugglerA==-1 || jugglerB==-1) {
				// all jugglers
				jBegin=0;
				jEnd=getPattern().getJugglerCount();
				tBegin=Math.min(timeA,timeB);
				tEnd=Math.max(timeA,timeB)+1;
			} else {
				tBegin=Math.min(timeA,timeB);
				tEnd=Math.max(timeA,timeB)+1;
				jBegin=Math.min(jugglerA,jugglerB);
				jEnd=Math.max(jugglerA,jugglerB)+1;
			}
			Pass[][] passes=new Pass[tEnd-tBegin][jEnd-jBegin];
			if (isCut) getController().startMultipleChanges();
			for (int t=tBegin;t<tEnd;t++) {
				for (int j=jBegin;j<jEnd;j++) {
					passes[t-tBegin][j-jBegin]=getPattern().getJuggler(j).getPass(t);
					if (isCut) {
						getController().removePass(t,j);
					}
				}
			}
			if (isCut) getController().finishMultipleChanges();
			return passes;
		}
		public void cut() {
			clipboardPasses=getSelection(true);
		}
		public void copy() {
			clipboardPasses=getSelection(false);
		}
		public boolean paste() {
			Pass[][] passes=clipboardPasses;
			if (passes==null) return true; // successfully pasted nothing
			getController().startMultipleChanges();
			Pattern pattern=getController().getPattern();
			int startTime=Math.min(Math.max(0,timeA),Math.max(0,timeB));
			int startJ=Math.min(Math.max(0,jugglerA),Math.max(0,jugglerB));
			boolean success=true;
			for (int t=0;t<passes.length;t++) {
				for (int j=0;j<passes[t].length;j++) {
					Pass pass=passes[t][j];
					// get EndPoint to paste to
					int pasteTime=startTime+t;
					int pasteJ=(startJ+j)%pattern.getJugglerCount();
					// left hand or right hand passing
					Juggler juggler=pattern.getJuggler(pasteJ);
					if (!patternController.setPass(juggler,pasteTime,pass)) /*juggler.setPass(pasteTime,pass))*/ {
						success=false;
					}
				}
			}
			if (success) {
				getController().finishMultipleChanges();
			} else {
				getController().abortMultipleChanges();
			}
			return success;
		}
		public void remove() {
			getSelection(true);
		}
		public int getFirstSelectedJuggler() {
			if (!isSelected()) return -1;
			if (jugglerA==-1 || jugglerB==-1) return 0;
			return Math.max(0,Math.min(jugglerA,jugglerB));
		}
		public int getLastSelectedJuggler() {
			if (!isSelected()) return -1;
			if (jugglerA==-1 || jugglerB==-1) return getController().getPattern().getJugglerCount()-1;
			return Math.max(jugglerA,jugglerB);
		}
	}
	static Pass[][] clipboardPasses;
	
	PatternController patternController;
	Observer patternObserver=new Observer() {
		public void update(Observable ob,Object o) {
			patternUpdate(((PatternController)ob).getPattern());
		}
	};;
	SelectionModel selectionModel=new SelectionModel();
	
	EndPoint dragEndPoint;

	int minTime=20;  // the minimum time to display (passes or no passes)
	int maxBeats=10; // how much extra time (beyond last pass) to display 
	int totalTime=0;
	int handCount=0;
	
	int beatPixels=20;
	int laneBorder=20;
	int laneSize=80;
	int leftBorder=30;
	int rightBorder=30;
	int topBorder=30;
	int bottomBorder=30;
	int endPointDiameter=10;
	// selection rectangle dimension
//	int sW=2;
//	int sH=2;
	boolean showGrid=true;
	boolean labelPasses=true;

	Color bgColor=Color.white;
	Color fgColor=Color.black;
	Color selectionColor=Color.lightGray;
	Color unknownBallColor=Color.black;

	Image ladderImage=null;
	Graphics ladderG=null;

	int w,h;

	LadderDiagram() {
		setBackground(bgColor);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				handleMousePressed(e,getEndPoint(e.getPoint()));
			}
			public void mouseReleased(MouseEvent e) {
				handleMouseReleased(e,getEndPoint(e.getPoint()));
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				int c=Cursor.DEFAULT_CURSOR;
				if (isOverEndPoint(e.getPoint(),getEndPoint(e.getPoint()))) {
					c=Cursor.HAND_CURSOR;
				}
				if (getCursor().getType()!=c) setCursor(Cursor.getPredefinedCursor(c));
			}
			
			public void mouseDragged(MouseEvent e) {
				if (mode==SELECTING) {
					selectionModel.updateSelection(getTime(e.getX()),getJugglerNumber(e.getY()));
				}
			}
			
		});
		selectionModel.addObserver(new Observer() {
			public void update(Observable ob,Object o) {
				paintLadder(ladderG);
				repaint();
			}
		});
	}
	protected int getTotalTime(Pattern pattern) {
		return Math.max(pattern.getTotalTime()+maxBeats,minTime);
	}
	public void setController(PatternController controller) {
		selectionModel.clearSelection();
		if (patternController!=null) patternController.deleteObserver(patternObserver);
		this.patternController=controller;
		controller.addObserver(patternObserver);
		patternUpdate(controller.getPattern());
	}
	public void setShowCausal(boolean causal) {
		this.showCausal=causal;
		paintLadder(ladderG);
		repaint();
	}
	public boolean isCausal() {
		return this.showCausal;
	}
	public PatternController getController() {
		return patternController;
	}
	public void patternUpdate(Pattern pattern) {
		selectionModel.clearSelection();
		if (pattern.getHandCount()!=handCount || getTotalTime(pattern)!=totalTime) {
			handCount=pattern.getHandCount();
			totalTime=getTotalTime(pattern);
			w=beatPixels*totalTime+leftBorder+rightBorder;
			h=(laneBorder*2+laneSize)*pattern.getJugglerCount()+topBorder+bottomBorder;
			Dimension newSize=new Dimension(w,h);
			if (!getSize().equals(newSize)) {
				setSize(newSize);
				// paintLadder called in update method
			} else {
				paintLadder(ladderG);
			}
		} else {
			paintLadder(ladderG);
		}
		repaint();
	}
	public SelectionModel getSelectionModel() {
		return selectionModel;
	}
	protected Pattern getPattern() {
		return patternController.getPattern();
	}
	protected void drawPass(Graphics g,EndPoint passPoint,EndPoint catchPoint) {
		int x1=getX(passPoint.getTime());
		int y1=getY(passPoint.getHand());
		int x2=getX(catchPoint.getTime());
		int y2=getY(catchPoint.getHand());
		double x=(double)(x2-x1);
		double y=(double)(y2-y1);
		double r=(double)endPointDiameter;
		// adjust to circles
		double adjX=r/Math.sqrt(1.0d+y*y/(x*x));
		double adjY=r/Math.sqrt(1.0d+x*x/(y*y));
		if (x2<x1) adjX=-adjX;
		if (y2<y1) adjY=-adjY;
		// label pass
		if (labelPasses) {
			g.setColor(fgColor);
			String label=passPoint.getPassLabel();
			int labelWidth=g.getFontMetrics().stringWidth(label);
			int labelHeight=g.getFontMetrics().getHeight();
			if (passPoint.getHand().getNumber()%2==0)
				g.drawString(label,x1-labelWidth/2,y1+endPointDiameter+labelHeight);
			else
				g.drawString(label,x1-labelWidth/2,y1-endPointDiameter-labelHeight/2);
		}
		x1=x1+(int)adjX;
		y1=y1+(int)adjY;
		x2=x2-(int)adjX;
		y2=y2-(int)adjY;
		g.setColor(getEndPointColor(passPoint));
		g.drawLine(x1,y1,x2,y2);
		// draw arrowhead
		g.fillPolygon(new int[]{x2,x2-(int)adjX-(int)adjY,x2-(int)adjX+(int)adjY},new int[]{y2,y2-(int)adjY+(int)adjX,y2-(int)adjY-(int)adjX},3);
	}
	Color getEndPointColor(EndPoint endPoint) {
		Ball ball=endPoint.getBall();
		if (ball.noBall()) {
			if (endPoint.getCatchOrigin().isValid() || endPoint.getPassDestination().isValid()) return unknownBallColor;
			return bgColor;
		}
		return patternController.getBallColor(ball);
	}
	void drawEndPoint(Graphics g,EndPoint endPoint) {
		int x=getX(endPoint.getTime())-endPointDiameter/2;
		int y=getY(endPoint.getHand())-endPointDiameter/2;
		int w=endPointDiameter;
		int h=endPointDiameter;

		// draw outline
		g.setColor(fgColor);
		g.fillOval(x,y,w,h);
		if (!endPoint.getBall().noBall() && (!endPoint.getCatchOrigin().isValid() || !endPoint.getPassDestination().isValid())) {
			// to stand out a little more for starting and end positions
			x+=2; y+=2;
			w-=4; h-=4;
		} else {
			x+=1; y+=1;
			w-=2; h-=2;
		}
		// colour according to caught ball
		g.setColor(getEndPointColor(endPoint));
		g.fillOval(x,y,w,h);
		if (showCausal) {
			EndPoint nextEndPoint=endPoint.getNext();
			if (nextEndPoint.isValid()) {
				EndPoint causalPass=nextEndPoint.getCatchOrigin();
				if (causalPass.isValid()) {
					drawPass(g,causalPass,endPoint);
				}
			}
		} else {
			EndPoint passDestination=endPoint.getPassDestination();
			if (passDestination.isValid()) {
					drawPass(g,endPoint,passDestination);
			}
		}
		/*
		if (selectionModel.isSelected(endPoint)) {
			// selected end point
			// draw boxes
			g.setColor(fgColor);
			g.fillRect(x,y,sW,sH);
			g.fillRect(x+w,y,sW,sH);
			g.fillRect(x,y+h,sW,sH);
			g.fillRect(x+w,y+h,sW,sH);
		}
		*/
	}
	int getX(int time) {
		return leftBorder+time*beatPixels+beatPixels/2;
	}
	int getY(Hand hand) {
		int n=hand.getNumber();
		return topBorder+(n/2)*(laneBorder*2+laneSize)+laneBorder+(n%2)*laneSize;
	}
	protected int getLeftGridX(int time) {
		return leftBorder+time*beatPixels;
	}
	protected int getRightGridX(int time) {
		return getLeftGridX(time+1);
	}
	protected int getTopGridY(int hand) {
		return topBorder+hand*(laneBorder+laneSize/2);
	}
	protected int getBottomGridY(int hand) {
		return getTopGridY(hand+1);
	}
	// returns nearest endpoint
	protected EndPoint getEndPoint(Point xy) {
		Hand hand=getHand(xy.y);
		if (hand==null) return EndPoint.INVALID_ENDPOINT;
		int time=getTime(xy.x);
		if (time==-1) return EndPoint.INVALID_ENDPOINT;
		EndPoint endPoint=hand.getEndPoint(time);
		return endPoint;
	}
	protected boolean isOverEndPoint(Point p,EndPoint endPoint) {
		return (endPoint.isValid() && Math.abs(p.y-getY(endPoint.getHand()))<=endPointDiameter && Math.abs(p.x-getX(endPoint.getTime()))<=endPointDiameter);
	}
	protected int getJugglerNumber(int y) {
		if (y<topBorder) return -1;
		if (y>(h-bottomBorder)) return -1;
		for (int i=0;i<getPattern().getJugglerCount();i++) {
			if (y<getBottomGridY(i*2+1)) return i;
		}
		return -1;
	}
	protected Hand getHand(int y) {
		if (y<topBorder) return null;
		if (y>(h-bottomBorder)) return null;
		for (int i=0;i<getPattern().getHandCount();i++) {
			if (y<getBottomGridY(i)) return getPattern().getHand(i);
		}
		return null;
	}
	int getTime(int x) {
		if (x<leftBorder || x>(w-rightBorder)) return -1;
		for (int time=0;time<totalTime;time++) {
			if (x<getRightGridX(time)) return time;
		}
		return -1;
	}
	public void paintLadder(Graphics g) {
		if (g==null) return;
		g.setColor(bgColor);
		g.fillRect(0,0,w,h);
		if (getPattern().getJugglerCount()==0) return;
		Rectangle rect=selectionModel.getSelectionBounds();
		if (rect!=null) {
			g.setColor(selectionColor);
			g.fillRect(rect.x,rect.y,rect.width,rect.height);
		}
		if (showGrid) {
			g.setColor(fgColor);
			Font defaultFont=g.getFont().deriveFont(Font.BOLD,14.0f);
			g.setFont(defaultFont);
			FontMetrics dfm=g.getFontMetrics();
			Font smallFont=g.getFont().deriveFont(10.0f);
			g.setFont(smallFont);
			FontMetrics fm=g.getFontMetrics();
			for (int t=0;t<=totalTime;t++) {
				int x=getLeftGridX(t);
				g.drawLine(x,topBorder,x,h-bottomBorder);
				// label beats
				if (t<totalTime) {
					int y=getTopGridY(0)-2;
					String timeLabel=Integer.toString(t+1);
					x=x+beatPixels/2-fm.stringWidth(timeLabel)/2;
					g.drawString(timeLabel,x,y);
					y=getBottomGridY(getPattern().getJugglerCount()*2-1)+fm.getAscent();
					g.drawString(timeLabel,x,y);
				}
			}
			for (int j=0;j<=getPattern().getJugglerCount();j++) {
				int y=getTopGridY(j*2);  // by hand NOT juggler
				g.drawLine(leftBorder,y,w-rightBorder,y);
				// label juggler
				if (j<getPattern().getJugglerCount()) {
					g.setFont(defaultFont);
					String jLabel=(new Character((char)('A'+j))).toString();
					int x=leftBorder-dfm.stringWidth(jLabel)-5;
					y=getTopGridY(j*2+1)+dfm.getAscent()/2; // between hands
					g.drawString(jLabel,x,y);
					g.setFont(smallFont);
					String leftHandLabel="L";
					y=getY(getPattern().getJuggler(j).getLeftHand())+fm.getAscent()/2;
					x=leftBorder-fm.stringWidth(leftHandLabel)-2;
					g.drawString(leftHandLabel,x,y);
					String rightHandLabel="R";
					y=getY(getPattern().getJuggler(j).getRightHand())+fm.getAscent()/2;
					x=leftBorder-fm.stringWidth(rightHandLabel)-2;
					g.drawString(rightHandLabel,x,y);
				}
			}
		}
		for (int t=0;t<totalTime;t++) {
			for (int h=0;h<getPattern().getHandCount();h++) {
				EndPoint endPoint=getPattern().getHand(h).getEndPoint(t);
				if (endPoint.isValid()) {
					drawEndPoint(g,endPoint);
				}				
			}			
		}
	}
	public void paint(Graphics g) {
		update(g);
	}
	public void update(Graphics g) {
		if (ladderImage==null || ladderImage.getWidth(this)!=w || ladderImage.getHeight(this)!=h) {
			ladderImage=createImage(w,h);
			ladderG=ladderImage.getGraphics();
			paintLadder(ladderG);
		}
		g.drawImage(ladderImage,0,0,this);
	}
	/*
	public Dimension getMaximumSize() {
		return getSize();
	}
	public Dimension getSize() {
		return new Dimension(w,h);
	}*/
	protected void handleMousePressed(MouseEvent e,EndPoint endPoint) {
		if (endPoint.isValid()) {
			int yTop=topBorder+endPoint.getHand().getNumber()*(laneBorder+laneSize/2);
			int yBottom=bottomBorder+(endPoint.getHand().getNumber()+1)*(laneBorder+laneSize/2);
		}
		if (isOverEndPoint(e.getPoint(),endPoint)) {
			dragEndPoint=endPoint;
			mode=PASSING;
		} else {
			selectionModel.startSelection(getTime(e.getX()),getJugglerNumber(e.getY()));
			mode=SELECTING;
		}
	}
	protected void handleMouseReleased(MouseEvent e,EndPoint dropEndPoint) {
		if (mode==PASSING) {
			if (dropEndPoint.equals(dragEndPoint)) {
				// simple click, select endpoint instead
				int t=getTime(e.getX());
				int j=getJugglerNumber(e.getY());
				selectionModel.startSelection(t,j);
				selectionModel.updateSelection(t,j);
			} else 	if (isOverEndPoint(e.getPoint(),dropEndPoint)) {
				if (!addPass(dragEndPoint,dropEndPoint) && !getController().swapOutgoing(dragEndPoint,dropEndPoint) && !getController().swapIncoming(dragEndPoint,dropEndPoint) ) {
					passingFailed(dragEndPoint,dropEndPoint);
				}
			}
		} else if (mode==SELECTING) {
			selectionModel.updateSelection(getTime(e.getX()),getJugglerNumber(e.getY()));
		}
		mode=DEFAULT;
	}
	protected boolean addPass(EndPoint endPointA,EndPoint endPointB) {
		if (endPointA.getTime()>endPointB.getTime()) {
			return (getController().makePass(endPointB,endPointA));
		} else if (endPointB.getTime()>endPointA.getTime()) {
			return (getController().makePass(endPointA,endPointB));
		} else {
			return false;
		}
	}
	protected void passingFailed(EndPoint endPointA,EndPoint endPointB) {
		//System.err.println("Failed to pass:"+endPointA+","+endPointB);
	}
}
