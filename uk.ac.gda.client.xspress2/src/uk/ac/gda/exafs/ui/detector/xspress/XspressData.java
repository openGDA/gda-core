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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.data.PathConstructor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.FileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.exafs.ui.detectorviews.Data;

public class XspressData extends Data{
	
	private static final Logger logger = LoggerFactory.getLogger(XspressData.class);
	
	protected void load(FileDialog openDialog, final int detectorListLength, final String filePath) {
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir += "processing";
		if (openDialog.getFilterPath() == null)
			openDialog.setFilterPath(dataDir);
		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(filePath));
						String line = reader.readLine();
						ArrayList<double[]> data = new ArrayList<double[]>();
						while (line != null) {
							StringTokenizer tokens = new StringTokenizer(line);
							double elementData[] = new double[tokens.countTokens()];
							for (int i = 0; i < elementData.length; i++)
								elementData[i] = Double.parseDouble(tokens.nextToken());
							data.add(elementData);
							line = reader.readLine();
						}
						int numberOfElements = data.size() / detectorListLength;
						double[][][] detectorData = new double[detectorListLength][numberOfElements][];
						int dataIndex = 0;
						for (int i = 0; i < detectorData.length; i++)
							for (int j = 0; j < numberOfElements; j++)
								detectorData[i][j] = data.get(dataIndex++);
						
					} catch (Exception e) {
						logger.warn("Exception while reading data from xspress parameters xml file", e);
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
								logger.warn("Exception while reading data from xspress parameters xml file", e);
							}
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

}