package juggling;

public class SimpleRhythm implements Rhythm {
	int intro;
	int beats;

	public SimpleRhythm(int intro,int beats) {
		this.intro=intro;
		this.beats=beats;
	}
	// copy constructor
	public SimpleRhythm(SimpleRhythm rhythm) {
		this.intro=rhythm.intro;
		this.beats=rhythm.beats;
	}
	public boolean isBeat(int time) {
		return (time>=intro && (time-intro)%beats==0);
	}
	public int getBeatCount(int time) {
		if (time<intro) return 0;
		return 1+((time-intro)/beats);
	}
	public int getTime(int beat) {
		if (beat<1) return -1;
		return intro+(beat-1)*beats;
	}
	public int getIntro() {
		return intro;
	}
	public int getBeats() {
		return beats;
	}
	public String toString() {
		return Integer.toString(intro)+" "+Integer.toString(beats);
	}
	public boolean equals(Object o) {
		if (o instanceof SimpleRhythm) {
			return (((SimpleRhythm)o).beats==beats && ((SimpleRhythm)o).intro==intro);
		}
		return false;
	}
}
