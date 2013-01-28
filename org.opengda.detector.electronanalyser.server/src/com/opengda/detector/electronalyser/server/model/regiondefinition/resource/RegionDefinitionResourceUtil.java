package com.opengda.detector.electronalyser.server.model.regiondefinition.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot;
import com.opengda.detector.electronalyser.server.model.regiondefinition.Region;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionFactory;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage;

public class RegionDefinitionResourceUtil {

	private String fileName;

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<Region> getRegions(boolean shouldCreate) {
		// Create a resource set to hold the resources.
		//
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the appropriate resource factory to handle all file
		// extensions.
		//
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		//
		resourceSet.getPackageRegistry().put(RegiondefinitionPackage.eNS_URI,
				RegiondefinitionPackage.eINSTANCE);

		URI fileURI = URI.createFileURI(fileName);
		Resource res = resourceSet.getResource(fileURI, true);
		if (res == null && shouldCreate) {
			Resource resource = resourceSet.createResource(URI
					.createURI(fileName));
			DocumentRoot root = RegiondefinitionFactory.eINSTANCE
					.createDocumentRoot();
			resource.getContents().add(root);
			try {
				resource.save(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof DocumentRoot) {
				DocumentRoot root = (DocumentRoot) eobj;
				return root.getSequence().getRegion();
			}

		}
		return Collections.emptyList();
	}

	public void save(Resource res) throws IOException {
		res.save(null);
	}
}
