package org.opengda.detector.electronanalyser.client.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.RegionCommand;
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
			IRegionDefinitionView sequenceView = (IRegionDefinitionView) activePart;
			try {
				if (sequenceView instanceof SequenceView) {
					((SequenceView)sequenceView).doSave(new NullProgressMonitor());
				}

				// TODO the following need to run on a work thread, not on GUI
				// thread??
				List<Region> regions = sequenceView
						.getRegionDefinitionResourceUtil().getRegions();
				for (Region region : regions) {
					if (region.isEnabled()) {
						final RegionCommand command=new RegionCommand(region);
						//(SequenceView)sequenceView).getProcessor().addToTail(command);
						Job job = new Job(command.getDescription()) {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									command.run();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return Status.OK_STATUS;
							}
						};
//						job.set
//						job.schedule();
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
						sequenceView.getRegionDefinitionResourceUtil()
								.getFileName(), e2);
			} catch (Exception e2) {
				logger.error("Cannot get resource.", e2);
			}
		}
		return null;
	}
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return super.isEnabled();
	}
}
