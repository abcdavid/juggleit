import juggling.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SoloJugglerAnimation extends JugglingAnimation {

	Point rightHandPassPoint,rightHandCatchPoint,leftHandPassPoint,leftHandCatchPoint;
	int maxBeats;
	double g; // acceleration due to gravity
	// scale scoop size to component size
	double handScale;
	// scale throw size to component size
	double scale;
	// initial throw velocities
	double[] u;

	public SoloJugglerAnimation(PatternController patternController) {
		super(patternController);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (isStopped()) play();
				else pause();
			}
		});
	}
	public void initAnimation() {
		maxBeats=patternController.getPattern().findMaxBeats();
		g=10.0d;
		int scoopHeight=getSize().height/10;
		int maxHeight=getSize().height-scoopHeight-getBallRadius()*6;
		int handPassSeparation=getSize().width/6;
		int handY=getSize().height-getBallRadius()*3-scoopHeight;
		rightHandPassPoint=new Point((getSize().width+handPassSeparation)/2,handY);
		leftHandPassPoint=new Point((getSize().width-handPassSeparation)/2,handY);
		rightHandCatchPoint=new Point(getSize().width-getBallRadius()*3,handY);
		leftHandCatchPoint=new Point(getBallRadius()*3,handY);
		// calculate the initial velocities for different beat throws
		if (maxBeats==0) return; // empty pattern
		u=new double[maxBeats];
		for (int b=0;b<maxBeats;b++) {
			// time taken for entire throw
			long t=getBeatTime()*(long)(b+1)-getHandTime(b+1);
			u[b]=g*(double)(t/2);
		}
		// now scale to fit max height
		long maxT=getBeatTime()*(long)maxBeats-getHandTime(maxBeats);
		// maximum at maxT/2
		double s=u[maxBeats-1]*(double)(maxT/2)-g*(double)((maxT/2)*(maxT/2))/2.0d;
		scale=(double)maxHeight/s;
		// scale for scoop
		// maximum scoop depth at handTime/2 for heighest throw
		handScale=1.0d;
		int y=getHandY(getHandTime(maxBeats)/2,maxBeats);
		handScale=(double)scoopHeight/(double)y;
	}
	protected int getAirY(long passTime,int beatThrow) {
		double pt=(double)passTime;
		double s=u[beatThrow-1]*pt-g*pt*pt/2;
		return (int)(scale*s);
	}
	protected int getHandY(long scoopTime,int beatThrow) {
		double t=(double)scoopTime;
		double a=2.0d*u[beatThrow-1]/(double)getHandTime(beatThrow);
		double s=-u[beatThrow-1]*t+a*t*t/2.0d;
		return (int)(s*handScale);
	}
	public void renderHands(Graphics g) {
		drawHand(g,rightHandPassPoint);
		drawHand(g,rightHandCatchPoint);
		drawHand(g,leftHandPassPoint);
		drawHand(g,leftHandCatchPoint);
	}
	public void renderPattern(Graphics g,long time,int beatsElapsed) {
		//long beatNo=time/getBeatTime();
		long beatNo=(long)beatsElapsed;
		long remainder=time%getBeatTime();
		Enumeration enu=patternController.getPattern().getPassingEndPoints(beatsElapsed);
		while (enu.hasMoreElements()) {
			EndPoint passPoint=(EndPoint)enu.nextElement();
			EndPoint catchPoint=passPoint.getPassDestination();
			// calculate fraction of pass which has elapsed
			// how many beats since pass
			long wholeBeats=beatNo-(long)passPoint.getTime();
			long totalPassTime=(long)(catchPoint.getTime()-passPoint.getTime())*getBeatTime();
			long elapsedPassTime=(long)wholeBeats*getBeatTime()+remainder;
			long handTime=getHandTime(catchPoint.getTime()-passPoint.getTime());
			double passFraction;
			Point fromPoint,toPoint;
			int y;
			// is ball in air?
			if (elapsedPassTime>(totalPassTime-handTime)) {
				// in hand
				passFraction=(double)(elapsedPassTime-(totalPassTime-handTime))/(double)handTime;
				if (catchPoint.getHand().isRight()) {
					fromPoint=rightHandCatchPoint;
					toPoint=rightHandPassPoint;
				} else {
					fromPoint=leftHandCatchPoint;
					toPoint=leftHandPassPoint;
				}
				y=fromPoint.y+getHandY(elapsedPassTime-(totalPassTime-handTime),catchPoint.getTime()-passPoint.getTime());
			} else {
				// in air
				passFraction=(double)elapsedPassTime/(double)(totalPassTime-handTime);
				if (passPoint.getHand().isRight()) {
					fromPoint=rightHandPassPoint;
				} else {
					fromPoint=leftHandPassPoint;
				}
				if (catchPoint.getHand().isRight()) {
					toPoint=rightHandCatchPoint;
				} else {
					toPoint=leftHandCatchPoint;
				}
				y=fromPoint.y-getAirY(elapsedPassTime,catchPoint.getTime()-passPoint.getTime());
			}
			int x=fromPoint.x+(int)((double)(toPoint.x-fromPoint.x)*passFraction);
			drawBall(g,x,y,passPoint.getBall());
		}
	}
	public void renderBeatPasses(Graphics g,long time,int beatsElapsed) {
		Enumeration enu=patternController.getPattern().getPassingEndPoints(beatsElapsed);
		while (enu.hasMoreElements()) {
			EndPoint passPoint=(EndPoint)enu.nextElement();
			if (passPoint.getTime()==beatsElapsed) {
				EndPoint catchPoint=passPoint.getPassDestination();
				Point fromPoint;
				if (passPoint.getHand().isRight()) fromPoint=rightHandPassPoint;
				else fromPoint=leftHandPassPoint;
				Point toPoint;
				if (catchPoint.getHand().isRight()) toPoint=rightHandCatchPoint;
				else toPoint=leftHandCatchPoint;
				long totalPassTime=(long)(catchPoint.getTime()-passPoint.getTime())*getBeatTime();
				long handTime=getHandTime(catchPoint.getTime()-passPoint.getTime());
				// elapsed time for peak of trajectory
				long elapsedPassTime=(totalPassTime-handTime)/2;
				int y=fromPoint.y-getAirY(elapsedPassTime,catchPoint.getTime()-passPoint.getTime());
				int x=fromPoint.x+(toPoint.x-fromPoint.x)/2;
				drawPass(g,fromPoint,new Point(x,y),passPoint.getBall(),passPoint.getPassLabel());
			}
		}
	}
}
