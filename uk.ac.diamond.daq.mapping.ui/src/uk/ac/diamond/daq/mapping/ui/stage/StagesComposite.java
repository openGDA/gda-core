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
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createComposite;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.position.summary.PositionSummaryComposite;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageType;
import uk.ac.gda.client.composites.StringManagedScannableCompositeFactory;
import uk.ac.gda.client.event.ManagedScannableEvent;
import uk.ac.gda.client.properties.stage.DefaultManagedScannable;
import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Creates a drop-down list in the parent Composite to switch between different stages.
 *
 * @author Maurizio Nagni
 */
public class StagesComposite {

	private final Composite parent;
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
		var pc = new StagesComposite(parent);
		pc.buildStageComposite(getStageController().getStageDescription());
		// Updates the mapping start/stop button following the mapping state
		return pc;
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
		var group = createClientGroup(getParent(), SWT.NONE, 1, STAGE);
		createClientGridDataFactory().grab(true, false).indent(5, SWT.DEFAULT).applyTo(group);
		var baseX = new StringManagedScannableCompositeFactory(getBaseX());
		baseX.createComposite(group, SWT.READ_ONLY);

		stageComposite = createComposite(group, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false)
				.indent(5, SWT.DEFAULT).applyTo(stageComposite);
		setStage(stage);

		new PositionSummaryComposite().createComposite(group, SWT.NONE);
		SpringApplicationContextFacade.addDisposableApplicationListener(group, scannableStateEventListener);
	}

	private void setStage(CommonStage stage) {
		Optional.ofNullable(getStageController())
			.ifPresent(c -> c.changeStage(stage));
		Arrays.stream(stageComposite.getChildren()).forEach(Control::dispose);
		stage.getUI(stageComposite);
		stageComposite.layout(true);
	}

	private static IStageController getStageController() {
		return getBean(StageController.class);
	}

	private ApplicationListener<ManagedScannableEvent<String>> scannableStateEventListener = new ApplicationListener<>() {
		@Override
		public void onApplicationEvent(ManagedScannableEvent<String> event) {
			if (!event.getScannablePropertiesDocument().equals(getBaseXProperties()))
				return;
			Optional.ofNullable(temporaryManagedScannablePositionToCommonStage(event.getPosition()))
				.ifPresent(this::setStage);
		}

		private void setStage(CommonStage stage) {
			Optional.ofNullable(getStageController())
				.ifPresent(c -> c.changeStage(stage));
			Arrays.stream(stageComposite.getChildren()).forEach(Control::dispose);
			stage.getUI(stageComposite);
			stageComposite.layout(true);
		}

		private CommonStage temporaryManagedScannablePositionToCommonStage(String position) {
			String enumName = getBaseXProperties().getEnumsMap().entrySet().stream()
				.filter(e -> e.getValue().equals(position))
				.findFirst()
				.map(Entry::getKey)
				.orElse(null);
			if (StageType.GTS.name().equals(enumName)) {
				return StageType.GTS.getCommonStage();
			} else if (StageType.TR6.name().equals(enumName)) {
				return StageType.TR6.getCommonStage();
			}
			return null;
		}

		private ScannableProperties getBaseXProperties() {
			return SpringApplicationContextFacade.getBean(ScannablesPropertiesHelper.class)
						.getScannablePropertiesDocument(DefaultManagedScannable.BASE_X);
		}
	};

	private ManagedScannable<String> getBaseX() {
		return SpringApplicationContextFacade.getBean(ScannablesPropertiesHelper.class)
					.getManagedScannable(DefaultManagedScannable.BASE_X, String.class);
	}
}