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
		CompositeFactoryView view = createView();
		view.setViewTitle(viewTitle);
		view.setCompositeFactories(compositeFactories);
		return view;
	}
	
	protected CompositeFactoryView createView() {
		CompositeFactoryView view = new CompositeFactoryView();
		return view;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
