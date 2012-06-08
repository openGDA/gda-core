/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to disconnect the current XYPlotView and create a new one
 */
public class XYPlotNewHandler extends AbstractHandler{
	private static final Logger logger = LoggerFactory.getLogger(XYPlotNewHandler.class);		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			IWorkbenchPart view = HandlerUtil.getActivePartChecked(event);
			if(view instanceof XYPlotView){
				XYPlotView xyview = (XYPlotView)view;
				if(!xyview.isDisconnected()){
					xyview.setConnect(false);
				}
				final IWorkbenchPage page = HandlerUtil.getActiveSite(event).getPage();
				final IViewPart      part = page.showView(XYPlotView.ID, XYPlotView.getUniqueSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);
				page.activate(part);
			}
		}
		catch (Exception e){
			logger.error(e.getMessage(),e);
		}
		return null;
	}
}
