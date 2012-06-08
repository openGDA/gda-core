/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.server.collisionAvoidance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;

import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.CheckedScannableMotion;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Localizable;
import gda.jython.JythonServerFacade;
import gda.server.collisionAvoidance.ArrayToString;
import gda.server.collisionAvoidance.HelperFunctions;

/**
 *
 */
public class CollisionAvoidanceController implements Findable, Configurable, Localizable {
	private static final Logger logger = LoggerFactory.getLogger(CollisionAvoidanceController.class);

	// Map of scannables (keyed to their internal name field)
	private Map<String, CacScannable> cacScannablesMap = new HashMap<String, CacScannable>();

	// Map of checkers (keyed to their internal name field)
	private Map<String, CacChecker> cacCheckersMap = new HashMap<String, CacChecker>();

	private boolean local = true;

	private String name = "";

	/**
	 * Shortcut method to get a link the collision avoidance controller from the finder.
	 * 
	 * @return the CAC
	 * @throws CacException
	 */
	static public CollisionAvoidanceController getInstanceFromFinder() throws CacException {

		CollisionAvoidanceController toReturn = (CollisionAvoidanceController) gda.factory.Finder.getInstance().find(
				"collision_avoidance_controller");
		if (toReturn == null) {
			throw new CacException(
					"Could not retrieve a CollisionAvoidanceController object named 'collision_avoidance_controller' from the finder.  Perhaps it has not been added to the server.xml file, or perhaps it was given a different name.");
		}
		return toReturn;
	}

	/*
	 * 
	 */
	private CollisionAvoidanceController() {
		// Singleton
	}

	@Override
	public void configure() throws FactoryException {

	}

	/**
	 * Totally deletes everything in the controller, (useful if you want to build it up again from a script)
	 */
	public void clearController() {
		cacScannablesMap = new HashMap<String, CacScannable>();

		cacCheckersMap = new HashMap<String, CacChecker>();
	}

	/**
	 * Registers a CheckedScannable. Until it registered CheckedScannables won't move.
	 * 
	 * @param scannableObject
	 * @throws CacException
	 */
	public void registerScannable(CheckedScannableMotion scannableObject) throws CacException {
		// Register if scannable has not been registered already.
		if (cacScannablesMap.containsKey(scannableObject.getName()) == false)
			cacScannablesMap.put(scannableObject.getName(), new CacScannable(scannableObject));
		else
			throw new CacException("CAC could not register scannable. A scannable with name "
					+ scannableObject.getName() + " is allready registered");

	}

	/**
	 * Unregisters a CheckedScannable. The CheckedScannables won't move. Make sure to unregister any Checkers that are
	 * tied to the scannable too.
	 * 
	 * @param scannableName
	 * @throws CacException
	 */
	public void unregisterScannable(String scannableName) throws CacException {
		// Check that scannable with name scannableName exists, and delete if so.
		if (cacScannablesMap.containsKey(scannableName) == true)
			cacScannablesMap.remove(scannableName);
		else
			throw new CacException("CAC could not unregister scannable. A scannable with name " + scannableName
					+ " is not registered");

	}

	/**
	 * Registers a checker with the CAC and ties it to fields from registers scannables. When a request to move one of
	 * these tied fields is made, the checker is called. a scannable's field
	 * 
	 * @param checkerObject
	 * @param scannableNames
	 * @param paramNames
	 * @throws CacException
	 */
	public void registerChecker(CollisionChecker checkerObject, String[] scannableNames, String[] paramNames)
			throws CacException {
		// TODO write register checker

		// Add the checker
		if (cacCheckersMap.containsKey(checkerObject.getName()) == false)
			cacCheckersMap.put(checkerObject.getName(), new CacChecker(checkerObject, scannableNames, paramNames));
		else
			throw new CacException("CAC could not register checker. A checker with name " + checkerObject.getName()
					+ " is allready registered");

	}

	/**
	 * Unregister a checker. If the CAC contains the last link to the actual checker, the checker will be deleted too.
	 * 
	 * @param checkerName
	 * @throws CacException
	 */
	public void unregisterChecker(String checkerName) throws CacException {
		// Check that a checker with name checkerName exists, and delete if so.
		if (cacCheckersMap.containsKey(checkerName) == true)
			cacCheckersMap.remove(checkerName);
		else
			throw new CacException("CAC could not unregister checkers. A checkers with name " + checkerName
					+ " is not registered");

	}

