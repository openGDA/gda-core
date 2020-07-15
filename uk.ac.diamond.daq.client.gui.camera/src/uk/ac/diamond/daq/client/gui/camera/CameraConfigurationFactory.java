package uk.ac.diamond.daq.client.gui.camera;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionComposite;
import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraCalibrationComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.HistogramComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlData;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.diamond.daq.client.gui.camera.positioning.CameraPositioningComposite;
import uk.ac.diamond.daq.client.gui.camera.roi.SensorSelectionComposite;
import uk.ac.diamond.daq.client.gui.camera.settings.CameraSettingsComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Composes instantiates the Composites for the Camera Configuration
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationFactory implements CompositeFactory {

	protected CameraImageComposite cameraImageComposite;

	// The overall container for this components
	private Composite container;
	
	private Composite menuBar;
	private Composite viewStream;
	private Composite viewStats;
	private Composite viewHisto;
	private Composite tabsContainer;
	
	private StreamController streamController;

	@Override
	public Composite createComposite(Composite parent, int style) {
		UUID uuid = UUID.randomUUID();
		try {
			configureController(uuid);
		} catch (GDAClientException e) {
			UIHelper.showWarning(e.getMessage(), e);
		}
		configureLayout(parent, style, uuid);
		try {
			createCameraImageComposite(viewStream);
			createHistogramComposite(viewHisto);
		} catch (Exception e) {
			UIHelper.showError("Cannot create CameraConfiguration", e);
			return null;
		}

		try {
			createTabFactory().createComposite(tabsContainer, SWT.NONE);
		} catch (GDAClientException e1) {
			UIHelper.showError("Cannot create CameraConfiguration", e1);
			return null;
		}
		return container;
	}

	private void configureLayout(Composite parent, int style, UUID uuid) {
	    container = createClientCompositeWithGridLayout(parent, SWT.NONE, 100);
	    createClientGridDataFactory().applyTo(container);
	    container.setData(CompositeFactory.COMPOSITE_ROOT, uuid);

		menuBar = createClientGroup(container, SWT.NONE, 1, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().span(100, 1).applyTo(menuBar);
		CompositeFactory cf = new StreamControlCompositeFactory(streamController);
		cf.createComposite(menuBar, SWT.HORIZONTAL);

		viewStream = createClientCompositeWithGridLayout(container, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).span(100, 1).applyTo(viewStream);

		viewStats = createClientGroup(container, SWT.NONE, 1, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).span(100, 3).applyTo(viewStats);

		viewHisto = createClientCompositeWithGridLayout(container, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).span(40, 30).applyTo(viewHisto);

		tabsContainer = createClientCompositeWithGridLayout(container, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).span(60, 30).applyTo(tabsContainer);
	}

	private void createCameraImageComposite(Composite panel) throws Exception {
		cameraImageComposite = new CameraImageComposite(panel, SWT.NONE);
	}

	private void createHistogramComposite(Composite parent) throws Exception {
		new HistogramComposite(cameraImageComposite.getPlottingSystem()).createComposite(parent, SWT.NONE);
		
	}

	private CompositeFactory createTabFactory() throws GDAClientException {
		TabFolderBuilder builder = new TabFolderBuilder();
		builder.addTab(createSettingsCompositeFactory());
		builder.addTab(createPositioningCompositeFactory());
		builder.addTab(createAbsorptionCompositeFactory());
		builder.addTab(createROICompositeFactory());
		builder.addTab(createCalibrationFactory());
		return builder.build();
	}

	private final TabCompositeFactory createROICompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new SensorSelectionComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ROI));
		return group;
	}

	private final TabCompositeFactory createSettingsCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new CameraSettingsComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.SETTINGS));
		return group;
	}

	private final TabCompositeFactory createPositioningCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new CameraPositioningComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.POSITIONING));
		return group;
	}
	
	private final TabCompositeFactory createAbsorptionCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		group.setCompositeFactory(new AbsorptionComposite(cameraImageComposite));
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ABSORPTION));
		group.setTooltip(ClientMessagesUtility.getMessage(ClientMessages.ABSORPTION_TP));
		return group;
	}

	/**
	 * Adds the Beam-Camera Mapping tab.
	 * 
	 * @return
	 */
	private final TabCompositeFactory createCalibrationFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		group.setCompositeFactory(new BeamCameraCalibrationComposite());
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.BEAM_CAMERA_MAPPING));
		group.setTooltip(ClientMessagesUtility.getMessage(ClientMessages.BEAM_CAMERA_MAPPING_TP));
		return group;
	}

	private void configureController(UUID uuid) throws GDAClientException {
		streamController = CameraHelper.getCameraComboItems().stream().findFirst().map(i -> {
			return new StreamController(new StreamControlData(i, StreamType.EPICS_ARRAY), uuid);
		}).orElseThrow(() -> new GDAClientException("No camera available"));
	}
}
