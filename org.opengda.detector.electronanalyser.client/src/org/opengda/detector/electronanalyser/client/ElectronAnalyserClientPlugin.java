package org.opengda.detector.electronanalyser.client;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.opengda.detector.electronanalyser.utils.SequenceEditingDomain;
import org.osgi.framework.BundleContext;

public class ElectronAnalyserClientPlugin extends AbstractUIPlugin {

	public static final String EDITING_DOMAIN_ID = "org.opengda.detector.electronanalyser.client.sequence.editingdomain";
	private static final String PLUGIN_ID = "org.opengda.detector.electronanalyser.client";

	public static final String TILE_QUAD = "TILEQUAD";
	public static final String STACK_PLOT = "STACKPLOT";
	public static final String PLOT_LAYOUT = "PLOT_LAYOUT";
	public static final String TILE_VERTICAL = "TILEVERTICAL";
	public static final String TILE_HORIZONTAL="TILEHORIZONTAL";

	public ElectronAnalyserClientPlugin() {
	}

	// The shared instance
	private static ElectronAnalyserClientPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// SpringObjectServer s = new SpringObjectServer(
		// new File(
		// "D:/gda/i09/workspace_git/opengda-electronanalyser.git/org.opengda.detector.electronanalyser.client.test/client.xml"),
		// true);
		// s.configure();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ElectronAnalyserClientPlugin getDefault() {
		return plugin;
	}

	public EditingDomain getSequenceEditingDomain() throws Exception {
		try {
			// return TransactionalEditingDomain.Registry.INSTANCE
			// .getEditingDomain(EDITING_DOMAIN_ID);
			return SequenceEditingDomain.INSTANCE.getEditingDomain();
		} catch (Exception ex) {
			throw new Exception("Unable to get editing domain:"
					+ ex.getMessage());
		}
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (String imgPath : ImageConstants.IMAGES) {
			reg.put(imgPath, imageDescriptorFromPlugin(PLUGIN_ID, imgPath));
		}
	}

}
