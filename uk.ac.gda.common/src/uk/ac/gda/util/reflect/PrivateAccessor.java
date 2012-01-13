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

package uk.ac.gda.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Provides access to private members in classes.
 */
public class PrivateAccessor {
	
	 /**
	 * @param o
	 * @param fieldName
	 * @return field
	 * @throws Exception
	 */
	public static Object getPrivateField (Object o, String fieldName) throws Exception {   
		
		  // Check we have valid arguments... 
	    assert(o!=null);
	    assert(fieldName!=null);
	    
	    // Go and find the private field... 
	    final Field fields[] = o.getClass().getDeclaredFields();
	    for (int i = 0; i < fields.length; ++i) {
	      if (fieldName.equals(fields[i].getName())) {
	          fields[i].setAccessible(true);
	          return fields[i].get(o);
	       }
	    }
	    return null;
	}
  
    /**
     * @param o
     * @param methodName
     * @param params
     * @return the method return object
     * @throws Exception
     */
    public static Object invokePrivateMethod (Object o, String methodName, Object... params) throws Exception {   
      
    	// Check we have valid arguments... 
	    assert(o!=null);
	    assert(methodName!=null);
	    assert(params!=null);
	    
	    // Go and find the private method... 
	    final Method methods[] = o.getClass().getDeclaredMethods();
	    for (int i = 0; i < methods.length; ++i) {
	      if (methodName.equals(methods[i].getName())) {
	          methods[i].setAccessible(true);
	          return methods[i].invoke(o, params);
	      }
	    }
	    return null;
	}  
}
