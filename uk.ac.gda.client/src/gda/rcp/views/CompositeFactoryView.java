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

package gda.rcp.views;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.springframework.beans.factory.InitializingBean;

public class CompositeFactoryView extends ViewPart implements InitializingBean{

	String viewTitle;


	public String getViewTitle() {
		return viewTitle;
	}

	public void setViewTitle(String viewTitle) {
		this.viewTitle = viewTitle;
	}


	List<CompositeFactory> compositeFactories= null;

	public List<CompositeFactory> getCompositeFactories() {
		return compositeFactories;
	}

	public void setCompositeFactories(List<CompositeFactory> compositeFactories) {
		this.compositeFactories = compositeFactories;
	}


	@Override
	public void createPartControl(Composite parent) {

		setPartName(viewTitle);

		ScrolledComposite scrolledComposite= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite top = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(compositeFactories.size()).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, true).applyTo(top);

		for(CompositeFactory compositeFactory : compositeFactories){
			Composite composite = compositeFactory.createComposite(top, SWT.NONE);
			GridDataFactory.fillDefaults().applyTo(composite);
		}

		scrolledComposite.setContent(top);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setShowFocusedControl(true);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

}
