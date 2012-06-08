/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PackageMaker Class
 */
public class PackageMaker {
	private static final Logger logger = LoggerFactory.getLogger(PackageMaker.class);

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		String destDir = null;
		if (args.length == 0)
			return;
		String filename = args[0];
		if (args.length > 1)
			destDir = args[1];
		try {
			ClassParser cp = new ClassParser(filename);
			JavaClass classfile = cp.parse();
			String packageName = classfile.getPackageName();
			String apackageName = packageName.replace(".", File.separator);
			if (destDir != null)
				destDir = destDir + File.separator + apackageName;
			else
				destDir = apackageName;
			File destFile = new File(destDir);
			File existFile = new File(filename);
			File parentDir = new File(existFile.getAbsolutePath().substring(0,
					existFile.getAbsolutePath().lastIndexOf(File.separator)));
			String allFiles[] = parentDir.list();
			Vector<String> selectedFiles = new Vector<String>();
			String toMatch = existFile.getName().substring(0, existFile.getName().lastIndexOf("."));
			for (int i = 0; i < allFiles.length; i++) {
				if (allFiles[i].startsWith(toMatch + "$"))
					selectedFiles.add(allFiles[i]);
			}
			FileUtils.copyFileToDirectory(existFile, destFile);
			Object[] filestoCopy = selectedFiles.toArray();
			for (int i = 0; i < filestoCopy.length; i++) {
				FileUtils.copyFileToDirectory(new File((String) filestoCopy[i]), destFile);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints usage message
	 */
	public void printUsage() {
		logger.debug("gda.util.PackageMaker <classfileName.class>  <optional destinationDirectory>");
	}

}
