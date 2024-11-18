package uk.ac.diamond.daq.arpes.ui.e4.views;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.tool.ToolPageFactory;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpesRoiSummaryViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(ArpesRoiSummaryViewE4.class);
	private IPlottingSystem<?> plottingSystem;
	private IEclipseContext context;
	private ExpandableComposite mainRegionInfoExpander;
	private Composite mainRegionComposite;
	private AbstractToolPage roiSumProfile;
	private Group regionSumGroup;
	private String targetPlotID;

	// Constructor
	@Inject
	ArpesRoiSummaryViewE4(@Named("targetPlotID") @Active @Optional String targetPlotID, IEclipseContext context) {
		this.targetPlotID = (targetPlotID != null) ? targetPlotID : "DetectorData";
		this.context = context;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());
		mainRegionInfoExpander = new ExpandableComposite(parent, SWT.NONE);
		mainRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		mainRegionInfoExpander.setLayout(new GridLayout(1, false));
		mainRegionInfoExpander.setText("Main Region Of Interest");
		mainRegionComposite = new Composite(mainRegionInfoExpander, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		mainRegionComposite.setLayout(new GridLayout(1, false));
		mainRegionComposite.setLayoutData(gridData);
		regionSumGroup = new Group(mainRegionComposite, SWT.NONE);
		regionSumGroup.setText("Sum");
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		regionSumGroup.setLayout(new GridLayout(1, false));
		regionSumGroup.setLayoutData(gridData);
		setupRoiSumProfile();
	}

	private void setupRoiSumProfile() {
		// Find target view
		MPart view = context.get(EPartService.class).findPart(targetPlotID);
		plottingSystem = context.get(IPlottingService.class).getPlottingSystem(view.getLabel());
		if (plottingSystem == null) {
			return;
		}

		// see uk.ac.diamond.scisoft.analysis.rcp.plotting.ROIProfilePlotWindow
		IToolPageSystem tps = plottingSystem.getAdapter(IToolPageSystem.class);
		try {
			roiSumProfile = (AbstractToolPage) ToolPageFactory
					.getToolPage("org.dawb.workbench.plotting.tools.regionSumTool");
			roiSumProfile.setToolSystem(tps);
			roiSumProfile.setPlottingSystem(plottingSystem);
			roiSumProfile.setTitle("_Region_Sum");
			roiSumProfile.setToolId(String.valueOf(roiSumProfile.hashCode()));
			roiSumProfile.createControl(regionSumGroup);
			roiSumProfile.activate();
			mainRegionInfoExpander.setClient(mainRegionComposite);
			mainRegionInfoExpander.setExpanded(true);
		} catch (Exception e) {
			logger.error("Failed to set Roi sum profile", e);
		}

		// add Region and ROI if there are no any
		if (plottingSystem.getRegions().isEmpty()) {
			IRegion newRegion;
			try {
				newRegion = plottingSystem.createRegion("InitROI", RegionType.PERIMETERBOX);
				IROI roi = new PerimeterBoxROI(20, 83, 970, 742, 0);
				newRegion.setROI(roi);
				plottingSystem.addRegion(newRegion);
			} catch (Exception e) {
				logger.error("Failed to create ROI", e);
			}
		}
	}

	@PreDestroy
	public void dispose() {
		try {
			if (plottingSystem != null) {
				plottingSystem.dispose();
			}
			if (roiSumProfile != null) {
				roiSumProfile.dispose();
			}
		} catch (Exception ne) {
			logger.error(ne.toString());
		}
	}

	@Focus
	public void setFocus() {
		plottingSystem.setFocus();
	}
}
