/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.metadata;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NXMetaDataProvider implements NexusTreeProvider, Map<String, String> {

	public NXMetaDataProvider() {
		super();
		reset();
	}

	@Override
	public INexusTree getNexusTree() {	
		INexusTree nexusTree = new NexusTreeNode("NXCollection", "NXCollection", null); 
		for( Entry<String, String> e : map.entrySet()){
			nexusTree.addChildNode(new NexusTreeNode(e.getKey(),"NX"+e.getKey(), nexusTree,new NexusGroupData(e.getValue())));
		}
		return nexusTree;
	}

	Map<String,String> map;
	
	public void reset(){
		map = new HashMap<String,String>();
	}
	public String listAsString() {
		// TODO Auto-generated method stub
		String total="";
		for( Entry<String, String> e : map.entrySet()){
			total += "," + e.getKey() + ":" + e.getValue();
		}
		return total;
	}

	public void add(String key, String value) {
		put(key, value);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public String get(Object key) {
		return map.get(key);
	}

	public String put(String key, String value) {
		return map.put(key, value);
	}

	public String remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Collection<String> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return map.entrySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

}
