package uk.ac.diamond.daq.arpes.ui.e4.views;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.dawnsci.plotting.tools.profile.BoxLineProfileTool;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.tool.ToolPageFactory;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpesRoiProfilePlotViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(ArpesRoiProfilePlotViewE4.class);
	private IEclipseContext context;
	private IPlottingSystem<?> plottingSystem;
	private BoxLineProfileTool sideProfile1;
	private SashForm sashForm;
	private String targetPlotID;

	@Inject
	private UISynchronize uiSync;

	// Constructor
	@Inject
	ArpesRoiProfilePlotViewE4(IEclipseContext context, @Named("targetPlotID") @Active @Optional String targetPlotID) {
		this.targetPlotID = (targetPlotID != null) ? targetPlotID : "Image";
		this.context = context;
		logger.debug(targetPlotID);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		// Delay view creation as otherwise target view plotting system is not ready yet
		uiSync.asyncExec(this::delayedCreation);
	}

	private Runnable delayedCreation() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e2) {
			logger.error(e2.toString());
		}

		// Find target view
		MPart view = context.get(EPartService.class).findPart(targetPlotID);
		plottingSystem = context.get(IPlottingService.class).getPlottingSystem(view.getLabel());

		// Create new region if there are no regions
		if (plottingSystem.getRegions().isEmpty()) {
			try {
				IRegion newRegion = plottingSystem.createRegion("InitROI1", RegionType.PERIMETERBOX);
				IROI roi = new PerimeterBoxROI(10, 10, 500, 500, 0);
				newRegion.setROI(roi);
				plottingSystem.addRegion(newRegion);
			} catch (Exception e1) {
				logger.error(e1.toString());
			}
		}

		// Set up boxLineProfile tool - see also
		// uk.ac.diamond.scisoft.analysis.rcp.plotting.ROIProfilePlotWindow
		IToolPageSystem toolSystem = plottingSystem.getAdapter(IToolPageSystem.class);
		try {
			sideProfile1 = (BoxLineProfileTool) ToolPageFactory
					.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
			sideProfile1.setLineOrientation(false);
			sideProfile1.setPlotEdgeProfile(false);
			sideProfile1.setPlotAverageProfile(true);
			sideProfile1.setToolSystem(toolSystem);
			sideProfile1.setPlottingSystem(plottingSystem);
			sideProfile1.setTitle("EDC_profile");
			sideProfile1.setToolId(String.valueOf(sideProfile1.hashCode()));
			sideProfile1.createControl(sashForm);
			sideProfile1.activate();
			sideProfile1.setIsUIJob(true);
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return null;
	}

	@PreDestroy
	public void dispose() {
		try {
			if (plottingSystem != null) {
				plottingSystem.dispose();
			}
			if (sideProfile1 != null) {
				sideProfile1.dispose();
			}
		} catch (Exception ne) {
			logger.error(ne.toString());
		}
	}

	@Focus
	public void setFocus() {
		this.plottingSystem.setFocus();
	}
}
