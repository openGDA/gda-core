/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.ExpressionObject;

public class ComparisonPlotUtils {

	/**
	 * The selection array must file java.io.File[] or org.eclipse.core.resources.IFile[]
	 * @param files -  java.io.File[] or org.eclipse.core.resources.IFile[]
	 * @param selections - list of either String data set name or ExpressionObject
	 */
	public static void createComparisionPlot(final Object[] files, final List<Object> selections, final PlotMode plotMode, final AbstractPlotWindow window, final IProgressMonitor monitor) throws Exception{

		Object xSel = selections.get(0);

		final List<Object> ySel = new ArrayList<Object>(3);
		for (int i = 1; i < selections.size(); i++) {
			ySel.add(selections.get(i));
		}

		final File[]         fa = getFiles(files);
		Dataset             x  = getLargestDataSet(fa, xSel, monitor);
		final List<Dataset> ys = getDataSets(fa, ySel, true, monitor);
		
		if (ys.isEmpty()) {
			ys.add(x);
			x = DatasetFactory.createRange(DoubleDataset.class, ys.get(0).getSize());
			x.setName("Index");
		}

		PlotUtils.create1DPlot(x, ys, plotMode, window, monitor);
	}

	private static List<Dataset> getDataSets(final File[] files, final List<Object> namesOrExpressions, final boolean useFileName, final IProgressMonitor monitor) throws Exception{
		final List<Dataset> ret = new ArrayList<Dataset>(files.length*namesOrExpressions.size());
		for (int i = 0; i < files.length; i++) {
			for (Object n : namesOrExpressions) {
				ret.add(getDataSet(files[i], n, useFileName, monitor));
			}
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private static Dataset getLargestDataSet(final File[] files, final Object nameOrExpression, final IProgressMonitor monitor) throws Exception {
		
		Dataset ret = null;
		int    size = Integer.MIN_VALUE;
		for (int i = 0; i < files.length; i++) {
			try {
				final Dataset set = getDataSet(files[i], nameOrExpression, false, monitor);
				if (set!=null) {
					if (set.getSize() > size) {
						size = set.getSize();
						ret  = set;
					}

				}
			} catch (Exception ignored) {
				continue; // Cannot be sure file has this set.
			}
		}

		return ret;
       	
  	}
	
	private static Dataset getDataSet(final File file, final Object nameOrExpression, final boolean useFileName, final IProgressMonitor monitor) throws Exception {
        
		Dataset set=null;
		if (nameOrExpression instanceof String) {
		    set = (Dataset)LoaderFactory.getDataSet(file.getAbsolutePath(), (String)nameOrExpression, new ProgressMonitorWrapper(monitor));
       	
        } else if (nameOrExpression instanceof ExpressionObject) {
        	final DataSetProvider prov = new DataSetProvider() {
				@Override
				public Dataset getDataSet(String name, IMonitor monitor) {
					try {
						return (Dataset)LoaderFactory.getDataSet(file.getAbsolutePath(), name, monitor);
					} catch (Exception e) {
						return DatasetFactory.zeros(DoubleDataset.class, null);
					}
				}

				@Override
				public boolean isDataSetName(String name, IMonitor monitor) {
					return true;
				}
        	};
        	((ExpressionObject)nameOrExpression).setProvider(prov);
        	set = ((ExpressionObject)nameOrExpression).getDataSet(monitor);
        }
		
		if (set!=null&&useFileName) {
			final String name = set.getName();
			if (name!=null) {
				set.setName(name+" ("+file.getName()+")");
			}
		}
		return set;
	}

	private static File[] getFiles(Object[] objects) {
		final File[] files = new File[objects.length];
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof File) {
			    files[i] = (File)objects[i];
			} else if (objects[i] instanceof IFile) {
				files[i] = ((IFile)objects[i]).getLocation().toFile();
			}
		}
		return files;
	}

}
