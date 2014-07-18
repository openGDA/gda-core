/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.data;

import gda.factory.Findable;
import gda.util.OSCommandRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import uk.ac.gda.server.ncd.beans.StoredDetectorInfo;

public class ProcessingRunner implements Findable, Map<String, String> {

	private String name;
	private String executablePath;
	private Map<String, String> environment = new HashMap<String, String>();
	private StoredDetectorInfo detectorInfo;
	
	public StoredDetectorInfo getDetectorInfo() {
		return detectorInfo;
	}

	public void setDetectorInfo(StoredDetectorInfo detectorInfo) {
		this.detectorInfo = detectorInfo;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void triggerProcessing(String datafilepath, String backgroundfilepath, String dataCollectionId) throws IOException {
		if (executablePath == null)
			throw new IOException("executablePath not set");
		if (! new File(executablePath).canExecute())
			throw new IOException(executablePath + " not executable");
		if (datafilepath == null)
			throw new IllegalArgumentException("need datafilepath");
		if (backgroundfilepath == null)
			backgroundfilepath = "";
		if (detectorInfo != null) {
			environment.put("NCDREDXML", detectorInfo.getDataCalibrationReductionSetupPath());
			environment.put("PERSISTENCEFILE", detectorInfo.getSaxsDetectorInfoPath());
		}
		
		OSCommandRunner.runNoWait(Arrays.asList(new String[] {executablePath, datafilepath, backgroundfilepath, dataCollectionId}), OSCommandRunner.LOGOPTION.ONLY_ON_ERROR, "/dev/null", environment, new Vector<String>());
	}

		
	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	@Override
	public void clear() {
		environment.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return environment.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return environment.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return environment.entrySet();
	}

	@Override
	public String get(Object arg0) {
		return environment.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return environment.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return environment.keySet();
	}

	@Override
	public String put(String arg0, String arg1) {
		return environment.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> arg0) {
		environment.putAll(arg0);
	}

	@Override
	public String remove(Object arg0) {
		return environment.remove(arg0);
	}

	@Override
	public int size() {
		return environment.size();
	}

	@Override
	public Collection<String> values() {
		return environment.values();
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
}