	/**
	 * Checks to see if a move is allowed.
	 * 
	 * @param scannableName
	 * @param pos
	 * @return empty string if okay, otherwise returns a list of strings of the rules this move breaks
	 * @throws CacException
	 *             exception if unable to answer request due to bad request.
	 */
	@SuppressWarnings("rawtypes")
	synchronized public String[] isMoveAllowedNow(String scannableName, Double[] pos) throws CacException {
		CacScannable CacScannableToMove;
		int nParameters;
		String[] paramNames;
		List<String> paramsRequestedToMove = new ArrayList<String>();
		Set<String> checkersToAsk = new HashSet<String>();
		List<String> problemsWithMove = new ArrayList<String>();

		// *************** Check that request is properly formed ****************

		// Check scannable is registered
		if (cacScannablesMap.containsKey(scannableName) == false) {
			throw new CacException("Move invalid. No scannable with name " + scannableName + " is registered");
		}

		// Make a shortcut to the CacScannable
		CacScannableToMove = cacScannablesMap.get(scannableName);
		paramNames = CacScannableToMove.getParameterNames();
		nParameters = CacScannableToMove.getNumberParameters();

		// Check input pos is the right length (same as number of parameters)
		if (nParameters != pos.length) {
			throw new CacException("Move request invalid. The scannable with name " + scannableName + " requires "
					+ nParameters + " but was sent only " + pos.length);
		}

		// *************** Check that requested move is safe ****************

		// Check scannable is not being moved
		if (CacScannableToMove.isScannableInTransition()) {
			problemsWithMove.add("Move not allowed: The scannable with name " + scannableName
					+ " is already in transition");
			return problemsWithMove.toArray(new String[0]);
		}

		// ********* Check move is allowed by all checkers *********

		// Make a list of parameters to be moved
		for (int i = 0; i < nParameters; i++) {
			if (pos[i] != null)
				paramsRequestedToMove.add(paramNames[i]);
		}

		// Make a Set (rather than list, as this inherantly removes duplicates)
		// of checkers tied to all parameters to be moved
		for (Iterator iter = paramsRequestedToMove.iterator(); iter.hasNext();) {
			String tmpParamName = (String) iter.next();
			String[] tmpcheckersArray = CacScannableToMove.getTiedCheckers(tmpParamName);
			// add each one to checkersToask list
			for (int i = 0; i < tmpcheckersArray.length; i++) {
				checkersToAsk.add(tmpcheckersArray[i]);
			}

		}

		// Call all checkers. They return null if move is okay, or an array of
		// Strings otherwise
		for (Iterator<String> iter = checkersToAsk.iterator(); iter.hasNext();) {
			String checkerName = iter.next();
			String[] checkerReport;

			checkerReport = cacCheckersMap.get(checkerName).checkMove(scannableName, pos);

			if (checkerReport != null) {
				for (int i = 0; i < checkerReport.length; i++) {
					problemsWithMove.add(checkerReport[i]);
				}
			}
		}

		return problemsWithMove.toArray(new String[0]);
	}

