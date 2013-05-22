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

package org.opengda.detector.electronanalyser.utils;

import java.util.HashMap;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.provider.RegiondefinitionItemProviderAdapterFactory;

public class SequenceEditingDomain implements IEditingDomainProvider {
	public final static SequenceEditingDomain INSTANCE = new SequenceEditingDomain();

	private ComposedAdapterFactory adapterFactory;
	private AdapterFactoryEditingDomain editingDomain;

	/**
	 * This sets up the editing domain for the model editor. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory
				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new RegiondefinitionItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are
		// executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to
		// be the selection of the viewer with focus.
		//
//		commandStack.addCommandStackListener(new CommandStackListener() {
//			public void commandStackChanged(final EventObject event) {
//				PlatformUI.getWorkbench().getDisplay()
//						.asyncExec(new Runnable() {
//							public void run() {
								// firePropertyChange(IEditorPart.PROP_DIRTY);
								//
								// // Try to select the affected objects.
								// //
								// Command mostRecentCommand =
								// ((CommandStack)event.getSource()).getMostRecentCommand();
								// if (mostRecentCommand != null) {
								// setSelectionToViewer(mostRecentCommand.getAffectedObjects());
								// }
								// if (propertySheetPage != null &&
								// !propertySheetPage.getControl().isDisposed())
								// {
								// propertySheetPage.refresh();
								// }
//							}
//						});
//			}
//		});

		// Create the editing domain with a special command stack.
		//
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
				commandStack, new HashMap<Resource, Boolean>());
	}

	@Override
	public EditingDomain getEditingDomain() {
		if (editingDomain == null) {
			initializeEditingDomain();
		}
		return editingDomain;
	}

}
