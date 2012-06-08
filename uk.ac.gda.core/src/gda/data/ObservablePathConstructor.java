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

package gda.data;

import gda.data.metadata.GdaMetadata;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.MetadataEntry;
import gda.device.DeviceBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An {@link ObservablePathProvider} that when configured with a template of the form expected by {@link PathConstructor}
 * creates a path using {@link PathConstructor}. A {@link PathChanged} event is sent to registered Observers when
 * any {@link MetadataEntry} referenced by the template changes.
 */
public class ObservablePathConstructor extends DeviceBase implements ObservablePathProvider, Findable, IObserver, InitializingBean  {

	private static final Logger logger = LoggerFactory.getLogger(ObservablePathConstructor.class);
	
	private String template;
	
	private GdaMetadata gdaMetadata;

	private List<IMetadataEntry> referedMetadataEntries;
			
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
		extractReferedMetadataEntries();
		registerWithReferedMetadataEntries();
	}

	@Override
	public void configure() throws FactoryException {
		extractReferedMetadataEntries();
		registerWithReferedMetadataEntries();
	}
	
	private void extractReferedMetadataEntries() {
		referedMetadataEntries =  new ArrayList<IMetadataEntry>();
		if (gdaMetadata == null) {
			return;
		}
		
		StringTokenizer st = new StringTokenizer(template, "$");
		ArrayList<IMetadataEntry> allEntries = gdaMetadata.getMetadataEntries();
		while (st.hasMoreTokens()) {
			IMetadataEntry possiblyEntry = pickMetadataEntry(allEntries, st.nextToken());
			if (possiblyEntry != null) {
				referedMetadataEntries.add(possiblyEntry);
			}
		}
	}
	
	private void registerWithReferedMetadataEntries() {
		for (IMetadataEntry entry : getReferedMetadataEntries()) {
			entry.addIObserver(this);
		}	
	}
	
	private IMetadataEntry pickMetadataEntry(List<IMetadataEntry> entries, String name) {
		for (IMetadataEntry entry : entries) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null; // Not metadata;
	}

	@Override
	public String getPath() {
		return PathConstructor.createFromTemplate(getTemplate());
	}

	public void setGdaMetadata(GdaMetadata gdaMetadata) {
		this.gdaMetadata = gdaMetadata;
		
	}
	
	public GdaMetadata getGdaMetadata() {
		return gdaMetadata;
	}

	public List<IMetadataEntry> getReferedMetadataEntries() {
		return referedMetadataEntries;
	}

	@Override
	public void update(Object source, Object arg) {
		logger.info(getName() + " path updated to: " + getPath());
		if (getReferedMetadataEntries().contains(source)) {
			notifyIObservers(this, new PathChanged(getPath()));
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (gdaMetadata==null) {
			throw new FactoryException("ObservablePathConstructor " + getName() + " GdaMetadata property has not been set");
		}
		if (template==null) {
			throw new FactoryException("ObservablePathConstructor " + getName() + " template property has not been set");
		}
	}
	
}
