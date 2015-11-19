package edu.zju.cadal.model;

import java.io.Serializable;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 * 
 */
public class Mention implements Serializable, Comparable<Mention> {

	private static final long serialVersionUID = 1L;

	//mention在文档中的起始位置
	private int position;
	//mention的长度
	private int length;
	//mention的置信分数值
	private float score;
	
	public Mention(int position, int length, float score) {
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
	 * 默认的score是1.0
	 * @param position
	 * @param length
	 */
	public Mention(int position, int length) {
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

	@Override
	public int compareTo(Mention o) {
		return this.getPosition() - o.getPosition();
	}

	@Override
	public String toString() {
		return String.format("<%d,%d,%f>", this.position, this.length, this.score);
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		if (position < 0)
			throw new RuntimeException("The Position of a Mention Would Never Be Negative.");
		this.position = position;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		if (length <= 0)
			throw new RuntimeException("The Length of a Mention Should Be Positive.");		
		this.length = length;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		if (score < 0)
			throw new RuntimeException("The Confidence Score of a Mention Should Be in [0, 1].");
		this.score = score;
	}
	
}
