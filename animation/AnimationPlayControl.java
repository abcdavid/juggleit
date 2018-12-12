package animation;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AnimationPlayControl extends Canvas {
	AnimationCanvas animation;

	long timeElapsed;
	long totalTime;
	Insets insets=new Insets(5,5,5,5);
	Dimension controlSize=new Dimension(8,10);
	Point timeDisplayPos;
	boolean dragging=false;
	int dragX;
	Dimension minimumSize;

	// coordinates for time line
	int startTimeX,endTimeX,timeY;
	CanvasButton playButton,stopButton,pauseButton,rewindButton,forwardButton;

	Image displayImage;
	Graphics imageG;

	abstract class CanvasButton extends Rectangle {
		boolean isEnabled=true;
		CanvasButton(Point location,Dimension size) {
			super(location,size);
		}
		public void setEnabled(boolean enabled) {
			isEnabled=enabled;
		}
		public boolean isMouseOver(Point p) {
			return isEnabled && contains(p);
		}
		public abstract void doAction();
		public void paint(Graphics g) {
			if (isEnabled) g.setColor(Color.black);
			else g.setColor(Color.lightGray);
			paintButton(g);
		}
		public abstract void paintButton(Graphics g);
	}
	class AMouseListener extends MouseAdapter implements MouseMotionListener {
		public void mousePressed(MouseEvent e) {
			if ((dragging=isOverControl(e.getPoint()))) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				dragX=e.getX();
			}
		}
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				dragging=false;
				animation.setElapsedTime(getElapsedForX(e.getX()));
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else {
				CanvasButton button;
				if ((button=findButton(e.getPoint()))!=null) {
					button.doAction();
				}
			}
		}
		public void mouseMoved(MouseEvent e) {
			if (findButton(e.getPoint())!=null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		public void mouseDragged(MouseEvent e) {
			if (dragging) {
				dragX=e.getX();
				repaint();
			}
		}
	}
	
	public AnimationPlayControl(AnimationCanvas animation) {
		this.animation=animation;
		totalTime=animation.getTotalTime();
		timeElapsed=animation.getTotalTime();
		initComponents();
		animation.addAnimationListener(new AnimationAdapter() {
			public void animationFrameChanged(AnimationEvent e) {
				timeElapsed=AnimationPlayControl.this.animation.getElapsedTime();
				rewindButton.setEnabled(timeElapsed>0);
				forwardButton.setEnabled(timeElapsed<totalTime);
				repaint();
			}
			public void animationPropertyChanged(AnimationEvent e) {
				setTotalTime(AnimationPlayControl.this.animation.getTotalTime());
			}
			public void animationStarted(AnimationEvent e) {
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				pauseButton.setEnabled(true);
				repaint();
			}
			public void animationStopped(AnimationEvent e) {
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
				pauseButton.setEnabled(false);
				repaint();
			}
			public void animationPaused(AnimationEvent e) {
				playButton.setEnabled(true);
				stopButton.setEnabled(true);
				pauseButton.setEnabled(true);
				repaint();
			}
		});
		AMouseListener aMouseListener=new AMouseListener();
		addMouseListener(aMouseListener);
		addMouseMotionListener(aMouseListener);
		setSize(minimumSize);
	}
	public Dimension getMinimumSize() {
		return minimumSize;
	}
	public long getElapsedTime() {
		return timeElapsed;
	}
	private void setElapsedTime(long elapsed) {
		if (elapsed!=timeElapsed) {
			this.timeElapsed=elapsed;
			repaint();
		}
	}
	public void setTotalTime(long totalTime) {
		if (totalTime!=this.totalTime) {
			this.totalTime=totalTime;
			repaint();
		}
	}
	public void update(Graphics g) {
		if (g==null) return;
		if (displayImage==null || displayImage.getWidth(this)!=getSize().width || displayImage.getHeight(this)!=getSize().height) {
			displayImage=createImage(getWidth(),getHeight());
			imageG=displayImage.getGraphics();
			// initialise dimensions
			Dimension size=getSize();
			startTimeX=insets.left+controlSize.width/2;
			endTimeX=size.width-insets.right-controlSize.width/2;
			timeY=size.height*2/3;
		}
		renderDisplay(imageG);
		g.drawImage(displayImage,0,0,this);
	}
	public void paint(Graphics g) {
		update(g);
	}
	protected void renderButtons(Graphics g) {
		if (g==null) return;
		playButton.paint(g);
		stopButton.paint(g);
		pauseButton.paint(g);
		rewindButton.paint(g);
		forwardButton.paint(g);
	}
	protected void renderDisplay(Graphics g) {
		if (g==null) return;
		Dimension size=getSize();
		g.setColor(Color.white);
		g.fillRect(0,0,size.width,size.height);
		g.setColor(Color.black);
		// draw time line
		g.drawLine(startTimeX,timeY,endTimeX,timeY);
		// control buttons
		renderButtons(g);
		g.setColor(Color.black);
		// calculate current position
		// small adjustments according to control size
		int adjX=controlSize.width/2;
		int adjY=controlSize.height/2;
		int x=0;
		int y=timeY-adjY;
		long elapsedDisplay;
		if (dragging) {
			x=Math.min(dragX,endTimeX);
			x=Math.max(startTimeX,x);
			elapsedDisplay=getElapsedForX(x);
			x-=adjX;
		} else {
			elapsedDisplay=timeElapsed;
			// fraction elapsed * total width
			if (totalTime>0) { // avoid division by zero
				// multiply first so as not to lose fraction
				x=getXForElapsed(timeElapsed)-adjX;
			} else {
				x=getXForElapsed(0)-adjX;
			}
		}
		String displayStr=animation.getTimeDisplay(elapsedDisplay,totalTime);
		g.drawString(displayStr,timeDisplayPos.x,timeDisplayPos.y+g.getFontMetrics().getHeight()/2);
		g.setColor(Color.white);
		g.fillRect(x,y,controlSize.width,controlSize.height);
		g.setColor(Color.black);
		g.drawRect(x,y,controlSize.width,controlSize.height);
	}
	private void initComponents() {
		insets=new Insets(5,30,5,5);
		playButton=new CanvasButton(new Point(insets.left,insets.top),new Dimension(21,21)) {
			public void doAction() {
				animation.play();
			}
			public void paintButton(Graphics g) {
				g.fillPolygon(new int[] {x,x,x+width},new int[] {y,y+height,y+height/2} ,3);
			}
		};
		stopButton=new CanvasButton(new Point(insets.left+playButton.width+10,insets.top+3),new Dimension(15,15)) {
			public void doAction() {
				animation.stop();
			}
			public void paintButton(Graphics g) {
				g.fillPolygon(new int[] {x,x,x+width,x+width},new int[] {y,y+height,y+height,y},4);
			}
		};
		pauseButton=new CanvasButton(new Point(insets.left+playButton.width+stopButton.width+20,insets.top+3),new Dimension(15,15)) {
			public void doAction() {
				animation.pause();
			}
			public void paintButton(Graphics g) {
				g.fillPolygon(new int[] {x,x,x+width/3,x+width/3},new int[] {y,y+height,y+height,y},4);
				g.fillPolygon(new int[] {x+width,x+width,x+width*2/3,x+width*2/3},new int[] {y,y+height,y+height,y},4);
			}
		};
		rewindButton=new CanvasButton(new Point(insets.left+playButton.width+stopButton.width+pauseButton.width+30,insets.top+3),new Dimension(15,15)) {
			public void doAction() {
				animation.rewind();
			}
			public void paintButton(Graphics g) {
				g.fillPolygon(new int[] {x,x+width/2,x+width/2},new int[] {y+height/2,y,y+height},3);
				g.fillPolygon(new int[] {x+width/2,x+width,x+width},new int[] {y+height/2,y,y+height},3);
			}
		};
		forwardButton=new CanvasButton(new Point(insets.left+playButton.width+stopButton.width+pauseButton.width+rewindButton.width+40,insets.top+3),new Dimension(15,15)) {
			public void doAction() {
				animation.forward();
			}
			public void paintButton(Graphics g) {
				g.fillPolygon(new int[] {x,x+width/2,x},new int[] {y,y+height/2,y+height},3);
				g.fillPolygon(new int[] {x+width/2,x+width,x+width/2},new int[] {y,y+height/2,y+height},3);
			}
		};
		timeDisplayPos=new Point(insets.left+playButton.width+stopButton.width+pauseButton.width+rewindButton.width+forwardButton.width+50,insets.top+forwardButton.height/2);
		minimumSize=new Dimension(300,60);
	}
	private boolean isOverControl(Point p) {
		Dimension size=getSize();
		if (p.y>=(timeY-controlSize.height/2) && p.y<(timeY+controlSize.height/2)) {
			if (p.x>=insets.left && p.x<=(size.width-insets.right)) return true;
		}
		return false;
	}
	private CanvasButton findButton(Point p) {
		if (playButton.isMouseOver(p)) return playButton;
		if (stopButton.isMouseOver(p)) return stopButton;
		if (pauseButton.isMouseOver(p)) return pauseButton;
		if (rewindButton.isMouseOver(p)) return rewindButton;
		if (forwardButton.isMouseOver(p)) return forwardButton;
		return null;
	}
	private int getXForElapsed(long elapsed) {
		return startTimeX+(int)(elapsed*(long)(endTimeX-startTimeX)/(long)(totalTime+startTimeX));
	}
	private long getElapsedForX(int x) {
		x=Math.min(x,endTimeX);
		x=Math.max(startTimeX,x);
		return totalTime*(long)(x-startTimeX)/(long)(endTimeX-startTimeX);
	}
}
