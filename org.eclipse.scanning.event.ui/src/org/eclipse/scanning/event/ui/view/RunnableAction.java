/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.ui.view;

import org.eclipse.jface.action.Action;
import org.eclipse.scanning.event.ui.Activator;

public class RunnableAction extends Action {

	private Runnable runnable;

	public RunnableAction(String label, String imageName, Runnable runnable) {
		super(label, Activator.getImageDescriptor(imageName));
		this.runnable = runnable;
	}

	public RunnableAction(String label, String imageName, Runnable runnable,  int style) {
		super(label, style);
		this.runnable = runnable;
		setImageDescriptor(Activator.getImageDescriptor(imageName));
	}

	@Override
	public void run() {
		runnable.run();
	}

}
