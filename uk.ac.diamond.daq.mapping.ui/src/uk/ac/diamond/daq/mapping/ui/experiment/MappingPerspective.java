package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.event.ui.view.StatusQueueView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

public class MappingPerspective implements IPerspectiveFactory {

	public static final String ID = "uk.ac.diamond.daq.mapping.ui.experiment.MappingPerspective";

	private static final Logger logger = LoggerFactory.getLogger(MappingPerspective.class);

	@Override
	public void createInitialLayout(IPageLayout layout) {

		logger.trace("Building Mapping perspective");
		layout.setEditorAreaVisible(false);

		IFolderLayout left = layout.createFolder("mappeddata", IPageLayout.RIGHT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		left.addView("org.dawnsci.mapping.ui.mappeddataview");
		IViewLayout vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mappeddataview");
		vLayout.setCloseable(false);

		IFolderLayout dataLayout = layout.createFolder("map", IPageLayout.RIGHT, 0.2f, "mappeddata");
		dataLayout.addView("org.dawnsci.mapping.ui.mapview");
		vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mapview");
		vLayout.setCloseable(false);

		IFolderLayout mappingParams = layout.createFolder("params", IPageLayout.RIGHT, 0.65f, "map");
		mappingParams.addView("uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView");
		vLayout = layout.getViewLayout("uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView");
		vLayout.setCloseable(false);

		IFolderLayout dataoutLayout = layout.createFolder("spectrum", IPageLayout.BOTTOM, 0.5f, "map");
		dataoutLayout.addView("org.dawnsci.mapping.ui.spectrumview");
		vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.spectrumview");
		vLayout.setCloseable(false);

		IFolderLayout folderLayout = layout.createFolder("console_folder", IPageLayout.BOTTOM, 0.65f, "params");
		folderLayout.addView("gda.rcp.jythonterminalview");
		String queueViewId = StatusQueueView.createId(LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, ""),
				"org.eclipse.scanning.api",
				"org.eclipse.scanning.api.event.scan.ScanBean",
				IEventService.STATUS_SET,
				IEventService.STATUS_TOPIC,
				IEventService.SUBMISSION_QUEUE);

		queueViewId = queueViewId + "partName=Queue";
		folderLayout.addView(queueViewId);

		folderLayout.addView("uk.ac.gda.client.livecontrol.LiveControlsView");
		folderLayout.addView("uk.ac.gda.client.liveplot.mjpeg.LiveMJPEGView");

		logger.trace("Finished building Mapping Visualisation perspective");
	}
}
