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
public class Candidate implements Serializable {

	private static final long serialVersionUID = 1L;

	//内嵌的Mention
	private Mention m;
	//集合的每个元素都是一个entity和score的pair
	private Set<Pair<Entity,Float>> pairSet;
	
	public Candidate(Mention m, Set<Pair<Entity, Float>> pairSet) {
		this.m = m;
		this.pairSet = pairSet;
	}
	
	/**
	 * 进行hash不使用pairSet，仅仅依赖于mention，如果依赖pairSet，就会间接依赖Pair的第二项分数值
	 * 
	 * 一般情况下一个mention对应一个candidate
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m == null) ? 0 : m.hashCode());
		return result;
	}

	/**
	 * 两个candidate匹配当且仅当两个mention匹配，
	 * 
	 * 且对应的Pair集合中有至少一个相同的entity，
	 * 
	 * 忽略entity对应的分数，在进行candidate比较时，只考虑entity，不考虑分数
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

	public void setMention(Mention m) {
		this.m = m;
	}

	public Set<Pair<Entity, Float>> getPairSet() {
		return pairSet;
	}

	public void setPairSet(Set<Pair<Entity, Float>> pairSet) {
		this.pairSet = pairSet;
	}
	
}
