package juggling;

import java.io.*;

public class Pass {
	int toJuggler;
	private int beats;
	Pass rightHandPass=this;
	Pass leftHandPass=this;
	// normally a pass with an even number of beats goes from R to R or L to L
	// and a pass with an odd number of beats goes from R to L or L to R
	// if switched is true the opposite applies
	boolean switched;

	public static Pass NO_PASS=new Pass(0);

// this constructor for synchro patterns
	Pass(Pass rightHandPass,Pass leftHandPass) {
		this.rightHandPass=rightHandPass;
		this.leftHandPass=leftHandPass;
	}
	Pass(int beats) {
		this(-1,beats,false);
	}
	Pass(int beats,boolean switched) {
		this(-1,beats,switched);
	}
	Pass(int toJuggler,int beats) {
		this(toJuggler,beats,false);
	}
	Pass(int toJuggler,int beats,boolean switched) {
		this.toJuggler=toJuggler;
		this.beats=beats;
		this.switched=switched;
	}
	public Pass getRightHandPass() {
		return rightHandPass;
	}
	public Pass getLeftHandPass() {
		return leftHandPass;
	}
	public int getToJuggler() {
		return toJuggler;
	}
	public int getBeats() {
		return beats;
	}
	public boolean isSelf() {
		if (noPass()) return false;
		return (toJuggler==-1);
	}
	public boolean isRtoRorLtoL() {
		return ( (beats%2==0 && !switched) || (beats%2==1 && switched) );
	}
	public boolean isRtoLorLtoR() {
		return ( (beats%2==1 && !switched) || (beats%2==0 && switched) );
	}
	public boolean isSwitched() {
		return switched;
	}
	public boolean noPass() {
		return this.equals(NO_PASS);
	}
	public String getLabel() {
		if (noPass()) return "0";
		String label=Integer.toString(getBeats());
		if (toJuggler!=-1) label+='p';
		if (switched) label+='x';
		return label;
	}
	public boolean equals(Object o) {
		if (o instanceof Pass) {
			Pass pass=(Pass)o;
			return ( this.beats==pass.beats && this.switched==pass.switched && this.toJuggler==pass.toJuggler);
		}
		return false;
	}
	public String toString() {
		return getLabel();
	}
}
