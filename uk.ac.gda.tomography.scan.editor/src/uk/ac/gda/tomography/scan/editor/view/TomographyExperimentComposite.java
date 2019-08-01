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

package uk.ac.gda.tomography.scan.editor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.model.ActionLog;
import uk.ac.gda.tomography.model.TomographyAcquisition;
import uk.ac.gda.tomography.model.TomographyExperiment;
import uk.ac.gda.tomography.scan.editor.TomographyAcquisitionController;
import uk.ac.gda.tomography.scan.editor.TomographyBindingElements;
import uk.ac.gda.tomography.scan.editor.TomographyResourceManager;
import uk.ac.gda.tomography.scan.editor.TomographySWTElements;

/**
 * Allows to edit a {@link TomographyExperiment} object.
 * An ExperimentController should be injected. TBD
 *
 * @author Maurizio Nagni
 */
public class TomographyExperimentComposite extends CompositeTemplate<TomographyExperiment> {

	private static final Logger logger = LoggerFactory.getLogger(TomographyExperimentComposite.class);

	// The current experiment
	private TomographyExperiment templateData;

	// The current acquisition
	private TomographyAcquisition acquisition;

	// Experiment UI
	private Composite experimentComposite;
	private Text experimentName;
	private ItemsViewer<TomographyAcquisition> acquisitions;
	private Button experimentLogBar;
	private ItemsViewer<ActionLog> experimentLogs;

	// Acquisition UI
	private Composite acquisitionComposite;

	public TomographyExperimentComposite(final Composite parent) {
		this(parent, new TomographyExperiment());
		getTemplateData().setAcquisitions(new ArrayList<>());
		getTemplateData().setLogs(new ArrayList<>());
	}

	public TomographyExperimentComposite(final Composite parent, final TomographyExperiment templateData) {
		this(parent, SWT.NONE, templateData);
	}

