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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Functions to convert PyObjects that have been changed into strings back to 
 * something useful. If you have a Jython interpreter simply get it to do the job
 *
 */
public class PyStringToJava {
	/**
	 * @param input - string returned by Jython on converting a list of tuples of strings which
	 *                may each contain parenthesis and single and double quotes to a string
	 * @return  the string converted to a list of lists of strings.
	 */
	public static List< ? extends List<String> >  ListOfTuplesToJava(String input){
		ArrayList< ArrayList<String> > result = new ArrayList< ArrayList<String> >();
		if( input.length() < 2)
			return result;
		try{
			/* remove  square brackets at the start and end */
			String input1 = input.substring(1, input.length()-1);
			
			/* breakup in an list of strings surrounded by parenthesis
			 * Once inside a parenthesis we need to take account of being inside quotes
			 * e.g. ('kjflkadj','kjlk)'),('kjflkadj','kjlk)') should produce
			 * ('kjflkadj','kjlk)') and ('kjflkadj','kjlk)')
			 */
			ArrayList<String> tuple = new ArrayList<String>();
			{
				StringReader reader= new StringReader(input1);
				char ch;
				char EOF= (char)-1;
				char quotemark='\'';
				boolean insideQuotes=false;
				while( (ch = (char)reader.read()) != EOF){
					if( ch == '('){
						StringBuffer s = new StringBuffer();
						s.append(ch);
						while( (ch = (char)reader.read()) != EOF){
							s.append(ch);
							if( insideQuotes){
								/* look for escape character */
								if( ch == '\\'){
									/* read next and ignore */
									if( (ch = (char)reader.read()) != EOF){
										s.append(ch);
									}
								} else {
									/* look for quotemark */
									if( ch == quotemark){
										insideQuotes = false;
									}
								}
							} else {
								/* looking for first quote */
								if( ch == '\"' || ch == '\'' ){
									insideQuotes=true;
									quotemark = ch;
								} else if(ch == ')'){
									tuple.add(s.toString());
									break;
								}
							}
						}
						
					}
				}	
				
			}
			{
				/* for each tuple break up into individual strings */	
				for(String item : tuple ){
					ArrayList<String> tupleStrings = new ArrayList<String>();
					StringReader reader= new StringReader(item);
					char ch;
					char EOF= (char)-1;
					char quotemark='\'';
					boolean insideQuotes=false;
					StringBuffer s = new StringBuffer();
					while( (ch = (char)reader.read()) != EOF){
						if( insideQuotes){
							/* look for escape character */
							if( ch == '\\'){
								/* read next and add both if not the quotemark - else just the quote*/
								char nextch;
								if( (nextch = (char)reader.read()) != EOF){
									if( nextch != quotemark){
										s.append(ch);
									}
									s.append(nextch);
								}
							} else {
								/* look for quotemark */
								if( ch == quotemark){
									insideQuotes = false;
									tupleStrings.add(s.toString());
									s = new StringBuffer();
								} else {
									s.append(ch);
								}
							}
						} else {
							/* looking for first quote - ignore all else*/
							if( ch == '\"' || ch == '\'' ){
								insideQuotes=true;
								quotemark = ch;
							}
						}
					}
					result.add(tupleStrings);
				}
			}
		} catch (Exception e){
			//do nothing
		}
		return result;
	}
}
