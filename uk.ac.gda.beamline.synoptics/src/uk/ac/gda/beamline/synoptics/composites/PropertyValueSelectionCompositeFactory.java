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

package uk.ac.gda.beamline.synoptics.composites;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.rcp.views.CompositeFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.InitializingBean;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.beamline.synoptics.utils.ListenableProperty;
import uk.ac.gda.ui.utils.SWTUtils;
/**
 * A Spring bean configurable factory that creates a drop down list of values for a specified property
 * with a label text to the left. The list of values can be defined explicitly in the bean if 
 * {@link #listIsSetExplicitlyInProperty} is set to false or use the listed value in the properties file 
 * which defines this specified property (the default).
 * 
 * Property {@link LocalProperties#GDA_DATAWRITER_DIR} is processed differently because it contains $template$
 * for substitution at runtime.
 * 
 * Example bean definition:
 * <pre>
 * {@code
<bean class="uk.ac.gda.beamline.synoptics.composites.PropertyValueSelectionCompositeFactory">
	<property name="eventAdminName" value="eventadmin" />
	<property name="label" value="Directory" />
	<property name="listIsSetExplicitlyInProperty" value="false" />
	<property name="propertyName">
		<util:constant static-field="gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR"/>
	</property>
</bean>
 * }
 * </pre>
 * {@link #eventAdminName} is the name of an event administrator 'eventadmin' which is a findable instance 
 * of the {@link Scriptcontroller} used to pass event from server to client. If this is not specified, the 
 * property value displayed in this drop down list will not be the same as that is set on the GDA server.
 * 
 * @author fy65
 *
 */
public class PropertyValueSelectionCompositeFactory implements CompositeFactory, InitializingBean {

	private String label;
	private String propertyName;
	private String[] valueList=null;
	//default support multi-valued property
	private boolean listIsSetExplicitlyInProperty=true;
	private String eventAdminName;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		if (isListIsSetExplicitlyInProperty()) {
			//support multi-valued property
			setValueList(LocalProperties.getStringArray(getPropertyName()));
		} else {
			//GDA data folders - visit ids
			if (getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR)) {
				//populate the list with visit proposal folders on the storage disk.
				setValueList(getVisitFolders());
			}
			// property with value list configured in XML bean just pass through here
		}
		ListenableProperty property=new ListenableProperty(getPropertyName());
		PropertyValueSelectionComposite propertyValueSelectionComposite = new PropertyValueSelectionComposite(parent, style, label, property, getValueList(), eventAdminName);
		propertyValueSelectionComposite.initialisation();
		return propertyValueSelectionComposite;
	}

	public static void main(String... args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new BorderLayout());

		String propertyName="my.test.property";
		ListenableProperty property = new ListenableProperty(propertyName);
		final PropertyValueSelectionComposite comp = new PropertyValueSelectionComposite(shell, SWT.NONE, "Test Property",property, new String[] {"item1", "item2"},"eventadmin");
		comp.setLayoutData(BorderLayout.NORTH);
		comp.setVisible(true);
		shell.pack();
		shell.setSize(400, 400);
		SWTUtils.showCenteredShell(shell);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!isListIsSetExplicitlyInProperty() && !getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR) && getValueList()==null) {
			throw new IllegalStateException("value list for property ["+getPropertyName()+"] is null");
		}

	}

	public String[] getValueList() {
		return valueList;
	}

	public void setValueList(String[] valueList) {
		this.valueList = valueList;
	}

	public boolean isListIsSetExplicitlyInProperty() {
		return listIsSetExplicitlyInProperty;
	}

	public void setListIsSetExplicitlyInProperty(
			boolean listIsSetExplicitlyInProperty) {
		this.listIsSetExplicitlyInProperty = listIsSetExplicitlyInProperty;
	}
	private String[] getVisitFolders() {
		String propertyValue = LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR);
		if (propertyValue.contains("$visit$/$subdirectory$")) {
			propertyValue=propertyValue.replace("$visit$/$subdirectory$", "");
		}
		String visitParentPath=PathConstructor.createFromTemplate(propertyValue);
		File dir=new File(visitParentPath);
		String[] list = dir.list();
		List<String> dirList=new ArrayList<String>();
		if (list != null) {
			for (String s : list) {
				File file=new File(visitParentPath+File.separator+s);
				if (file.isDirectory()) {
					dirList.add(file.getAbsolutePath());
				}
			}
		}
		return dirList.toArray(new String[0]);
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}
