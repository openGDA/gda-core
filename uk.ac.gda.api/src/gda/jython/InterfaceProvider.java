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

package gda.jython;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.commandinfo.ICommandThreadInfoProvider;
import gda.jython.completion.TextCompleter;

/**
 * Static methods to get current implementation for various interfaces supported by JythonServerFacade and JythonServer
 * that are used by other classes in GDA. 
 * <p>
 * Provided to ensure loose coupling between callers and command runner
 * implementation. Allows the use of mock versions of the interfaces to allow stand alone testing
 */
public class InterfaceProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(InterfaceProvider.class);

	static private ICommandRunner commandRunner;
	static private ICurrentScanController currentScanController;
	static private ITerminalPrinter terminalPrinter;
	static private IScanStatusHolder scanStatusHolder;
	static private IScriptController scriptController;
	static private IJythonNamespace jythonNamespace;
	static private IAuthorisationHolder authorisationHolder;
	static private ICommandAborter commandAborter;
	static private ICurrentScanInformationHolder currentScanHolder;
	static private IJythonServerNotifer jythonServerNotifer;
	static private IDefaultScannableProvider defaultScannableProvider;
	static private IBatonStateProvider batonStateProvider;
	static private JSFObserver jSFObserver;
	static private IScanDataPointProvider scanDataPointProvider;
	static private AliasedCommandProvider aliasedCommandProvider;
	static private ICommandThreadInfoProvider commandInfoProvider;
	private static IJythonServerStatusProvider jythonServerStatusProvider;
	private static TextCompleter jythonCompleter;

	/**
	 * @return current selected implementation of ICommandRunner
	 */
	public static AliasedCommandProvider getAliasedCommandProvider() {
		if (aliasedCommandProvider == null) {
			aliasedCommandProvider = JythonServerFacade.getInstance();
		}
		return aliasedCommandProvider;
	}

	
	/**
	 * @return current selected implementation of ICommandRunner
	 */
	public static ICommandRunner getCommandRunner() {
		if (commandRunner == null) {
			commandRunner = JythonServerFacade.getInstance();
		}
		return commandRunner;
	}

	/**
	 * @return current selected implementation of ICommandRunner
	 */
	public static ICommandThreadInfoProvider getCommandThreadInfoProvider() {
		if (commandInfoProvider == null) {
			commandInfoProvider = JythonServerFacade.getInstance();
		}
		return commandInfoProvider;
	}

	/**
	 * @return current selected implementation of ICurrentScanController
	 */
	public static ICurrentScanController getCurrentScanController() {
		if (currentScanController == null) {
			currentScanController = JythonServerFacade.getInstance();
		}
		return currentScanController;
	}

	/**
	 * @return current selected implementation of ITerminalPrinter
	 */
	public static ITerminalPrinter getTerminalPrinter() {
		if (terminalPrinter == null) {
			terminalPrinter = JythonServerFacade.getInstance();
		}
		return terminalPrinter;
	}

	/**
	 * @return current selected implementation of IScanStatusHolder
	 */
	public static IScanStatusHolder getScanStatusHolder() {
		if (scanStatusHolder == null) {
			scanStatusHolder = JythonServerFacade.getInstance();
		}
		return scanStatusHolder;

	}

	/**
	 * @return current selected implementation of IScriptStatusHolder
	 */
	public static IScriptController getScriptController() {
		if (scriptController == null) {
			scriptController = JythonServerFacade.getInstance();
		}
		return scriptController;
	}	

	/**
	 * @return current selected implementation of IScriptStatusHolder
	 */
	public static ICommandAborter getCommandAborter() {
		if (commandAborter == null) {
			commandAborter = JythonServerFacade.getInstance();
		}
		return commandAborter;
	}		
	/**
	 * @return current selected implementation of IJythonNamespace
	 */
	public static IJythonNamespace getJythonNamespace() {
		if (jythonNamespace == null) {
			jythonNamespace = JythonServerFacade.getInstance();
		}
		return jythonNamespace;
	}

	/**
	 * IMPORTANT: this method is for scans which operate local to the Command Server
	 * 
	 * @return JythonServer
	 */
	private static LocalJython getLocalJythonServerFromFinder() {
		Findable obj = Finder.getInstance().find(Jython.SERVER_NAME);
		if (obj != null && obj instanceof LocalJython) {
			return (LocalJython) obj;
		}
		throw new IllegalStateException("Unable to find local object of type LocalJython called " + Jython.SERVER_NAME);
	}

	/**
	 * @return current selected implementation of ICurrentScanInformation
	 */
	public static ICurrentScanInformationHolder getCurrentScanInformationHolder() {
		if (currentScanHolder == null) {
			currentScanHolder = getLocalJythonServerFromFinder();
		}
		return currentScanHolder;
	}

	/**
	 * @return current selected implementation of IJythonServerNotifer
	 */
	public static IJythonServerNotifer getJythonServerNotifer() {
		if (jythonServerNotifer == null) {
			jythonServerNotifer = getLocalJythonServerFromFinder();
		}
		return jythonServerNotifer;
	}

	/**
	 * @return current selected implementation of IDefaultScannableProvider
	 */
	public static IDefaultScannableProvider getDefaultScannableProvider() {
		if (defaultScannableProvider == null) {
			defaultScannableProvider = getLocalJythonServerFromFinder();
		}
		return defaultScannableProvider;
	}

	/**
	 * @return current selected implementation of IAuthorisationHolder
	 */
	public static IAuthorisationHolder getAuthorisationHolder() {
		if (authorisationHolder == null) {
			authorisationHolder = JythonServerFacade.getInstance();
		}
		return authorisationHolder;
	}
	

	/**
	 * @return current selected implementation of IScanDataPointProvider
	 */
	public static IScanDataPointProvider getScanDataPointProvider() {
		if (scanDataPointProvider == null) {
			scanDataPointProvider = JythonServerFacade.getInstance();
		}
		return scanDataPointProvider;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param jSFObserver
	 */
	public static void setJSFObserverForTesting(JSFObserver jSFObserver) {
		logger.warn("setJSFObserverForTesting called");
		InterfaceProvider.jSFObserver = jSFObserver;
	}
	
	/**
	 * @return current selected implementation of JSFObserver
	 */
	public static JSFObserver getJSFObserver() {
		if (jSFObserver == null) {
			jSFObserver = JythonServerFacade.getInstance();
		}
		return jSFObserver;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param scanDataPointProvider
	 */
	public static void setScanDataPointProviderForTesting(IScanDataPointProvider scanDataPointProvider) {
		logger.warn("setScanDataPointProviderForTesting called");
		InterfaceProvider.scanDataPointProvider = scanDataPointProvider;
	}
	
	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param commandRunner
	 */
	public static void setCommandRunnerForTesting(ICommandRunner commandRunner) {
		logger.warn("setCommandRunnerForTesting called");
		InterfaceProvider.commandRunner = commandRunner;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param currentScanController
	 */
	public static void setCurrentScanControllerForTesting(ICurrentScanController currentScanController) {
		logger.warn("setCurrentScanControllerForTesting called");
		InterfaceProvider.currentScanController = currentScanController;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param terminalPrinter
	 */
	public static void setTerminalPrinterForTesting(ITerminalPrinter terminalPrinter) {
		logger.warn("setTerminalPrinterForTesting called");
		InterfaceProvider.terminalPrinter = terminalPrinter;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param scanStatusHolder
	 */
	public static void setScanStatusHolderForTesting(IScanStatusHolder scanStatusHolder) {
		logger.warn("setScanStatusHolderForTesting called");
		InterfaceProvider.scanStatusHolder = scanStatusHolder;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param panicStop
	 */
	public static void setPanicStopForTesting(ICommandAborter panicStop) {
		logger.warn("setPanicStopForTesting called");
		InterfaceProvider.commandAborter = panicStop;
	}	
	
	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param scriptController
	 */
	public static void setScriptControllerForTesting(IScriptController scriptController) {
		logger.warn("setScriptControllerForTesting called");
		InterfaceProvider.scriptController = scriptController;
	}	
	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param jythonNamespace
	 */
	public static void setJythonNamespaceForTesting(IJythonNamespace jythonNamespace) {
		logger.warn("setJythonNamespaceForTesting called");
		InterfaceProvider.jythonNamespace = jythonNamespace;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param currentScanHolder
	 */
	public static void setCurrentScanInformationHolderForTesting(ICurrentScanInformationHolder currentScanHolder) {
		logger.warn("setCurrentScanHolderForTesting called");
		InterfaceProvider.currentScanHolder = currentScanHolder;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param jythonServerNotifer
	 */
	public static void setJythonServerNotiferForTesting(IJythonServerNotifer jythonServerNotifer) {
		logger.warn("setJythonServerNotiferForTesting called");
		InterfaceProvider.jythonServerNotifer = jythonServerNotifer;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * 
	 * @param defaultScannableProvider
	 */
	public static void setDefaultScannableProviderForTesting(IDefaultScannableProvider defaultScannableProvider) {
		logger.warn("setDefaultScannableProviderForTesting called");
		InterfaceProvider.defaultScannableProvider = defaultScannableProvider;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * @param authorisationHolder 
	 */
	public static void setAuthorisationHolderForTesting(IAuthorisationHolder authorisationHolder) {
		logger.warn("setAuthorisationHolderForTesting called");
		InterfaceProvider.authorisationHolder = authorisationHolder;
	}

	/**
	 * @return current selected implementation of IBatonStateProvider
	 */
	public static IBatonStateProvider getBatonStateProvider() {
		if (batonStateProvider == null) {
			batonStateProvider = JythonServerFacade.getInstance();
		}
		return batonStateProvider;
	}

	public static TextCompleter getCompleter() {
		if (jythonCompleter == null) {
			jythonCompleter = JythonServerFacade.getInstance();
		}
		return jythonCompleter;
	}

	/**
	 * call this only when you wish to run outside of gda when JythonServer does not exist
	 * @param batonStateProvider 
	 */
	public static void setBatonStateProviderForTesting(IBatonStateProvider batonStateProvider) {
		logger.warn("setBatonStateProviderForTesting called");
		InterfaceProvider.batonStateProvider = batonStateProvider;
	}
	

	public static void setAliasedCommandProvider(AliasedCommandProvider aliasedCommandProvider) {
		logger.warn("setAliasedCommandProvider called");
		InterfaceProvider.aliasedCommandProvider = aliasedCommandProvider;
	}
	
	public static IJythonServerStatusProvider getJythonServerStatusProvider(){
		if (jythonServerStatusProvider == null) {
			jythonServerStatusProvider = getLocalJythonServerFromFinder();
		}
		return jythonServerStatusProvider;
	}
	public static void setJythonServerStatusProviderForTesting(IJythonServerStatusProvider jythonServerStatusProvider) {
		logger.warn("setJythonServerStatusProviderForTesting called");
		InterfaceProvider.jythonServerStatusProvider = jythonServerStatusProvider;
	}
		
}