	/**
	 * Asks the CAC to move a scannable.
	 * <p>
	 * TO BE REMOVED: a null value in pos indicates that the corresponding position of the scannable will not be moved
	 * from its current value. (For future expandability). This method trhows an exception if the move is not performed
	 * for any reason.
	 * 
	 * @param scannableName
	 * @param pos
	 * @throws CacException
	 *             If the move cannot be performed
	 */
	synchronized public void requestMove(String scannableName, Double[] pos) throws CacException {
		CacScannable CacScannableToMove;
		int nParameters;
		String[] isMoveAllowedReport;

		// Check scannable is registered
		if (cacScannablesMap.containsKey(scannableName) == false) {
			throw new CacException("Move invalid. No scannable with name " + scannableName + " is registered");
		}

		// Make a shortcut to the CacScannable
		CacScannableToMove = cacScannablesMap.get(scannableName);
		nParameters = CacScannableToMove.getNumberParameters();

		// Show input request
		logger.debug("CAC received request from " + scannableName + " to move to (or by) " + ArrayToString.get(pos));

		// Assume the CheckedScannable has already checked its own limits
		// report=cacScannablesMap.get(scannableName).getScannableObject().isPositionWithinLimits(pos);

		// **Check move is safe**
		isMoveAllowedReport = isMoveAllowedNow(scannableName, pos);

		// return the messages sent back by the checkers if a move failed.
		if (isMoveAllowedReport.length != 0) {
			String errorString = "Moving " + scannableName + " to " + ArrayToString.get(pos)
					+ " breaks the folowing rule(s):\n ";

			for (int i = 0; i < isMoveAllowedReport.length; i++) {
				errorString = errorString + isMoveAllowedReport[i] + "\n";
			}
			throw new CacException(errorString);
		}

		// *************** Perform move ****************

		// Make shortcut to actual external Scannable object
		CheckedScannableMotion scannableObject = CacScannableToMove.getScannableObject();

		// Set flag in scannable saying it is being moved by the CAC. This will
		// cause
		// the actual scannable's isBusy() method to return true.
		scannableObject.setCacIsMovingThis();

		// set location Start to current position
		Object posObject;
		try {
			posObject = scannableObject.getPosition();
		} catch (DeviceException e) {
			throw new CacException("Exception wile checking device position: " + e);
		}

		CacScannableToMove.locationStart = HelperFunctions.positionToArray(posObject, nParameters);

		// Set location end position to desired location
		CacScannableToMove.locationEnd = pos;

		// Set is moving flags for those parameters that are being moved
		// i.e. those whose final position is not null
		for (int i = 0; i < nParameters; i++) // Set Location Start and
		// inTransition
		{
			if (pos[i] != null) {
				CacScannableToMove.inTransition[i] = true;
			}
		}

		// Start the move in a new thread. This thread will monitor the scannables
		// uncheckedIsBusy() method and then set the CacSCannable's inTransition
		// bits
		// to false, and then tell the outside world the move is complete by
		// calling
		// unsetCacIsMovingThis(), so that the scannables isBusy() method will
		// return false.

		CheckedScannableMover mover = new CheckedScannableMover(CacScannableToMove, pos);
		mover.start();

		// The thread that entered this method (which came from outside this
		// object via
		// one of the move() methods on a scannable) will now leave this method,
		// releasing
		// the lock on this method and allowing another thread through to request
		// and initiate a move.

	}

	/**
	 * Forces CAC to consider all moves to be complete: Unsets all the actual-scannable's CacIsMovingThis flags Unsets
	 * all the registered scannables parameter's inTransition flags
	 */
	public void refresh() {
		// loop over all registered scannables
		cacScannablesMap.values().iterator();
		for (Iterator<CacScannable> iter = cacScannablesMap.values().iterator(); iter.hasNext();) {
			CacScannable cacScannable = iter.next();

			for (int i = 0; i < cacScannable.getNumberParameters(); i++) {
				cacScannable.inTransition[i] = false;
			}
			cacScannable.getScannableObject().unsetCacIsMovingThis();
		}
	}

	/**
	 * {@inheritDoc} Shows the state of the contoller, including that of its registered scannables and checkers.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toReturn;

		toReturn = "<< Collision avoidance controller >>\n";

		// include scannables
		toReturn = toReturn + "<scannables>\n";
		for (String key : cacScannablesMap.keySet())
			toReturn = toReturn + cacScannablesMap.get(key).toString() + "\n";

		// include checkers
		toReturn = toReturn + "<checkers>\n";
		for (String key : cacCheckersMap.keySet())
			toReturn = toReturn + cacCheckersMap.get(key).toString() + "\n";

		// include the details of any checkers
		toReturn = toReturn + "\n<<Checker insides>>\n";
		for (String key : cacCheckersMap.keySet())
			toReturn = toReturn + cacCheckersMap.get(key).getCheckerObject().toString() + "\n";

		return toReturn;
	}

	/**
	 * Jython method to return string description of the object
	 * 
	 * @return the result of the toString method
	 */
	public PyString __str__() {
		return new PyString(toString());
	}

