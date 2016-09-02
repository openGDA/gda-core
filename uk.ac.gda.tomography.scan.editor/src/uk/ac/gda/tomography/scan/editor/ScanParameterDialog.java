package uk.ac.gda.tomography.scan.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.configuration.properties.LocalProperties;
import gda.util.FileUtil;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanFactory;
import uk.ac.gda.tomography.scan.presentation.ParametersComposite;
import uk.ac.gda.tomography.scan.presentation.ScanEditorPlugin;
import uk.ac.gda.tomography.scan.provider.ScanItemProviderAdapterFactory;

public class ScanParameterDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ScanParameterDialog.class);
	private static final String TOMOSCAN_DIRECTORY = "tomoScan";

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

		ResourceSet resourceSet = editingDomain.getResourceSet();
		String defaultScanParametersFilePath = getDefaultScanParameterFilePath();
		if (!defaultScanParametersFilePath.isEmpty() && (new File(defaultScanParametersFilePath)).exists()) {
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

	private static String getDefaultScanParameterFilePath() {
		String string = ScanEditorPlugin.getPlugin()
				.getPreferenceStore()
				.getString("DefaultScanParametersFilename");
		if( string.isEmpty())
			return string;
		return LocalProperties.getVarDir() + File.separator + string;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Run",
				false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		try {
			// Create output directory if necessary
			final Path scanFileDirectory = Paths.get(LocalProperties.getVarDir(), TOMOSCAN_DIRECTORY);
			Files.createDirectories(scanFileDirectory);

			final String uniqueFilename = getUniqueFilename("TomoScan", ".scan");
			final File gridScanFileWithTime = new File(scanFileDirectory.toString(), uniqueFilename);

			final Resource resource = editingDomain.getResourceSet().getResources().get(0);
			final Parameters x = (Parameters) (resource.getContents().get(0));
			final OutputStream os = new FileOutputStream(gridScanFileWithTime);
			final Map<String, Boolean> options = new HashMap<String, Boolean>();

			options.put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, true);
			resource.save(os, options);
			os.close();

			final String defaultScanParametersFilePath = getDefaultScanParameterFilePath();
			if (!defaultScanParametersFilePath.isEmpty()) {
				FileUtil.copy(gridScanFileWithTime.getAbsolutePath(), defaultScanParametersFilePath);
			}

			final String command = "tomographyScan.ProcessScanParameters('" + gridScanFileWithTime.getAbsolutePath() + "')";
			final String jobLabel = "TomoScan Scan: " + x.getTitle();

			CommandQueueViewFactory.getQueue().addToTail(new JythonCommandCommandProvider(command, jobLabel, gridScanFileWithTime.getAbsolutePath()));
			CommandQueueViewFactory.showView();

		} catch (Exception e1) {
			reportErrorToUserAndLog("Error submitting tomoscan to queue", e1);
		}
		super.okPressed();
	}

	private static void reportErrorToUserAndLog(String s, Throwable th) {
		logger.error(s, th);
		MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR);
		messageBox.setMessage(s + ":" + th.getMessage());
		messageBox.open();
	}

	private static String getUniqueFilename(String prefix, String suffix) {
		final UUID uuid = UUID.randomUUID();
		final String uuidWithoutHyphens = uuid.toString().replace("-", "");
		final String timeString = (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date());
		return String.format("%s-%s-%s%s", prefix, timeString, uuidWithoutHyphens, suffix);
	}
}
