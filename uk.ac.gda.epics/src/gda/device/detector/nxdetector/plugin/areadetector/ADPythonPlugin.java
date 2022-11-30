/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDPython;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.scan.ScanInformation;

public class ADPythonPlugin implements NXPluginBase, InitializingBean {

	private NDPython ndPython;

	private Map<String, Object> parameterValues;

	private String pythonFile;

	private String pythonClass;

	private String inputPort;

	public NDPython getNdPython() {
		return ndPython;
	}

	public void setNdPython(NDPython ndPython) {
		this.ndPython = ndPython;
	}

	public String getPythonFile() {
		return pythonFile;
	}

	public void setPythonFile(String pythonFile) {
		this.pythonFile = pythonFile;
	}

	public String getPythonClass() {
		return pythonClass;
	}

	public void setPythonClass(String pythonClass) {
		this.pythonClass = pythonClass;
	}

	public Map<String, Object> getParameterValues() {
		return parameterValues;
	}

	public void setParameterValues(Map<String, Object> parameterValues) {
		this.parameterValues = parameterValues;
	}

	public String getInputPort() {
		return inputPort;
	}

	public void setInputPort(String inputPort) {
		this.inputPort = inputPort;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getNdPython() == null) {
			throw new RuntimeException("NdPython must be set");
		}
		if ("".equals(getPythonFile())) {
			throw new RuntimeException("Python file must be set");
		}
	}

	@Override
	public String getName() {
		return "py";
	}

	@Override
	public boolean willRequireCallbacks() {
		return true;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		NDPython nPy = getNdPython();
		String port = getInputPort();
		if (port != null) {
			nPy.getPluginBase().setNDArrayPort(port);
		}
		nPy.getPluginBase().enableCallbacks();
		nPy.setFilename(getPythonFile());
		nPy.setClassname(getPythonClass());
		nPy.readFile();
		if (nPy.getStatus() != 0) {
			throw new DeviceException("ADPythonPlugin errored reading python file " + getPythonFile());
		}
		for (Map.Entry<String, Object> p : getParameterValues().entrySet()) {
			nPy.putParam(p.getKey(), p.getValue());
		}
		if (nPy.getStatus() != 0) {
			throw new DeviceException("ADPythonPlugin errored putting to parameters");
		}
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

}
