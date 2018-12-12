package juggling;

import java.util.*;
import java.io.*;

public class Hand {

	Juggler juggler;
	Rhythm rhythm;
	int ballCount=0;
	Vector endPoints=new Vector();
	int lastBeat=-1; // the last catch beat
	int lastPassBeat=-1; // now for passes

	Hand(Juggler juggler,Rhythm rhythm) {
		this.juggler=juggler;
		this.rhythm=rhythm;
	}
	protected void findLastBeat() {
		// find the new last beat
		lastBeat=-1;
		lastPassBeat=-1;
		Iterator it=endPoints.iterator();
		while (it.hasNext()) {
			EndPoint endPoint=(EndPoint)it.next();
			if (endPoint!=null && endPoint.beatIndex>lastBeat && endPoint.getCatchOrigin().isValid()) {
				lastBeat=endPoint.beatIndex;
			}
			if (endPoint!=null && endPoint.beatIndex>lastPassBeat && endPoint.getPassDestination().isValid()) {
				lastPassBeat=endPoint.beatIndex;
			}
		}
/*		for (int i=0;i<endPoints.length;i++) {
			if (endPoints[i]!=null && endPoints[i].beatIndex>lastBeat && endPoints[i].getCatchOrigin().isValid()) {
				lastBeat=endPoints[i].beatIndex;
			}
			if (endPoints[i]!=null && endPoints[i].beatIndex>lastPassBeat && endPoints[i].getPassDestination().isValid()) {
				lastPassBeat=endPoints[i].beatIndex;
			}
		}
*/
	}
	protected int getLastCatchTime() {
		if (lastBeat==-1) return 0;
		return rhythm.getTime(lastBeat+1);
	}
	protected int getLastPassTime() {
		if (lastPassBeat==-1) return 0;
		return rhythm.getTime(lastPassBeat+1);
	}
	protected Ball getNewBall() {
		ballCount++;
		return getJuggler().getPattern().getBall();
	}
	protected void removeOldBall(Ball ball) {
		if (!ball.noBall()) {
			getJuggler().getPattern().putBall(ball);
			ballCount--;
		}
	}
	public int getBallCount() {
		return ballCount;
	}
	public String getLabel() {
		if (isLeft()) return "L";
		return "R";
	}
	public int getNumber() {
		return juggler.getHandNumber(this);
	}
/*
	protected void setNumber(int number) {
		this.number=number;
	}
*/
	public Juggler getJuggler() {
		return juggler;
	}
	public boolean isLeft() {
		return juggler.getLeftHand().equals(this);
	}
	public boolean isRight() {
		return juggler.getRightHand().equals(this);
	}
	public synchronized void setRhythm(Rhythm rhythm) throws PatternException {
		Rhythm oldRhythm=this.rhythm;
		this.rhythm=rhythm;
		if (!checkPassesAndCatches()) {
			this.rhythm=oldRhythm;
			throw new PatternException("Rhythm is not valid:Cannot catch before pass is made");
		}
	}
	protected boolean checkPassesAndCatches() {
		Iterator it=endPoints.iterator();
		while (it.hasNext()) {
			EndPoint endPoint=(EndPoint)it.next();
			if (endPoint!=null) {
				EndPoint passPoint=endPoint.getCatchOrigin();
				if (passPoint.isValid() && passPoint.getTime()>=endPoint.getTime()) return false;
				EndPoint catchPoint=endPoint.getPassDestination();
				if (catchPoint.isValid() && catchPoint.getTime()<=endPoint.getTime()) return false;
			}
		}
		return true;
	}
	public Rhythm getRhythm() {
		return rhythm;
	}
	public Hand getOther() {
		if (isRight()) return juggler.getLeftHand();
		return juggler.getRightHand();
	}
	public boolean isBeat(int time) {
		return rhythm.isBeat(time);
	}
	protected EndPoint getEndPointByBeatIndex(int index) {
		if (index>=endPoints.size()) {
			endPoints.setSize(index+1);
		}
		EndPoint endPoint;
		synchronized (this) {
			endPoint=(EndPoint)endPoints.get(index);
			if (endPoint==null) {
				endPoint=new EndPoint(this,index);
				endPoints.set(index,endPoint);
			}
		}
		return endPoint;
	}
	public EndPoint getEndPoint(int time) {
		if (!isBeat(time)) return EndPoint.INVALID_ENDPOINT;
		int index=rhythm.getBeatCount(time)-1;
		return getEndPointByBeatIndex(index);
	}
	// no more passes and catches and balls returned
	protected void goodbye() {
		Iterator it=endPoints.iterator();
		while (it.hasNext()) {
			EndPoint endPoint=(EndPoint)it.next();
			if (endPoint!=null) {
				endPoint.removePass();
				EndPoint passPoint=endPoint.getCatchOrigin();
				if (passPoint.isValid()) passPoint.removePass();
			}
		}
	}
	protected int getTime(EndPoint endPoint) {
		return rhythm.getTime(endPoint.beatIndex+1);
	}
	public boolean makePass(int time,int beats,Hand toHand) {
		EndPoint toEndPoint=toHand.getEndPoint(time+beats);
		EndPoint fromEndPoint=getEndPoint(time);
		return fromEndPoint.makePass(toEndPoint);
	}
}
