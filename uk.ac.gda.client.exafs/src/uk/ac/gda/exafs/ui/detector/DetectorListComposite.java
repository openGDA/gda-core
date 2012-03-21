/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.IExpansionListener;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.richbeans.components.selector.GridListEditor;

import com.swtdesigner.SWTResourceManager;

public class DetectorListComposite extends Composite {

	protected GridListEditor           detectorList;
	private DetectorElementComposite detectorElementComposite;

	public DetectorListComposite(final Composite                  parent, 
            final Class<? extends IDetectorElement>    editorClass, 
            final int elementListSize,
            final Class<? extends DetectorROI>    regionClass,
            final IDetectorROICompositeFactory regionEditorFactory, Boolean showAdvanced) {
		super(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		// Important Note: This is designed to work with more than one detector element.
		// That simply comes from the data, if it has four elements it will show the grid.
		this.detectorList = new GridListEditor(this, SWT.NONE, elementListSize);
		detectorList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		detectorList.setEditorClass(editorClass);
		
		this.detectorElementComposite = new DetectorElementComposite(detectorList, SWT.NONE, elementListSize > 1,
				regionClass, regionEditorFactory,showAdvanced);
		detectorElementComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
		detectorList.setEditorUI(detectorElementComposite);
		detectorList.setGridWidth(200);
		detectorList.setEnabled(false);
		detectorList.setAdditionalLabelProvider(new ColumnLabelProvider() {
		    private final Color lightGray   = SWTResourceManager.getColor(SWT.COLOR_GRAY);
			@Override
			public Color getForeground(Object element) {
				if (element instanceof IDetectorElement) {
					IDetectorElement detectorElement = (IDetectorElement) element;
					if (detectorElement.isExcluded())
						return lightGray;
				}
				return null;
			}
			@Override
			public String getText(Object element) {
				return null;
			}
		});
			

	}
	



	/**
	 * Notified when the advanced section is expanded.
	 * @param l
	 */
	public void addExpansionListener(IExpansionListener l) {
		detectorElementComposite.addExpansionListener(l);
	}
	
	public void removeExpansionListener(IExpansionListener l){
		detectorElementComposite.removeExpansionListener(l);
	}


	public DetectorElementComposite getDetectorElementComposite() {
		return detectorElementComposite;
	}


	public GridListEditor getDetectorList() {
		return detectorList;
	}

}