	/**
	 * Jython method to return a string representation of the object
	 * 
	 * @return the result of the toString method
	 */
	public PyString __repr__() {
		return __str__();
	}

	// /////////// Implements Localizable interface. /////////////

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	// /////////// Implement Findable interface./////////////

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	// /////////// Implements Configurable interface./////////////

	static void printTerm(String toPrint) {
		// TODO: Remove delay after print hack!
		JythonServerFacade.getInstance().print(toPrint);

		// Delay so that any following print will come out in the right order
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/********************************************************************************
	 * 
	 ********************************************************************************/
	class CacChecker {
		CollisionChecker checkerObject = null; // The external checker object
		String checkerName;

		// The parameters this checker ties to.
		// [n][0]->scannable name, [n][1]->parameter name
		String[][] tiedParameters;

		int nTiedParameters;
		Double[] currentPosStartRange;
		Double[] currentPosEndRange;

		CacChecker(CollisionChecker _checkerObject, String[] scannableNames, String[] paramNames) throws CacException {
			checkerObject = _checkerObject;
			checkerName = checkerObject.getName();

			// Store in order the list of parameters passed in. The order is
			// important
			// as this is the order that they will be passed to the actual checker
			// object.
			nTiedParameters = scannableNames.length;
			tiedParameters = new String[nTiedParameters][2];
			for (int i = 0; i < nTiedParameters; i++) {
				tiedParameters[i][0] = scannableNames[i];
				tiedParameters[i][1] = paramNames[i];
			}

			// Inform the cacScannables this checker ties to.
			for (int i = 0; i < nTiedParameters; i++) {
				// Check scannable is registered
				if (cacScannablesMap.containsKey(scannableNames[i]) == false) {
					throw new CacException("Could not register checker: No scannable named " + scannableNames[i]
							+ " is registered with CAC");
				}

				// Tie the checker to the scannable
				try {
					cacScannablesMap.get(scannableNames[i]).tieToChecker(paramNames[i], checkerName);
				} catch (CacException e) {
					throw new CacException("Could not tie checker to scannable parameter. " + e.getMessage());
				}
			}
		}

		/*
		 * Returns a two by N array describing the. The first row of N values
		 */
		void updateRegisteredParameterValues() throws CacException {
			currentPosStartRange = new Double[nTiedParameters];
			currentPosEndRange = new Double[nTiedParameters];
			Double[] tmpPos;
			String tmpName;
			String tmpParam;
			for (int i = 0; i < nTiedParameters; i++) {
				tmpName = tiedParameters[i][0];
				tmpParam = tiedParameters[i][1];
				tmpPos = cacScannablesMap.get(tmpName).getPosition(tmpParam);
				currentPosStartRange[i] = tmpPos[0];
				currentPosEndRange[i] = tmpPos[1];
			}

		}

		/**
		 * Ask a checker if its safe to move a scannable it is tied to.
		 * 
		 * @param scannableName
		 *            The scannable to be moved
		 * @param position
		 *            The position to move it to
		 * @return null if move okay, or an array of problems described in strings
		 * @throws CacException
		 */
		public String[] checkMove(String scannableName, Double[] position) throws CacException {
			// If this checker is tied to more than one scannable, then the
			// position passed in
			// will have less parameters than the configuration space the checker
			// checker.
			// So build up a a configuration and call checkConfiguration
			Double[] configuration = new Double[nTiedParameters];
			// get parameter names for this scannable
			String[] paramNames = cacScannablesMap.get(scannableName).getParameterNames();

			// Initialise configuration to be all nulls
			for (int i = 0; i < configuration.length; i++) {
				configuration[i] = null;
			}

			// Loop through the configuration array to fill
			posloop: for (int p = 0; p < paramNames.length; p++) {
				// If this value corresponds to a parameter associated with a the
				// scannable
				// to be moved, then set it to the desired position value. Do this
				// by comparing
				// its assoiciated scannable and parameter names to those of the
				// scannabel being moved.
				for (int i = 0; i < nTiedParameters; i++) {
					if ((tiedParameters[i][0] == scannableName) && // The right
							// scannable
							// and...
							(tiedParameters[i][1] == paramNames[p]))// ... the right
					// parameter
					{
						configuration[i] = position[p];
						continue posloop;
					}
				}
			}

			return checkMoveWithChecker(configuration);
		}

		String[] checkMoveWithChecker(Double[] configuration) throws CacException {
			// Get the current position and store it in this checker object
			updateRegisteredParameterValues();
			logger.debug("CacChecker.checkConfiguration(" + ArrayToString.get(configuration)
					+ ") called. current position:");

			for (int i = 0; i < nTiedParameters; i++) {
				logger.debug(tiedParameters[i][0] + "." + tiedParameters[i][1] + ": " + currentPosStartRange[i]
						+ " <-> " + currentPosEndRange[i]);
			}

			// Perform the check

			return checkerObject.checkMove(currentPosStartRange, currentPosEndRange, configuration);
		}

		CollisionChecker getCheckerObject() {
			return checkerObject;
		}

		@Override
		public String toString() {
			String toReturn;
			// Display name
			toReturn = "\t" + checkerName + ": \t";

			// Display ordered list of tied paraemeters
			for (int i = 0; i < nTiedParameters; i++) {
				toReturn = toReturn + "'" + tiedParameters[i][0] + "." + tiedParameters[i][1] + "', ";
			}
			toReturn = toReturn.substring(0, toReturn.length() - 2);
			return toReturn;
		}

	} // end class CacChecker

	/*********************************************************************************
	 * 
	 **********************************************************************************/
	class CacScannable {
		CheckedScannableMotion scannableObject = null; // The external scannable
		// object
		String scannableName;
		String[] parameterNames;
		int nParameters;

		Boolean[] inTransition; // Set if CAC is moveing a parameter
		Double[] locationStart; // one bound if Cac is moving parameter
		Double[] locationEnd; // other bound if Cac is moving parameter

		// Each parameter can be tied to multiple checkers.
		// Make an array (that will be nParametrs long), each elemnt of which
		// is a List of strings of checker names (in the order they were added)
		List<String>[] registeredCheckers;

		/*
		 * 
		 */
		@SuppressWarnings("unchecked")
		CacScannable(CheckedScannableMotion _scannableObject) {
			scannableObject = _scannableObject;
			scannableName = _scannableObject.getName();
			parameterNames = _scannableObject.getInputNames();
			nParameters = parameterNames.length;

			inTransition = new Boolean[nParameters];
			locationStart = new Double[nParameters];
			locationEnd = new Double[nParameters];
			registeredCheckers = new List[nParameters];
			for (int i = 0; i < parameterNames.length; i++) {

				inTransition[i] = false;
				locationStart[i] = null;
				locationEnd[i] = null;
				registeredCheckers[i] = new ArrayList<String>(); // Fill array
				// with
				// lists
			}
		}

		/*
		 * 
		 */
		String[] getParameterNames() {
			return parameterNames;
		}

		CheckedScannableMotion getScannableObject() {
			return scannableObject;
		}

		/*
		 * 
		 */
		int getNumberParameters() {
			return nParameters;
		}

		/*
		 * Returns a two element array describing the position of the parameter. If the parameter is not in transition,
		 * returns the actual position obtained from the external scannable object in both elements. If the parameter is
		 * in transition, the two elements describe the bounds between which it is currently moving
		 */
		Double[] getPosition(String parameterName) throws CacException {
			Double[] toReturn = new Double[2];
			if (isParameterInTransition(parameterName)) {
				toReturn[0] = locationStart[paramIndex(parameterName)];
				toReturn[1] = locationEnd[paramIndex(parameterName)];
			} else
			// parameter is not in transition so read the actual position
			{
				Object posObject;
				try {
					posObject = scannableObject.getPosition();
				} catch (DeviceException e) {
					throw new CacException("Exception wile checking device position: " + e);
				}

				Double[] posArray = HelperFunctions.positionToArray(posObject, nParameters);
				toReturn[0] = posArray[paramIndex(parameterName)];
				toReturn[1] = null;
			}

			return toReturn;
		}

		/*
		 * Returns the checkers a parameter is tied to
		 */
		String[] getTiedCheckers(String param) throws CacException {
			List<String> aList = registeredCheckers[paramIndex(param)];
			return aList.toArray(new String[0]);
		}

		void tieToChecker(String paramName, String CacCheckerName) throws CacException {
			logger.debug("tieToChecker(" + paramName + "," + CacCheckerName + ")");
			try {
				registeredCheckers[paramIndex(paramName)].add(CacCheckerName);
			} catch (CacException e) {
				throw new CacException("CAC could not register checker with parameter. " + e.getMessage());
			}
		}

		void untieToChecker(String paramName, String CacCheckerName) throws CacException {
			try {
				registeredCheckers[paramIndex(paramName)].remove(CacCheckerName);
			} catch (CacException e) {
				throw new CacException("CAC could not unregister checker with parameter. " + e.getMessage());
			}

		}

		Boolean isParameterInTransition(String parameterName) throws CacException {
			try {
				return inTransition[paramIndex(parameterName)];
			} catch (CacException e) {
				throw new CacException("Could not check if parameter is in transition: " + e.getMessage());
			}
		}

		Boolean isScannableInTransition() {
			// checks to see if any parameter is in transition
			for (int i = 0; i < parameterNames.length; i++) {
				if (inTransition[i])
					return true;
			}
			return false;

		}

		@Override
		public String toString() {
			String toReturn = "";

			// Display each input and limits if set
			for (int i = 0; i < parameterNames.length; i++) {

				toReturn = toReturn + "\t" + scannableName + "." + parameterNames[i].toString() + ":\t";

				// display this scannables parameters
				// get the current position as [startrange,endrange]
				Double[] posRange;
				try {
					posRange = getPosition(parameterNames[i]);
				} catch (CacException e) {
					throw new RuntimeException(e);
				}

				// add start position
				toReturn = toReturn + "\t" + posRange[0];

				// add end position (if appropriate)
				if (posRange[1] == null) // The location is known precisely
					toReturn = toReturn + "\t";
				else
					toReturn = toReturn + "<->" + posRange[1] + "\t";

				int nCheckers = registeredCheckers[i].size();

				// display the checkers
				if (nCheckers > 0) {
					toReturn = toReturn + "(";
					for (Iterator<String> it = registeredCheckers[i].iterator(); it.hasNext();) {
						toReturn = toReturn + "'" + it.next() + "', ";
					}
					toReturn = toReturn.substring(0, toReturn.length() - 2); // looses
					// extra
					// ', '
					toReturn = toReturn + ")";
				}
				toReturn = toReturn + "\n";
			}
			toReturn = toReturn.substring(0, toReturn.length() - 1); // loose lase
			// carriage
			// return
			return toReturn;
		}

		/*
		 * 
		 */
		private int paramIndex(String paramName) throws CacException {
			int index = -1;
			for (int i = 0; i < parameterNames.length; i++) {
				if (parameterNames[i] == paramName) {
					index = i;
				}
			}
			if (index == -1) {
				throw new CacException("parameter '" + paramName + "' not found in '" + scannableName + "'");
			}
			return index;
		}
	} // end class cacScannable

	/*********************************************************************************
	 * 
	 *********************************************************************************/
	class CheckedScannableMover extends Thread {
		CacScannable cacScannable;
		Double[] position;
		CheckedScannableMotion scannableObject;

		CheckedScannableMover(CacScannable _CacScannable, Double[] _position) {
			cacScannable = _CacScannable;
			position = _position;
			scannableObject = cacScannable.getScannableObject();
		}

		@Override
		public void run() {
			try {
				// call the scannable's actual move function
				scannableObject.rawAsynchronousMoveTo(position);

				while (scannableObject.rawIsBusy()) {
					Thread.sleep(50);
				}
			}

			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			} finally {
				// Unset inTransition flags in CacScannable
				// (no need to set locationEnd and Start, as these will be set if
				// ever
				// they are to be read.)
				// TODO is this thread safe?
				// This may change the cacScannable while another thread is on
				// requestMove()
				// but this should not be unsafe.
				for (int i = 0; i < cacScannable.getNumberParameters(); i++) {
					cacScannable.inTransition[i] = false;
				}

				// Unset flag in scannable saying it is being moved by the CAC
				scannableObject.unsetCacIsMovingThis();
			}
		} // end CheckedScannableMover

	} // end class CAC

}
