package org.opengda.detector.electronanalyser.client.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.views.SequenceView;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartCollectionAction extends AbstractHandler implements IHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(StartCollectionAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;
			try {
				if (regionDefView instanceof SequenceView) {
					((SequenceView)regionDefView).doSave(new NullProgressMonitor());
				}

				// TODO the following need to run on a work thread, not on GUI
				// thread??
				List<Region> regions = regionDefView
						.getRegionDefinitionResourceUtil().getRegions();
				for (Region region : regions) {
					if (region.isEnabled()) {
						// send region parameters to EPICS driver
						// set a region running status before start collection
						// in EPICS for this region
						// status should be reset by monitor EPICS State PV
						// wait for EPICS collection to finish i.e. status is
						// not RUNNING before start next
						// TODO using QUEUE here?
					}
				}
			} catch (IOException e2) {
				logger.error("Cannot save data to the sequence file: {}",
						regionDefView.getRegionDefinitionResourceUtil()
								.getFileName(), e2);
			} catch (Exception e2) {
				logger.error("Cannot get resource.", e2);
			}
		}
		return null;
	}
}
