package juggling;

public interface Rhythm {
	public boolean isBeat(int time);
	public int getTime(int beat);
	public int getBeatCount(int time);
}
