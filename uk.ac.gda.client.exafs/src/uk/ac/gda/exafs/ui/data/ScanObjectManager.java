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

package uk.ac.gda.exafs.ui.data;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import uk.ac.gda.client.experimentdefinition.ExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

public final class ScanObjectManager extends ExperimentObjectManager implements IExperimentObjectManager {

	/**
	 * On I20, if the XES scan is an option, according to the extension
	 * registry, then it should be the only scan option and the appearance of
	 * the xml editors should be slightly different.
	 * <p>
	 * XES and non-XES scans should not be mixed when running a multi-scan as
	 * this is physically impossible.
	 * 
	 * @return true if XES scanning is an option, or false if not running in an
	 *         Equinox environment.
	 */
	public static boolean isXESOnlyMode() {

		return ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.XES_MODE_ENABLED);
	}

	public static void setXESOnlyMode(boolean onlyXESScans) {
		ExafsActivator.getDefault().getPreferenceStore().setValue(ExafsPreferenceConstants.XES_MODE_ENABLED, onlyXESScans);
	}

	@Override
	protected IExperimentObject createNewExperimentObject(String line) {
		final String[] items = line.split(" ");
		if (items.length > 5) {
			return createNewScanObject(items[0], items[1], items[2], items[3], items[4], Integer.parseInt(items[5]));
		}
		return createNewScanObject(items[0], items[1], items[2], items[3], items[4], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IExperimentObject> getExperimentObjectType() {
		return (Class<IExperimentObject>) ScanObject.class.asSubclass(IExperimentObject.class);
	}

	@Override
	public IExperimentObject createCopyOfExperiment(IExperimentObject original) throws CoreException {
		ScanObject origAsScanObj = (ScanObject) original;

		final String name = getUniqueName(original.getRunName());
		IFile scanFile = createCopy(origAsScanObj.getScanFile());
		IFile sampleFile = createCopy(origAsScanObj.getSampleFile());
		IFile detFile = createCopy(origAsScanObj.getDetectorFile());
		IFile outFile = createCopy(origAsScanObj.getOutputFile());
		return createNewScanObject(name, sampleFile.getName(), scanFile.getName(), detFile.getName(),
				outFile.getName(), original.getNumberRepetitions());

	}

	@Override
	public IExperimentObject cloneExperiment(IExperimentObject original) {
		ScanObject origAsScanObj = (ScanObject) original;
		return createNewScanObject(original.getRunName(), origAsScanObj.getSampleFileName(),
				origAsScanObj.getScanFileName(), origAsScanObj.getDetectorFileName(),
				origAsScanObj.getOutputFileName(), original.getNumberRepetitions());

	}

	private ScanObject createNewScanObject(String runName, String sampleFileName, String scanFileName,
			String detFileName, String outputFileName, int numRepetitions) {
		ScanObject newScan = new ScanObject();
		newScan.setRunName(runName);
		newScan.setRunFileManager(this);
		newScan.setScanFileName(scanFileName);
		newScan.setDetectorFileName(detFileName);
		newScan.setSampleFileName(sampleFileName);
		newScan.setOutputFileName(outputFileName);
		newScan.setNumberRepetitions(numRepetitions);
		return newScan;
	}

	@Override
	public String[] getOrderedColumnBeanTypes() {
		return new String[]{"Scan","Detector","Sample","Output"};
	}

}
