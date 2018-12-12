import java.awt.*;
import java.util.*;

import animation.*;
import juggling.*;

public abstract class JugglingAnimation extends AnimationCanvas {
//	boolean repeat=true;
	boolean showBeatPasses=false;
	PatternController patternController;
	private static long beatTime;  // how many milliseconds per beat
	private long handTime; // how many milliseconds between catching and throwing
	private int ballRadius;
	private boolean initDone=false;
	protected Font boldFont,defaultFont;
	private Observer patternObserver=new Observer() {
		public void update(Observable ob,Object o) {
			//initAnimation(); // reinitialises
			initDone=false;
			setBeatTime(beatTime); // indirectly calls setTotalTime if length of animation has changed
		}
	};

	public JugglingAnimation(PatternController patternController) {
		setController(patternController);
		if (beatTime>0) setBeatTime(beatTime); // keep setting between animations
		else setBeatTime(500);
		handTime=400;
		ballRadius=10;
	}
	public void setShowBeatPasses(boolean show) {
		if (show!=showBeatPasses) {
			showBeatPasses=show;
			if (isPaused() || isStopped()) repaint();
		}
	}
	public boolean getShowBeatPasses() {
		return showBeatPasses;
	}
	public void setController(PatternController controller) {
		if (patternController!=null) {
			patternController.deleteObserver(patternObserver);
		}
		if (controller!=null) {
			controller.addObserver(patternObserver);
		}
		patternController=controller;
	}
	public long getBeatTime() {
		return beatTime;
	}
	public void setBeatTime(long millis) {
		this.beatTime=millis;
		setSkipInterval(millis);
		setTotalTime((long)patternController.getPattern().getTotalTime()*beatTime);
		repaint(); // ball positions have changed
	}
	public long getHandTime(int throwBeats) {
		if (throwBeats==1) return handTime/2;
		return handTime;
	}
	public void setHandTime(long millis) {
		if (millis>beatTime) throw new RuntimeException("handTime>beatTime");
		this.handTime=millis;
	}
	public void setTotalTime(long totalTime) {
		super.setTotalTime(totalTime);
		if (initDone) initAnimation(); // need to update
	}
	public int getBallRadius() {
		return ballRadius;
	}
	public void setBallRadius(int r) {
		ballRadius=r;
	}
	public void play() {
		synchronized (this) {
			if (!initDone) {
				initAnimation();
				initDone=true;
			}
		}
		super.play();
	}
	// override this method for any required initialisation
	public void initAnimation() {}
	public void render(Graphics g,long time) {
		if (!initDone) initAnimation();
		renderHands(g);
		if (boldFont==null) {
			defaultFont=g.getFont();
			boldFont=defaultFont.deriveFont(Font.BOLD,14.0f);
		}
		int beatsElapsed=(int)(time/beatTime);
		if (showBeatPasses) {
			renderBeatPasses(g,time,beatsElapsed);
		} else {
			renderPattern(g,time,beatsElapsed);
		}
	}
	public abstract void renderHands(Graphics g);
	public abstract void renderPattern(Graphics g,long time,int beatsElapsed);
	public abstract void renderBeatPasses(Graphics g,long time,int beatsElapsed);
	
	protected void drawBall(Graphics g,int x,int y,Ball ball) {
		g.setColor(patternController.getBallColor(ball));
		g.fillOval(x-ballRadius,y-ballRadius,ballRadius*2,ballRadius*2);
	}
	protected void drawHand(Graphics g,Point p) {
		g.setColor(Color.black);
		g.drawLine(p.x-2,p.y,p.x+2,p.y);
		g.drawLine(p.x,p.y-2,p.x,p.y+2);
	}
	protected void drawPass(Graphics g,Point a,Point b,Ball ball,String label) {
		g.setColor(patternController.getBallColor(ball));
		g.drawLine(a.x,a.y,b.x,b.y);
		double arrowHeadSize=10.0d;
		double x=(double)(b.x-a.x);
		double y=(double)(b.y-a.y);
		double adjX=arrowHeadSize/Math.sqrt(1.0f+y*y/(x*x));
		double adjY=arrowHeadSize/Math.sqrt(1.0f+x*x/(y*y));
		int arrowX=(int)adjX;
		int arrowY=(int)adjY;
		if (b.x<a.x) arrowX=-arrowX;
		if (b.y<a.y) arrowY=-arrowY;
		g.fillPolygon(new int[]{b.x,b.x-arrowX-arrowY,b.x-arrowX+arrowY},new int[]{b.y,b.y-arrowY+arrowX,b.y-arrowY-arrowX},3);
		g.setColor(Color.black);
		g.setFont(boldFont);
		FontMetrics fm=g.getFontMetrics();
		g.drawString(label,a.x-fm.stringWidth(label)/2,a.y+fm.getHeight()/2);
		g.setFont(defaultFont);
	}
	public String getTimeDisplay(long elapsed,long totalTime) {
		return super.getTimeDisplay(elapsed,totalTime)+" beat "+Long.toString(elapsed/getBeatTime()+1);
	}
}
