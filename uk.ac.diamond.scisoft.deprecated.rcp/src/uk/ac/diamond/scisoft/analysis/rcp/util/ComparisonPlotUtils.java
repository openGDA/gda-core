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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.ExpressionObject;
import uk.ac.gda.monitor.IMonitor;
import uk.ac.gda.monitor.ProgressMonitorWrapper;

public class ComparisonPlotUtils {

	/**
	 * The selection array must file java.io.File[] or org.eclipse.core.resources.IFile[]
	 * @param files -  java.io.File[] or org.eclipse.core.resources.IFile[]
	 * @param selections - list of either String data set name or ExpressionObject
	 */
	public static void createComparisionPlot(final Object[] files, final List<Object> selections, final PlotMode plotMode, final PlotWindow window, final IProgressMonitor monitor) throws Exception{

		Object xSel = selections.get(0);

		final List<Object> ySel = new ArrayList<Object>(3);
		for (int i = 1; i < selections.size(); i++) {
			ySel.add(selections.get(i));
		}

		final File[]         fa = getFiles(files);
		AbstractDataset             x  = getLargestDataSet(fa, xSel, monitor);
		final List<AbstractDataset> ys = getDataSets(fa, ySel, true, monitor);
		
		if (ys.isEmpty()) {
			ys.add(x);
			x = DoubleDataset.arange(ys.get(0).getSize());
			x.setName("Index");
		}

		PlotUtils.create1DPlot(x, ys, plotMode, window, monitor);
	}

	private static List<AbstractDataset> getDataSets(final File[] files, final List<Object> namesOrExpressions, final boolean useFileName, final IProgressMonitor monitor) throws Exception{
		final List<AbstractDataset> ret = new ArrayList<AbstractDataset>(files.length*namesOrExpressions.size());
		for (int i = 0; i < files.length; i++) {
			for (Object n : namesOrExpressions) {
				ret.add(getDataSet(files[i], n, useFileName, monitor));
			}
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private static AbstractDataset getLargestDataSet(final File[] files, final Object nameOrExpression, final IProgressMonitor monitor) throws Exception {
		
		AbstractDataset ret = null;
		int    size = Integer.MIN_VALUE;
		for (int i = 0; i < files.length; i++) {
			try {
				final AbstractDataset set = getDataSet(files[i], nameOrExpression, false, monitor);
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
	
	private static AbstractDataset getDataSet(final File file, final Object nameOrExpression, final boolean useFileName, final IProgressMonitor monitor) throws Exception {
        
		AbstractDataset set=null;
		if (nameOrExpression instanceof String) {
		    set = LoaderFactory.getDataSet(file.getAbsolutePath(), (String)nameOrExpression, new ProgressMonitorWrapper(monitor));
       	
        } else if (nameOrExpression instanceof ExpressionObject) {
        	final DataSetProvider prov = new DataSetProvider() {
				@Override
				public AbstractDataset getDataSet(String name, IMonitor monitor) {
					try {
						return LoaderFactory.getDataSet(file.getAbsolutePath(), name, monitor);
					} catch (Exception e) {
						return new DoubleDataset();
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
