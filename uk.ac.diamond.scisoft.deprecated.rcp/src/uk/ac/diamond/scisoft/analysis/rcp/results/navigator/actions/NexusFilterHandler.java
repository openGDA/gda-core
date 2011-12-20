/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.DataNavigator;
import uk.ac.gda.common.rcp.util.EclipseUtils;


public class NexusFilterHandler extends AbstractHandler implements IHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(NexusFilterHandler.class);

	private boolean filterNexus = false;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
            final DataNavigator nav = (DataNavigator)EclipseUtils.getActivePage().findView(DataNavigator.ID);
            filterNexus = !filterNexus;
            nav.setNexusFilter(filterNexus);
			
			return Boolean.TRUE;
			
		} catch (Exception e) {
			logger.error("Cannot determine data sets from selected files", e);
			return Boolean.FALSE;
		}
	}

}
