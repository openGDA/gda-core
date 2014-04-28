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

package uk.ac.gda.client.logpanel.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.client.logpanel.view.Logpanel;
import uk.ac.gda.client.logpanel.view.LogpanelView;

/**
 * TODO using IElementUpdater(?) synchronise state with other instances of command and return value of Logpanel.createScrollLockPanel
 */
public class ScrollLockHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LogpanelView logpanelView = (LogpanelView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("uk.ac.gda.client.logpanel.view");
		Logpanel logpanel = logpanelView.getLogpanel();
		State state = event.getCommand().getState("uk.ac.gda.client.logpanel.commands.scrollLock.toggleState"); 
		state.setValue(!(Boolean) state.getValue());
		logpanel.setScrollLockChecked((Boolean) state.getValue());
		return null;
	}

}
