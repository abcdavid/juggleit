import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import animation.*;
import juggling.*;

public class PassingAnimation extends JugglingAnimation {
	class Passer {
		int number;
		Point position;
		double orientation;
		int jugglerZone=80;
		int handZone=40;
		int passWidth=20; // how wide for passing
		int catchWidth=45; // how wide for catching
		Point leftHandPassPos,leftHandCatchPos,rightHandPassPos,rightHandCatchPos,leftHandPos,rightHandPos;
		Rectangle jugglerBounds,leftHandBounds,rightHandBounds;
		
		Passer(int number,Point position,double orientation) {
			this.number=number;
			this.position=position;
			this.orientation=orientation;
			positionChanged();
		}
		public Point getPosition() {
			return position;
		}
		public void setPosition(Point pos) {
			this.position=pos;
			patternController.setLocation(patternController.getPattern().getJuggler(number),pos,orientation);
			positionChanged();
		}
		public double getOrientation() {
			return orientation;
		}
		// facing which way 
		public void setOrientation(double orientation) {
			this.orientation=orientation;
			patternController.setLocation(patternController.getPattern().getJuggler(number),position,orientation);
			updateHandPositions();
		}
		private void positionChanged() {
			jugglerBounds=new Rectangle(position.x-jugglerZone/2,position.y-jugglerZone/2,jugglerZone,jugglerZone);
			updateHandPositions();
		}
		private void updateHandPositions() {
			// init hand positions
			double sinTheta=Math.sin(orientation);
			double cosTheta=Math.cos(orientation);
			int passSinTheta=(int)((double)passWidth*sinTheta);
			int passCosTheta=(int)((double)passWidth*cosTheta);
			int catchSinTheta=(int)((double)catchWidth*sinTheta);
			int catchCosTheta=(int)((double)catchWidth*cosTheta);
			leftHandPassPos=new Point(position);
			leftHandPassPos.translate(passSinTheta,-passCosTheta);
			leftHandCatchPos=new Point(position);
			leftHandCatchPos.translate(catchSinTheta,-catchCosTheta);
			rightHandPassPos=new Point(position);
			rightHandPassPos.translate(-passSinTheta,passCosTheta);
			rightHandCatchPos=new Point(position);
			rightHandCatchPos.translate(-catchSinTheta,catchCosTheta);
			// positions for hand labels
			leftHandPos=new Point((leftHandPassPos.x+leftHandCatchPos.x)/2,(leftHandPassPos.y+leftHandCatchPos.y)/2);
			rightHandPos=new Point((rightHandPassPos.x+rightHandCatchPos.x)/2,(rightHandPassPos.y+rightHandCatchPos.y)/2);
			// bounds for selection
			leftHandBounds=new Rectangle(leftHandPos.x-handZone/2,leftHandPos.y-handZone/2,handZone,handZone);
			rightHandBounds=new Rectangle(rightHandPos.x-handZone/2,rightHandPos.y-handZone/2,handZone,handZone);
			// phew
		}
		public Point getLHPassPosition() {
			return leftHandPassPos;
		}
		public Point getRHPassPosition() {
			return rightHandPassPos;
		}
		public Point getLHCatchPosition() {
			return leftHandCatchPos;
		}
		public Point getRHCatchPosition() {
			return rightHandCatchPos;
		}
		public EndPoint getEndPoint(Point pos,int time) {
			Juggler juggler=patternController.getPattern().getJuggler(number);
			Hand hand;
			if (isOverLeftHand(pos)) {
				hand=juggler.getLeftHand();
			} else {
				hand=juggler.getRightHand();
			}
			if (!hand.isBeat(time)) hand=hand.getOther();
			if (!hand.isBeat(time)) return null;
			return hand.getEndPoint(time);
		}
		boolean isOver(Point p) {
			return jugglerBounds.contains(p)||leftHandBounds.contains(p)||rightHandBounds.contains(p);
		}
		boolean isOverLeftHand(Point p) {
			return leftHandBounds.contains(p);
		}
		boolean isOverRightHand(Point p) {
			return rightHandBounds.contains(p);
		}
		public void paint(Graphics g) {
			drawHand(g,leftHandPassPos);
			drawHand(g,rightHandPassPos);
			drawHand(g,leftHandCatchPos);
			drawHand(g,rightHandCatchPos);
			// label juggler and hands
			g.setFont(boldFont);
			FontMetrics fm=g.getFontMetrics();
			String label=patternController.getPattern().getJuggler(number).getLabel();
			g.drawString(label,position.x-fm.stringWidth(label)/2,position.y+fm.getAscent()/2);
			g.setFont(defaultFont);
			fm=g.getFontMetrics();
			String leftHandLabel="L";
			g.drawString(leftHandLabel,leftHandPos.x-fm.stringWidth(leftHandLabel)/2,leftHandPos.y+fm.getAscent()/2);
			String rightHandLabel="R";
			g.drawString(rightHandLabel,rightHandPos.x-fm.stringWidth(rightHandLabel)/2,rightHandPos.y+fm.getAscent()/2);
		}
	}
	java.util.Vector passers=new java.util.Vector();
	Point dragPoint;
	Passer dragPasser;
	boolean overHand;

