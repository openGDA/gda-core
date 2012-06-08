/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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


import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.observable.IObserver;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPartSite;
import org.springframework.util.StringUtils;

public class GroupCompositeFactory implements CompositeFactory {

	String label;
	private boolean controlledByBaton = false;
	
	public boolean isControlledByBaton() {
		return controlledByBaton;
	}

	public void setControlledByBaton(boolean controlledByBaton) {
		this.controlledByBaton = controlledByBaton;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	Integer columns = 1;
	
	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	List<CompositeFactory> compositeFactories= null;
	
	public List<CompositeFactory> getCompositeFactories() {
		return compositeFactories;
	}

	public void setCompositeFactories(List<CompositeFactory> compositeFactories) {
		this.compositeFactories = compositeFactories;
	}
	
	@Override
	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
		Composite cmp;
		if( StringUtils.hasLength(label)){
			Group grp = new Group(parent,SWT.NONE);
			grp.setText(label);
			cmp = grp;
			GridLayoutFactory.swtDefaults().applyTo(cmp);
			GridDataFactory.swtDefaults().applyTo(cmp);		
		} else {
			cmp = new Composite(parent, SWT.NONE);
			GridLayoutFactory.fillDefaults().applyTo(cmp);
			GridDataFactory.fillDefaults().applyTo(cmp);		
		}
		
		int columnIndex=0;
		Composite currentComp=null;
		for(CompositeFactory compositeFactory : compositeFactories){
			if(currentComp == null){
				currentComp = new Composite(cmp, SWT.NONE);
				GridLayoutFactory.fillDefaults().numColumns(columns).applyTo(currentComp);
				GridDataFactory.fillDefaults().applyTo(currentComp);		
			}
			compositeFactory.createComposite(currentComp, SWT.NONE, iWorkbenchPartSite);
			columnIndex++;
			if(columnIndex == columns){
				columnIndex = 0;
				currentComp = null;
			}
		}
		
		if (controlledByBaton)
			setBatonControl(cmp);

		return cmp;
	}
	
	private void setBatonControl(final Composite comp) {
		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(new IObserver() {
			
			@Override
			public void update(Object observed, Object changeCode) {
				if (changeCode instanceof BatonChanged) {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							boolean batonHeld = InterfaceProvider.getBatonStateProvider().amIBatonHolder();
							comp.setEnabled(batonHeld);
						}
					});
					
				}
			}
		});	
	}
}