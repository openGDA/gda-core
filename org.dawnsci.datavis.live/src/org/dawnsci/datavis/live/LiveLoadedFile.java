package org.dawnsci.datavis.live;

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class LiveLoadedFile extends LoadedFile implements IRefreshable {

	private IRemoteDatasetService remoteService;
	private ILoaderService localService;
	
	
	private boolean live = true;
	private boolean finished = false;
	private String host;
	private int port;
	private boolean initialised;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveLoadedFile.class);
	
	public LiveLoadedFile(String path, String host, int port, IRemoteDatasetService remote, ILoaderService local) {
		super(new DataHolder());
		this.host = host;
		this.port = port;
		this.remoteService = remote;
		this.localService = local;
		
		dataHolder.set(createDataHolder(path, host, port));
	}
	
	@Override
	public Map<String, int[]> getDataMaxShapes(){
		
		Map<String, int[]> m = new HashMap<>();
		
		Map<String, ILazyDataset> lazyMap = dataHolder.get().toLazyMap();
		
		for (Entry<String, ILazyDataset> entry : lazyMap.entrySet()) {
			
			ILazyDataset v = entry.getValue();
			
			if (v instanceof IDynamicShape) {
				m.put(entry.getKey(), ((IDynamicShape) v).getMaxShape());
			}
		}
		
		return m;

	}
	

	private IDataHolder createDataHolder(String path, String host, int port) {
		
		DataHolder dh = new DataHolder();
		dh.setFilePath(path);
		
		try {
			IDataHolder rdh = remoteService.createRemoteDataHolder(path, host, port, true);
			Map<String, NodeLink> result = findNodes(rdh.getTree());
			
			if (result == null) {
				return dh;
			}
			
			buildDataStructures(rdh, result);
			
			return rdh;
		} catch (RuntimeException e) {
			return dh;
		}
	}
	
	private void buildDataStructures(IDataHolder dh, Map<String, NodeLink> result) {
		
		for (Entry<String, NodeLink> s : result.entrySet()) {
			
			String name = "/"+s.getKey();
			
			if (dataOptions.containsKey(name)) continue;
			
			IDynamicDataset dataset = (IDynamicDataset)((DataNode)s.getValue().getDestination()).getDataset();
			
			DataNode dn = (DataNode)s.getValue().getDestination();
			
			long[] maxShape = ((DataNode)s.getValue().getDestination()).getMaxShape();
			
			if (maxShape.length == 0 || allOnes(maxShape)) {
				if (!possibleLabels.containsKey(name)) {
					possibleLabels.put(name, dataset);
				}
				continue;
			}
			
			Node source = s.getValue().getSource();
			
			if (source instanceof GroupNode) {
				storeNXData((GroupNode)source, dn, s.getKey());
			}
			
			if (!dataset.getElementClass().equals(String.class)) {
				DataOptions d = new DataOptions(name, this, signals.getOrDefault(name, null));
				dataOptions.put(d.getName(),d);
			}
		}
		
		if (getLabel().isEmpty() && !getLabelName().isEmpty()){
			setLabelName(getLabelName());
		}
	}
	
	private Map<String, NodeLink> findNodes(Tree tree){

		IFindInTree finder = new IFindInTree() {
			
			@Override
			public boolean found(NodeLink node) {
				Node d = node.getDestination();
				
				return d instanceof DataNode;
			}
		};
		return TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), finder, false, null);

	}
	
	private static boolean allOnes(long[] shape) {
		for (long l : shape) {
			if (l != 1) return false;
		}
		
		return true;
	}
	
	private static boolean allOnes(int[] shape) {
		for (int l : shape) {
			if (l != 1) return false;
		}
		
		return true;
	}
	
	private void updateOptionsNonLiveDataHolder() {

		String[] names = dataHolder.get().getNames();
		for (String n : names) {
			
			if (dataOptions.containsKey(n)) continue;
			
			ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
			addNonStringDataset(lazyDataset, n, false);
			
		}
		
		updateDataOptions();
	}
	
	private void addNonStringDataset(ILazyDataset lazyDataset, String name, boolean live){
		
		if(!name.startsWith("/")) {
			//local.name dataset, messes with validation so don't add
			return;
		}
		
		
		int[] shape = lazyDataset.getShape();

		if (lazyDataset instanceof IDynamicDataset && live) {
			try {
				((IDynamicDataset)lazyDataset).refreshShape();
				AxesMetadata ax = lazyDataset.getFirstMetadata(AxesMetadata.class);
				if (ax != null) {
					int[] refresh = ax.refresh(shape);
					((IDynamicDataset) lazyDataset).resize(refresh);
				}


			}catch (Exception e) {
				logger.error("Could not refresh dataset:" + lazyDataset.getName(),e);
			}
		}
		
		if (shape.length == 0 || allOnes(shape)) return;
		
		if (!lazyDataset.getElementClass().equals(String.class)) {
			DataOptions d = new DataOptions(name, this);
			dataOptions.put(d.getName(),d);
		}
	}
	
	private void refreshAllDatasets(){
		IDataHolder dh = dataHolder.get();
		
		if (dh instanceof IRemoteDataHolder) {
			((IRemoteDataHolder)dh).update();
		}
		
		String[] names = dh.getNames();
		for (String n : names) {
			try {
				ILazyDataset lazyDataset = dh.getLazyDataset(n);
				if (lazyDataset instanceof IDynamicDataset) {
					((IDynamicDataset)lazyDataset).refreshShape();
					
					AxesMetadata ax = lazyDataset.getFirstMetadata(AxesMetadata.class);
					if (ax != null) {
						int[] refresh = ax.refresh(lazyDataset.getShape());
						((IDynamicDataset) lazyDataset).resize(refresh);
					}
				}
			}catch (Exception e) {
				logger.error("Could not refresh dataset: " + n,e);
			}
		}
	}

	private void updateDataOptions() {
		DataOptions[] array = dataOptions.values().stream()
				.filter(d -> d.getPlottableObject() != null && d.getPlottableObject().getNDimensions() != null).toArray(size ->new DataOptions[size]);

		for (DataOptions o : array) {
			NDimensions nDimensions = o.getPlottableObject().getNDimensions();
			o.setAxes(null);
			int[] shape = o.getLazyDataset().getShape();
			nDimensions.updateShape(shape);
		}
	}

	@Override
	public void refresh() {
		if (!live) return;
		long t = System.currentTimeMillis();
		if (finished) {
			locallyReload();
			return;
		}
		
		if (dataHolder.get().getList().isEmpty()){
			String path = dataHolder.get().getFilePath();
			dataHolder.set(createDataHolder(path, host, port));
			if (dataHolder.get().getList().isEmpty()) return;
		}
		
		
		if (dataOptions.isEmpty()) {
			String[] names = dataHolder.get().getNames();
			for (String n : names) {
				ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
				addNonStringDataset(lazyDataset, n, true);
			}
			
			if (!dataOptions.isEmpty()) {
				return;
			}
			
			return;
		}
		
		refreshAllDatasets();
		updateDataOptions();
		
		logger.debug("Refresh of file {} took {} ms",getFilePath(),System.currentTimeMillis()-t);
		return;
	}

	@Override
	public void locallyReload() {
		if (finished) return;
		
		finished = true;
		
		try {
			String path = getFilePath();
			IDataHolder tmp = localService.getData(path, null);
			
			if (tmp == null || tmp.getTree() == null || tmp.getNames() == null) {
				logger.error("Scan finished but local reload not ready for {}!", path);
				return;
			}
			
			dataHolder.set(tmp);
			
			if (dataOptions.isEmpty()) {
				logger.debug("Data options empty on local reload");
				String[] names = dataHolder.get().getNames();
				for (String n : names) {
					ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(n);
					//this method should not mess with metadata refreshing here
					addNonStringDataset(lazyDataset, n, false);
				}
				live = false;
				return;
			} else {
				updateOptionsNonLiveDataHolder();
			}
			
			IDataHolder dh = dataHolder.get();
			dataOptions.keySet().removeIf(not(dh::contains));
			dataOptions.values().stream().forEach(o -> o.setAxes(null));
			
		} catch (Exception e) {
			logger.error("Could not locally load data!",e);
		}
		live = false;
		
	}

	@Override
	public boolean isLive() {
		return live;
	}

	@Override
	public boolean isEmpty() {
		return dataOptions.isEmpty();
	}

	@Override
	public boolean isInitialised() {
		return initialised;
	}

	@Override
	public void setInitialised() {
		initialised = true;
		
	}

	@Override
	public List<DataOptions> getUninitialisedDataOptions() {
		return new ArrayList<>(dataOptions.values());
	}
	
	@Override
	public List<DataOptions> getDataOptions(boolean signalsOnly) {
		if (!initialised) return Collections.emptyList();
		return super.getDataOptions(signalsOnly);
	}
	
	@Override
	public List<DataOptions> getDataOptions() {
		if (!initialised) return Collections.emptyList();
		return super.getDataOptions();
	}
}
