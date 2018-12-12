package juggling;

import java.io.*;
import java.util.*;

public class Juggler {
	Pattern pattern;
	int number;
	Hand leftHand;
	Hand rightHand;
	Location location;

	Juggler(int number,Pattern pattern) {
		this.number=number;
		this.pattern=pattern;
		rightHand=new Hand(this,new SimpleRhythm(0,2));
		leftHand=new Hand(this,new SimpleRhythm(1,2));
		location=new Location();
	}
	public Location getLocation() {
		return location;
	}
	protected void setNumber(int number) {
		this.number=number;
	}
	protected int getHandNumber(Hand hand) {
		if ((pattern.leftRightOrder && hand.isLeft()) || (!pattern.leftRightOrder && hand.isRight())) {
			return getNumber()*2;
		} else {
			return getNumber()*2+1;
		}
	}
	public int getNumber() {
		return number;
	}
	public String getLabel() {
		return new StringBuffer("").append((char)('A'+number)).toString();
	}
	public int getBallCount() {
		return rightHand.getBallCount()+leftHand.getBallCount();
	}
	public Pass getPass(int time) {
		if (rightHand.isBeat(time) && !leftHand.isBeat(time)) {
			return rightHand.getEndPoint(time).getPass();
		}
		if (leftHand.isBeat(time) && !rightHand.isBeat(time)) {
			return leftHand.getEndPoint(time).getPass();
		}
		if (leftHand.isBeat(time) && rightHand.isBeat(time)) {
			// synchro pass
			return new Pass(rightHand.getEndPoint(time).getPass(),leftHand.getEndPoint(time).getPass());
		}
		return Pass.NO_PASS;
	}
	public void removePass(int time) {
		if (rightHand.isBeat(time)) {
			rightHand.getEndPoint(time).removePass();
		}
		if (leftHand.isBeat(time)) {
			leftHand.getEndPoint(time).removePass();
		}
	}
	public boolean setPass(int time,Pass pass) {
		boolean failed=false;
		if (rightHand.isBeat(time)) {
			failed=!setPass(rightHand.getEndPoint(time),pass.getRightHandPass());
		}
		if (leftHand.isBeat(time)) {
			failed=failed||!setPass(leftHand.getEndPoint(time),pass.getLeftHandPass());
		}
		return !failed;
	}
	private boolean setPass(EndPoint passPoint,Pass pass) {
		if (pass.noPass()) return true;
		EndPoint[] endPoints=passPoint.getPossibleDestinations(pass);
		for (int i=0;i<endPoints.length;i++) {
			if (passPoint.makePass(endPoints[i])) return true;
		}
		return false;
	}
	protected void goodbye() {
		rightHand.goodbye();
		leftHand.goodbye();
	}
	public Pattern getPattern() {
		return pattern;
	}
	public Hand getLeftHand() {
		return leftHand;
	}
	public Hand getRightHand() {
		return rightHand;
	}
}
