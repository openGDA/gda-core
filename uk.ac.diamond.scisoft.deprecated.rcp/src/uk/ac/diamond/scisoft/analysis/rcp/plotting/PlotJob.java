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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

/**
 * Job object
 */

public class PlotJob {

	private PlotJobType type;
	private GuiBean guiBean = null;
	
	/**
	 * Constructor for a new PlotJob
	 * @param type
	 */
	
	public PlotJob(PlotJobType type)
	{
		this.type = type;
	}
	
	/**
	 * Get the type of the job
	 * @return type of the job
	 */
	
	public PlotJobType getType()
	{
		return type;
	}

	/**
	 * Set the GuiBean object for this job
	 * @param guiBean
	 */
	public void setGuiBean(GuiBean guiBean) {
		this.guiBean = guiBean;
	}

	/**
	 * Get the GuiBean object for this job
	 * @return the GuiBean object if exist
	 */
	public GuiBean getGuiBean() {
		return guiBean;
	}
}
