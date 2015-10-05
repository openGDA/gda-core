package uk.ac.gda.tomography.scan.editor;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.configuration.properties.LocalProperties;
import gda.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanFactory;
import uk.ac.gda.tomography.scan.presentation.ParametersComposite;
import uk.ac.gda.tomography.scan.provider.ScanItemProviderAdapterFactory;

public class ScanParameterView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(ScanParameterView.class);

	private AdapterFactoryEditingDomain editingDomain;

	public ScanParameterView() {
	}
	public static String getUniqueFilename(String prefix, String suffix) {
		final UUID uuid = UUID.randomUUID();
		final String uuidWithoutHyphens = uuid.toString().replace("-", "");
		final String timeString = (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date());
		return String.format("%s-%s-%s%s", prefix, timeString, uuidWithoutHyphens, suffix);
	}
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());


		ParametersComposite parametersComposite = new ParametersComposite(parent);
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
		String defaultScanParametersFilePath = ScanParameterDialog.getDefaultScanParameterFilePath();
		if (!defaultScanParametersFilePath.isEmpty()&& (new File(defaultScanParametersFilePath)).exists()) {
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

		
		Button btnRun = new Button(parent, SWT.WRAP);
		btnRun.setImage(ResourceManager.getPluginImage("uk.ac.gda.tomography.scan.editor", "icons/go-queue.png"));
		FormData fd_btnRun = new FormData();
		fd_btnRun.top = new FormAttachment(0);
		fd_btnRun.bottom = new FormAttachment(100);
		fd_btnRun.right = new FormAttachment(100);
		fd_btnRun.left = new FormAttachment(parametersComposite);
		btnRun.setLayoutData(fd_btnRun);
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				OutputStream os;
				try {
					final String uniqueFilename = getUniqueFilename("TomoScan", ".scan");
					final File gridScanFileWithTime = new File(LocalProperties.getVarDir(), uniqueFilename);
					
					Resource resource = editingDomain.getResourceSet()
							.getResources().get(0);
					Parameters x = (Parameters) (resource.getContents().get(0));
					os = new FileOutputStream(gridScanFileWithTime);
					Map<String, Boolean> options = new HashMap<String,Boolean >();
					options.put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, true);
					resource.save(os, options);
					os.close();
					String defaultScanParametersFilePath = ScanParameterDialog.getDefaultScanParameterFilePath();
					if( !defaultScanParametersFilePath.isEmpty())
						FileUtil.copy(gridScanFileWithTime.getAbsolutePath(), defaultScanParametersFilePath);
						
					String command = "tomographyScan.ProcessScanParameters('" + gridScanFileWithTime.getAbsolutePath() + "')";
					String jobLabel = "TomoScan Scan: "+x.getTitle();
					
					CommandQueueViewFactory.getQueue().addToTail(new JythonCommandCommandProvider(command, jobLabel, gridScanFileWithTime.getAbsolutePath()));
					CommandQueueViewFactory.showView();

				} catch (Exception e1) {
					reportErrorToUserAndLog("Error submitting tomoscan to queue", e1);
				}
			}
		});
		btnRun.setText("Add to Queue");

		
	}
	public static void reportErrorToUserAndLog(String s, Throwable th) {
		logger.error(s, th);
		MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				SWT.ICON_ERROR);
		messageBox.setMessage(s + ":" + th.getMessage());
		messageBox.open();

	}
	@Override
	public void setFocus() {
	}

}
