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





import java.util.HashMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.springframework.beans.factory.InitializingBean;


public class TabFolderCompositeFactory implements CompositeFactory, InitializingBean {
	protected TabCompositeFactory[] factories;

	@Override
	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
		final TabFolderComposite comp = new TabFolderComposite(parent, style, iWorkbenchPartSite, factories);
		comp.createControls();
		return comp;
	}


	public void setFactories(TabCompositeFactory[] factories) {
		this.factories = factories;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if (factories == null) {
			throw new IllegalArgumentException("availableModes == null");
		}
	}

}

class TabFolderComposite extends Composite {
	
	private CTabFolder tabFolder;
	protected TabCompositeFactory[] availableModes;
	private HashMap<TabCompositeFactory, CTabItem> tabs;
	private final IWorkbenchPartSite iWorkbenchPartSite;
	
	public TabFolderComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite,  TabCompositeFactory[] availableModes) {
		super(parent, style);
		this.iWorkbenchPartSite = iWorkbenchPartSite;
		this.availableModes = availableModes;
	}	
	
	
	public CTabFolder getTabFolder() {
		return tabFolder;
	}


	void createControls() {
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,0).spacing(0,0).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);		
		tabFolder = new CTabFolder(this, SWT.TOP | SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,0).spacing(0,0).applyTo(tabFolder);
		GridDataFactory.fillDefaults().applyTo(tabFolder);
		
		tabs = new HashMap<TabCompositeFactory, CTabItem>();
		for (int i = 0; i < availableModes.length; i++) {
			TabCompositeFactory mode = availableModes[i];
			CTabItem cTab = new CTabItem(tabFolder, SWT.NONE);
			Image tabImage = mode.getImage();
			if (tabImage != null){
				cTab.setImage(tabImage);
			}
			cTab.setText(mode.getLabel());
			cTab.setToolTipText(mode.getTooltip());
			Control control = mode.createComposite(tabFolder, SWT.NONE, iWorkbenchPartSite);
			cTab.setControl(control);
			tabs.put(mode, cTab);
			
		}
		tabFolder.setSelection(0);
		setVisible(true);
		
	}

}
