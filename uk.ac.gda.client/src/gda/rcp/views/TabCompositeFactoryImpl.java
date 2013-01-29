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


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TabCompositeFactoryImpl implements TabCompositeFactory {
	
	private Image image;
	private CompositeFactory compositeFactory;
	private String imagePluginId;
	private String imageFilePath;
	private String tooltip;
	private String label;
	
	

	public CompositeFactory getCompositeFactory() {
		return compositeFactory;
	}


	public void setCompositeFactory(CompositeFactory compositeFactory) {
		this.compositeFactory = compositeFactory;
	}


	public String getImagePluginId() {
		return imagePluginId;
	}


	public void setImagePluginId(String imagePluginId) {
		this.imagePluginId = imagePluginId;
	}


	public String getImageFilePath() {
		return imageFilePath;
	}


	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}


	@Override
	public Image getImage() {
		if (image == null && imagePluginId != null) {
			image = AbstractUIPlugin.imageDescriptorFromPlugin(imagePluginId, imageFilePath).createImage();
		}
		return image;
	}


	@Override
	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
		return compositeFactory.createComposite(parent, SWT.NONE, null);
	}


	@Override
	public String getTooltip() {
		return tooltip != null ? tooltip : label;
	}


	@Override
	public String getLabel() {
		return label;
	}


	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}


	public void setLabel(String label) {
		this.label = label;
	}


}
