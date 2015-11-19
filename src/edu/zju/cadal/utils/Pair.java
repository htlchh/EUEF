package edu.zju.cadal.utils;

import java.io.Serializable;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Pair<T1 extends Serializable, T2 extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public T1 first;
	public T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", this.first.toString(), this.second.toString());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

}
