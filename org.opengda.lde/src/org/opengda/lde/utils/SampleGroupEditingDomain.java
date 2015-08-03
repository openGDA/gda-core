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

package org.opengda.lde.utils;

import java.util.HashMap;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.provider.LDEExperimentsItemProviderAdapterFactory;
import org.opengda.lde.model.ldeexperiment.util.LDEExperimentsResourceFactoryImpl;

public class SampleGroupEditingDomain implements IEditingDomainProvider {
	public final static SampleGroupEditingDomain INSTANCE = new SampleGroupEditingDomain();

	private ComposedAdapterFactory adapterFactory;
	private AdapterFactoryEditingDomain editingDomain;

	/**
	 * This sets up the editing domain for the model editor. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		setAdapterFactory(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));

		getAdapterFactory().addAdapterFactory(new ResourceItemProviderAdapterFactory());
		getAdapterFactory().addAdapterFactory(new LDEExperimentsItemProviderAdapterFactory());
		getAdapterFactory().addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
//		 commandStack.addCommandStackListener(new CommandStackListener() {
//			 @Override
//			 public void commandStackChanged(final EventObject event) {
//				 PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					 public void run() {
//						 firePropertyChange(IEditorPart.PROP_DIRTY);
//		
//						 // Try to select the affected objects.
//						 Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
//						 if (mostRecentCommand != null) {
//							 setSelectionToViewer(mostRecentCommand.getAffectedObjects());
//						 }
//						 if (propertySheetPage != null && !propertySheetPage.getControl().isDisposed()) {
//							 propertySheetPage.refresh();
//						 }
//					 }
//				 });
//			 }
//		 });

		// Create the editing domain with a special command stack.
		editingDomain = new AdapterFactoryEditingDomain(getAdapterFactory(), commandStack, new HashMap<Resource, Boolean>());

		// Register the appropriate resource factory to handle all file
		ResourceSet resourceSet = editingDomain.getResourceSet();
		// extensions.
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new LDEExperimentsResourceFactoryImpl());
		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(LDEExperimentsPackage.eNS_URI, LDEExperimentsPackage.eINSTANCE);
	}

	@Override
	public EditingDomain getEditingDomain() {
		if (editingDomain == null) {
			initializeEditingDomain();
		}
		return editingDomain;
	}

	public ComposedAdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public void setAdapterFactory(ComposedAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

}
