package juggling;

public class Ball {

	public static Ball NO_BALL=new Ball(-1);

	private int number;

	Ball(int number) {
		this.number=number;
	}

	public int getNumber() {
		return number;
	}

	public boolean noBall() {
		return number==-1;
	}
	public boolean equals(Object o) {
		if (o instanceof Ball) {
			return ((Ball)o).number==number;
		}
		return false;
	}
	public String toString() {
		return Integer.toString(number);
	}
}
