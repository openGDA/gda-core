/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.stage;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.ClientMessages.STAGE;
import static uk.ac.gda.ui.tool.ClientMessages.STAGE_TP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createCombo;
import static uk.ac.gda.ui.tool.ClientSWTElements.createComposite;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Stage;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageType;

/**
 * Creates a drop-down list in the parent Composite to switch between different stages.
 *
 * @author Maurizio Nagni
 */
public class StagesComposite {
	private final Composite parent;
	private Combo stagesCombo;
	private Composite stageComposite;

	private StagesComposite(Composite parent) {
		this.parent = parent;
	}

	/**
	 * A composite to control a stage
	 * @param parent the container where deploy this composite
	 * @return the created composite
	 */
	public static final StagesComposite buildModeComposite(Composite parent) {
		StagesComposite pc = new StagesComposite(parent);
		pc.buildStageComposite(getStageController().getStageDescription());
		return pc;
	}

	/**
	 * @param parent the container where deploy this composite
	 * @param stageController the stage controller. Actually this parameter is no more used
	 * @return a stage composite
	 * @Deprecated Will be deleted in a next release. Use instead {@link #buildModeComposite(Composite)}
	 */
	@Deprecated
	public static final StagesComposite buildModeComposite(Composite parent, IStageController stageController) {
		return buildModeComposite(parent);
	}

	public CommonStage getStage() {
		return getStageController().getStageDescription();
	}

	private Composite getParent() {
		return parent;
	}

	/**
	 */
	private void buildStageComposite(CommonStage stage) {
		Group group = createClientGroup(getParent(), SWT.NONE, 1, STAGE);
		createClientGridDataFactory().grab(true, false).indent(5, SWT.DEFAULT).applyTo(group);
		stagesCombo = createCombo(group, SWT.READ_ONLY, getTypes(), STAGE_TP);
		createClientGridDataFactory().grab(true, false).indent(5, SWT.DEFAULT).applyTo(stagesCombo);

		stageComposite = createComposite(group, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false)
				.indent(5, SWT.DEFAULT).applyTo(stageComposite);
		setStage(stage);

		comboStageSelectionListener();
	}

	private void comboStageSelectionListener() {
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo source = Combo.class.cast(e.getSource());
				if (source.getSelectionIndex() > -1) {
					filterPerspectiveLabel(getTypes()[source.getSelectionIndex()]).findFirst()
							.map(StageType::getCommonStage)
							.ifPresent(s -> setStage(s));
				}
			}
		};
		stagesCombo.addSelectionListener(listener);
	}

	private Stream<StageType> filterPerspectiveLabel(final String perspectiveLabel) {
		return Arrays.stream(StageType.values()).filter(p -> p.getStage().name().equals(perspectiveLabel));
	}

	private void setStage(CommonStage stage) {
		Optional.ofNullable(getStageController())
			.ifPresent(c -> c.changeStage(stage));
		stagesCombo.setText(stage.getStage().name());
		Arrays.stream(stageComposite.getChildren()).forEach(Control::dispose);
		stage.getUI(stageComposite);
		stageComposite.layout(true);
	}

	private String[] getTypes() {
		return Arrays.stream(StageType.values()).map(StageType::getStage).map(Stage::name).collect(Collectors.toList())
				.toArray(new String[0]);
	}

	private static IStageController getStageController() {
		return getBean(StageController.class);
	}
}
