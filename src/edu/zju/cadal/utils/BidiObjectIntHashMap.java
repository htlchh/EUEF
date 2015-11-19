package edu.zju.cadal.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.Serializable;

public class BidiObjectIntHashMap<E> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Object2IntMap<E> o2i = new Object2IntOpenHashMap<E>();
	private Int2ObjectMap<E> i2o = new Int2ObjectOpenHashMap<E>();
	
	public boolean hasObject (E o){
		return o2i.containsKey(o);
	}

	public boolean hasInt (int n){
		return i2o.containsKey(n);
	}
	
	public E getByInt(int n){
		return i2o.get(n);
	}

	public int getByObject(E o){
		return o2i.get(o);
	}
	
	/**Use n==-1 to indicate a missing value.
	 * @param o an object
	 * @param n must be positive.
	 */
	public void put(E o, int n){
		if (n==-1)
			o2i.put(o, -1);
		else{
			o2i.put(o, n);
			i2o.put(n, o);
		}
	}
	

}
