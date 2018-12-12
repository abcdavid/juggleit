package juggling;

import java.util.*;
import java.io.*;

public class Pattern {
	boolean leftRightOrder=true;
	Vector jugglers=new Vector();
	Vector balls=new Vector();
	String comment;

	public Pattern() {
	}
	public void setComment(String comment) {
		this.comment=comment;
	}
	public String getComment() {
		return comment;
	}
	public Juggler addJuggler() {
		Juggler juggler=new Juggler(jugglers.size(),this);
		jugglers.addElement(juggler);
		return juggler;
	}
	public boolean removeJuggler(Juggler juggler) {
		if (jugglers.removeElement(juggler)) {
			// remove any passes
			juggler.goodbye();
			// update numbering
			for (int i=0;i<jugglers.size();i++) {
				((Juggler)jugglers.elementAt(i)).setNumber(i);
			}
			return true;
		}
		return false;
	}

	public void setJugglerCount(int jCount) {
		if (jCount<0) throw new RuntimeException("Cannot set juggler count to "+jCount);
		while (jugglers.size()<jCount) addJuggler();
		while (jugglers.size()>jCount) removeJuggler(getJuggler(jugglers.size()-1));;
	}
	protected void putBall(Ball ball) {
		balls.removeElement(ball);
	}
	protected Ball getBall() {
		int ballNo=0;
		Ball ball=new Ball(ballNo);
		while (balls.contains(ball)) {
			ball=new Ball(++ballNo);
		}
		balls.addElement(ball);
		return ball;
	}
	/** @return The time (in beats) of the last catch */
	public int getTotalTime() {
		int max=0;
		for (int h=0;h<getHandCount();h++) {
			int t=getHand(h).getLastCatchTime();
			if (t>max) max=t;
		}
		return max;
	}
	/** @return The time (in beats) of the last pass */
	public int getTotalPassTime() {
		int max=0;
		for (int h=0;h<getHandCount();h++) {
			int t=getHand(h).getLastPassTime();
			if (t>max) max=t;
		}
		return max;
	}
	/** @return The maximum height of throw in beats */
	public int findMaxBeats() {
		int time=0;
		int maxBeats=0;
		int totalTime=getTotalTime();
		while (time<=totalTime) {
			for (int h=0;h<getHandCount();h++) {
				Hand hand=getHand(h);
				if (hand.isBeat(time)) {
					EndPoint endPoint=hand.getEndPoint(time);
					EndPoint catchOrigin;
					if ((catchOrigin=endPoint.getCatchOrigin()).isValid()) {
						int beats=endPoint.getTime()-catchOrigin.getTime();
						if (beats>maxBeats) {
							maxBeats=beats;
						}
					}
				}
			}
			time++;
		}
		return maxBeats;
	}
	public Juggler getJuggler(int n) {
		if (n>=jugglers.size()) return null;
		return (Juggler)jugglers.elementAt(n);
	}
	public int getJugglerCount() {
		return jugglers.size();
	}
	public int getHandCount() {
		return jugglers.size()*2;
	}
	public int getBallCount() {
		return balls.size();
	}
	public Ball getBall(int n) {
		if (n<balls.size())
			return (Ball)balls.elementAt(n);
		return null;
	}
	public Hand getHand(int number) {
		Juggler j=(Juggler)jugglers.elementAt(number/2);
		if ((number%2==0 && leftRightOrder) || (number%2==1 && !leftRightOrder)) return j.getLeftHand();
		return j.getRightHand();
	}
	
	/** @return The passing Endpoints - balls are in the air or not yet thrown again at this time */
	public Enumeration getPassingEndPoints(int time) {
		Vector endPoints=new Vector();
		int maxBeats=findMaxBeats();
		for (int t=Math.max(0,time-maxBeats);t<=time;t++) {
			for (int h=0;h<getHandCount();h++) {
				EndPoint endPoint=getHand(h).getEndPoint(t);
				if (endPoint.isValid() && !endPoint.getBall().noBall() && endPoint.getPassDestination().isValid() && endPoint.getPassDestination().getTime()>time) endPoints.addElement(endPoint);
			}
		}
		return endPoints.elements();
	}
}
