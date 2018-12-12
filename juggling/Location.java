package juggling;

public class Location {
	private int x,y,orientation;
	
	Location() {
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getOrientation() {
		return orientation;
	}
	public void setPosition(int x,int y) {
		this.x=x;
		this.y=y;
	}
	public void setOrientation(int degrees) {
		this.orientation=degrees;
	}
}
