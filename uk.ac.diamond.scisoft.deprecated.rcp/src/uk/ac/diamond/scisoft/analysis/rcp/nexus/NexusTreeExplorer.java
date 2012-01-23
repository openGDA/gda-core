/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.nexus;

import gda.data.nexus.FileNameBufToStrings;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.observable.IObserver;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Nexus;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoaderEx;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.plotserver.NexusDataBean;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;

/**
 * View NeXus tree and plot items within it
 */
public class NexusTreeExplorer extends Composite implements IObserver, ISelectionProvider {

	private static final String DATA_FILENAME_ATTR_NAME = "data_filename";

	private static final Logger logger = LoggerFactory.getLogger(NexusTreeExplorer.class);

	private Display display = null;
	private IWorkbenchPartSite site;

	// Variables for the plotServer
	private PlotServer plotServer;
	private GuiBean guiBean;

	private static final String NAME = "nexusTreeViewer";

	private UUID plotID;

	private INexusTree tree;

	private ILazyDataset cData; // chosen dataset
	List<AxisSelection> axes; // list of axes for each dimension
	private String filename;

	private class NXSelection extends DatasetSelection {
		private String fileName;
		private String node;

		public NXSelection(InspectorType type, String filename, String node, List<AxisSelection> axes, ILazyDataset... dataset) {
			super(type, axes, dataset);
			this.fileName = filename;
			this.node = node;
		}

		@Override
		public boolean equals(Object other) {
			if (super.equals(other) && other instanceof NXSelection) {
				NXSelection that = (NXSelection) other;
				if (fileName == null && that.fileName == null)
					return node.equals(that.node);
				if (fileName != null && fileName.equals(that.fileName))
					return node.equals(that.node);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 17 + node.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			return node + " = " + super.toString();
		}
	}

	private NXSelection nxSelection;
	private Set<ISelectionChangedListener> cListeners;

	// GUI elements
	NexusTableTree tableTree = null;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param site
	 */
	public NexusTreeExplorer(Composite parent, int style, IWorkbenchPartSite site) {
		super(parent, style);

		this.site = site;
		plotID = UUID.randomUUID();
		plotServer = PlotServerProvider.getPlotServer();
		plotServer.addIObserver(this);
		// generate the bean that will contain all the information about this GUI
		guiBean = new GuiBean();
		// publish this to the server
		try {
			plotServer.updateGui(NAME, guiBean);
		} catch (Exception e) {
			logger.error("Problem pushing initial GUI bean to plot server");
			e.printStackTrace();
		}

		display = parent.getDisplay();

		setLayout(new FillLayout());

		tableTree = new NexusTableTree(this, null, new Listener() {
			@Override
			public void handleEvent(Event event) {
				handleDoubleClick();
			}
		});

		cListeners = new HashSet<ISelectionChangedListener>();
		getTreeFromServer();
		site.setSelectionProvider(this);
		axes = new ArrayList<AxisSelection>();
	}

	@Override
	public void dispose() {
		cListeners.clear();
		plotServer.deleteIObserver(this);
		super.dispose();
	}

	private AsciiTextView getTextView() {
		AsciiTextView textView = null;
		// check if Dataset Table View is open
		try {
			textView = (AsciiTextView) site.getPage().showView(AsciiTextView.ID);
		} catch (PartInitException e) {
			logger.error("All over now! Cannot find ASCII text view: {} ", e);
		}

		return textView;
	}

	/**
	 * @param bean
	 */
	private void processGUIUpdate(GuiBean bean) {
		if (bean.containsKey(GuiParameters.METADATANODEPATH)) {
			String path = (String) bean.get(GuiParameters.METADATANODEPATH);
			INexusTree node = tree.getNode(path);
			if (node != null) {
				if (!handleSelectedNode(node)) {
					logger.error("Could not process update of selected node: {}", path);
				}
			}
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof GuiUpdate) {
			GuiUpdate gu = (GuiUpdate) changeCode;

			if (gu.getGuiName().contains(NAME)) {
				guiBean = gu.getGuiData();
				syncGuiToBean();
				GuiBean bean = gu.getGuiData();
				UUID id = (UUID) bean.get(GuiParameters.PLOTID);

				if (id == null || plotID.compareTo(id) != 0) { // filter out own beans
					if (guiBean == null)
						guiBean = bean.copy(); // cache a local copy
					else
						guiBean.merge(bean);   // or merge it

					logger.debug("Processing update received from {}: {}", theObserved, changeCode);

					// now update GUI
					processGUIUpdate(bean);
				}
			}
		} else if (changeCode instanceof String) {
			String guiName = (String) changeCode;
			if (guiName.equals(NAME)) {
				getTreeFromServer();
			}
		}

	}

