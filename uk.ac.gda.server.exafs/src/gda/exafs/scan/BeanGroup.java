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

/**
 *
 */
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
	/**
	 * @return Returns the controller.
	 */
	public Object getController() {
		return controller;
	}
	/**
	 * @param controller The controller to set.
	 */
	public void setController(Object controller) {
		this.controller = controller;
	}
	/**
	 * @return Returns the scriptFolder.
	 */
	public String getXmlFolder() {
		return xmlFolder;
	}
	/**
	 * @param scriptFolder The scriptFolder to set.
	 */
	public void setXmlFolder(String scriptFolder) {
		this.xmlFolder = scriptFolder;
	}
	/**
	 * @return Returns the scannable.
	 */
	public Scannable getScannable() {
		return scannable;
	}
	/**
	 * @param scannable The scannable to set.
	 */
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}
	/**
	 * @return Returns the scan.
	 */
	public IScanParameters getScan() {
		return scan;
	}
	/**
	 * @param scan The scan to set.
	 */
	public void setScan(IScanParameters scan) {
		this.scan = scan;
	}
	/**
	 * @return Returns the sample.
	 */
	public ISampleParameters getSample() {
		return sample;
	}
	/**
	 * @param sample The sample to set.
	 */
	public void setSample(ISampleParameters sample) {
		this.sample = sample;
	}
	/**
	 * @return Returns the detector.
	 */
	public IDetectorParameters getDetector() {
		return detector;
	}
	/**
	 * @param detector The detector to set.
	 */
	public void setDetector(IDetectorParameters detector) {
		this.detector = detector;
	}
	/**
	 * @return Returns the output.
	 */
	public IOutputParameters getOutput() {
		return output;
	}
	/**
	 * @param output The output to set.
	 */
	public void setOutput(IOutputParameters output) {
		this.output = output;
	}
	/**
	 * @return Returns the experimentFolderName.
	 */
	public String getExperimentFolderName() {
		return experimentFolderName;
	}
	/**
	 * @param experimentFolderName The experimentFolderName to set.
	 */
	public void setExperimentFolderName(String experimentFolderName) {
		if (experimentFolderName==null) experimentFolderName = "_script";
		this.experimentFolderName = experimentFolderName;
	}
	/**
	 * @return Returns the scanNumber.
	 */
	public int getScanNumber() {
		return scanNumber;
	}
	/**
	 * @param scanNumber The scanNumber to set.
	 */
	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}
	/**
	 * @return Returns the incompleteDataAllowed.
	 */
	public boolean isIncompleteDataAllowed() {
		return incompleteDataAllowed;
	}
	/**
	 * @param incompleteDataAllowed The incompleteDataAllowed to set.
	 */
	public void setIncompleteDataAllowed(boolean incompleteDataAllowed) {
		this.incompleteDataAllowed = incompleteDataAllowed;
	}
	
	/**
	 * @return d
	 */
	public boolean isTimeEstimation() {
		return timeEstimation;
	}
	/**
	 * @param timeEstimation
	 */
	public void setTimeEstimation(boolean timeEstimation) {
		this.timeEstimation = timeEstimation;
	}
	/**
	 * @return Returns the validate.
	 */
	public boolean isValidate() {
		return validate;
	}
	/**
	 * @param validate The validate to set.
	 */
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
