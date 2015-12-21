package edu.zju.cadal.model;

import java.io.Serializable;


public class Mention implements Serializable, Comparable<Mention>, Model {

	private static final long serialVersionUID = 1L;

	String surfaceForm;
	private int position;
	private int length;
	private float score;
	
	public Mention(String surfaceForm, int position, int length, float score) {
		this.surfaceForm = surfaceForm;
		if (position < 0)
			throw new RuntimeException("The Position of a Mention Would Never Be Negative.");
		this.position = position;
		
		if (length <= 0)
			throw new RuntimeException("The Length of a Mention Should Be Positive.");
		this.length = length;
		
		if (score < 0)
			throw new RuntimeException("The Confidence Score of a Mention Should Be Positive.");
		this.score = score;
	}
	
	/**
	 * The default score is 1.0
	 * @param position
	 * @param length
	 */
	public Mention(String surfaceForm, int position, int length) {
		this.surfaceForm = surfaceForm;
		if (position < 0)
			throw new RuntimeException("The Position of a Mention Would Never Be Negative.");		
		this.position = position;
		
		if (length <= 0)
			throw new RuntimeException("The Length of a Mention Should Be Positive.");
		this.length = length;
		this.score = 1.0f;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mention other = (Mention) obj;
		if (length != other.length)
			return false;
		if (position != other.position)
			return false;
		return true;
	}
	
	public boolean overlap(Mention m) {
		int p1 = this.getPosition();
		int l1 = this.getLength();
		int e1 = p1 + l1 - 1;
		int p2 = m.getPosition();
		int l2 = m.getLength();
		int e2 = p2 + l2 - 1;
		return (
				(p1 <= p2 && p2 <= e1) ||
				(p2 <= p1 && p1 <= e2) ||
				(p1 >= p2 && e1 <= e2) ||
				(p1 <= p2 && e1 >= e2)
				);		
	}	

	@Override
	public int compareTo(Mention o) {
		return this.getPosition() - o.getPosition();
	}

	@Override
	public String toString() {
		return String.format("<%s,%d,%d,%f>", this.surfaceForm, this.position, this.length, this.score);
	}
	
	public String getSurfaceForm() {
		return surfaceForm;
	}
	
	public int getPosition() {
		return position;
	}


	public int getLength() {
		return length;
	}

	public float getScore() {
		return score;
	}

	@Override
	public Mention getMention() {
		return this;
	}
	
}
