package juggling;

import java.util.*;

public class RhythmSequence implements Rhythm {
	int intro;
	List sequences=new LinkedList();
	
	public class SequenceItem {
		SequenceItem(int beats,int repeats) {
			this.beats=beats;
			this.repeats=repeats;
		}
		int beats;
		int repeats;
		public int getBeats() {
			return beats;
		}
		public int getRepeats() {
			return repeats;
		}
	}
	public RhythmSequence(int beats) {
//	public void setIntro(int beats) {
		intro=beats;
	}
	// copy constructor
	public RhythmSequence(RhythmSequence rhythm) {
		intro=rhythm.intro;
		Iterator it=rhythm.getSequences();
		while (it.hasNext()) {
			SequenceItem item=(SequenceItem)it.next();
			addSequence(item.beats,item.repeats);
		}
	}
	public void setIntro(int intro) {
		this.intro=intro;
	}
	public int getIntro() {
		return intro;
	}
	public void addSequence(int beats,int repeats) {
		sequences.add(new SequenceItem(beats,repeats));
	}
	public Iterator getSequences() {
		return sequences.iterator();
	}
//	public SequenceItem getSequence(int number) {
//		return (SequenceItem)sequences.get(number);
//	}
	public int getSequenceCount() {
		return sequences.size();
	}
	public void removeSequence(int number) {
		sequences.remove(number);
	}
	public boolean isBeat(int time) {
		if (time<intro) return false;
		time-=intro;
		while (true) {
			Iterator it=sequences.iterator();
			while (it.hasNext()) {
				SequenceItem item=(SequenceItem)it.next();
				int length=item.beats*item.repeats;
				if (time<=length) return (time%item.beats==0);
				time-=length;
			}
		}
	}
	public int getBeatCount(int time) {
		if (time<intro) return 0;
		int count=0;
		time-=intro;
		while (true) {
			Iterator it=sequences.iterator();
			while (it.hasNext()) {
				SequenceItem item=(SequenceItem)it.next();
				int length=item.beats*item.repeats;
				if (time<length) {
					return count+1+time/item.beats;
				} else {
					count+=item.repeats;
				}
				time-=length;
			}
		}
	}
	public int getTime(int beat) {
		if (beat<1) return -1;
		int count=0;
		int time=intro;
		while (true) {
			Iterator it=sequences.iterator();
			while (it.hasNext()) {
				SequenceItem item=(SequenceItem)it.next();
				if (beat<=(count+item.repeats)) {
					time+=(beat-count-1)*item.beats;
					return time;
				}
				count+=item.repeats;
				time+=item.repeats*item.beats;
			}
		}
	}
	public int getBaseBeat(int time) {
		if (time<intro) return 0;
		time-=intro;
		Iterator it=sequences.iterator();
		while (time>=0) {
			while (it.hasNext()) {
				SequenceItem item=(SequenceItem)it.next();
				int length=item.beats*item.repeats;
				if (time<length) return item.beats;
				time-=length;
			}
			it=sequences.iterator(); // repeat
		}
		return 0;
	}
	public String toString() {
		StringBuffer buffer=new StringBuffer("");
		buffer.append(Integer.toString(intro));
		Iterator it=sequences.iterator();
		while (it.hasNext()) {
			buffer.append(" ");
			SequenceItem item=(SequenceItem)it.next();
			buffer.append(Integer.toString(item.beats)+"#"+Integer.toString(item.repeats));
		}
		return buffer.toString();
	}
}
