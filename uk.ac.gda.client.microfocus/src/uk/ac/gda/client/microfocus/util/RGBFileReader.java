/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.io.FileUtils;

public class RGBFileReader {
	
	private static final Logger logger = LoggerFactory.getLogger(RGBFileReader.class);
	private String headerArray[];
	private int contents[][];

	public RGBFileReader (String filePath){
		try {
			List <String> fileContents = FileUtils.readFileAsList(new File(filePath));
			StringTokenizer header = new StringTokenizer(fileContents.get(0));
			headerArray= new String[header.countTokens()];	
			contents = new int[header.countTokens()][fileContents.size() -1];
			int index =0;
			while(header.hasMoreTokens()){
				headerArray[index++] = header.nextToken();				
			}
			StringTokenizer lineContent;
			for(int i =1; i< fileContents.size(); i++)
			{
				lineContent  = new StringTokenizer(fileContents.get(i));
				if(lineContent.countTokens() != contents.length)
				{
					logger.error("The file contents does not match with the number of columns");
					return;
				}
				for(int j =0 ; j< contents.length; j++)
				{					
					contents[j][i - 1] = Double.valueOf(lineContent.nextToken().trim()).intValue();
				}
				System.out.println("The number of elements are " + header);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String[] getHeader(){
		return headerArray;
	}
	
	public int[][] getContents()
	{
		return contents;
	}
	public static void main(String args[]){
		RGBFileReader file = new RGBFileReader("/home/nv23/workspaces/gdatrunk_sep09ws/config/users/data/2010/sp0/processing/17_1.rgb");
		System.out.println(file.getContents()[0][0]);
	}

}
