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

package gda.jython;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.IScanDataPoint;
import gda.scan.Scan;
import gda.scan.ScanDataPoint;
import gda.scan.ScanEvent;
import gda.scan.ScanInformation;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of interfaces usually provided by JythonServerFacade to be used when running tests outside of gda
 * This implementation is used if you set the property JythonServerFacade.dummy to true
 */
public class MockJythonServerFacade implements IDefaultScannableProvider, ICurrentScanInformationHolder,
		IJythonServerNotifer, IScanStatusHolder, ICommandRunner, ITerminalPrinter, ICurrentScanController,
		IJythonNamespace, IAuthorisationHolder, IScanDataPointProvider, IScriptController, ICommandAborter,
		IBatonStateProvider, JSFObserver, AliasedCommandProvider {

	private static final Logger logger = LoggerFactory.getLogger(MockJythonServerFacade.class);

	private volatile int scanStatus = Jython.IDLE;

	private HashMap<String, Object> hashTable = new HashMap<String, Object>();
	private int authorisationLevel = 0;
	private ObservableComponent scanDataPointObservers = new ObservableComponent();
	private ObservableComponent scanEventObervable = new ObservableComponent();
	private IScanDataPoint lastScanDataPoint = null;
	private String terminalOutput = "";
//	private String scanObserverName = "";
	private String evaluateCommandResult = "";
	private Scan currentScan = null;

	private ClientDetails[] others = new ClientDetails[] { new ClientDetails(1, "A.N. Other", "A.N. Other", "pc012345", 3,
			false, "0-0") };
	private ClientDetails myDetails;
	{
		// mock UserAuthentication.getUsername();
		// mock LibGdaCommon.getFullNameOfUser(username);
		final String username = "mockusername";
		final String fullName = "Mock FullNameOfUser";
		myDetails = new ClientDetails(0, username, fullName, "pc012345", 3, true, "0-0");
	}

	public String getTerminalOutput() {
		return terminalOutput;
	}

	public void setTerminalOutput(String terminalOutput) {
		this.terminalOutput = terminalOutput;
	}

	@Override
	public void runCommand(String command) {
		logger.info("MockJythonServerFacade - runCommand " + command);
	}

	@Override
	public void requestFinishEarly() {
	}

	@Override
	public boolean isFinishEarlyRequested() {
		return false;
	}

	@Override
	public void print(String text) {
		logger.info(text);
		terminalOutput += text + "\n";
	}

	public void setScanStatus(int newStatus) {
		scanStatus = newStatus;
	}

	@Override
	public int getScanStatus() {
		return scanStatus;
	}

	@Override
	public void pauseCurrentScan() {
	}

	@Override
	public void restartCurrentScan() {
	}

	@Override
	public void resumeCurrentScan() {
	}

	@Override
	public Object getFromJythonNamespace(String objectName) {
		return hashTable.get(objectName);
	}

	@Override
	public void placeInJythonNamespace(String objectName, Object obj) {
		hashTable.put(objectName, obj);
	}

	@Override
	public int getAuthorisationLevel() {
		return authorisationLevel;
	}

	@Override
	public int getAuthorisationLevelAtRegistration() {
		return authorisationLevel;
	}

	/**
	 * Allows tests to change authorisation level returned by getAuthorisationLevel
	 *
	 * @param authorisationLevel
	 */
	public void setAuthorisationLevel(int authorisationLevel) {
		this.authorisationLevel = authorisationLevel;
	}

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		scanDataPointObservers.addIObserver(anObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		scanDataPointObservers.deleteIObserver(anObserver);
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return lastScanDataPoint;
	}

	@Override
	public void setScriptStatus(int newStatus) {
		scanStatus = newStatus;
	}

	@Override
	public int getScriptStatus() {
		return scanStatus;
	}

	@Override
	public void abortCommands() {
		scanStatus = Jython.IDLE;
	}

	@Override
	public void beamlineHalt() {
		abortCommands();
	}

	@Override
	public void pauseCurrentScript() {
	}

	@Override
	public void resumeCurrentScript() {
	}

	@Override
	public void update(Object dataSource, Object data) {
		if (data instanceof IScanDataPoint) {
			lastScanDataPoint = (IScanDataPoint) data;
			scanDataPointObservers.notifyIObservers(dataSource, data);
		}
	}

	@Override
	public void addBatonChangedObserver(IObserver anObserver) {

	}

	@Override
	public boolean amIBatonHolder() {
		return true;
	}

	@Override
	public void assignBaton(int index) {
	}

	@Override
	public void deleteBatonChangedObserver(IObserver anObserver) {
	}

	@Override
	public ClientDetails getBatonHolder() {
		return myDetails;
	}

	@Override
	public ClientDetails[] getOtherClientInformation() {
		return others;
	}

	@Override
	public boolean isBatonHeld() {
		return true;
	}

	@Override
	public void returnBaton() {
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public String evaluateCommand(String command) {
		return evaluateCommandResult;
	}

	public void setEvaluateCommandResult(String evaluateCommandResult) {
		this.evaluateCommandResult = evaluateCommandResult;
	}

	@Override
	public void changeVisitID(String visitID) {
		myDetails.setVisitID(visitID);
	}

	@Override
	public void revertToOriginalUser() {
	}

	@Override
	public Vector<String> getAliasedCommands() {
		return new Vector<String>();
	}

	@Override
	public Vector<String> getAliasedVarargCommands() {
		return new Vector<String>();
	}

	@Override
	public ClientDetails getMyDetails() {
		return myDetails;
	}

	@Override
	public boolean requestBaton() {
		return true;
	}

	@Override
	public void sendMessage(String message) {
	}

	@Override
	public List<UserMessage> getMessageHistory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean switchUser(String username, String password) {
		return false;
	}

	@Override
	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException {
		SortedSet<String> set = new TreeSet<String>(hashTable.keySet());
		LinkedHashMap<String, Object> output = new LinkedHashMap<String, Object>();
		for (String objName : set) {
			output.put(objName, hashTable.get(objName));
		}
		return output;
	}

	@Override
	public CommandThreadEvent runScript(File script) {
		return null;
	}

	@Override
	public boolean runsource(String command) {
		return true;
	}

	@Override
	public String locateScript(String scriptToRun) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addScanEventObserver(IObserver anObserver) {
		scanEventObervable.addIObserver(anObserver);
	}

	@Override
	public void deleteScanEventObserver(IObserver anObserver) {
		scanEventObervable.deleteIObserver(anObserver);
	}

	@Override
	public void notifyServer(Object source, Object data) {
		if (data instanceof ScanEvent) {
			scanStatus = ((ScanEvent) data).getLatestStatus().asJython();
			scanEventObervable.notifyIObservers(source, data);
		} else if (data instanceof ScanDataPoint) {
			lastScanDataPoint = (ScanDataPoint) data;
			scanDataPointObservers.notifyIObservers(source, data);
		}
	}

	/**
	 * This should be used by tests to tell this class the current scan. In the real system the JythonServer would know
	 * what the current scan is and so would be able to provide information about it. In this mock, the unit tests need
	 * to tell this MockJythonServer what the current scan is.
	 */
	@Override
	public void setCurrentScan(Scan newScan) {
		currentScan = newScan;
	}

	@Override
	public ScanInformation getCurrentScanInformation() {
		return currentScan.getScanInformation();
	}

	@Override
	public Vector<Scannable> getDefaultScannables() {
		return new Vector<Scannable>();
	}

	public void setScanObserver(@SuppressWarnings("unused") String scanObserver) {
		// not used
//		this.scanObserverName = scanObserver;
	}
}