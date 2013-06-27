package gopheratl.biolock.common;
/**
 * This should be in java already, but it's not...
 * hashable pair<> generic, for when you just need one.
 * 
 * Code taken from stackoverflow, submitted as answer by arturh
 * http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java
 * minor modifications by me
 * @author arturh
 *
 * @param <A>
 * @param <B>
 */
public final class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
    	this.first = first;
    	this.second = second;
    }

    public int hashCode() {
    	int hashFirst = first != null ? first.hashCode() : 0;
    	int hashSecond = second != null ? second.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
    	if (other instanceof Pair) {
    		Pair otherPair = (Pair) other;
    		return 
    		((  this.first == otherPair.first ||
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	this.second == otherPair.second ||
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) );
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
    	return first;
    }

    public B getSecond() {
    	return second;
    }
}