	private void getTreeFromServer() {
		DataBean dataBean;

		try {
			logger.debug("Pulling data to client");
			long start = System.nanoTime();
			dataBean = plotServer.getData(NAME);
			start = System.nanoTime() - start;
			logger.debug("Data pushed to client: {} in {} s", dataBean, String.format("%.3g", start*1e-9));
			syncTreeToBean(dataBean);
		} catch (Exception e) {
			logger.error("Problem pushing data to plot server");
			e.printStackTrace();
		}
	}

	private void syncGuiToBean() {
		if (display != null)
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					display.update();
				}
			});
	}

	private void syncTreeToBean(DataBean bean) {
		if (bean == null) {
			logger.warn("Plot server has no info for NTV");
			return;
		}

		if (!(bean instanceof NexusDataBean))
			return;

		// grab first metadata tree
		List<INexusTree> ntList = ((NexusDataBean) bean).getNexusTrees();
		if (ntList == null) {
			logger.warn("Plot server did not push a list of trees");
			return;
		}
		if (ntList.size() == 0) {
			logger.warn("Plot server pushed an empty list of trees");
			return;
		}
		setNexusTree(ntList.get(0));
	}
	
	/**
	 * Set and display a Nexus tree
	 * @param nexusTree 
	 */
	public void setNexusTree(INexusTree nexusTree) {
		tree = nexusTree;

		setSelection(new DatasetSelection()); // set up null selection to clear plot

		if (display != null)
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					tableTree.setInput(tree);
					display.update();
				}
			});
	}

	/*
	 * populate a selection object
	 */
	public void selectNexusTreeNode(INexusTree node, InspectorType type) {
		if (node != null) {
			if (handleSelectedNode(node)) {
				// provide selection
				setSelection(new NXSelection(type, filename, node.getNodePath(), axes, cData));

				// notify other clients that a node has been selected
				pushGUIUpdate(GuiParameters.METADATANODEPATH, node.getNodePath());
			} else
				logger.error("Could not process update of selected node: {}", node.getName());
		}
	}

	private void handleDoubleClick() {
		final Cursor cursor = getCursor();
		Cursor tempCursor = getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		if (tempCursor != null)
			setCursor(tempCursor);

		IStructuredSelection selection = tableTree.getSelection();

		// check if selection is valid for plotting
		if (selection != null) {
			INexusTree node = (INexusTree) selection.getFirstElement();
			selectNexusTreeNode(node, InspectorType.LINE);
		}

		if (tempCursor != null)
			setCursor(cursor);
	}

	private boolean handleSelectedNode(INexusTree node) {
		if (processTextNode(node)) {
			return false;
		}

		if (!processSelectedNode(node))
			return false;

		if (cData == null)
			return false;

		return true;
	}

	/**
	 * @param node 
	 * @param name
	 * @return an Integer attribute (or null)
	 */
	public Integer getIntAttribute(INexusTree node, String name) {
		Serializable n = node.getAttribute(name);
		if (n instanceof Integer)
			return (Integer) n;
		else if (n instanceof String)
			return Integer.valueOf((String) n);
		return null;
	}

	private boolean isNodeAnExternalFilePath(INexusTree node){
		if( node.getNxClass().equals(NexusExtractor.SDSClassName) && node.getAttribute(DATA_FILENAME_ATTR_NAME)!=null){
			NexusGroupData g = node.getData();
			return (g != null && g.type == NexusFile.NX_CHAR);
		}
		return false;
		
	}
	
	private boolean processTextNode(INexusTree node) {
		NexusGroupData g = node.getData();
		if (g != null && g.type == NexusFile.NX_CHAR) {
			try {
				Serializable buf = g.getBuffer();
				if (buf == null) {
					g = NexusExtractor.getNexusGroupDataWithBuffer(node, true);
					buf = g.getBuffer();
				}
				if(isNodeAnExternalFilePath(node)){
					try{
						String[] filenames = new FileNameBufToStrings(g.dimensions,( byte[]) buf).getFilenames();
						StringBuilder sBuilder = new StringBuilder();
						boolean addCR = false;
						for (String filename : filenames) {
							if (addCR) {
								sBuilder.append("\n");
							}
							sBuilder.append(filename);
							addCR = true;
						}
						getTextView().setData(sBuilder.toString());
						return false;
					} catch (UnsupportedEncodingException e) {
						logger.warn("Unable to convert to strings", e);
						String msg = new String((byte[]) buf, "UTF-8");
						getTextView().setData(msg);
					}
				} else {
					String msg = new String((byte[]) buf, "UTF-8");
					getTextView().setData(msg);
				}
			} catch (Exception e) {
				logger.error("Error processing text node {}: {}", node.getNodePath(), e);
			}
			return true;
		}

		return false;
	}

	private boolean processSelectedNode(INexusTree node) {
		// two cases: axis and primary or axes
		// iterate through each child to find axes and primary attributes
		INexusTree dataNode = node;
		boolean foundData = false;
		List<AxisChoice> choices = new ArrayList<AxisChoice>();
		String axesAttr = null;

		// see if chosen node is a NXdata class
		if (dataNode.getNxClass().equals(NexusExtractor.NXDataClassName)) {
			// find data (signal=1) and check for axes attribute
			for (int i = 0; i < dataNode.getNumberOfChildNodes(); i++) {
				INexusTree n = dataNode.getChildNode(i);
				NexusGroupData g = n.getData();
				if (g != null) {
					Integer attribute = getIntAttribute(n, "signal");
					if (attribute != null) {
						foundData = true;
						try {
							if(isNodeAnExternalFilePath(n)){
								try{
									HandleFileNames hFileNames = new HandleFileNames(n);
									cData = hFileNames.cData;
									axesAttr = hFileNames.axesAttr;
									foundData = hFileNames.foundData;
								} catch (Exception e) {
									logger.warn("Unable to handle filenames", e);
								}
							} else {
								cData = Nexus.createLazyDataset(n);
							}
						} catch (Exception e) {
							logger.error("Could not convert NeXus item {} to a dataset", n.getName(),e);
							foundData = false;
						}
						axesAttr = (String) n.getAttribute("axes");
						break; // only one signal per NXdata item
					}
				}
			}
		} else if(isNodeAnExternalFilePath(dataNode)){
			try{
				HandleFileNames hFileNames = new HandleFileNames(dataNode);
				cData = hFileNames.cData;
				axesAttr = hFileNames.axesAttr;
				foundData = hFileNames.foundData;
			} catch (Exception e){
				logger.warn("Unable to handle filenames",e);
			}
		} else if (dataNode.getNxClass().equals(NexusExtractor.SDSClassName)) {
			// else just use chosen node if it's an SDS
			if (isNodeAnExternalFilePath(dataNode)) {
				try {
					HandleFileNames hFileNames = new HandleFileNames(dataNode);
					cData = hFileNames.cData;
					axesAttr = hFileNames.axesAttr;
					foundData = hFileNames.foundData;
				} catch (Exception e) {
					logger.warn("Unable to handle filenames", e);
				}
			} else {
				INexusTree n = dataNode;
				NexusGroupData g;
				g = n.getData();
				if (g != null) {
					foundData = true;
					try {
						cData = Nexus.createLazyDataset(n);
					} catch (Exception e) {
						logger.error("Could not convert NeXus item {} to a dataset", n.getName());
						foundData = false;
					}
					axesAttr = (String) n.getAttribute("axes");
					dataNode = node.getParentNode(); // before hunting for axes
				}
			}
		}

		if (foundData) {
			// remove extraneous dimensions
			cData.squeeze(true);

			// set up slices
			int rank = cData.getRank();

			// scan children for SDS as possible axes (could be referenced by axes)
			if (dataNode != null) {
				for (int i = 0, imax = dataNode.getNumberOfChildNodes(); i < imax; i++) {
					INexusTree n =  dataNode.getChildNode(i);
					if (n == dataNode)
						continue;

					NexusGroupData g = n.getData();
					if (g != null && g.type != NexusFile.NX_CHAR) {
						HashMap<String, Serializable> attributes = n.getAttributes();

						if (attributes != null && attributes.containsKey("signal"))
							continue;

						ILazyDataset d;
						try {
							int[] s = g.dimensions.clone();
							s = AbstractDataset.squeezeShape(s, true);
							if (s.length > 1)
								continue; // axis data must be 1D: FIXME
							d = Nexus.createLazyDataset(n);
							d.squeeze(true);
							AxisChoice choice = new AxisChoice(d);
							if (attributes != null) {
								if (attributes.containsKey("axis")) {
									Object thisAxis = attributes.get("axis");
									Integer intAxis = Integer.parseInt(thisAxis.toString()) - 1;
									choice.setIndexMapping(intAxis);
									choice.setAxisNumber(intAxis);
								}
								if (attributes.containsKey("primary")) {
									Object thisPrimary = attributes.get("primary");
									Integer intPrimary = Integer.parseInt(thisPrimary.toString());
									choice.setPrimary(intPrimary);
								}
							}
							choices.add(choice);
						} catch (Exception e) {
							logger.error("Could not convert NeXus item {} to a dataset", n.getName(), e);
							continue;
						}
					}
				}
			}

			List<String> aNames = new ArrayList<String>();
			if (axesAttr != null) { // check axes attribute for list axes
				axesAttr = axesAttr.trim();
				if (axesAttr.startsWith("[")) { // strip opening and closing brackets
					axesAttr = axesAttr.substring(1, axesAttr.length() - 1);
				}

				// check if axes referenced by data exists
				String[] names = null;
				names = axesAttr.split(",");
				for (int i = 0; i < names.length; i++) {
					String s = names[i];
					if (!choices.contains(s)) {
						logger.warn("Referenced axis {} does not exist in NeXus tree node {}", new Object[] { s,
								dataNode });
						aNames.add(null);
					} else {
						aNames.add(s);
					}
				}
			}

			// set up AxisSelector
			// build up list of choice per dimension
			axes.clear();

			for (int i = 0; i < rank; i++) {
				int dim = cData.getShape()[i];
				AxisSelection aSel = new AxisSelection(dim, i);
				axes.add(aSel);
				for (AxisChoice c : choices) {
					// check if dimension number and axis length matches
					if (c.getAxisNumber() == i) {
						if (c.getSize() == dim) {
							aSel.addChoice(c, c.getPrimary());
						} else {
							logger.warn("Ignoring axis {} as its length ({}) does not match data dimension ({})",
									new Object[] { c.getName(), c.getSize(), dim });
						}
					}
				}

				for (AxisChoice c : choices) {
					// add in others if axis length matches
					if (c.getAxisNumber() != i) {
						if (c.getSize() == dim)
							aSel.addChoice(c, 0);
					}
				}

				if (i < aNames.size()) {
					for (AxisChoice c : choices) {
						if (c.getName().equals(aNames.get(i))) {
							if (c.getSize() == dim) {
								aSel.addChoice(c, 1);
							} else {
								logger.warn("Ignoring specified axis {} as its length ({}) does not match data dimension ({})",
											new Object[] { aNames.get(i), c.getSize(), dim });
							}
						}
					}
				}

				// add in an automatically generated axis with top order so it appears after primary axes
				AbstractDataset axis = AbstractDataset.arange(dim, AbstractDataset.INT32);
				axis.setName("dim:" + (i+1));
				AxisChoice newChoice = new AxisChoice(axis);
				newChoice.setIndexMapping(i);
				newChoice.setAxisNumber(i);
				aSel.addChoice(newChoice, aSel.getMaxOrder() + 1);
			}
		}

		return foundData;
	}

	/**
	 * Push gui information back to plot server
	 * @param key 
	 * @param value 
	 */
	public void pushGUIUpdate(GuiParameters key, Serializable value) {
		if (guiBean == null) {
			try {
				guiBean = plotServer.getGuiState(NAME);
			} catch( Exception e) {
				logger.error("Problem with getting GUI data from plot server");
			}
			if (guiBean == null)
				guiBean = new GuiBean();
		}

		if (!guiBean.containsKey(GuiParameters.PLOTID))
			guiBean.put(GuiParameters.PLOTID, plotID); // put plotID in bean

		guiBean.put(key, value);

		try {
			plotServer.updateGui(NAME, guiBean);
		} catch (Exception e) {
			logger.error("Problem with updating plot server with GUI data");
			e.printStackTrace();
		}
	}

	public void expandAll() {
		tableTree.expandAll();
	}

	// selection provider interface
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (cListeners.add(listener)) {
			return;
		}
	}

	@Override
	public ISelection getSelection() {
		return nxSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (cListeners.remove(listener))
			return;
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof NXSelection)
			nxSelection = (NXSelection) selection;
		else
			return;

		SelectionChangedEvent e = new SelectionChangedEvent(this, nxSelection);
		for (ISelectionChangedListener s : cListeners)
			s.selectionChanged(e);
	}

	public void setFilename(String fileName) {
		filename = fileName;
	}

}

class HandleFileNames {
	LazyDataset cData;
	String axesAttr;
	boolean foundData = false;

	HandleFileNames(INexusTree dataNode) throws Exception {
		NexusGroupData g = dataNode.getData();
		Serializable buf = g.getBuffer();
		if (buf == null) {
			g = NexusExtractor.getNexusGroupDataWithBuffer(dataNode, true);
			buf = g.getBuffer();
		}
		String[] filenames = new FileNameBufToStrings(g.dimensions, (byte[]) buf).getFilenames();
		ImageStackLoaderEx loader;
		int[] dimFileNames = java.util.Arrays.copyOfRange(g.dimensions, 0, g.dimensions.length - 1);
		loader = new ImageStackLoaderEx(dimFileNames, filenames);
		int[] imageShape = loader.getShape();
		cData = new LazyDataset("file_name", loader.getDtype(), imageShape, loader);
		axesAttr = (String) dataNode.getAttribute("axes");
		foundData = true;
	}
}
