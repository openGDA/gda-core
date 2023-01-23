/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.factory.FactoryException;
import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControl;
import uk.ac.gda.client.livecontrol.LiveControlGroup;
import uk.ac.gda.client.livecontrol.ScannablePositionerControl;


public class LiveControlsScannableGroup extends LiveControlGroup {

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsScannableGroup.class);

	private IScannableGroup scannableGroup;
	private int displayNameWidth = 0;
	private boolean horizontalLayout = false;
	private int incrementTextWidth = 30;
	private boolean showStop = true;
	private boolean readOnly = false;
	private boolean showIncrement = true;
	private int widgetWidth = SWT.DEFAULT;

	// For setting the description of each widget in the group
	private List<String> descriptions = Collections.emptyList();

	// For setting the read only property of each widget in the group
	private List<Boolean> readOnlyList = Collections.emptyList();

	public void setScannableGroupName(String name) throws FactoryException {
		IScannableGroup scnGroup = Finder.findOptionalOfType(name, IScannableGroup.class)
									.orElseThrow(() -> new FactoryException("Could not find scannable called "+name));
		setScannableGroup(scnGroup);
	}

	public void setScannableGroup(IScannableGroup crystalScannableGroup) {
		logger.info("Setting scannable group to : {}", crystalScannableGroup.getName());
		scannableGroup = crystalScannableGroup;
	}

	@Override
	public void createControl(Composite composite) {
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);

		// Create LiveControl object for each scannable in the group
		String[] groupMemberNames = scannableGroup.getGroupMemberNames();
		List<LiveControl> liveControls = new ArrayList<>();
		if (groupMembersAreGroups(groupMemberNames[0]) ){

			// Parent container for all the widgets
			composite.setLayout(new GridLayout(1, false));

			for(int i=0; i<groupMemberNames.length; i++) {

				// Get the group name from the descriptions
				String groupLabel = descriptions.get(i%descriptions.size());

				// Find the ScannableGroup object, so we can get the list of scannables it contains
				IScannableGroup grp = Finder.findOptionalOfType(groupMemberNames[i], IScannableGroup.class).orElseThrow();

				var liveControlsForGroup = createControlsForGroup(grp.getGroupMemberNames(), Collections.emptyList(), groupLabel);
				setControls(liveControlsForGroup);

				// Create group to put the widgets in
				Group container = new Group(composite, SWT.NONE);
				container.setText(groupLabel);

				// Creat the controls
				super.createControl(container);

				liveControls.addAll(liveControlsForGroup);
			}
			setControls(liveControls);

		} else {
			liveControls = createControlsForGroup(groupMemberNames, descriptions, getGroup());
			setControls(liveControls);
			// Make new composite for all the widgets to go into
			final Composite container = createContainer(composite);

			super.createControl(container);
		}

	}

	private Composite createContainer(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		if (widgetWidth > 0 && !(parent.getLayout() instanceof RowLayout)) {
			GridDataFactory.fillDefaults().hint(widgetWidth, SWT.DEFAULT).applyTo(container);
		} else {
			container.setLayout(new FillLayout());
		}
		return container;
	}

	private boolean groupMembersAreGroups(String name) {
		return Finder.findOptionalOfType(name, IScannableGroup.class).isPresent();
	}

	private List<LiveControl> createControlsForGroup(String[] namesOfScannables, List<String> labels, String groupLabel) {
		List<LiveControl> liveControls = new ArrayList<>();
		for(String name : namesOfScannables) {
			int itemIndex = liveControls.size();
			Boolean isReadOnlyFromList = null;
			if (!readOnlyList.isEmpty()) {
				int size = readOnlyList.size();
				isReadOnlyFromList = readOnlyList.get(itemIndex%size);
			}

			// set the display name from the description
			String description = null;
			if (!labels.isEmpty()) {
				description = labels.get(itemIndex%labels.size());
			}

			var control = new ScannablePositionerControl();
			control.setScannableName(name);
			control.setDisplayName(name);
			control.setDisplayNameWidth(displayNameWidth);
			control.setIncrementTextWidth(incrementTextWidth);
			control.setHorizontalLayout(horizontalLayout);
			control.setShowStop(showStop);
			if (readOnly || Boolean.TRUE.equals(isReadOnlyFromList)) {
				control.setReadOnly(true);
			}
			control.setShowIncrement(showIncrement);
			if (description != null) {
				control.setDisplayName(description);
			}
			control.setGroup(groupLabel);


			liveControls.add(control);
		}
		return liveControls;
	}
	public Boolean getShowIncrement() {
		return showIncrement;
	}

	public void setShowIncrement(Boolean showIncrement) {
		this.showIncrement = showIncrement;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setReadOnlyList(List<Boolean> readOnly) {
		this.readOnlyList = new ArrayList<>(readOnly);
	}

	public void setDisplayNameWidth(int displayNameWidth) {
		this.displayNameWidth = displayNameWidth;
	}

	public void setWidgetWidth(int widgetWidth) {
		this.widgetWidth = widgetWidth;
	}

	public void setHorizontalLayout(boolean horizontalLayout) {
		this.horizontalLayout = horizontalLayout;
	}

	public void setIncrementTextWidth(int incrementTextWidth) {
		this.incrementTextWidth = incrementTextWidth;
	}

	public void setShowStop(boolean showStop) {
		this.showStop = showStop;
	}

	/**
	 * Set the description for each widget in the group.
	 * <li> If empty, the scannable name will be used.
	 * <li> If there are fewer descriptions than scannables,
	 * the description indices will 'wrap around'.
	 * i.e. description index = widget index % num descriptions
	 * @param descriptions
	 */
	public void setDescriptions(List<String> descriptions) {
		this.descriptions = new ArrayList<>(descriptions);
	}


	/**
	 * Toggle states of showIncrement, showStop and layout flags when toggle
	 * button in view toolbar are pressed
	 */

	@Override
	public void toggleIncrementControlDisplay() {
		showIncrement = !showIncrement;
	}

	@Override
	public void toggleShowStopButton() {
		showStop = !showStop;
	}

	@Override
	public void toggleLayoutControl() {
		horizontalLayout = !horizontalLayout;
	}
}
