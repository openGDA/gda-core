/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.util;

import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ObjectStateManager {

	private static Hashtable<Object,Boolean> objectTable = new Hashtable<Object, Boolean>();
	private static final Logger logger = LoggerFactory.getLogger(ObjectStateManager.class);
	public static void register(Object obj)
	{
		if(!objectTable.containsKey(obj))
			objectTable.put(obj, false);
	}
	public static void remove(Object obj)
	{
		if(objectTable.containsKey(obj))
			objectTable.remove(obj);
	}
	public static void setActive(Object obj)
	{
		Enumeration<Object> e = objectTable.keys();
		if(objectTable.containsKey(obj))
		{			
			while(e.hasMoreElements())
			{
				objectTable.put(e.nextElement(), false);
			}
			objectTable.put(obj, true);
			
		}
		e = objectTable.keys();
		while(e.hasMoreElements())
		{
			Object s = e.nextElement();
			logger.info("The object is " + s + " " + objectTable.get(s));
		}
	}
	public static void setInactive(Object obj)
	{
		if(obj != null && objectTable.containsKey(obj))
		{
			objectTable.put(obj, false);
			
		}
		
	}
	public static boolean isActive(Object obj)
	{
		if(obj != null && objectTable.containsKey(obj))
		{
			return objectTable.get(obj);
		}
		return false;
	}
	
	public static void main(String args[])
	{
		String s1 = new String ("String 1");
		ObjectStateManager.register(s1);
		ObjectStateManager.setActive(s1);
		String s2 = new String ("String 2");
		ObjectStateManager.register(s2);
		ObjectStateManager.setActive(s2);
	}
}
