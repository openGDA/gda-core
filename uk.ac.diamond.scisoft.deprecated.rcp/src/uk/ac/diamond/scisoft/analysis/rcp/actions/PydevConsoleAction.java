/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.rcp.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.python.pydev.debug.newconsole.PydevConsoleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use uk.ac.diamond.scisoft.pydev.rcp.handlers.PydevInjectConsoleHandler which is a fully
 *             parameterised Handler
 */
@Deprecated
public class PydevConsoleAction extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(PydevConsoleAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		PydevConsoleFactory factory = new PydevConsoleFactory();
		try {
			factory.openConsole();
		} catch (Exception e) {
			logger.error("Cannot open console", e);
			throw new ExecutionException("Cannot open console", e);
		}

		return Boolean.TRUE;
	}
	//
	// /**
	// * Python to link to current editor when action is run.
	// * @return python
	// */
	// private static String createLinkCurrentEditor() {
	//
	// String editorName = null;
	// try {
	// final IEditorPart editor = EclipseUtils.getActiveEditor();
	// if (editor instanceof IDataSetPlotViewProvider) {
	// editorName = editor.getEditorInput().getName();
	// }
	// } catch (Exception ignored) {
	// editorName = null;
	// }
	//
	// final StringBuilder buf = new StringBuilder();
	// buf.append("# Importing scisoftpy.\n");
	// buf.append("import scisoftpy as dnp\n");
	// if (editorName!=null) {
	// buf.append("# Connecting to plot '"+editorName+"'.\n");
	// buf.append("dnp.plot.setdefname('"+editorName+"')\n");
	// }
	// return buf.toString();
	// }
	//
	// private static PydevConsoleInterpreter getConsole() throws Exception {
	//
	// IProcessFactory iprocessFactory = new IProcessFactory();
	//
	// // Shows GUI - NOTE Change here to always link into Jython without
	// showing dialog.
	// Tuple4<Launch, Process, Integer, IInterpreterInfo> launchAndProcess =
	// iprocessFactory.createInteractiveLaunch();
	// if(launchAndProcess == null){
	// return null;
	// }
	//
	// return PydevConsoleFactory.createPydevInterpreter(
	// launchAndProcess.o1, launchAndProcess.o2,
	// launchAndProcess.o3, launchAndProcess.o4,
	// iprocessFactory.getNaturesUsed());
	// }

}
