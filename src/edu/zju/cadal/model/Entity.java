package edu.zju.cadal.model;

import java.io.Serializable;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Entity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//维基百科对应页面的id
	private int id; 
	//维基百科对应页面的标题
	private String title;
	
	public Entity(int id) {
		if (id < 0)
			throw new RuntimeException("The Id of an Entity Would Never Be Negative.");		
		this.id = id;
		this.title = "";
	}
	
	public Entity(int id, String title) {
		if (id < 0)
			throw new RuntimeException("The Id of an Entity Would Never Be Negative.");
		this.id = id;
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Entity other = (Entity) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	
}
