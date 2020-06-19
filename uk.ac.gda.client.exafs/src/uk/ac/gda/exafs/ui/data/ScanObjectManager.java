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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ScanObject;
import gda.exafs.scan.ScanStartedMessage;
import gda.exafs.scan.ScanType;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.scriptcontroller.logging.ILoggingScriptController;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.preferences.PreferenceConstants;

public final class ScanObjectManager extends ExperimentObjectManager implements IExperimentObjectManager, IObserver {
	private static IScanParameters currentScan;
	private static IDetectorParameters currentDetectorParameters;
	private static LoggingScriptController messageController;
	private static final Logger logger = LoggerFactory.getLogger(ScanObjectManager.class);
	private static final IEclipsePreferences serverPrefs = InstanceScope.INSTANCE.getNode("uk.ac.gda.server.exafs");
	private static final String[] DEFAULT_SCAN_TAB_ORDER = { "Scan", "Detector", "Sample", "Output" };
	private static final String DEFAULT_SELECTED_SCAN_TAB = "Scan";

	public ScanObjectManager() {
		String controllers = GDAClientActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.GDA_LOGGINGSCRIPTCONTROLLERS);
		String[] controllerNames = controllers.split(",");

		for (String name : controllerNames) {
			Findable objRef = Finder.find(name.trim());
			if (objRef instanceof ILoggingScriptController) {
				ILoggingScriptController newcontroller = (ILoggingScriptController) objRef;
				try {
					newcontroller.addIObserver(this);
				} catch (Exception e) {
					logger.error("Error adding Observer to LoggingScriptController", e);
				}
			}
		}

		setInstancePreferences();
	}

	/**
	 *  Set the instance scope scan preferences from values set at client startup plugin_customization.ini
	 */
	private void setInstancePreferences() {
		Map<String, ScanType> prefStrings = new HashMap<>();
		prefStrings.put(ExafsPreferenceConstants.XES_MODE_ENABLED, ScanType.XES_ONLY);
		prefStrings.put(ExafsPreferenceConstants.QEXAFS_IS_DEFAULT_SCAN_TYPE, ScanType.QEXAFS_DEFAULT);
		prefStrings.put(ExafsPreferenceConstants.XANES_IS_DEFAULT_SCAN_TYPE, ScanType.XANES_DEFAULT);
		for(Entry<String, ScanType> entry : prefStrings.entrySet()) {
			boolean isSelected = ExafsActivator.getDefault().getPreferenceStore().getBoolean(entry.getKey());
			serverPrefs.putBoolean(entry.getValue().toString(), isSelected);
		}
	}

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
		return ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.XES_MODE_ENABLED);
	}

	public static void setXESOnlyMode(boolean onlyXESScans) {
		ExafsActivator.getDefault().getPreferenceStore().setValue(ExafsPreferenceConstants.XES_MODE_ENABLED, onlyXESScans);
		serverPrefs.putBoolean(ScanType.XES_ONLY.toString(), onlyXESScans);
	}

	public static boolean isQEXAFSDefaultScanType() {
		return ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.QEXAFS_IS_DEFAULT_SCAN_TYPE);
	}

	public static void setQEXAFSDefaultScanType(boolean qexafsIsDefault) {
		ExafsActivator.getDefault().getPreferenceStore().setValue(ExafsPreferenceConstants.QEXAFS_IS_DEFAULT_SCAN_TYPE, qexafsIsDefault);
		serverPrefs.putBoolean(ScanType.QEXAFS_DEFAULT.toString(), qexafsIsDefault);
	}

	public static boolean isXANESDefaultScanType() {
		return ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.XANES_IS_DEFAULT_SCAN_TYPE);
	}

	public static void setXANESDefaultScanType(boolean xanesIsDefault) {
		ExafsActivator.getDefault().getPreferenceStore().setValue(ExafsPreferenceConstants.XANES_IS_DEFAULT_SCAN_TYPE, xanesIsDefault);
		serverPrefs.putBoolean(ScanType.XANES_DEFAULT.toString(), xanesIsDefault);
	}

	/**
	 * Based on information returned from the specific Script Controller, returns the ScanObject currently in progress.
	 * <p>
	 * For parts of the UI whose display varies based on what is running.
	 *
	 * @return null if no scan running
	 */
	public static IScanParameters getCurrentScan() {
		return currentScan;
	}

	public static IDetectorParameters getCurrentDetectorParameters() {
		return currentDetectorParameters;
	}

	public LoggingScriptController getMessageController() {
		return messageController;
	}

	public void setMessageController(LoggingScriptController messageController) {
		ScanObjectManager.messageController = messageController;
		messageController.addIObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScanStartedMessage){
			currentScan = ((ScanStartedMessage)arg).getStartedScan();
			currentDetectorParameters = ((ScanStartedMessage)arg).getDetectorParams();
		}
	}

	@Override
	protected IExperimentObject createNewExperimentObject(String line) {
		final String[] items = line.split(" ");
		if (items.length > 5)
			return createNewScanObject(items[0], items[1], items[2], items[3], items[4], Integer.parseInt(items[5]));
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
		String name = getUniqueName(original.getRunName());
		IFile scanFile = createCopy(origAsScanObj.getScanFile());
		IFile sampleFile = createCopy(origAsScanObj.getSampleFile());
		IFile detFile = createCopy(origAsScanObj.getDetectorFile());
		IFile outFile = createCopy(origAsScanObj.getOutputFile());
		return createNewScanObject(name, sampleFile.getName(), scanFile.getName(), detFile.getName(), outFile.getName(), original.getNumberRepetitions());
	}

	@Override
	public IExperimentObject cloneExperiment(IExperimentObject original) {
		ScanObject origAsScanObj = (ScanObject) original;
		return createNewScanObject(original.getRunName(), origAsScanObj.getSampleFileName(),origAsScanObj.getScanFileName(), origAsScanObj.getDetectorFileName(), origAsScanObj.getOutputFileName(), original.getNumberRepetitions());

	}

	private ScanObject createNewScanObject(String runName, String sampleFileName, String scanFileName, String detFileName, String outputFileName, int numRepetitions) {
		ScanObject newScan = new ScanObject();
		newScan.setRunName(runName);
		newScan.setMultiScanName(this.getName());
		newScan.setFolder(getContainingFolder());
		newScan.setScanFileName(scanFileName);
		newScan.setDetectorFileName(detFileName);
		newScan.setSampleFileName(sampleFileName);
		newScan.setOutputFileName(outputFileName);
		newScan.setNumberRepetitions(numRepetitions);
		return newScan;
	}

	@Override
	public String[] getOrderedColumnBeanTypes() {
		String orderFromPref = ExafsActivator.getDefault().getPreferenceStore().getString(ExafsPreferenceConstants.SCAN_TAB_ORDER);
		if (orderFromPref != null && !orderFromPref.isEmpty()) {
			return orderFromPref.trim().split("\\W+");
		} else {
			return DEFAULT_SCAN_TAB_ORDER;
		}
	}

	@Override
	public int getDefaultSelectedColumnIndex() {
		String selectedTabName = ExafsActivator.getDefault().getPreferenceStore().getString(ExafsPreferenceConstants.SELECTED_SCAN_TAB);
		if (selectedTabName == null || selectedTabName.isEmpty()) {
			selectedTabName = DEFAULT_SELECTED_SCAN_TAB;
		}

		int selectedTabIndex = Arrays.asList(getOrderedColumnBeanTypes()).indexOf(selectedTabName);
		if (selectedTabIndex < 0) {
			selectedTabIndex = 0;
		}
		return selectedTabIndex;
	}
}