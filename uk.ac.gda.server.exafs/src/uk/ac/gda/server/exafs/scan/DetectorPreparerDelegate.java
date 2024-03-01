/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

/**
 * Base class to use for DetectorPreparer implementations. It contains a list of DetectorPreparers whose
 * configure, beforeEachRepetition, completeCollection methods can be run from the derived class in
 * the configure, beforeEachRepetition, completeCollection methods. This allows functionality to be
 * easily extended, modified and adapted as required by modifying the list of DetectorPreparers
 * (Multi-cast delegate pattern)
 * 29/2/2024
 */
public class DetectorPreparerDelegate {

	private static final Logger logger = LoggerFactory.getLogger(DetectorPreparerDelegate.class);

	/** List of detector preparer objects to be used */
	private List<DetectorPreparer> delegates = Collections.emptyList();

	/** Set to true to allow exceptions to be thrown in {@link #runConfigure} and {@link #runBeforeEachRepetition()} methods */
	private boolean throwExceptions;

	/**
	 * Call {@link DetectorPreparer#configure} method on each detector preparer object.
	 * Any exceptions can be optionally caught so that each preparer can be run.
	 *
	 * @param scanBean
	 * @param detectorBean
	 * @param outputBean
	 * @param experimentFullPath
	 * @throws Exception
	 */
	public void runConfigure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean, String experimentFullPath) throws Exception {
		for(var preparer : delegates ) {
			try {
				preparer.configure(scanBean, detectorBean, outputBean, experimentFullPath);
			} catch (Exception e) {
				String msg = generateMessage("configure", preparer);
				if (throwExceptions) {
					throw new Exception(msg, e);
				}
				logger.warn("Problem encountered running 'configure' on preparer "+preparer.toString(), e);
			}
		}
	}

	/**
	 * Run {@link DetectorPreparer#beforeEachRepetition} method on each detectorPreparer object
	 * Any exceptions can be optionally caught and logged, so each preparer can be run.
	 * @throws Exception
	 */
	public void runBeforeEachRepetition() throws Exception {
		for(var preparer : delegates) {
			try {
				preparer.beforeEachRepetition();
			} catch (Exception e) {
				String msg = generateMessage("beforeEachRepetition", preparer);
				if (throwExceptions) {
					throw new Exception(msg, e);
				}
				logger.warn("Problem encountered running 'beforeEachRepetition' on preparer "+preparer.toString(), e);
			}
		}
	}

	/**
	 * Run completeCollection method on each of the detectorPreparer objects
	 */
	public void runCompleteCollection() {
		delegates.forEach(DetectorPreparer::completeCollection);
	}

	private String generateMessage(String method, Object preparer) {
		return "Problem encountered running '"+method+"'' on preparer "+preparer.toString();
	}

	public List<DetectorPreparer> getPreparers() {
		return delegates;
	}

	public void setPreparers(List<DetectorPreparer> preparers) {
		this.delegates = preparers;
	}

	public boolean isThrowExceptions() {
		return throwExceptions;
	}

	public void setThrowExceptions(boolean throwExceptions) {
		this.throwExceptions = throwExceptions;
	}
}
