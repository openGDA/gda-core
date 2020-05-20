package uk.ac.diamond.daq.client.gui.camera;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionComposite;
import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraCalibrationComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.HistogramComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.roi.SensorROIComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Composes instantiates the Composites for the Camera Configuration
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationFactory implements CompositeFactory {

	private final ImagingCameraConfigurationController controller;

	private Composite topArea;
	private Composite bottomArea;

	protected CameraImageComposite cameraImageComposite;
	private Composite histogram;
	
	private Composite container;
	private Composite menuBar;
	private Composite viewStream;
	private Composite viewStats;
	private Composite viewHisto;
	private Composite tabsContainer;

	public CameraConfigurationFactory(ImagingCameraConfigurationController controller) {
		this.controller = controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		configureLayout(parent, style);
		try {
			createCameraImageComposite(viewStream);
			createHistogramComposite(viewHisto);
		} catch (Exception e) {
			UIHelper.showError("Cannot create CameraConfiguration", e);
			return null;
		}

		try {
			CompositeFactory cf = createTabFactory();
			bottomArea = cf.createComposite(tabsContainer, SWT.NONE);
		} catch (GDAClientException e1) {
			UIHelper.showError("Cannot create CameraConfiguration", e1);
			return null;
		}
		return container;
	}

	private void configureLayout(Composite parent, int style) {
		UUID uuid = UUID.randomUUID();
		container = ClientSWTElements.createComposite(parent, style, 100);
		container.setData(CompositeFactory.COMPOSITE_ROOT, uuid);
		container.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		menuBar = ClientSWTElements.createGroup(container, 1, ClientMessages.EMPTY_MESSAGE);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 100, 8);
		menuBar.setLayoutData(gridData);
		CompositeFactory cf = new StreamControlCompositeFactory();
		cf.createComposite(menuBar, SWT.HORIZONTAL);
		
		
		viewStream = ClientSWTElements.createComposite(container, style);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 100, 124);
		viewStream.setLayoutData(gridData);
		
		viewStats = ClientSWTElements.createGroup(container, 1, ClientMessages.EMPTY_MESSAGE);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 100, 8);
		viewStats.setLayoutData(gridData);
		
		
		viewHisto = ClientSWTElements.createComposite(container, style);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 30, 50);
		viewHisto.setLayoutData(gridData);
		
		tabsContainer = ClientSWTElements.createComposite(container, style);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 70, 50);
		tabsContainer.setLayoutData(gridData);
	}
	
	private void createCameraImageComposite(Composite panel) throws Exception {
		cameraImageComposite = new CameraImageComposite(panel, SWT.NONE);
	}

	private void createHistogramComposite(Composite panel) throws Exception {
		histogram = new HistogramComposite(panel, cameraImageComposite.getPlottingSystem(), SWT.NONE);
	}

	private CompositeFactory createTabFactory() throws GDAClientException {
		TabFolderBuilder builder = new TabFolderBuilder();
		//builder.addTab(createStreamControlCompositeFactory());
		builder.addTab(createExposureCompositeFactory());
		builder.addTab(createAbsorptionCompositeFactory());
		builder.addTab(createROICompositeFactory());
		builder.addTab(createCalibrationFactory());
		return builder.build();
	}

	private final TabCompositeFactory createStreamControlCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new StreamControlCompositeFactory();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.STREAM));
		return group;
	}

	private final TabCompositeFactory createROICompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new SensorROIComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ROI));
		return group;
	}

	private final TabCompositeFactory createExposureCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new CameraConfigurationComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.CAMERA));
		return group;
	}

	private final TabCompositeFactory createAbsorptionCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		group.setCompositeFactory(new AbsorptionComposite());
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

}