	public TomographyExperimentComposite(final Composite parent, int style, final TomographyExperiment templateData) {
		super(parent, style, templateData);
		GridLayoutFactory.swtDefaults().margins(TomographySWTElements.defaultCompositeMargin()).applyTo(this);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.LEFT, SWT.TOP).applyTo(this);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(this, SWT.NONE, TomographyMessages.EXPERIMENT, null,
				FontDescriptor.createFrom(TomographyResourceManager.getDefaultFont(), 14, SWT.BOLD));
		createExperimentContent(this, labelStyle, textStyle);
	}

	private void createExperimentContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NAME);
		experimentName = TomographySWTElements.createText(parent, textStyle, null, null, TomographyMessages.TOMOGRAPHY_EXPERIMENT_NAME_TP,
				TomographySWTElements.DEFAULT_COMPOSITE_SIZE);

		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ACQUISITIONS);
		acquisitions = new ItemsViewer<>(parent, textStyle, getAcquisitions(), getAcquisitionController());

		ExpandBarBuilder customBarHelper = new ExpandBarBuilder(parent, TomographyMessages.NOTES);
		experimentLogs = LogsViewer.createLogsViewer(customBarHelper.getInternalArea(), textStyle, new ArrayList<ActionLog>(), getExperimentLogsController());
		customBarHelper.buildExpBar();
	}

	private Map<String, TomographyAcquisition> getAcquisitions() {
		Map<String, TomographyAcquisition> acquisitionsMap = new HashMap<>();

		getTemplateData().getAcquisitions().forEach(a -> {
			acquisitionsMap.put(a.getName(), a);
		});

		return acquisitionsMap;
	}

	private Map<String, TomographyAcquisition> getExperimentLogs() {
		Map<String, TomographyAcquisition> acquisitionsMap = new HashMap<>();

		getTemplateData().getAcquisitions().forEach(a -> {
			acquisitionsMap.put(a.getName(), a);
		});

		return acquisitionsMap;
	}

	private ItemViewerController<TomographyAcquisition> getAcquisitionController() {
		return new ItemViewerController<TomographyAcquisition>() {

			@Override
			public TomographyAcquisition createItem() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public TomographyAcquisition editItem(TomographyAcquisition item) {
				TomographyAcquisitionController controller = new TomographyAcquisitionController(item);
				// Arrays.stream(acquisitionComposite.getChildren()).forEach(Control::dispose);
				if (acquisitionComposite != null) {
					acquisitionComposite.dispose();
				}
				acquisitionComposite = new TomographyAcquisitionComposite(getParent(), controller);
				acquisitionComposite.layout();
				acquisitionComposite.getParent().layout();
				return item;
			}

			@Override
			public TomographyAcquisition deleteItem(TomographyAcquisition item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getItemName(TomographyAcquisition item) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	// private Map<String, TomographyConfiguration> getConfigurations() {
	// Map<String, TomographyConfiguration> configurationsMap = new HashMap<>();
	// IntStream.rangeClosed(0, 2).forEach(i -> {
	// TomographyConfiguration item = new TomographyConfiguration();
	// item.setName("Configuration " + Integer.toString(i));
	// configurationsMap.put(item.getName(), item);
	// });
	//
	// return configurationsMap;
	// }
	//
	// private ItemViewerController<TomographyConfiguration> getConfigurationController() {
	// return new ItemViewerController<TomographyConfiguration>() {
	//
	// @Override
	// public TomographyConfiguration createItem() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public TomographyConfiguration editItem(TomographyConfiguration item) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public TomographyConfiguration deleteItem(TomographyConfiguration item) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getItemName(TomographyConfiguration item) {
	// // TODO Auto-generated method stub
	// return null;
	// }};
	// }

	// private Map<String, URI> getScripts() {
	// Map<String, URI> scriptsMap = new HashMap<>();
	//
	// IntStream.rangeClosed(0, 2).forEach(i -> {
	// URI item;
	// try {
	// item = new URI("Script " + Integer.toString(i));
	// scriptsMap.put(item.getPath(), item);
	// } catch (URISyntaxException e) {
	// // TODO Auto-generated catch block
	// logger.error("TODO put description of error here", e);
	// }
	// });
	//
	// return scriptsMap;
	// }

	// private ItemViewerController<URI> getScriptController() {
	// return new ItemViewerController<URI>() {
	//
	// @Override
	// public URI createItem() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public URI editItem(URI item) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public URI deleteItem(URI item) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getItemName(URI item) {
	// // TODO Auto-generated method stub
	// return null;
	// }};
	// }

	@Override
	protected void bindElements() {
		DataBindingContext dbc = new DataBindingContext();
		//
		// bindScanType(dbc);
		// bindMultipleScanType(dbc);
		// binRangeType(dbc);
		//
		TomographyBindingElements.bindText(dbc, experimentName, String.class, "name", getTemplateData());

		// acquisitions.addListener(SWT.Selection, event -> {
		// acquisition = getTemplateData().getAcquisitions().get(acquisitions.getSelectedIndex());
		// logger.debug("here");
		// });

		// IObservableValue<List> iTarget = WidgetProperties.text(SWT.Modify).observe(acquisitions);
		// IObservableValue<Integer> iModel = PojoProperties.value(modelProperty, clazz).observe(model);
		// UpdateValueStrategy iTargetToModelStrategy = new UpdateValueStrategy();
		// UpdateValueStrategy iModelToTargetStrategy = new UpdateValueStrategy();
		// dbc.bindValue(iTarget, iModel, iTargetToModelStrategy, iModelToTargetStrategy);

		// TomographyBindingElements.bindText(dbc, startAngleText, Double.class, "start.start", getTomographyData());
		// // TomographyBindingElements.bindText(dbc, numberRotation, Integer.class, "end.numberRotation",
		// // getTomographyData());
		// TomographyBindingElements.bindText(dbc, customAngle, Double.class, "end.customAngle", getTomographyData());
		// TomographyBindingElements.bindText(dbc, totalProjections, Double.class, "projections.totalProjections", getTomographyData());
		// TomographyBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark", getTomographyData());
		// TomographyBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat", getTomographyData());
		// TomographyBindingElements.bindText(dbc, numberRepetitions, Integer.class, "multipleScans.numberRepetitions", getTomographyData());
		// TomographyBindingElements.bindText(dbc, waitingTime, Integer.class, "multipleScans.waitingTime", getTomographyData());
		//
		// TomographyBindingElements.bindCheckBox(dbc, currentAngleButton, "start.useCurrentAngle", getTomographyData());
		// TomographyBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition", getTomographyData());
		// TomographyBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition", getTomographyData());
	}

	@Override
	protected void initialiseElements() {
	}

	private ItemViewerController<ActionLog> getExperimentLogsController() {
		return new ItemViewerController<ActionLog>() {

			@Override
			public ActionLog createItem() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ActionLog editItem(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ActionLog deleteItem(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getItemName(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
}
