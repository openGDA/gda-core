/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.handlers;

import static gda.jython.commandinfo.CommandThreadEventType.BUSY;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.sample.plate.management.ui.ScriptEditorView;
import uk.ac.gda.client.UIHelper;

public class RunScriptHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunScriptHandler.class);

	@Inject
	private EPartService partService;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		String dirPath = InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("xml/scripts");
		int scriptCnt = 0;
		while (new File(dirPath + "/script" + scriptCnt + ".py").exists()) {
			++scriptCnt;
		}
		String scriptPath = dirPath + "/script" + scriptCnt + ".py";

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(scriptPath))) {
			writer.write(((ScriptEditorView) part.getObject()).getScript());
		} catch (IOException e1) {
			logger.error("Could not save script. Running the script failed", e1);
		}

		var status = InterfaceProvider.getCommandRunner().runScript(new File(scriptPath));
		if ((status.getEventType() == BUSY)) {
			UIHelper.showError("Could not start script", "Could not run script as there is another script running");
		}

		partService.showPart("gda.rcp.jythonterminalview", PartState.ACTIVATE);
	}

	@CanExecute
	public boolean canExecute() {
		return true;
	}
}
