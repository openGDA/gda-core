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

package uk.ac.diamond.daq.sample.plate.management.ui;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.plotting.system.AnnotationWrapper;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.VanillaPlottingSystemView;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.swtdesigner.SWTResourceManager;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.mapping.api.PathInfoCalculationException;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfoRequest;
import uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob;
import uk.ac.diamond.daq.mapping.ui.path.PointGeneratorPathInfoCalculator;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.CollectedParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.PresetParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.SetParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.models.AbstractParam;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegionConfig;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegisteredPlate;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegisteredSample;
import uk.ac.diamond.daq.sample.plate.management.ui.models.ScanModel;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.AnalyserComposite;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.ParamComposite;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.ShapeComposite;
public class PathscanConfigView {
	public static final String ID = "uk.ac.diamond.daq.sample.plate.management.ui.PathscanConfigView";

	private static final Logger logger = LoggerFactory.getLogger(PathscanConfigView.class);

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private EPartService partService;

	private RegisteredPlate currentPlate;

	private IPlottingSystem<Composite> plot;

	private List<IAnnotation> annotations = new ArrayList<>();

	private ArrayList<ScanModel> scanModels;

	private int scanId = 0;

	private PathInfo pathInfo;

	private PathInfoCalculatorJob pathInfoCalculatorJob;

	private MultiPlottingController pc;

	private PointGeneratorPathInfoCalculator pathCalclulator = new PointGeneratorPathInfoCalculator();

	private Composite child;

	private ScrolledComposite scrollComp;

	private CTabFolder shapeTabFolder;

	private EventHandler selectScanHandler = event -> {
		ScanModel scanModel = (ScanModel) event.getProperty(IEventBroker.DATA);
		selectScan(scanModel);
	};

	private EventHandler syncSummaryHandler = event -> syncSummary();

	private EventHandler openSpecsHandler = event -> {
		IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench()
				.getPerspectiveRegistry()
				.findPerspectiveWithId("uk.ac.diamond.daq.devices.specs.phoibos.ui.perspective.specsperspective");
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(descriptor);
	};

