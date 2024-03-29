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
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageType;
import uk.ac.gda.client.composites.StringManagedScannableCompositeFactory;
import uk.ac.gda.client.event.ManagedScannableEvent;
import uk.ac.gda.client.exception.GDAClientException;
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

	private Composite stageComposite;

	private static final Logger logger = LoggerFactory.getLogger(StagesComposite.class);

	public CommonStage getStage() {
		return getStageController().getStageDescription();
	}

	public void buildStageComposite(Composite parent) {
		var composite = composite(parent, 1);

		var baseX = new StringManagedScannableCompositeFactory(getBaseX());
		baseX.createComposite(composite, SWT.READ_ONLY);

		stageComposite = composite(composite, 1);

		try {
			var position = getBaseX().getPosition(); // never null, if it throws it becomes out of scope
			var stage = temporaryManagedScannablePositionToCommonStage(position); // could throw, but never return null
			setStage(stage); // with confidence!
		} catch (GDAClientException e) {
		  logger.error("Could not set stage", e);
		}

		SpringApplicationContextFacade.addDisposableApplicationListener(composite, scannableStateEventListener);
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
			try {
				setStage(temporaryManagedScannablePositionToCommonStage(event.getPosition()));
			} catch (GDAClientException e) {
				logger.error("Cannot set new baseX Stage", e);
			}
		}

		private void setStage(CommonStage stage) {
			Optional.ofNullable(getStageController())
				.ifPresent(c -> c.changeStage(stage));
			Arrays.stream(stageComposite.getChildren()).forEach(Control::dispose);
			stage.getUI(stageComposite);
			stageComposite.layout(true);
		}
	};

	private ManagedScannable<Object> getBaseX() {
		return getScannablesPropertiesHelper()
					.getManagedScannable(DefaultManagedScannable.BASE_X);
	}

	private ScannableProperties getBaseXProperties() {
		return getScannablesPropertiesHelper()
				.getScannablePropertiesDocument(DefaultManagedScannable.BASE_X.getScannableKey());
	}

	private CommonStage temporaryManagedScannablePositionToCommonStage(Object position) throws GDAClientException {
		String enumName = getBaseXProperties().getEnumsMap().entrySet().stream()
			.filter(e -> e.getValue().equals(position))
			.findFirst()
			.map(Entry::getKey)
			.orElseThrow(() -> new GDAClientException("No position defined for BaseX"));
		if (StageType.GTS.name().equals(enumName)) {
			return StageType.GTS.getCommonStage();
		} else if (StageType.TR6.name().equals(enumName)) {
			return StageType.TR6.getCommonStage();
		}
		throw new GDAClientException("No position defined for BaseX");
	}

	private ScannablesPropertiesHelper getScannablesPropertiesHelper() {
		return SpringApplicationContextFacade.getBean(ScannablesPropertiesHelper.class);
	}
}