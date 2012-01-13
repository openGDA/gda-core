/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Class behaves like swing ButtonGroup because I 
 * could not find a jface class for this. Probably
 * because Actions are not very commonly uses and
 * actions are configured via the plugin.xml
 */
public class CheckableActionGroup implements IPropertyChangeListener {

	private Collection<IAction> actions = new ArrayList<IAction>(7);
	
	public void add(IAction action) {
		if (action.getStyle() != IAction.AS_CHECK_BOX) throw new RuntimeException("Only check actions are supported!");
		action.addPropertyChangeListener(this);
		actions.add(action);
	}

	private boolean off = false;
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (off) return;
		try {
			off = true;
			final Action action = (Action)event.getSource();
			final Collection<IAction> others = new ArrayList<IAction>(actions);
			others.remove(action);
			action.setChecked(true);
			for (IAction other : others) other.setChecked(false);
		} finally {
			off = false;
		}
	}
	
	public void dipose() {
		actions.clear();
	}
}
