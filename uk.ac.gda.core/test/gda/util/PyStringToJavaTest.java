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

package gda.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * class to test PyStringToJava
 */
public class PyStringToJavaTest {

	private void runtest(ArrayList< ArrayList<String> > expected){
		String inputStr="[";
		{
			boolean firstTuple=true;
			for( ArrayList<String> tuple : expected ){
				if(!firstTuple){
					inputStr += ", ";
				}
				inputStr += "(";
				boolean firstInTuple=true;
				for(String item : tuple ){
					if(!firstInTuple){
						inputStr += ", ";
					}
					/* whichever quote mark it finds first choose the other or else choose  single quote mark */
					if( item.indexOf('\"') >= 0 && item.indexOf('\"') < item.indexOf("\'")
							||  item.indexOf('\"') == -1 &&  item.indexOf('\'') >= 0){
						inputStr += "\"";
						inputStr += item.replace("\"", "\\\"");
						inputStr += "\"";
					} else {
						inputStr += "\'";
						inputStr += item.replace("\'", "\\\'");
						inputStr += "\'";
					}
					firstInTuple = false;
				}
				inputStr += ")";
				firstTuple = false;
			}
		}
		inputStr += "]";
		List< ? extends List<String> > actual = PyStringToJava.ListOfTuplesToJava(inputStr);
		Assert.assertEquals(expected, actual);
	}
	/**
	 * Test of strings with quotes and parenthesis
	 */
	@Test
	public void testListOfTuplesToJava() {
		ArrayList< ArrayList<String> > expected = new ArrayList< ArrayList<String> >();
		{
			{
				ArrayList<String> tuple = new ArrayList<String>();
				tuple.add("noquote\n123");
				tuple.add("singlequote'''ffasfs");
				tuple.add("doublequote"+ "\"" +"fdsfsff");
				expected.add(tuple);
			}
			{
				ArrayList<String> tuple = new ArrayList<String>();
				tuple.add("singlequote'''beforedouble"+ "\""+ "ffasfs");
				tuple.add("doublequote"+ "\"" +"beforesingle''ff");
				expected.add(tuple);
			}
		}
		runtest(expected);
	}

	/**
	 * Simple input test
	 */
	@Test
	public void testSimpleListOfTuplesToJava() {
		ArrayList< ArrayList<String> > expected = new ArrayList< ArrayList<String> >();
		{
			ArrayList<String> tuple = new ArrayList<String>();
			tuple.add("1");
			tuple.add("2");
			expected.add(tuple);
			expected.add(tuple);
		}
		runtest(expected);
	}

}