	private EventHandler resizeScrollHandler = event -> scrollComp.setMinHeight(child.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

	@Inject
	public PathscanConfigView() {
		logger.trace("Constructor called");
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");

		scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL);
		child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().grab(false, false).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(child);

		Button toggleAnnotationsButton = addToggleButton(child, "Toggle Annotations", span(4).align(SWT.FILL, SWT.FILL), true);
		toggleAnnotationsButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event ->
			showAnnotations(((Button) event.getSource()).getSelection())
		));

		addLabel(child, "Plate:", span(1).align(SWT.FILL, SWT.FILL));
		Text platePathText = addText(child, span(2).align(SWT.FILL, SWT.FILL).grab(true, false), true);
		platePathText.setEditable(false);

		Button loadPlateButton = addButton(child, "Load...", span(1), true);
		Button newShape = addButton(child, "New Shape", span(4), true);
		newShape.setEnabled(false);

		loadPlateButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.SINGLE);
			fileDialog.setFilterPath(InterfaceProvider.getPathConstructor().getVisitSubdirectory("xml/plates"));
			fileDialog.setFilterExtensions(new String[] {"*.json"});
			String firstFile = fileDialog.open();
			if (firstFile != null) {
				platePathText.setText(firstFile);
				loadPlate(platePathText.getText());
				// Auto-create initial shape tab and select it
				newShape.setEnabled(true);
				newShape.notifyListeners(SWT.Selection, new Event());
				newShape.setSelection(true);
				shapeTabFolder.setSelection(0);
				shapeTabFolder.notifyListeners(SWT.Selection, new Event());
			}
		}));

		shapeTabFolder = new CTabFolder(child, SWT.CLOSE);
		shapeTabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		shapeTabFolder.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			String name = ((ShapeComposite) shapeTabFolder.getSelection().getControl()).getShapeName();
			unselectAllShapes();
			selectShape(plot.getRegion(name));
		}));

		shapeTabFolder.addCTabFolder2Listener(CTabFolder2Listener.closeAdapter(event -> {
			if (plot.getRegion(((CTabFolder) event.getSource()).getSelection().getText()) != null) {
				pc.removePath(((CTabFolder) event.getSource()).getSelection().getText());
				plot.getRegion(((CTabFolder) event.getSource()).getSelection().getText()).remove();
			}
			eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null);
		}));
		span(4).align(SWT.FILL, SWT.FILL).grab(true, false).span(4,1).applyTo(shapeTabFolder);

		newShape.addSelectionListener(getNewShapeSelectionListener());

		// TODO Validation: build might not work if a required field is empty
		Button buildScriptButton = addButton(child, "Build script", span(1), true);
		buildScriptButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			partService.showPart("uk.ac.diamond.daq.sample-plate-management.ui.part.scripteditor", PartState.ACTIVATE);
			String script = buildScript();
			eventBroker.post(PathscanConfigConstants.TOPIC_BUILD_SCRIPT, script);
		}));

		// TODO Validation: build might not work if a required field is empty
		Button buildAndRunScanButton = addButton(child, "Build and run script", span(1), true);
		buildAndRunScanButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event ->{
			partService.showPart("uk.ac.diamond.daq.sample-plate-management.ui.part.scripteditor", PartState.ACTIVATE);
			String script = buildScript();
			eventBroker.post(PathscanConfigConstants.TOPIC_BUILD_AND_RUN_SCRIPT, script);
		}));

		eventBroker.subscribe(PathscanConfigConstants.TOPIC_SELECT_SCAN, selectScanHandler);
		eventBroker.subscribe(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, syncSummaryHandler);
		eventBroker.subscribe(PathscanConfigConstants.TOPIC_OPEN_SPECS, openSpecsHandler);
		eventBroker.subscribe(PathscanConfigConstants.TOPIC_RESIZE_SCROLL, resizeScrollHandler);

		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);

		// Expand both horizontally and vertically
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		logger.trace("Finished building composite");
	}

	public void syncSummary() {
		scanModels = generateScans();
		eventBroker.post(PathscanConfigConstants.TOPIC_GENERATE_SUMMARY, scanModels);
	}

	private void selectScan(ScanModel scanModel) {
		// Select shape tab
		CTabItem shapeTabItem = scanModel.getShapeTabItem();
		shapeTabFolder.setSelection(shapeTabItem);
		shapeTabFolder.notifyListeners(SWT.Selection, new Event());

		// Select analyser from list
		AnalyserComposite analyserComposite = ((ShapeComposite) shapeTabItem.getControl()).getAnalyserComposite();
		analyserComposite.getAnalysersList().setSelection(new String[] {scanModel.getAnalyser()});
		analyserComposite.getAnalysersList().notifyListeners(SWT.Selection, new Event());

		// Select param set tab
		CTabFolder paramTabFolder = analyserComposite.getParamTabFolders().get(scanModel.getAnalyser());
		paramTabFolder.setSelection(scanModel.getParamTabItem());
	}

	private ArrayList<ScanModel> generateScans() {
		ArrayList<ScanModel> generatedScanModels = new ArrayList<>();
		for (CTabItem shapeTabItem: shapeTabFolder.getItems()) {
			ShapeComposite shapeComposite = (ShapeComposite) shapeTabItem.getControl();
			for (String analyser: shapeComposite.getAnalyserComposite().getAnalysersList().getItems()) {
				CTabFolder paramTabFolder = shapeComposite.getAnalyserComposite().getParamTabFolders().get(analyser);
				for (CTabItem paramTabItem: paramTabFolder.getItems()) {
					generatedScanModels.add(new ScanModel(scanId++, shapeTabItem, analyser, paramTabItem));
				}
			}
		}
		return generatedScanModels;
	}

	public void unselectAllShapes() {
		for (IRegion region: plot.getRegions()) {
			if (region != null) {
				Color orange = new Color(null, 255, 196, 0);
				region.setRegionColor(orange);
				region.repaint();
			}
		}
	}

	public void selectShape(IRegion region) {
		if (region != null) {
			Color lightBlue = new Color(null, 127, 127, 255);
			region.setRegionColor(lightBlue);
			region.repaint();
		}
	}

	private SelectionAdapter getNewShapeSelectionListener() {
		return new SelectionAdapter() {
			int id = 0;
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem shapeTabItem = new CTabItem(shapeTabFolder, SWT.NONE);
				String shapeName = "Shape " + ++id;
				shapeTabItem.setText(String.valueOf(shapeName));
				ShapeComposite shapeComposite = new ShapeComposite(shapeTabFolder, SWT.NONE, shapeName, currentPlate.getSamples(), eventBroker);
				shapeComposite.getShapeCombo().addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
					String comboText = ((Combo) event.getSource()).getText();
					pc.createNewPlotRegion(shapeComposite.getRegionConfigs().get(comboText).getMappingRegion(), shapeComposite.getShapeName());
					shapeComposite.getPointsSpinner().addSelectionListener(SelectionListener.widgetSelectedAdapter(event1 -> {
						try {
							String name = ((ShapeComposite) shapeTabFolder.getSelection().getControl()).getShapeName();
							pc.plotPath(getPathInfo(plot.getRegion(name).getROI()), name);
						} catch (PathInfoCalculationException ex) {
							logger.error("Cannot plot path", ex);
						}
					}));
				}));
				plot.addRegionListener(getRegionListener());
				shapeTabItem.setControl(shapeComposite);
				shapeTabFolder.setSelection(shapeTabItem);
				shapeTabFolder.notifyListeners(SWT.Selection, new Event());
				syncSummary();
				child.layout(true, true);
				scrollComp.setMinHeight(child.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			}
		};
	}

	private IRegionListener getRegionListener() {
		return new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
					// TODO: unlock tab switching
					try {
						evt.getRegion().snapToGrid();
						shapeTabFolder.notifyListeners(SWT.Selection, new Event());
						String name = ((ShapeComposite) shapeTabFolder.getSelection().getControl()).getShapeName();
						pathInfo = getPathInfo(plot.getRegion(name).getROI());
						pc.plotPath(pathInfo, String.valueOf(name));
						setShapeCoords(evt.getRegion().getROI());
					} catch (PathInfoCalculationException e) {
						logger.error("Cannot plot path", e);
					}
					evt.getRegion().addROIListener(new IROIListener.Stub() {
						@Override
						public void update(ROIEvent evt) {
							try {
								String name = ((IRegion) evt.getSource()).getName();
								CTabItem nextTabItem = null;
								for (CTabItem tabItem: shapeTabFolder.getItems()) {
									if (tabItem.getText().equals(name)) {
										nextTabItem = tabItem;
										break;
									}
								}
								shapeTabFolder.setSelection(nextTabItem);
								shapeTabFolder.notifyListeners(SWT.Selection, new Event());
								plot.getRegion(String.valueOf(name)).snapToGrid();
								pathInfo = getPathInfo(plot.getRegion(name).getROI());
								pc.plotPath(pathInfo, String.valueOf(name));
								setShapeCoords(evt.getROI());
							} catch (PathInfoCalculationException e) {
								logger.error("Cannot update path", e);
							}
						}
					});
				}
		};
	}

	private void setShapeCoords(IROI roi) {
		ShapeComposite shapeComposite = (ShapeComposite) shapeTabFolder.getSelection().getControl();
		if (roi instanceof LinearROI) {
			int startX = (int) Math.round(((LinearROI) roi).getPointX());
			int startY = (int) Math.round(((LinearROI) roi).getPointY());
			int endX = (int) Math.round(((LinearROI) roi).getEndPoint()[0]);
			int endY = (int) Math.round(((LinearROI) roi).getEndPoint()[1]);

			shapeComposite.getXStartLineText().setText(String.valueOf(currentPlate.getXCalibratedAxis()[startX]));
			shapeComposite.getYStartLineText().setText(String.valueOf(currentPlate.getYCalibratedAxis()[startY]));
			shapeComposite.getXEndLineText().setText(String.valueOf(currentPlate.getXCalibratedAxis()[endX]));
			shapeComposite.getYEndLineText().setText(String.valueOf(currentPlate.getYCalibratedAxis()[endY]));
		}
		else if (roi instanceof PointROI) {
			int x = (int) Math.round(((PointROI) roi).getPointX());
			int y = (int) Math.round(((PointROI) roi).getPointY());

			shapeComposite.getXPointText().setText(String.valueOf(currentPlate.getXCalibratedAxis()[x]));
			shapeComposite.getYPointText().setText(String.valueOf(currentPlate.getYCalibratedAxis()[y]));
		}
	}

	private PathInfo getPathInfo(IROI roi) throws PathInfoCalculationException {
		ShapeComposite shapeComposite = (ShapeComposite) shapeTabFolder.getSelection().getControl();
		Combo combo = shapeComposite.getShapeCombo();
		Map<String, RegionConfig> regionConfigs = shapeComposite.getRegionConfigs();
		if (regionConfigs.get(combo.getText()).getPointGeneratorModel() instanceof TwoAxisLinePointsModel) {
			Spinner pointsSpinner = shapeComposite.getPointsSpinner();
			((TwoAxisLinePointsModel) regionConfigs.get(combo.getText()).getPointGeneratorModel()).setPoints(pointsSpinner.getSelection());
		}
		PathInfoRequest request = PathInfoRequest.builder()
				.withEventId(UUID.randomUUID())
				.withSourceId("uk.ac.diamond.daq.sample-plate-management.ui.plate")
				.withScanPathModel(regionConfigs.get(combo.getText()).getPointGeneratorModel())
				.withScanRegion(roi)
				.withMaxPoints(ShapeComposite.MAX_POINTS)
				.build();

		return pathCalclulator.calculatePathInfo(request);
	}

	private String buildScript() {
		String[] motors = new String[]{"sm52b_yp", "sm52b_zp", "sm52b_xp"};
		String script = "";
		script = ScanModel.initScript(motors);
		for (ScanModel scanModel: scanModels) {
			ArrayList<Double[]> motorCoords = scanModel.getMotorCoords(plot, currentPlate.getXCalibratedAxis(), currentPlate.getYCalibratedAxis());
			ParamComposite paramComposite = (ParamComposite) scanModel.getParamTabItem().getControl();
			script += scanModel.closeFastShutter();
			for (AbstractParam presetParam: paramComposite.getParams(new PresetParamBuilder())) {
				script += "pos " + presetParam.getParam() + "\n";
			}
			script += scanModel.posPgmEnergyStart();
			script += scanModel.openFastShutter();
			if (!scanModel.getAnalyser().equals(PathscanConfigConstants.NO_ANALYSER)) {
				script += "analyser.setSequenceFile('" + scanModel.getAnalyser() + "')\n";
			}
			script += "scan ";
			if (motorCoords.size() == 1) {
				script += "dummy_a 0 1 1 ";
			}
			script += scanModel.getMotorCoordsFormatted(plot, currentPlate.getXCalibratedAxis(), currentPlate.getYCalibratedAxis());
			for (AbstractParam setParam: paramComposite.getParams(new SetParamBuilder())) {
				script += setParam.getParam();
				script += " ";
			}
			if (!scanModel.getAnalyser().equals(PathscanConfigConstants.NO_ANALYSER)) {
				script += "analyser ";
			}
			for (AbstractParam collectedParam: paramComposite.getParams(new CollectedParamBuilder())) {
				script += collectedParam.getParam();
				script += " ";
			}
			script += "\n";
			script += scanModel.closeFastShutter();
			script += "\n\n";
		}

		return script;
	}

	private void loadPlate(String path) {
		try {
			Gson gson = new Gson();
			currentPlate = gson.fromJson(new BufferedReader(new FileReader(path)), RegisteredPlate.class);
			getImageView(currentPlate);
			pc = new MultiPlottingController(plot);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error("Could not load plate", e);
		} catch (PartInitException e) {
			logger.error("Could not display plate image", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void getImageView(RegisteredPlate plate) throws PartInitException {
		RGBByteDataset loadedDataset = new RGBByteDataset(plate.getDataset(), plate.getDatasetShape());
		VanillaPlottingSystemView vanillaView = null;
		vanillaView = (VanillaPlottingSystemView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("uk.ac.diamond.daq.sample-plate-management.ui.plate", null, 3);
		plot = (IPlottingSystem<Composite>) vanillaView.getAdapter(IPlottingSystem.class);
		plot.clear();
		plot.updatePlot2D(loadedDataset, null, "Snapshot Data", new NullProgressMonitor());
		Map<Double, Integer> xInvertedCalibratedAxis = getInvertedDataset(plate.getXCalibratedAxis());
		Map<Double, Integer> yInvertedCalibratedAxis = getInvertedDataset(plate.getYCalibratedAxis());
		for (RegisteredSample sample: plate.getSamples()) {
			Double positionX = sample.getPositionX();
			Double positionY = sample.getPositionY();
			int midPixelX = plate.getXCalibratedAxis().length / 2;
			int midPixelY = plate.getYCalibratedAxis().length / 2;
			int annotationX = xInvertedCalibratedAxis.get(positionX);
			int annotationY = yInvertedCalibratedAxis.get(positionY);
			int direction = 0;
			if (annotationX < midPixelX && annotationY < midPixelY) {
				direction = 0;
			} else if (annotationX < midPixelX && annotationY >= midPixelY) {
				direction = 1;
			} else if (annotationX >= midPixelX && annotationY < midPixelY) {
				direction = 2;
			} else {
				direction = 3;
			}
			addSampleAnnotation(plot, sample.getId() + ": " + sample.getLabel(), annotationX, annotationY, direction);
		}
	}

	private void addSampleAnnotation(IPlottingSystem<Composite> plot, String name, double x, double y, int direction) {
		Axis xAxis = (Axis) plot.getSelectedXAxis();
		Axis yAxis = (Axis) plot.getSelectedYAxis();
		Annotation a = new Annotation(name, xAxis, yAxis);
		a.setAnnotationColor(new Color(null, 0, 255, 0));
		a.setLocation(x, y);
		a.addAnnotationListener((oldX, oldY, newX, newY) -> a.setLocation(oldX, oldY));
		if (direction == 0) {
			a.setdxdy(40, 40);
		} else if (direction == 1) {
			a.setdxdy(40, -40);
		} else if (direction == 2) {
			a.setdxdy(-40, 40);
		} else {
			a.setdxdy(-40, -40);
		}
		IAnnotation annotation = new AnnotationWrapper(a);
		annotation.setShowName(true);
		annotation.setShowPosition(false);
		annotation.setShowInfo(false);
		annotations.add(annotation);
		Display.getDefault().syncExec(() -> plot.addAnnotation(annotation));
		plot.repaint();
	}

	private void showAnnotations(boolean show) {
		for (IAnnotation annotation: annotations) {
			annotation.setVisible(show);
		}
	}

	private Map<Double, Integer> getInvertedDataset(double[] dataset) {
		Map<Double, Integer> invertedDataset = new HashMap<>();
		for (int pixel = 0; pixel < dataset.length; pixel++) {
			invertedDataset.put(dataset[pixel], pixel);
		}
		return invertedDataset;
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		layout.applyTo(label);
		return label;
	}

	private Text addText(Composite parent, GridDataFactory layout, boolean textEnabled) {
		Text text = new Text(parent, SWT.BORDER);
		text.setEnabled(textEnabled);
		layout.applyTo(text);
		return text;
	}

	private Button addButton(Composite parent, String buttonText, GridDataFactory layout, boolean buttonEnabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(buttonText);
		button.setEnabled(buttonEnabled);
		layout.applyTo(button);
		return button;
	}

	private Button addToggleButton(Composite parent, String buttonText, GridDataFactory layout, boolean buttonEnabled) {
		Button button = new Button(parent, SWT.TOGGLE);
		button.setText(buttonText);
		button.setEnabled(buttonEnabled);
		button.setSelection(true);
		layout.applyTo(button);
		return button;
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.swtDefaults().span(span, 1);
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}
}