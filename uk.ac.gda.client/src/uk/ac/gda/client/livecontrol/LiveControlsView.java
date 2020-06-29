/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;

public class LiveControlsView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.livecontrol.LiveControlsView";

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsView.class);

	private boolean controlsWithNoGroup;

	private Composite parent;

	private List<ControlSet> controlSets;

	@Override
	public void createPartControl(Composite parent) {

		// Cache the composite for setFocus()
		this.parent = parent;

		controlSets = Finder.listLocalFindablesOfType(ControlSet.class);

		if (controlSets.isEmpty()) {
			displayAndLogError(parent, "No controls sets were found");
			return;
		}

		if (controlSets.size() == 1) {
			createControlsView(parent, controlSets.get(0));
		} else {
			// TODO if more than one control set is available allow user to choose
			displayAndLogError(parent, "More than one controls set was found. This is not supported yet!");
		}
	}

	private void createControlsView(Composite parent, ControlSet controlSet) {

		List<LiveControl> controls = controlSet.getControls();

		// Create a list of required groups and check if there are controls without group set
		List<String> groups = new ArrayList<String>();
		for (LiveControl control : controls) {
			// If there is a control with no group set the flag
			if (control.getGroup() == null) {
				controlsWithNoGroup = true;
				continue;
			}
			// if groups doesn't contain the group add it
			if (!groups.contains(control.getGroup())) {
				groups.add(control.getGroup());
			}
		}
		//DAQ-855 make the view scrollable
		final ScrolledComposite scrolledComposite=new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite content=new Composite(scrolledComposite, SWT.NONE);
		content.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		// Layout the composite
		content.setLayout(new RowLayout(SWT.VERTICAL));

		// Define the row layout to be used bay all the groups
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;

		// Loop through the groups
		for (String group : groups) {
			// Create a new group
			Group displayGroup = new Group(content, SWT.INHERIT_DEFAULT);
			displayGroup.setLayout(rowLayout);
			displayGroup.setText(group);

			for (LiveControl control : controls) {
				// If the control belongs in this group add it check for null group first!
				if (control.getGroup() != null && control.getGroup().equals(group)) {
					// Create Composite for this control
					control.createControl(displayGroup);
				}
			}
		}

		if (controlsWithNoGroup) {
			// Add controls with no group directly to another composite
			Composite displayGroup = new Composite(content, SWT.NONE);
			displayGroup.setLayout(rowLayout);

			for (LiveControl control : controls) {
				// If the control belongs in this group add it
				if (control.getGroup() == null) {
					// Create Composite for this control
					control.createControl(displayGroup);
				}
			}
		}
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setShowFocusedControl(true);
}

	@Override
	public void setFocus() {
		parent.setFocus();
	}

	@Override
	public void dispose() {
		for (ControlSet controlSet : controlSets) {
			controlSet.dispose();
		}
		super.dispose();
	}

	private void displayAndLogError(final Composite parent, final String errorMessage) {
		Label errorLabel = new Label(parent, SWT.NONE);
		errorLabel.setText(errorMessage);
		parent.layout(true);
		logger.error(errorMessage);
	}

}
