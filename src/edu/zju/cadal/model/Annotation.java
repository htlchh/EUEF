package edu.zju.cadal.model;

import java.io.Serializable;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Annotation implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Mention m;
	private Entity e;
	private float score;
	
	public Annotation(Mention m, Entity e, float score)	{
		this.m = m;
		this.e = e;
		if (score < 0)
			throw new RuntimeException("The Confidence Score of an Annotation Would Never Be Negative.");
		this.score = score;
	}
	
	/**
	 * 默认的score是1.0f
	 * @param m
	 * @param e
	 */
	public Annotation(Mention m, Entity e) {
		this.m = m;
		this.e = e;
		this.score = 1.0f;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e == null) ? 0 : e.hashCode());
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
		Annotation other = (Annotation) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		if (m == null) {
			if (other.m != null)
				return false;
		} else if (!m.equals(other.m))
			return false;
		return true;
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
	
}
