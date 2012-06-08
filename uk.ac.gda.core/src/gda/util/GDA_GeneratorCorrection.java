/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class used to correct output from GDA_generator.py
 */
public class GDA_GeneratorCorrection {

	private static final Pattern DEVICE_REP = Pattern.compile("(.*\\>)(.*)\\.(.*)(\\<.*)");
	
	private static final Pattern NAME_REP   = Pattern.compile("(.*\\>)(.*)(\\<.*)");
	
	private static final String NAME_COL_REP         = (".*\\<name\\>.*\\<\\/name\\>.*");
	private static final String MOTOR_NAME_COL_REP   = (".*\\<motorName\\>.*\\<\\/motorName\\>.*");
	private static final String MOV_NAME_COL_REP     = (".*\\<moveableName\\>.*\\<\\/moveableName\\>.*");
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		final List<String> lines = new ArrayList<String>(89);
		final File       xmlFile = new File(args[0]);
		
		final BufferedReader r   = new BufferedReader(new FileReader(xmlFile));
		final Map<String,String> nameReplace = new HashMap<String,String>(31);
		
		try {
			
			String line = null;
			while ((line=r.readLine())!=null) {
				
				try {
					
					if ("".equals(line.trim())) {
						lines.add(line);
						continue;
					}

					final Matcher match = DEVICE_REP.matcher(line);
	 				if (match.matches()) {
	 					if (match.group(2).equals(match.group(3))) {
	 						String prevLine    = lines.get(lines.size()-1);
	 						final Matcher prev = NAME_REP.matcher(prevLine);
		                    if (prev.matches()) {
		 						final String origName = prev.group(2);
		 						final String newName  = origName.substring(match.group(2).length());
		 						nameReplace.put(origName, newName);
		 						final String newNameLine = (prev.group(1)+newName+prev.group(3));
		 						
		 						// Assign lines
		 						lines.set(lines.size()-1, newNameLine);
		 						line = match.group(1)+match.group(2)+match.group(4);
		                    }
	 					}
	 				}
				} finally {
					lines.add(line);
				}
			}
			
		} finally {
			r.close();
		}
		
		
		// Replace any of the long motor names
		for (int i = 0; i < lines.size(); i++) {

			String line = lines.get(i);
			
			// Warning pseudo n^2 but it does not matter.
			for (String longName : nameReplace.keySet()) {
				if (line.indexOf(longName)>-1) {
					line = line.replace(longName, nameReplace.get(longName));
					lines.set(i, line);
					continue;
				}
				final String posName = longName.replace("Motor", "Positioner");
				if (line.indexOf(posName)>-1) {
					line = line.replace(posName, nameReplace.get(longName).replace("Motor", "Positioner"));
					lines.set(i, line);
				}
			}
			
			if (line.matches(NAME_COL_REP)||line.matches(MOTOR_NAME_COL_REP)||line.matches(MOV_NAME_COL_REP)) {
				line = line.replace(':','_');
				lines.set(i, line);
			}

		}
		
		// Write the file back out
		final File out = new File(args[0]+"_corrected.xml");
		if (out.exists()) out.createNewFile();
		final BufferedWriter w = new BufferedWriter(new FileWriter(out));
		try {
			for (String line : lines) {
				w.write(line);
				w.newLine();
			}
		} finally {
			w.close();
		}
		System.out.println("Written "+out);
	}
	
}
