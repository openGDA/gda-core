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

package uk.ac.gda.tomography.stage;

import java.util.Arrays;
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

import uk.ac.gda.tomography.stage.enumeration.Stage;
import uk.ac.gda.tomography.stage.enumeration.StageType;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Creates a drop-down list in the parent Composite to switch between different stages.
 *
 * @author Maurizio Nagni
 */
public class StagesComposite {
	private final Composite parent;
	private Combo stagesCombo;
	private Composite stageComposite;

	private final IStageController stageController;

	private StagesComposite(Composite parent, IStageController stageController) {
		this.parent = parent;
		this.stageController = stageController;
	}

	public static final StagesComposite buildModeComposite(Composite parent, IStageController stageController) {
		StagesComposite pc = new StagesComposite(parent, stageController);
		pc.buildStageComposite(stageController.getStageDescription());
		return pc;
	}

	public CommonStage getStage() {
		return stageController.getStageDescription();
	}

	private Composite getParent() {
		return parent;
	}

	/**
	 */
	private void buildStageComposite(CommonStage stage) {
		Group group = ClientSWTElements.createGroup(getParent(), 1, ClientMessages.STAGE);
		stagesCombo = ClientSWTElements.createCombo(group, SWT.READ_ONLY, getTypes(), ClientMessages.STAGE_TP);
		stageComposite = ClientSWTElements.createComposite(group, SWT.NONE, 1);
		setStage(stage);

		comboStageSelectionListener();
	}

	private void comboStageSelectionListener() {
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo source = Combo.class.cast(e.getSource());
				if (source.getSelectionIndex() > -1) {
					filterPerspectiveLabel(getTypes()[source.getSelectionIndex()]).findFirst().ifPresent(p -> setStage(p.getCommonStage()));
				}
			}
		};
		stagesCombo.addSelectionListener(listener);
	}

	private Stream<StageType> filterPerspectiveLabel(final String perspectiveLabel) {
		return Arrays.stream(StageType.values()).filter(p -> p.getStage().name().equals(perspectiveLabel));
	}

	private void setStage(CommonStage stage) {
		stageController.changeStage(stage);
		stagesCombo.setText(stage.getStage().name());
		Arrays.stream(stageComposite.getChildren()).forEach(Control::dispose);
		stage.getUI(stageComposite);
		stageComposite.layout(true);
	}

	private String[] getTypes() {
		return Arrays.stream(StageType.values()).map(StageType::getStage).map(Stage::name).collect(Collectors.toList()).toArray(new String[0]);
	}
}
