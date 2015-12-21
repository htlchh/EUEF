package edu.zju.cadal.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Candidate implements Serializable, Model {

	private static final long serialVersionUID = 1L;

	private Mention m;
	/**<entity,score> pairs embedded in candidates */
	private Set<Pair<Entity,Float>> pairSet;
	
	public Candidate(Mention m, Set<Pair<Entity, Float>> pairSet) {
		this.m = m;
		this.pairSet = pairSet;
	}
	
	/**
	 * In the hash step, it does not take pairSet into consideration,
	 * and is only based on the mention. 
	 * If two candidates have same mentions, their hash codes are same.
	 * */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m == null) ? 0 : m.hashCode());
		return result;
	}

	/**
	 * Two candidates are equal <=>
	 * The embedded mentions are matched &&
	 * There are at least one same entity in the pair sets.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Candidate other = (Candidate) obj;
		if (m == null) {
			if (other.m != null)
				return false;
		} else if (!m.equals(other.m))
			return false;
		Set<Entity> s1 = new HashSet<Entity>();
		Set<Entity> s2 = new HashSet<Entity>();
		for (Pair<Entity, Float> e : this.pairSet)
			s1.add(e.first);
		for (Pair<Entity, Float> e : other.pairSet)
			s2.add(e.first);
		s1.retainAll(s2);
		return s1.size() > 0 ? true : false;
	}

	public Mention getMention() {
		return m;
	}


	public Set<Pair<Entity, Float>> getPairSet() {
		return pairSet;
	}

	@Override
	public int getLength() {
		return m.getLength();
	}

	
}
