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

package uk.ac.gda.exafs.ui.controller;

import gda.jython.JythonServerFacade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * NOTE Could possibly be using Eclipse Job class
 */
@Deprecated
public class ExafsScanController /*extends ExperimentController*/ {

	private static final Logger logger = LoggerFactory.getLogger(ExafsScanController.class);

	public ExafsScanController() {
		super();
	}




	/**
	 * Gets time estimation of current scan from server.
	 * 
	 * @param ob
	 * @return Run time in milliseconds
	 * @throws Exception
	 */
	public int getEstimatedTime(final ScanObject ob) throws Exception {

		// final String fileName = ob.isXanes() ? "XanesScanParameters" : "XasScanParameters";
		final String command = getCommandLine(ob, ob.getScanFileName());

		// Run command and block.
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();

		final Map<String, Serializable> jythonObjects = new HashMap<String, Serializable>(1);
		Serializable currentBean = null;
		final RichBeanMultiPageEditorPart part = ExperimentFactory.getExperimentEditorManager().getEditor(
				ob.getScanFile());
		if (part != null) {
			final RichBeanEditorPart curEd = part.getRichBeanEditor();
			if (curEd != null) {
				currentBean = (Serializable) curEd.getEditingBean();
			}
		}
		// Get it from file.
		if (currentBean == null) {
			currentBean = ob.getScanParameters();
		}
		jythonObjects.put(ob.getScanFileName(), currentBean);

		if (jythonObjects.isEmpty()) {
			throw new Exception("Cannot determine editing bean.");
		}
		for (Map.Entry<String, Serializable> pair : jythonObjects.entrySet()) {
			jythonServerFacade.placeInJythonNamespace(pair.getKey(), pair.getValue());
		}
		return Integer.parseInt(jythonServerFacade.evaluateCommand(command));
	}

	private String getCommandLine(ScanObject ob, final String fileName) throws Exception {
		if (ob.isXanes()) {
			return "estimateXanes " + fileName + " " + ob.getNumberRepetitions() + " True";
		} /*else if (ob.isEde()) {
			// TODO estimateEde need implementing in the script
			return "1";
		}*/
		return "estimateXas " + fileName + " " + ob.getNumberRepetitions() + " True";
	}

}