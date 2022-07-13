/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


public class DatFilterHandler extends AbstractHandler implements IHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DatFilterHandler.class);

	private boolean filterDat = false;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
            final DataNavigator nav = (DataNavigator)EclipseUtils.getActivePage().findView(DataNavigator.ID);
            filterDat = !filterDat;
            nav.setAsciiFilter(filterDat);
			
			return Boolean.TRUE;
			
		} catch (Exception e) {
			logger.error("Cannot determine data sets from selected files", e);
			return Boolean.FALSE;
		}
	}

}
