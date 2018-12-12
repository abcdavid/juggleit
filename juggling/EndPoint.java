package juggling;

import java.io.*;

public class EndPoint {
	private Hand hand;
	int beatIndex;
	private Ball ball=Ball.NO_BALL;
	private EndPoint catchOrigin=INVALID_ENDPOINT;
	private EndPoint passDestination=INVALID_ENDPOINT;

	public static EndPoint INVALID_ENDPOINT=new EndPoint(null,-1);

	EndPoint(Hand hand,int beatIndex) {
		this.hand=hand;
		this.beatIndex=beatIndex;
	}
	// return the time of this beat
	public int getTime() {
		if (!isValid()) return -1;
		return hand.getTime(this);
	}
	public Hand getHand() {
		return hand;
	}
	public Juggler getJuggler() {
		if (hand==null) return null;
		return hand.getJuggler();
	}
	public Ball getBall() {
		return ball;
	}
	
	protected void setBall(Ball ball) {
		if (!this.ball.equals(ball)) {
			this.ball=ball;
			if (passDestination.isValid()) {
				passDestination.setBall(ball);
			}
		}
	}
	public boolean isValid() {
		return (beatIndex!=-1);
	}
	public boolean equals(Object o) {
		if (o instanceof EndPoint) {
			EndPoint endPoint=(EndPoint)o;
			return (endPoint.beatIndex==beatIndex && ((hand!=null && endPoint.hand!=null && endPoint.hand.equals(hand)) || hand==null && endPoint.hand==null));
		}		
		return false;
	}
	protected Pass getPass() {
		if (isValid() && passDestination.isValid()) {
			int beats=passDestination.getTime()-getTime();
			// is this a switched pass?
			boolean switched=(
					(beats%2==0 && getHand().isLeft()!=passDestination.getHand().isLeft())
					||
					(beats%2==1 && getHand().isLeft()==passDestination.getHand().isLeft())
					);
			if (passDestination.getJuggler().equals(getJuggler())) return new Pass(beats,switched);
			return new Pass(passDestination.getJuggler().getNumber(),beats,switched);
		}
		return Pass.NO_PASS;
	}
	public String getPassLabel() {
		if (!passDestination.isValid()) return "0";
		int beats=passDestination.getTime()-getTime();
		boolean switched=(
					(beats%2==0 && getHand().isLeft()!=passDestination.getHand().isLeft())
					||
					(beats%2==1 && getHand().isLeft()==passDestination.getHand().isLeft())
					);
		boolean passed=!passDestination.getJuggler().equals(getJuggler());
		String label=Integer.toString(beats);
		if (passed) label+='p';
		if (switched) label+='x';
		return label;
	}
	public EndPoint getPassDestination() {
		return passDestination;
	}
	public EndPoint getCatchOrigin() {
		return catchOrigin;
	}
	public EndPoint getNext() {
		return hand.getEndPointByBeatIndex(beatIndex+1);
		
	}
	public boolean makePass(EndPoint endPoint) {
		if (isValid() && endPoint.isValid() && !passDestination.isValid() && !endPoint.catchOrigin.isValid() && endPoint.getTime()>getTime()) {
			if (catchOrigin.isValid()) {
				// we have a ball to throw
				ball=catchOrigin.getBall(); // this should be defined
				if (endPoint.getBall().noBall()) {
					// catcher doesn't have a ball
					endPoint.setBall(ball);
				} else {
					// catcher has a ball already
					// remove it first
					endPoint.hand.removeOldBall(endPoint.ball);
					endPoint.setBall(ball);
				}
			} else {
				// we need a ball, check out the catcher
				ball=endPoint.getBall();
				if (ball.noBall()) {
					// a new one
					ball=hand.getNewBall();
					endPoint.setBall(ball);
				} else {
					// use the same ball as the catcher 
					endPoint.hand.ballCount--;
					hand.ballCount++;
				}
			}
			passDestination=endPoint;
			endPoint.catchOrigin=this;
			// keep track of last catch
			if (endPoint.hand.lastBeat<endPoint.beatIndex) endPoint.hand.lastBeat=endPoint.beatIndex;
			// keep track of last pass
			if (hand.lastPassBeat<this.beatIndex) hand.lastPassBeat=this.beatIndex;
			return true;
		}
		return false;
	}
	public boolean removePass() {
		if (passDestination.isValid()) {
			if (catchOrigin.isValid()) {
				// ball originated elsewhere
				// destination may require a new ball
				if (passDestination.getPassDestination().isValid()) {
					// ball is required later on
					passDestination.setBall(passDestination.hand.getNewBall());
				} else {
					passDestination.ball=Ball.NO_BALL;
				}
			} else {
				// ball originates here
				if (passDestination.getPassDestination().isValid()) {
					// ball has a life yet
					hand.ballCount--;
					passDestination.hand.ballCount++;
					ball=Ball.NO_BALL;
				} else {
					// ball no longer used
					hand.removeOldBall(ball);
					ball=Ball.NO_BALL;
					passDestination.ball=Ball.NO_BALL;
				}
			}
			passDestination.catchOrigin=EndPoint.INVALID_ENDPOINT;
			EndPoint endPoint=passDestination;
			passDestination=INVALID_ENDPOINT;
			// keep track of last pass and catch
			if (endPoint.hand.lastBeat==endPoint.beatIndex) endPoint.hand.findLastBeat();
			if (hand.lastPassBeat==beatIndex) hand.findLastBeat();
			return true;
		}
		return false;
	}
	public EndPoint[] getPossibleDestinations(Pass pass) {
		int t=getTime()+pass.getBeats();
		if (pass.isSelf()) {
			if (pass.isRtoRorLtoL()) {
				if (getHand().isBeat(t)) return new EndPoint[] {getHand().getEndPoint(t)};
			} else {
				if (getHand().getOther().isBeat(t)) return new EndPoint[] {getHand().getOther().getEndPoint(t)};
			}
			return new EndPoint[] {}; // no valid endpoint found
		}
		java.util.ArrayList passEndPoints=new java.util.ArrayList();
		int jugglerCount=getHand().getJuggler().getPattern().getJugglerCount();
		for (int j=0;j<jugglerCount;j++) {
			Juggler juggler=getHand().getJuggler().getPattern().getJuggler(j);
			if (juggler.getNumber()!=getHand().getJuggler().getNumber()) {
				Hand hand;
				if ( (getHand().isRight() && pass.isRtoRorLtoL()) || (getHand().isLeft() && pass.isRtoLorLtoR()) ) hand=juggler.getRightHand();
				else hand=juggler.getLeftHand();
				if (hand.isBeat(t)) {
					if (hand.getJuggler().getNumber()==pass.getToJuggler()) {
						// pass to same person most likely option
						passEndPoints.add(0,hand.getEndPoint(t));
					} else {
						passEndPoints.add(hand.getEndPoint(t));
					}
				}
			}
		}
		EndPoint[] endPointArray=new EndPoint[passEndPoints.size()];
		passEndPoints.toArray(endPointArray);
		return endPointArray;
	}
	public String toString() {
		return "EndPoint{time="+getTime()+",hand="+getHand().getNumber()+",ball="+getBall().getNumber()+"}";
	}
}
