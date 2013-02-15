package uk.ac.gda.tomography.scan.editor;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanFactory;
import uk.ac.gda.tomography.scan.presentation.ParametersComposite;
import uk.ac.gda.tomography.scan.presentation.ScanEditorPlugin;
import uk.ac.gda.tomography.scan.provider.ScanItemProviderAdapterFactory;

public class ScanParameterDialog extends Dialog {
	private AdapterFactoryEditingDomain editingDomain;
	public ScanParameterDialog(Shell parentShell) {
		super(parentShell);

	
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tomography Scan Parameters");
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cmp = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(cmp);
		cmp.setLayout(new FormLayout());


		ParametersComposite parametersComposite = new ParametersComposite(cmp);
		parametersComposite.setLayoutData(new FormData());

		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory
				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ScanItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		BasicCommandStack commandStack = new BasicCommandStack();

		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
				commandStack);

		String defaultScanParametersFilePath = ScanEditorPlugin.getPlugin()
				.getPreferenceStore()
				.getString("DefaultScanParametersFilePath");
		ResourceSet resourceSet = editingDomain.getResourceSet();
		if (!defaultScanParametersFilePath.isEmpty()) {
			resourceSet.getResource(
					URI.createFileURI(defaultScanParametersFilePath), true);
		} else {

			Resource resource = resourceSet.createResource(URI
					.createURI("http:///My.scan"));
			Parameters root = ScanFactory.eINSTANCE.createParameters();
			resource.getContents().add(root);
			try {
				resource.save(System.out, null);
			} catch (IOException e1) {
			}
		}
		parametersComposite.setInput(editingDomain);

		return cmp;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText("Run");
		GridDataFactory.fillDefaults().applyTo(getButton(IDialogConstants.OK_ID));
	}

	@Override
	protected void okPressed() {
		OutputStream os;
		try {
			final String uniqueFilename = ScanParameterView.getUniqueFilename("TomoScan", ".scan");
			final File gridScanFileWithTime = new File(LocalProperties.getVarDir(), uniqueFilename);
			
			Resource resource = editingDomain.getResourceSet()
					.getResources().get(0);
			Parameters x = (Parameters) (resource.getContents().get(0));
			os = new FileOutputStream(gridScanFileWithTime);
			Map<String, Boolean> options = new HashMap<String,Boolean >();
			options.put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, true);
			resource.save(os, options);
			os.close();
			
			String command = "tomographyScan.ProcessScanParameters('" + gridScanFileWithTime.getAbsolutePath() + "')";
			String jobLabel = "TomoScan Scan: "+x.getTitle();
			
			CommandQueueViewFactory.getQueue().addToTail(new JythonCommandCommandProvider(command, jobLabel, gridScanFileWithTime.getAbsolutePath()));
			CommandQueueViewFactory.showView();

		} catch (Exception e1) {
			ScanParameterView.reportErrorToUserAndLog("Error submitting tomoscan to queue", e1);
		}
		super.okPressed();
	}

}
