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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.rcp.GDAClientActivator;

public class LiveControlsView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.livecontrol.LiveControlsView";

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsView.class);

	private boolean controlsWithNoGroup;

	private Composite parent;

	private List<ControlSet> controlSets;

	private String configName = "";

	private ControlSet displayedControlSet;

	@Override
	public void createPartControl(Composite parent) {

		// Cache the composite for setFocus()
		this.parent = parent;

		controlSets = Finder.listLocalFindablesOfType(ControlSet.class);

		if (controlSets.isEmpty()) {
			displayAndLogError(parent, "No controls sets were found");
			return;
		}

		// If configName is set then reopen the view, passing the ControlSet object name via the secondary Id
		if (!configName.isEmpty()) {
			logger.debug("Trying to create 'live controls' view using configuration : {}", configName);
			Display.getDefault().asyncExec(() -> reopenWithSecondaryId(configName));
			return;
		}

		boolean merged = GDAClientActivator.getDefault().getPreferenceStore().getBoolean(LiveControlPreferencePage.GDA_SHOW_ALL_CONTROLSETS_IN_SINGLE_VIEW);
		// Try to open view from secondary Id - this should be the name of the ControlSet object to be used for the view
		String secondaryId = getViewSite().getSecondaryId();
		if (secondaryId != null && !merged) {
			controlSets.stream()
				.filter(s -> s.getName().equals(secondaryId))
				.findFirst()
				.ifPresentOrElse(controlSet -> createControlsView(parent, controlSet),
						() -> displayAndLogError(parent, "Could not create 'live controls' view - configuration called "
								+configName+" was not found") );
			return;
		}

		if (controlSets.size() > 1) {
			if (merged) {
				createControlsView(parent, merge(controlSets));
			} else {
				// Show a dialog to allow user to select a control set
				selectControlSet(parent, controlSets);
			}
		} else {
			createControlsView(parent, controlSets.get(0));
		}
	}

	private ControlSet merge(List<ControlSet> controlSets) {
		String name = controlSets.stream().map(ControlSet::getName).collect(Collectors.joining("_"));
		List<LiveControl> collect = controlSets.stream().map(ControlSet::getControls).flatMap(Collection::stream).collect(Collectors.toList());
		ControlSet controlset = new ControlSet();
		controlset.setControls(collect);
		controlset.setName(name);
		return controlset;
	}

	/**
	 * Allow user to select a view to be opened from list of all available
	 * ControlSet objects. The view is shown by opening a new view and passing
	 * the ControlSet name as the secondaryId.
	 * @param parent
	 * @param controlSets
	 */
	private void selectControlSet(Composite parent, List<ControlSet> controlSets) {
		ListDialog dialog = new ListDialog(parent.getShell());
		dialog.setAddCancelButton(true);
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element == null ? "" : ((Findable)element).getName();
			}
		});
		dialog.setInput(controlSets);
		dialog.setTitle("Select name of the Live Controls view to open");
		dialog.setBlockOnOpen(true);
		if (dialog.open() == Window.OK) {
			ControlSet selectedControlSet = (ControlSet) dialog.getResult()[0];
			logger.debug("Opening 'live controls' view from user selected configuration : {}", selectedControlSet.getName());
			Display.getDefault().asyncExec(() -> reopenWithSecondaryId(selectedControlSet.getName()));
		}
	}

	/**
	 * Close this view and open again with the secondary ID specified
	 * (This function seems to need calling from the GUI thread to avoid NPE when closing the view).
	 *
	 * @param controlset
	 */
	private void reopenWithSecondaryId(String controlsetName) {
		IWorkbenchPage page = getSite().getPage();
		try {
			page.hideView(LiveControlsView.this);
			page.showView(LiveControlsView.ID, controlsetName, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Error activating view with secondary ID {}", controlsetName, e);
		}
	}

	private void createControlsView(Composite parent, ControlSet controlSet) {

		displayedControlSet = controlSet;

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

		setTitleToolTip(controlSet.getName());
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

	/**
	 * Set the name of the ControlSet object to be used to create the view
	 * in the call to {@link #createPartControl(Composite)}
	 * @param configName
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	@Override
	public void setPartName(String partName) {
		super.setPartName(partName);
	}

	public ControlSet getDisplayedControlSet() {
		return displayedControlSet;
	}
}
