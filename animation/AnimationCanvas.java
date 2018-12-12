package animation;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AnimationCanvas extends Canvas implements Runnable {

	Thread animationThread;
	static int framesPerSecond; // default 20 frames per second
	static long frameDuration;
	long totalTime=10000; // 10 seconds duration (10 000 milliseconds)
	int frameNo=0;
	long skipInterval=1000;

	long startTime=0;
	boolean stopped=true;
	boolean paused=false; 
	Vector animationListeners=new Vector();

	Image animationImage;
	Graphics animationG;

	protected AnimationCanvas() {
		setFramesPerSecond(20);
	}
	public void addAnimationListener(AnimationListener listener) {
		animationListeners.addElement(listener);
	}
	public void removeAnimationListener(AnimationListener listener) {
		animationListeners.removeElement(listener);
	}
	private void notifyFrameChanged() {
		Enumeration enu=animationListeners.elements();
		AnimationEvent e=new AnimationEvent(this);
		while (enu.hasMoreElements()) {
			AnimationListener listener=(AnimationListener)enu.nextElement();
			listener.animationFrameChanged(e);
		}
	}
	private void notifyStarted() {
		Enumeration enu=animationListeners.elements();
		AnimationEvent e=new AnimationEvent(this);
		while (enu.hasMoreElements()) {
			AnimationListener listener=(AnimationListener)enu.nextElement();
			listener.animationStarted(e);
		}
	}
	private void notifyStopped() {
		Enumeration enu=animationListeners.elements();
		AnimationEvent e=new AnimationEvent(this);
		while (enu.hasMoreElements()) {
			AnimationListener listener=(AnimationListener)enu.nextElement();
			listener.animationStopped(e);
		}
	}
	private void notifyPaused() {
		Enumeration enu=animationListeners.elements();
		AnimationEvent e=new AnimationEvent(this);
		while (enu.hasMoreElements()) {
			AnimationListener listener=(AnimationListener)enu.nextElement();
			listener.animationPaused(e);
		}
	}
	private void notifyPropertyChanged() {
		Enumeration enu=animationListeners.elements();
		AnimationEvent e=new AnimationEvent(this);
		while (enu.hasMoreElements()) {
			AnimationListener listener=(AnimationListener)enu.nextElement();
			listener.animationPropertyChanged(e);
		}
	}
	public synchronized void setFramesPerSecond(int frames) {
		//if (!isPaused() && !isStopped()) pause();
		long elapsed=getElapsedTime();
		framesPerSecond=frames;
		frameDuration=(long)(1000/frames);
		setElapsedTime(elapsed);
	}
	public int getFramesPerSecond() {
		return framesPerSecond;
	}
	public void setTotalTime(long time) {
		//if (!isPaused() && !isStopped()) pause();
		// make sure this is divisible by frameDuration
		long extra=time%frameDuration;
		this.totalTime=time-extra;
		if (getElapsedTime()>totalTime) {
			setFrameNumber(getFrameNumber(totalTime));
		}
		notifyPropertyChanged();
	}
	private void setFrameNumber(int number) {
		setFrameNumber(number,true);
	}
	private void setFrameNumber(int number,boolean updateStartTime) {
		if (frameNo!=number && number>=0 && getFrameTime(number)<=getTotalTime()) {
			frameNo=number;
			repaint();
			notifyFrameChanged();
			if (stopped) {
				// stopped->paused transition
				if (getFrameTime(frameNo)<getTotalTime()) {
					paused=true;
					stopped=false;
					notifyPaused();
				}
			} else if (getElapsedTime()==getTotalTime()) {
				// playing->stopped or paused->stopped transition
				if (animationThread==null) {
					notifyStopped();
				}
				stopped=true; // thread can notify
			} else if (!paused && updateStartTime) {
				// playing - startTime update required for thread
				startTime=System.currentTimeMillis()-getElapsedTime();
			}
		}
	}
	public long getFrameTime(int frameNo) {
		return frameNo*frameDuration;
	}
	public long getWaitTime() {
		long nextFrameTime=startTime+getFrameTime(frameNo+1);
		return Math.max(0,nextFrameTime-System.currentTimeMillis());
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setElapsedTime(long elapsedTime) {
		// which frame?
		elapsedTime=Math.min(elapsedTime,totalTime);
		elapsedTime=Math.max(0,elapsedTime);
		setFrameNumber(getFrameNumber(elapsedTime));
	}
	private void nextFrame() {
		setFrameNumber(frameNo+1,false);
	}
	public long getElapsedTime() {
		return frameNo*frameDuration;
	}
	public int getFrameNumber(long time) {
		return (int)(time/frameDuration);
	}
	public void setSkipInterval(long interval) {
		this.skipInterval=interval;
	}
	public void forward() {
		setElapsedTime(getElapsedTime()+skipInterval);
	}
	public void rewind() {
		setElapsedTime(getElapsedTime()-skipInterval);
	}
	public void pause() {
		if (paused) {
			play();
		} else if (!stopped) {
			paused=true; // let thread notify
		}
	}
	public void play() {
		if (animationThread==null) {
			if (stopped) setFrameNumber(0); // play from beginning
			startTime=System.currentTimeMillis()-getElapsedTime();
			stopped=false;
			paused=false;
			animationThread=new Thread(this);
			animationThread.start();
			notifyStarted();
		}
	}
	public void run() {
		try {
			while (!isStopped() && !isPaused() && getElapsedTime()<getTotalTime()) {
				Thread.currentThread().sleep(getWaitTime());
				nextFrame();
			}
		} catch (InterruptedException ex) {
		}
		repaint();
		if (isPaused()) notifyPaused();
		else {
			stopped=true; // if played to completion
			notifyStopped();
		}
		animationThread=null;
	}
	public void stop() {
		if (stopped) return;
		boolean waitToDie=(animationThread!=null);
		paused=false;
		stopped=true;
		if (!waitToDie) notifyStopped(); // thread not alive so cannot notify
		setElapsedTime(totalTime);
	}
	public boolean isStopped() {
		return stopped;
	}
	public boolean isPaused() {
		return paused;
	}
	public String getTimeDisplay(long elapsed,long totalTime) {
		return Long.toString(elapsed/1000)+"."+Long.toString((elapsed%1000)/100)+" / "+Long.toString(totalTime/1000)+"."+Long.toString((totalTime%1000)/100)+"s";
	}
	public void paint(Graphics g) {
		update(g);
	}
	public void update(Graphics g) {
		if (g==null) return;
		if (animationImage==null || animationImage.getWidth(this)!=getSize().width || animationImage.getHeight(this)!=getSize().height) {
			animationImage=createImage(getWidth(),getHeight());
			animationG=animationImage.getGraphics();
		}
		renderBackground(animationG);
		render(animationG,getFrameTime(frameNo));
		g.drawImage(animationImage,0,0,this);
	}
	protected void renderBackground(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0,0,animationImage.getWidth(this),animationImage.getHeight(this));
		g.setColor(getForeground());
	}
	protected void render(Graphics g,long time) {
		g.drawString(getTimeDisplay(time,getTotalTime()),100,100);
	}
/* for testing purposes
	public static void main(String[] args) {
		Frame frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		AnimationCanvas animation=new AnimationCanvas();
		animation.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				((AnimationCanvas)e.getSource()).pause();
			}
		});
		frame.add(animation);
		frame.setSize(300,300);
		frame.setVisible(true);
		animation.play();
	}
 */
} 
