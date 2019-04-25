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
import org.eclipse.core.databinding.beans.PojoProperties;
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
 * <p>
 * The options are of type OpenViewOption.
 */
class OpenViewListBoxComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(OpenViewListBoxComposite.class);

	private static final String SHOW_OPTION_NAME = "showOption";

	private final ComboViewer comboShow;
	private OpenViewOption openViewOption;
	private final OpenViewOption defOption;

	private final IObservableValue<OpenViewOption> showOptionObserveValue;
	private final IObservableValue<OpenViewOption> comboShowObservableValue;

	@SuppressWarnings("unchecked")
	public OpenViewListBoxComposite(Composite parent, int style, String label, String tooltipText, List<OpenViewOption> options) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		defOption = options.get(0);

		final Group grpShow = new Group(this, SWT.NONE);
		final GridData gdGrpShow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdGrpShow.widthHint = 151;
		grpShow.setLayoutData(gdGrpShow);
		grpShow.setText(label);
		grpShow.setLayout(new FillLayout(SWT.HORIZONTAL));

		comboShow = new ComboViewer(grpShow, SWT.READ_ONLY);
		comboShow.getControl().setToolTipText(tooltipText);
		comboShow.setContentProvider(ArrayContentProvider.getInstance());
		comboShow.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OpenViewOption) {
					return ((OpenViewOption) element).getLabel();
				}
				return super.getText(element);
			}
		});

		comboShow.setInput(options);

		comboShowObservableValue = ViewersObservables.observeSingleSelection(comboShow);
		showOptionObserveValue = PojoProperties.value(OpenViewListBoxComposite.class, SHOW_OPTION_NAME, OpenViewOption.class).observe(this);

		final DataBindingContext bindingContext = new DataBindingContext();
		bindingContext.bindValue(comboShowObservableValue, showOptionObserveValue);
		showOptionObserveValue.setValue(defOption);
	}

	public OpenViewOption getShowOption() {
		return openViewOption;
	}

	public void setShowOption(OpenViewOption showOption) {
		this.openViewOption = showOption;
		final ViewDefinition vd = openViewOption.getViewDefinition();
		final String viewId = vd.viewId;
		if (viewId != null && viewId.length() > 0) {
			final String secondaryId = StringUtils.hasText(vd.secondaryId) ? vd.secondaryId : null;
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				logger.error("Error opening view {} with secondary id='{}'", viewId, secondaryId, e);
			}
			comboShowObservableValue.setValue(defOption);
		}
	}
}