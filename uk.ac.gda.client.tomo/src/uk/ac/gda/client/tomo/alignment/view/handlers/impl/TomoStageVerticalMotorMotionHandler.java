/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.util.Sleep;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.python.core.PyBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentCommands;
import uk.ac.gda.client.tomo.alignment.view.handlers.IVerticalMotorMotionHandler;
import uk.ac.gda.client.tomo.preferences.TomoAlignmentPreferencePage;

public class TomoStageVerticalMotorMotionHandler implements IVerticalMotorMotionHandler {

	private static final Logger logger = LoggerFactory.getLogger(TomoStageVerticalMotorMotionHandler.class);

	private IObservable tomoScriptController;

	@Override
	public void moveVerticalMotorBy(final IProgressMonitor monitor, double position) {
		logger.debug("Moving vertical Motors by:{}", position);

		final SubMonitor progress = SubMonitor.convert(monitor);
		final String[] exString = new String[1];
		final boolean[] isComplete = new boolean[] { false };
		IObserver obs = new IObserver() {

			@Override
			public void update(Object source, Object arg) {

				if (arg instanceof PyBaseException) {
					logger.error("Error:{}", ((PyBaseException) arg).getMessage());
					exString[0] = ((PyBaseException) arg).getMessage().toString();
					isComplete[0] = true;
				} else {
					String msg = arg.toString();
					progress.subTask(msg);
					if ("Vertical Move Complete".equals(msg)) {
						isComplete[0] = true;
					}
				}
			}
		};

		tomoScriptController.addIObserver(obs);
		boolean useY1 = TomoClientActivator.getDefault().getPreferenceStore()
				.getBoolean(TomoAlignmentPreferencePage.TOMO_CLIENT_VERTICAL_STAGE_USE_Y1);

		String useY1Val = useY1 ? "True" : "False";

		boolean y2BeforeY3 = TomoClientActivator.getDefault().getPreferenceStore()
				.getBoolean(TomoAlignmentPreferencePage.TOMO_CLIENT_VERTICAL_STAGE_Y2_BEFORE_Y3);
		String y2BeforeY3Val = y2BeforeY3 ? "True" : "False";

		String moveVerticalCmd = String.format(TomoAlignmentCommands.MOVE_VERTICAL, useY1Val, y2BeforeY3Val, position);

		JythonServerFacade.getInstance().evaluateCommand(moveVerticalCmd);

		while (!isComplete[0]) {
			Sleep.sleep(100);
		}
		tomoScriptController.deleteIObserver(obs);

		if (exString[0] != null) {
			throw new IllegalStateException(exString[0]);
		}
		logger.debug("Position to move by:{}", position);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> getVerticalMotorPositions() {
		final Object[] maps = new Object[1];
		IObserver obs = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				logger.debug("vertical motor positions:{}", arg);
				String returnString = arg.toString();
				if (returnString.startsWith("{") && returnString.endsWith("}")) {
					String replacedUnnecessaryChars = returnString.replace("{", "").replace("}", "").replace("u'", "")
							.replace("'", "");
					StringTokenizer tokens = new StringTokenizer(replacedUnnecessaryChars, ",");
					HashMap<String, Double> motorPositionMap = new HashMap<String, Double>();
					while (tokens.hasMoreTokens()) {
						String nextToken = tokens.nextToken();
						String[] split = nextToken.split(":");
						motorPositionMap.put(split[0].trim(), Double.parseDouble(split[1].trim()));
					}
					logger.debug("After replacing unnecessary chars:{}", replacedUnnecessaryChars);
					maps[0] = motorPositionMap;
				} else {
					maps[0] = new Object();
				}
			}
		};
		tomoScriptController.addIObserver(obs);
		JythonServerFacade.getInstance().evaluateCommand(TomoAlignmentCommands.GET_VERTICAL);
		while (maps[0] == null) {
			Sleep.sleep(200);
		}
		tomoScriptController.deleteIObserver(obs);
		if (maps[0] instanceof Map<?, ?>) {
			return (Map<String, Double>) maps[0];
		}
		return Collections.emptyMap();
	}

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}
}
