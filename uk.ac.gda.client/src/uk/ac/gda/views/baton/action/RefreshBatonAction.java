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

package uk.ac.gda.views.baton.action;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.views.baton.BatonView;

/**
 *
 */
public class RefreshBatonAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return refresh();
	}

	/**
	 * @return Boolean.TRUE if refreshed.
	 */
	public static Object refresh() {
		final BatonView view = (BatonView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(BatonView.ID);
		if (view!=null) {
			view.refresh();
			return Boolean.TRUE;
		}
		return  Boolean.FALSE;
	}

}
