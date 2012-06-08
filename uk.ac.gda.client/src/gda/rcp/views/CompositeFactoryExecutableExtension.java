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

package gda.rcp.views;


import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.springframework.beans.factory.InitializingBean;

public class CompositeFactoryExecutableExtension extends AbstractFindableExecutableExtension{

	
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
	public Object create() throws CoreException {
		CompositeFactoryView view = new CompositeFactoryView();
		view.setViewTitle(viewTitle);
		view.setCompositeFactories(compositeFactories);
		return view;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}

class CompositeFactoryView extends ViewPart implements InitializingBean{
	
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
		Group grp= new Group(parent, SWT.NONE);
		GridLayout statusLayout = new GridLayout(compositeFactories.size(), false);
		grp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
		grp.setLayout(statusLayout);

		for(CompositeFactory compositeFactory : compositeFactories){
			Composite composite = compositeFactory.createComposite(grp, SWT.NONE, this.getSite());
			GridDataFactory.fillDefaults().applyTo(composite);
		}
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

}
