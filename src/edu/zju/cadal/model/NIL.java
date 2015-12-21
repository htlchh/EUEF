package edu.zju.cadal.model;

import java.io.Serializable;

public class NIL implements Serializable, Model {

	private static final long serialVersionUID = 1L;

	private Mention m;
	private Entity e;
	private float score;
	
	/**
	 * Id of NIL is 0
	 * @param m
	 * @param score
	 */
	public NIL(Mention m, float score) {
		this.m = m;
		if (score < 0)
			throw new RuntimeException("The Confidence Score of an NIL Would Never Be Negative.");
		this.score = score;
		this.e = new Entity(0, "*null*");
	}
	
	public NIL(Mention m) {
		this.m = m;
		this.score = 1.0f;
		this.e = new Entity(0, "*null*");
	}

	public Mention getMention() {
		return m;
	}

	public Entity getEntity() {
		return e;
	}

	public float getScore() {
		return score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m == null) ? 0 : m.hashCode());
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
		NIL other = (NIL) obj;
		if (m == null) {
			if (other.m != null)
				return false;
		} else if (!m.equals(other.m))
			return false;
		return true;
	}

	@Override
	public int getLength() {
		return m.getLength();
	}
	
}
