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

package gda.gui.scriptcontroller.logging;

import gda.observable.IObserver;

import java.io.Serializable;

/**
 * Oberver to be passed by the content provider to the remote ScriptController.
 */
public class ScriptControllerLogHelper implements IObserver, Serializable {

	transient IObserver contentProvider;

	public ScriptControllerLogHelper(IObserver contentProvider) {
		super();
		this.contentProvider = contentProvider;
	}

	@Override
	public void update(Object source, Object arg) {
		contentProvider.update(source, arg);
	}

}
