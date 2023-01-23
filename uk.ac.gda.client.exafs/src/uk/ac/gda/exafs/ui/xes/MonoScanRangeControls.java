/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class MonoScanRangeControls extends XesControlsBuilder {

	private Group mainGroup;
	private ScaleBox initialEnergy;
	private ScaleBox finalEnergy;
	private ScaleBox stepSize;

	private static final double MIN_MONO_ENERGY = 2000;
	private static final double MAX_MONO_ENERGY = 35000;

	private static final double MIN_MONO_STEP = 0.05;
	private static final double MAX_MONO_STEP = 100;

	@Override
	public void createControls(Composite parent) {
		GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		mainGroup = new Group(parent, SWT.NONE);
		mainGroup.setText("Mono Scan");

		GridLayout gridLayout = new GridLayout(2, false);
		mainGroup.setLayout(gridLayout);
		gdFactory.applyTo(mainGroup);

		Label lblInitialEnergy = new Label(mainGroup, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		initialEnergy = new ScaleBox(mainGroup, SWT.NONE);
		initialEnergy.setUnit("eV");
		initialEnergy.on();
		gdFactory.applyTo(initialEnergy);

		Label label = new Label(mainGroup, SWT.NONE);
		label.setText("Final Energy");
		finalEnergy = new ScaleBox(mainGroup, SWT.NONE);
		finalEnergy.setUnit("eV");
		gdFactory.applyTo(finalEnergy);

		label = new Label(mainGroup, SWT.NONE);
		label.setText("Step Size");
		stepSize = new ScaleBox(mainGroup, SWT.NONE);
		stepSize.setUnit("eV");
		gdFactory.applyTo(stepSize);

		createBounds();

		// Activate the widget listeners, notify observers on value changes
		setupFieldWidgets(getWidgets());

		parent.addDisposeListener(l -> {
			getWidgets().forEach(IFieldWidget::dispose);
			deleteIObservers();
		});

	}

	private List<IFieldWidget> getWidgets() {
		return Arrays.asList(initialEnergy, finalEnergy, stepSize);
	}

	private void createBounds() {
		initialEnergy.setMinimum(MIN_MONO_ENERGY);
		finalEnergy.setMaximum(MAX_MONO_ENERGY);

		initialEnergy.setMaximum(finalEnergy);
		finalEnergy.setMinimum(initialEnergy);

		stepSize.setMinimum(MIN_MONO_STEP);
		stepSize.setMaximum(MAX_MONO_STEP);
	}

	public static double getMinMonoEnergy() {
		return MIN_MONO_ENERGY;
	}
	public static double getMaxMonoEnergy() {
		return MAX_MONO_ENERGY;
	}

	public Composite getMainComposite() {
		return mainGroup;
	}

	public ScaleBox getInitialEnergy() {
		return initialEnergy;
	}

	public ScaleBox getFinalEnergy() {
		return finalEnergy;
	}

	public ScaleBox getStepSize() {
		return stepSize;
	}

	public void showMain(boolean show) {
		setVisible(mainGroup, show);
	}
}
