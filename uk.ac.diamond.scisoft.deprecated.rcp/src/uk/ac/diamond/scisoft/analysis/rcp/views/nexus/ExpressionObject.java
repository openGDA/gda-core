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

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
import uk.ac.gda.monitor.ProgressMonitorWrapper;

public class ExpressionObject {
	
	private String expression;
	private String mementoKey;
	private DataSetProvider provider;
	
	public ExpressionObject(DataSetProvider provider) {
		this(provider, null, generateMementoKey());
	}


	public ExpressionObject(final DataSetProvider provider, String expression, String mementoKey) {
		this.provider   = provider;
		this.expression = expression;
		this.mementoKey = mementoKey;
	}

	public static boolean isExpressionKey(final String key) {
		if (key==null)      return false;
		if ("".equals(key)) return false;
		return key.matches("Expression\\:(\\d)+");
	}

	private static String generateMementoKey() {
		return "Expression:"+((new Date()).getTime());
	}


	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression The expression to set.
	 */
	public void setExpression(String expression) {
		this.dataSet    = null;
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((mementoKey == null) ? 0 : mementoKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionObject other = (ExpressionObject) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (mementoKey == null) {
			if (other.mementoKey != null)
				return false;
		} else if (!mementoKey.equals(other.mementoKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return expression!=null ? expression : "";
	}
	
	public boolean isValid(IProgressMonitor monitor) {
		try {
			final SymbolTable vars = getSymbolTable();
		    for (Object key : vars.keySet()) {
		    	final Object value = vars.getValue(key);
		    	if (value==null) {
		    		if (monitor.isCanceled()) return false;
		    		if (!provider.isDataSetName(key.toString(), new ProgressMonitorWrapper(monitor))) return false;
		    	}
			}
			return true;
		} catch (Exception ne) {
			return false;
		}
	}

	/**
	 * Returns the size of the expression in the current environment.
	 * @return the size
	 */
	public int getSize(IProgressMonitor monitor) {
		if (dataSet==null) {
			try {
				getDataSet(monitor);
			} catch (Exception e) {
				return 0;
			}
		}
		return dataSet!=null ? dataSet.getSize() : 0;
	}

	private JEP             jepParser;
	private AbstractDataset dataSet;
	public AbstractDataset getDataSet(IProgressMonitor monitor) throws Exception {
		
		if (dataSet!=null) return dataSet;
		
	    if (expression==null||provider==null) return new DoubleDataset();
		
		final List<AbstractDataset> refs = getVariables(monitor);
		final double[]       data = new double[refs.get(0).getSize()];
		
		for (int i = 0; i < data.length; i++) {
			for (AbstractDataset d : refs) {
				jepParser.addVariable(d.getName(), d.getDouble(i));
			}
			jepParser.parseExpression(expression);
			data[i] = jepParser.getValue();
		}
		
		this.dataSet = new DoubleDataset(data);
		dataSet.setName(getExpression());
		return this.dataSet;
	}

	private List<AbstractDataset> getVariables(IProgressMonitor monitor) throws Exception {
		
		final List<AbstractDataset> refs = new ArrayList<AbstractDataset>(7);
		final SymbolTable vars = getSymbolTable();
	    for (Object key : vars.keySet()) {
	    	final Object value = vars.getValue(key);
	    	if (value==null) {
	    		if (monitor.isCanceled()) return null;
	    		final AbstractDataset set = provider!=null ? provider.getDataSet(key.toString(), new ProgressMonitorWrapper(monitor)) : null;
	    		if (set!=null) refs.add(set);
	    	}
		}
	    
		if (refs.isEmpty()) throw new Exception("No variables recognized in expression.");
		
		// Check all same size
		final int size = refs.get(0).getSize();
		for (IDataset dataSet : refs) {
			if (dataSet.getSize()!=size) throw new Exception("Data sets in expression are not all the same size.");
		}

	    return refs;
	}

	private SymbolTable getSymbolTable() throws ParseException {
		jepParser = new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(true);
		jepParser.setImplicitMul(true);
		
	    jepParser.parse(expression);
	    return jepParser.getSymbolTable();
	}

	/**
	 * @return Returns the provider.
	 */
	public DataSetProvider getProvider() {
		return provider;
	}


	/**
	 * @param provider The provider to set.
	 */
	public void setProvider(DataSetProvider provider) {
		this.provider = provider;
	}


	/**
	 * @return Returns the mementoKey.
	 */
	public String getMementoKey() {
		return mementoKey;
	}


	/**
	 * @param mementoKey The mementoKey to set.
	 */
	public void setMementoKey(String mementoKey) {
		this.mementoKey = mementoKey;
	}


	/**
	 * Clears the current calculated data set from memory.
	 * Does not 
	 */
	public void clear() {
		this.dataSet = null;
	}
	
}
