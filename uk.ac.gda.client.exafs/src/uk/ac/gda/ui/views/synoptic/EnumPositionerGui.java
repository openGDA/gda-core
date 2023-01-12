/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.Finder;
import gda.observable.IObserver;

/**
 * Class to create ComboBox to control an EnumPositioner. The Combo box is coupled to the underlying EnumPositioner by implementing the
 * IObserver interface and by a listener on the Combo box. <p>
 * {@link #createCombo()} function creates just a single combo box (e.g. for adding to parent layout as part of more complex gui)
 * {@link #createControls()} function puts a combo box in a labelled group inside a new composite.
 * @since 11/5/2017
 */
public class EnumPositionerGui implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerGui.class);

	private Composite parent;
	private EnumPositioner enumPositioner;
	private Combo selectionCombo;

	private Label nameLabel;
	private Group group;

	public EnumPositionerGui(Composite parent, String positionerName) {
		this.parent = parent;
		enumPositioner = Finder.find(positionerName);
	}

	public EnumPositionerGui(Composite parent, EnumPositioner enumPositioner) {
		this.parent = parent;
		this.enumPositioner = enumPositioner;
	}

	/**
	 * Create new {@link EnumPositionerGui} with just the combo box to control the enumPositioner
	 * @param parent
	 * @param enumPositioner
	 * @return EnumPositionerGui
	 * @throws DeviceException
	 */
	public static EnumPositionerGui getCombo(Composite parent, EnumPositioner enumPositioner) throws DeviceException {
		EnumPositionerGui gui = new EnumPositionerGui(parent, enumPositioner);
		gui.createCombo(parent);
		return gui;
	}

	/**
	 * Add combo box to parent composite
	 */
	public void createCombo(Composite parent) {
		selectionCombo = new Combo(parent, SWT.READ_ONLY);
		try {
			selectionCombo.setItems( enumPositioner.getPositions() );
		} catch (DeviceException e) {
			logger.warn("Problem getting positions from {} to use in combo box- widget will be disabled", enumPositioner.getName(), e);
			selectionCombo.setEnabled(false);
			selectionCombo.setToolTipText("Widget disabled - problem getting values from "+enumPositioner.getName());
		}
		selectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		updateComboBox();
		addListenerObservers();
		setComboWidth();
	}

	public void createCombo() {
		createCombo(parent);
	}

	/**
	 * Set width of combo box based based on max pixel width of
	 * the text items it contains.
	 */
	public void setComboWidth() {
		GridData gridData = (GridData) selectionCombo.getLayoutData();
		String[] items = selectionCombo.getItems();
		int maxLength = 0;
		GC gc = new GC(selectionCombo);
		for(String item : items) {
			Point size = gc.stringExtent(item);
			if (size.x>maxLength) {
				maxLength = size.x;
			}
		}
		gridData.widthHint = maxLength+20;
	}

	/**
	 * Make new composite containing labelled group and combo box
	 * Create GUI elements
	 */
	public void createControls() {
		group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		Composite controlWidgets = new Composite(group, SWT.NONE);
		controlWidgets.setLayout(new GridLayout(1, false));
		controlWidgets.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		nameLabel = new Label(controlWidgets, SWT.SINGLE);
		nameLabel.setText(enumPositioner.getName());
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		nameLabel.setToolTipText(getToolTipText());

		createCombo(group);

		updateComboBox();
		addListenerObservers();
	}

	public String getToolTipText() {
		String tooltipText = "";
		if (enumPositioner != null) {
			try {
				tooltipText = enumPositioner.getName()+ " : "+ArrayUtils.toString(enumPositioner.getPositions());
			} catch (DeviceException e) {
				logger.warn("Problem getting positions for tooltip text", e);
			}
		}
		return tooltipText;
	}

	private void addListenerObservers() {
		selectionCombo.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnumPositioner(selectionCombo.getText());
			}
		});
		enumPositioner.addIObserver(this);
	}

	/**
	 * Move enumpositioner to new value
	 * @param newposition
	 */
	private void updateEnumPositioner(String newposition) {
		try {
			String newPosition = selectionCombo.getText();
			enumPositioner.moveTo(newPosition);
		} catch (DeviceException e1) {
			logger.error("Problem moving {} to {}", enumPositioner.getName(), newposition, e1);
		}

	}

	/**
	 * Update combo box to reflect change in underlying EnumPositioner
	 * @param source
	 * @param arg
	 */
	@Override
	public void update(Object source, Object arg) {
		// Update combo box in gui thread
		Display.getDefault().syncExec( new Runnable() {
			@Override
			public void run() {
				updateComboBox();
			}
		});
	}

	/**
	 * Update combo box with current value from EnumPositioner
	 */
	private void updateComboBox() {
		String newPosition = "";
		try {
			newPosition = (String) enumPositioner.getPosition();
			String[] items = selectionCombo.getItems();
			int newIndex = ArrayUtils.indexOf(items, newPosition);
			selectionCombo.select(newIndex);
		} catch (DeviceException e) {
			logger.error("Problem updating combo box with value {} from {}", newPosition, enumPositioner, e);
		}
	}

	public void setLabel(String label) {
		nameLabel.setText(label);
	}

	public Group getGroup() {
		return group;
	}

	public Combo getCombo() {
		return selectionCombo;
	}
}
