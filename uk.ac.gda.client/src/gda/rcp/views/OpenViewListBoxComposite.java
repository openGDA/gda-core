/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Displays a button with title - when pressed opens the view specified in the selected option 
 * 
 * The options are of type OpenViewOption. 
 */
class OpenViewListBoxComposite extends Composite{
	
	private ComboViewer comboShow;
	private OpenViewOption openViewOption;
	
	OpenViewOption defOption;
	
	IObservableValue showOptionObserveValue;
	IObservableValue comboShowObservableValue;
	
	private static final Logger logger = LoggerFactory.getLogger(OpenViewListBoxComposite.class);
	public OpenViewListBoxComposite(Composite parent, int style, String label, String tooltipText, List<OpenViewOption> options) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		defOption = options.get(0);
		Group grpShow = new Group(this, SWT.NONE);
		GridData gd_grpShow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpShow.widthHint = 151;
		grpShow.setLayoutData(gd_grpShow);
		grpShow.setText(label);
		grpShow.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		comboShow = new ComboViewer(grpShow, SWT.READ_ONLY);
		comboShow.getControl().setToolTipText(tooltipText);
		comboShow.setContentProvider(ArrayContentProvider.getInstance());
		comboShow.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof OpenViewOption) {
					OpenViewOption opt = (OpenViewOption) element;
					return opt.getLabel();
				}
				return super.getText(element);
			}

		});


		comboShow.setInput(options);

		comboShowObservableValue = ViewersObservables.observeSingleSelection(comboShow);
		showOptionObserveValue = PojoObservables.observeValue(this, showOptionName);

		DataBindingContext bindingContext = new DataBindingContext();
		bindingContext.bindValue(comboShowObservableValue, showOptionObserveValue);
		showOptionObserveValue.setValue(defOption);

//		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboShow.getControl());
	}
	static final String showOptionName = "showOption";

	public OpenViewOption getShowOption() {
		return openViewOption;
	}

	public void setShowOption(OpenViewOption showOption) {
		this.openViewOption = showOption;
		ViewDefinition vd = openViewOption.getViewDefinition();
		String viewId = vd.viewId;
		if( viewId != null && viewId.length()>0){
			String secondaryId = StringUtils.hasText(vd.secondaryId) ? vd.secondaryId : null;
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId,secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				logger.error("Error opening  view " + viewId + " with sec id='" +secondaryId +"'" , e);
			}
			comboShowObservableValue.setValue(defOption);
		}
	}	
	
}