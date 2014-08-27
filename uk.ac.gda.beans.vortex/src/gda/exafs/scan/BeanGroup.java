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

package gda.exafs.scan;

import gda.device.Scannable;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

public class BeanGroup {
	private boolean   incompleteDataAllowed=false;
	private boolean   timeEstimation=false;
	private Object    controller;
	private String    xmlFolder;
	private Scannable scannable;
	private IScanParameters    scan;
	private ISampleParameters    sample;
	private IDetectorParameters    detector;
	private IOutputParameters    output;
	private String    experimentFolderName;
	private int       scanNumber;
	private boolean   validate=false;

	public Object getController() {
		return controller;
	}

	public void setController(Object controller) {
		this.controller = controller;
	}

	public String getXmlFolder() {
		return xmlFolder;
	}

	public void setXmlFolder(String scriptFolder) {
		xmlFolder = scriptFolder;
	}

	public Scannable getScannable() {
		return scannable;
	}

	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

	public IScanParameters getScan() {
		return scan;
	}

	public void setScan(IScanParameters scan) {
		this.scan = scan;
	}

	public ISampleParameters getSample() {
		return sample;
	}

	public void setSample(ISampleParameters sample) {
		this.sample = sample;
	}

	public IDetectorParameters getDetector() {
		return detector;
	}

	public void setDetector(IDetectorParameters detector) {
		this.detector = detector;
	}

	public IOutputParameters getOutput() {
		return output;
	}

	public void setOutput(IOutputParameters output) {
		this.output = output;
	}

	public String getExperimentFolderName() {
		return experimentFolderName;
	}

	public void setExperimentFolderName(String experimentFolderName) {
		if (experimentFolderName==null) experimentFolderName = "_script";
		this.experimentFolderName = experimentFolderName;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}

	public boolean isIncompleteDataAllowed() {
		return incompleteDataAllowed;
	}

	public void setIncompleteDataAllowed(boolean incompleteDataAllowed) {
		this.incompleteDataAllowed = incompleteDataAllowed;
	}
	

	public boolean isTimeEstimation() {
		return timeEstimation;
	}

	public void setTimeEstimation(boolean timeEstimation) {
		this.timeEstimation = timeEstimation;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	@Override
	public BeanGroup clone() {
		BeanGroup clone = new BeanGroup();
		
		clone.incompleteDataAllowed  = this.incompleteDataAllowed;
		clone.timeEstimation         = this.timeEstimation;
		clone.controller             = this.controller;
		clone.xmlFolder           	 = this.xmlFolder;
		clone.scannable              = this.scannable;
		clone.scan                   = this.scan;
		clone.sample                 = this.sample;
		clone.detector               = this.detector;
		clone.output                 = this.output;
		clone.experimentFolderName   = this.experimentFolderName;
		clone.scanNumber             = this.scanNumber;
		clone.validate               = this.validate;

		return clone;
	}
}
