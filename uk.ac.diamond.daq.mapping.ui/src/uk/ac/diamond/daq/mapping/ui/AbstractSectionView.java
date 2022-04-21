/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import gda.configuration.properties.LocalProperties;

/**
 * Abstract superclass of views made up of sections.
 *
 * @param <B> bean class
 */
public abstract class AbstractSectionView<B> implements ISectionView<B> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSectionView.class);

	@Inject
	private IEclipseContext eclipseContext;

	private final ClassToInstanceMap<IViewSection<B, ISectionView<B>>> sections = MutableClassToInstanceMap.create();

	@Override
	@PreDestroy
	public void dispose() {
		getAllSections().stream().forEach(IViewSection::dispose);
	}

	protected <T> T getRemoteService(Class<T> klass) {
		IEventService eventService = eclipseContext.get(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (Exception e) {
			logger.error("Error getting remote service {}", klass, e);
			return null;
		}
	}

	@Override
	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	@Override
	public <S> S getService(Class<S> serviceClass) {
		return eclipseContext.get(serviceClass);
	}

	@Override
	public Shell getShell() {
		return (Shell) eclipseContext.get(IServiceConstants.ACTIVE_SHELL);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends ISectionView<B>, S extends IViewSection<B, V>> S getSection(Class<S> sectionClass) {
		return (S) sections.get(sectionClass);
	}

	public Collection<IViewSection<B, ISectionView<B>>> getAllSections() {
		return sections.values();
	}

	protected <V extends AbstractSectionView<B>> void createSections(Composite parent,List<? extends IViewSection<B, V>> sectionsToCreate, Map<String, String> persistedState) {
		for (IViewSection<B, V> section : sectionsToCreate) {
			createSection(parent, persistedState, section);
		}
	}

	@SuppressWarnings("unchecked")
	private <V extends ISectionView<B>, S extends IViewSection<B, V>> void createSection(Composite parent,
			Map<String, String> persistedState, S section) {
		final String sectionName = section.getClass().getSimpleName();
		logger.debug("Creating mapping section {}", sectionName);

		try {
			section.initialize((V) this);
			section.loadState(persistedState);
			section.createControls(parent);

			final Class<IViewSection<B, ISectionView<B>>> sectionClass =
					(Class<IViewSection<B, ISectionView<B>>>) section.getClass();
			final IViewSection<B, ISectionView<B>> castSection = (IViewSection<B, ISectionView<B>>) section;
			sections.put(sectionClass, castSection); // we need to cast the section to the expected generic type
		} catch (Exception e) {
			logger.error("Error creating view section {}", sectionName, e);
		}
	}

	public void updateControls() {
		for (IViewSection<?, ?> section : getAllSections()) {
			section.updateControls();
		}
		relayout();
	}

}
