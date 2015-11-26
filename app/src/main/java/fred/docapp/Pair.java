package fred.docapp;

public class Pair<E,F> {
    public E left;
    public F right;

    public Pair(E left, F right) {
	this.left = left;
	this.right = right;
    }

    public String toString() {
	return "<" + left.toString() + "," + right.toString() + ">";
    }
}
