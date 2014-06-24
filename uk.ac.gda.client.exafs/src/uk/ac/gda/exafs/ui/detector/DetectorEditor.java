/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector;

import java.net.URL;

import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public abstract class DetectorEditor extends RichBeanEditorPart {
	
	protected Detector detector;
	private String serverCommand;
	
	public DetectorEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean, String serverCommand) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.serverCommand = serverCommand;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class)
			return detector.getSashPlotFormComposite().getPlottingSystem();
		return super.getAdapter(clazz);
	}

	@Override
	public void createPartControl(Composite parent) {
		detector = new Detector(serverCommand, this.getSite(), parent, path);
	}
	

	@Override
	public void dispose() {
		if(detector!=null)
			if (detector.getSashPlotFormComposite() != null)
				detector.getSashPlotFormComposite().dispose();
		super.dispose();
	}

}