	public PassingAnimation(PatternController patternController) {
		super(patternController);
		addMouseListener(new MouseAdapter() {
			/*
			public void mouseClicked(MouseEvent e) {
				if (dragPasser!=null) {
					System.out.println(e.getPoint());
				} else {
					System.out.println("Null");
				}
			}
			*/
			public void mousePressed(MouseEvent e) {
				dragPoint=e.getPoint();
				dragPasser=getPasser(dragPoint);
			}
			public void mouseReleased(MouseEvent e) {
				Point dropPoint=e.getPoint();
				if (dragPasser!=null) {
					Passer dropPasser=getPasser(dropPoint);
					if (showBeatPasses) {
						if (dropPasser==null) return;
						// modify passes
						if (!(isPaused() || isStopped())) return;
						int time=(int)(getElapsedTime()/getBeatTime());
						EndPoint passPoint=dragPasser.getEndPoint(dragPoint,time);
						if (passPoint==null) return;
						EndPoint passDestination=passPoint.getPassDestination();
						int catchTime;
						if (passDestination.isValid()) {
							catchTime=passDestination.getTime();
						} else {
							// 3 beat passes is a good guess
							catchTime=passPoint.getTime()+3;
						}
						EndPoint catchPoint=dropPasser.getEndPoint(dropPoint,catchTime);
						if (catchPoint!=null) {
							PassingAnimation.this.patternController.makePass(passPoint,catchPoint);
						}
					} else {
						// modify positions
						boolean overLeft=false;
						if ((dropPasser!=null && dragPasser.equals(dropPasser)) && ((overLeft=dragPasser.isOverLeftHand(dragPoint))||dragPasser.isOverRightHand(dragPoint))) {
							// change orientation
							double orientation;
							Point pos=dragPasser.getPosition();
							if (dropPoint.x>pos.x) {
								orientation=Math.atan((double)(dropPoint.y-pos.y)/(double)(dropPoint.x-pos.x));
							} else if (dropPoint.x<pos.x) {
								orientation=(Math.atan((double)(dropPoint.y-pos.y)/(double)(dropPoint.x-pos.x))+Math.PI)%(2*Math.PI);
							} else {
								// dropPoint.x==pos.x
								if (dropPoint.y>pos.y) orientation=Math.PI/2;
								else orientation=3*Math.PI/2;
							}
							if (overLeft) orientation=(orientation+Math.PI/2)%(Math.PI*2);
							else orientation=(orientation-Math.PI/2)%(2*Math.PI);
							dragPasser.setOrientation(orientation);
							repaint();
						} else if (dropPasser==null || dropPasser.equals(dragPasser)) {
							// change position
							dragPasser.setPosition(e.getPoint());
							repaint();
						} else { //if (!dropPasser.isOverRightHand(dropPoint) && !dropPasser.isOverLeftHand(dropPoint)) {
							// swap locations
							Point dragPos=dragPasser.getPosition();
							double dragOrientation=dragPasser.getOrientation();
							dragPasser.setPosition(dropPasser.getPosition());
							dragPasser.setOrientation(dropPasser.getOrientation());
							dropPasser.setPosition(dragPos);
							dropPasser.setOrientation(dragOrientation);
							repaint();
						}
					}
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (getPasser(e.getPoint())!=null) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
			public void mouseDragged(MouseEvent e) {
				if (dragPasser!=null) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
	}
	public void initAnimation() {
		double rads=0.0d;
		int x,y;
		double orientation;
		double radius=(double)((Math.min(getSize().width,getSize().height)-getBallRadius()*3)/2);// the radius of the circle of jugglers
		Pattern pattern=patternController.getPattern();
		double radianSeparation=(2.0d*Math.PI)/(double)pattern.getJugglerCount();
		passers.removeAllElements();
		for (int j=0;j<pattern.getJugglerCount();j++) {
			Juggler juggler=pattern.getJuggler(j);
			Point position=patternController.getPosition(juggler);
			orientation=patternController.getOrientation(juggler);
			if (position==null) {
				x=(int)(radius*Math.cos(rads));
				y=(int)(radius*Math.sin(rads));
				orientation=rads+Math.PI;
				position=new Point(x,y);
				position.translate(getSize().width/2,getSize().height/2);
				// todo - is there already a juggler here?
				// init positions in model too
				patternController.setLocation(juggler,position,orientation);
			}
			passers.add(new Passer(j,position,orientation));
			rads+=radianSeparation;
		}
	}
	private Passer getPasser(Point p) {
		java.util.Iterator it=passers.iterator();
		while (it.hasNext()) {
			Passer passer=(Passer)it.next();
			if (passer.isOver(p)) {
				return passer;
			}
		}
		return null;
	}
	private Point getPassPosition(EndPoint endPoint) {
		Hand hand=endPoint.getHand();
		Passer passer=(Passer)passers.get(hand.getJuggler().getNumber());
		if (hand.isLeft()) {
			return passer.getLHPassPosition();
		}
		return passer.getRHPassPosition();
	}
	private Point getCatchPosition(EndPoint endPoint) {
		Hand hand=endPoint.getHand();
		Passer passer=(Passer)passers.get(hand.getJuggler().getNumber());
		if (hand.isLeft()) {
			return passer.getLHCatchPosition();
		}
		return passer.getRHCatchPosition();
	}
	public void renderPattern(Graphics g,long time,int beatsElapsed) {
		long remainder=time%getBeatTime();
		Enumeration enu=patternController.getPattern().getPassingEndPoints(beatsElapsed);
		while (enu.hasMoreElements()) {
			renderBall(g,(EndPoint)enu.nextElement(),beatsElapsed,remainder);
		}
	}
	private void renderBall(Graphics g,EndPoint passPoint,int beatsElapsed,long remainder) {
		long beatNo=(long)beatsElapsed;
		EndPoint catchPoint=passPoint.getPassDestination();
		// calculate fraction of pass which has elapsed
		// how many beats since pass
		long wholeBeats=beatNo-(long)passPoint.getTime();
		long totalPassTime=(long)(catchPoint.getTime()-passPoint.getTime())*getBeatTime();
		long elapsedPassTime=(long)wholeBeats*getBeatTime()+remainder;
		long handTime=getHandTime(catchPoint.getTime()-passPoint.getTime());
		double passFraction;
		Point fromPoint,toPoint;
		// is ball in air?
		if (elapsedPassTime>(totalPassTime-handTime)) {
			// in hand
			passFraction=(double)(elapsedPassTime-(totalPassTime-handTime))/(double)handTime;
			fromPoint=getCatchPosition(catchPoint);
			toPoint=getPassPosition(catchPoint);
		} else {
			// in air
			passFraction=(double)elapsedPassTime/(double)(totalPassTime-handTime);
			fromPoint=getPassPosition(passPoint);
			toPoint=getCatchPosition(catchPoint);
		}
		int x=fromPoint.x+(int)((double)(toPoint.x-fromPoint.x)*passFraction);
		int y=fromPoint.y+(int)((double)(toPoint.y-fromPoint.y)*passFraction);
		drawBall(g,x,y,passPoint.getBall());
	}
	public void renderBeatPasses(Graphics g,long time,int beatsElapsed) {
		Enumeration enu=patternController.getPattern().getPassingEndPoints(beatsElapsed);
		while (enu.hasMoreElements()) {
			EndPoint passPoint=(EndPoint)enu.nextElement();
			if (passPoint.getTime()==beatsElapsed) {
				// thrown on current beat
				EndPoint catchPoint=passPoint.getPassDestination();
				Point fromPoint=getPassPosition(passPoint);
				Point toPoint=getCatchPosition(catchPoint);
				drawPass(g,fromPoint,toPoint,passPoint.getBall(),passPoint.getPassLabel());
			}
		}
	}
	public void renderHands(Graphics g) {
		g.setColor(Color.black);
		java.util.Iterator it=passers.iterator();
		while (it.hasNext()) {
			Passer passer=(Passer)it.next();
			passer.paint(g);
		}
	}
}
