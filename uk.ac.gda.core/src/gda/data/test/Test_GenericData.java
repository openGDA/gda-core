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

package gda.data.test;

import gda.data.DataManagerInterface;
import gda.data.generic.DataManager;
import gda.data.generic.GenericData;
import gda.data.generic.IGenericData;
import gda.data.srs.SrsBuffer;
import gda.data.srs.SrsFile;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>
 * </p>
 * <p>
 * <b>Description: </b>
 * </p>
 */

public class Test_GenericData {
	private static final Logger logger = LoggerFactory.getLogger(Test_GenericData.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("Program starting...");

		// GenericData<String, Object> data = new GenericData<String, Object>();
		GenericData data = new GenericData();

		Vector<String> al = new Vector<String>();
		al.add("1");
		al.add("2");
		al.add("6");
		al.add("8");

		Vector<String> al2 = new Vector<String>();
		al2.add("14");
		al2.add("24");
		al2.add("64");
		al2.add("84");

		Vector<String> al3 = new Vector<String>();
		al3.add("18");
		al3.add("28");
		al3.add("68");
		al3.add("88");

		data.put("Header", "Hello");
		data.put("mylist", al);
		data.put("mylist2", al2);
		data.put("mylist3", al3);

		logger.debug("Keys: " + data.keySet());
		logger.debug("Values:" + data.values());
		logger.debug("data: " + data);

		// Message.debug("data1 type: " + data.get("data1").getClass());
		// int[] x = (int[]) data.get("data1");
		// Message.debug("x: " + x[0]);

		logger.debug("\n****\n");

		// Vector<String> p = data.print();
		// for (String i : p) {
		// Message.debug(i);
		// }

		// Message.debug("data.items() : " + data.items());

		logger.debug("\nNow using DataManager.\n");

		DataManagerInterface dataMan = new DataManager();
		dataMan.add("scan1", data);
		dataMan.add("scan2", data);

		logger.debug(dataMan.list().toString());

		logger.debug("dataMan.get(\"scan2\").get(\"data\"): " + dataMan.get("scan2").get("pressure"));
		dataMan.remove("scan1");
		logger.debug(dataMan.list().toString());

		logger.debug("\nGetting and writing an SRS buffer/file:");
		// Message.debug("data: " + data.print());
		logger.debug("data: " + data.toString());
		SrsFile file = new SrsFile();
		try {
			file.writeFile(new SrsBuffer(data), "C:\\my_new_file.srs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now try to read the file into a GenericData.
		SrsBuffer newBuf = null;
		try {
			newBuf = file.readFile("C:\\my_new_file.srs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (newBuf != null) {
			IGenericData newData = newBuf.getGenericData();
			logger.debug("Printing out data from new GenericData (read from file):");
			logger.debug("newData: " + newData.print());
			// Message.debug("data: " + data.toString());
		}

		logger.debug("\nFinished.");

	}

}
