package compiler.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiValueMap<K, V> {
	HashMap<K, LinkedList<V>> data = new HashMap<K, LinkedList<V>>();
	int size = 0;
	
	public MultiValueMap() {
		
	}
	
	public MultiValueMap(MultiValueMap<K,V> copy) {
		for (K key : copy.keySet()) {
			LinkedList<V> listCopy = new LinkedList<V>(copy.get(key));
			this.data.put(key, listCopy);
		}
		this.size = copy.size;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size != 0;
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(Object value) {
		for (LinkedList<V> list : data.values()) {
			if (list.contains(value))
				return true;
		}
		return false;
	}

	public List<V> get(Object key) {
		return data.get(key);
	}

	public V put(K key, V value) {
		if (containsKey(key)) {
			data.get(key).add(value);
		} else {
			LinkedList<V> list = new LinkedList<V>();
			list.add(value);
			data.put(key, list);
		}
		size ++;
		return null;
	}

	public List<V> remove(Object key) {
		List<V> ret = get(key);
		data.remove(key);
		if (ret != null)
			size -= ret.size();
		return ret;
	}

	public void clear() {
		data.clear();
		size = 0;
	}

	public Set<K> keySet() {
		return data.keySet();
	}
}
