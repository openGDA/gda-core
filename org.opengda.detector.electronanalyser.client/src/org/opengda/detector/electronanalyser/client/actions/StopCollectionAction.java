package org.opengda.detector.electronanalyser.client.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceView;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class StopCollectionAction extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;
				// TODO the following need to run on a work thread, not on GUI
				// thread??
				List<Region> regions;
				try {
					regions = regionDefView
							.getRegionDefinitionResourceUtil().getRegions();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//	stop the collection queue?
		}
		return null;
	}

}
