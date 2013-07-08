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

package gda.data.scan.datawriter.scannablewriter;

import gda.data.scan.datawriter.SelfCreatingLink;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple example of a scannable with one input or extra name that needs 
 * to record in a specific place
 */
public class SimpleSingleScannableWriter implements ScannableWriter {
	private static final Logger logger = LoggerFactory.getLogger(SimpleSingleScannableWriter.class);

	private String path;
	private String units;
	private Collection<String> prerequisiteScannableNames;
	
	private int levels = 0;
	
	@Override
	public Collection<String> getPrerequisiteScannableNames() {
		return prerequisiteScannableNames;
	}
	
	/**
	 * Set the file into the position to write the data
	 * 
	 * @param file
	 * @return name of trailing component 
	 * @throws NexusException
	 */
	protected String enterLocation(NeXusFileInterface file) throws NexusException {
		levels = 0;
		StringTokenizer st = new StringTokenizer(path, "/");
		while(st.hasMoreTokens()) {
			String[] split = st.nextToken().split(":");
			String name = split[0];
			if (split.length == 1) {
				// no class, write data
				return name;
			}
			String clazz = split[1];
			try {
				file.makegroup(name, clazz);
			} catch (NexusException ne) {
				// ignore, it might be there already
			}
			file.opengroup(name, clazz);
			levels++;
		}
		
		throw new IllegalArgumentException("configured path is not well formed (suspect no trailing component)");
	}
	
	protected void leaveLocation(NeXusFileInterface file) throws NexusException {
		for (int i = 0; i < levels; i++) {
			file.closegroup();
		}
	}
	
	protected int[] makedimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = -1;
		}
		return mdim;
	}

	protected int[] nulldimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 0;
		}
		return mdim;
	}
	
	protected int[] onedimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 1;
		}
		return mdim;
	}
	
	@Override
	public Collection<? extends SelfCreatingLink> makeScannable(NeXusFileInterface file, Scannable s, Object position,
			int[] dim) throws NexusException {
		Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();

		String name = enterLocation(file);
		
		file.makedata(name, NexusFile.NX_FLOAT64, dim.length, dim);
		file.opendata(name);
		
		String axislist = "1";
		for (int j = 2; j <= dim.length; j++) {
			axislist = axislist + String.format(",%d", j);
		}
		file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
		
		try {
			file.putslab(positionToWriteableSlab(position, s), nulldimfordim(dim), onedimfordim(dim));
		} catch (DeviceException e) {
			logger.error("error converting scannable data", e);
		}
		
		if (getUnits() != null && !getUnits().isEmpty())
			file.putattr("units", getUnits().getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);

		sclc.add(new SelfCreatingLink(file.getdataID()));
		file.closedata();

		leaveLocation(file);
		return sclc;
	}

	@Override
	public void writeScannable(NeXusFileInterface file, Scannable s, Object position, int[] start) throws NexusException {
		String name = enterLocation(file);
		
		file.opendata(name);
		try {
			file.putslab(positionToWriteableSlab(position, s), start, onedimfordim(start));
		} catch (DeviceException e) {
			logger.error("error converting scannable data", e);
		}
		file.closedata();

		leaveLocation(file);
	}

	protected Object positionToWriteableSlab(Object position, Scannable s) throws DeviceException {
		return ScannableUtils.positionToArray(position, s);
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void setPrerequisiteScannableNames(Collection<String> prerequisiteScannableNames) {
		this.prerequisiteScannableNames = prerequisiteScannableNames;
	}
}