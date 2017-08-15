/*-
 * Copyright © 2017 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.exafs.ui.dialogs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.exafs.ui.dialogs.OverridesForParametersFile.ParameterOverride;

public class MethodTreeViewDialog extends Dialog {
	private List<TreeItemData> treeItemDataList = new ArrayList<TreeItemData>();
	private Class<?> classTypeForTree;
	private Tree tree;

	protected MethodTreeViewDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		if (classTypeForTree==null) {
			classTypeForTree= B18SampleParameters.class;
		}
		treeViewTest(container);
		return container;
	}

	public OverridesForParametersFile getOverrideBean() {
		return getOverrideBean(treeItemDataList);
	}

	private OverridesForParametersFile getOverrideBean(List<TreeItemData> itemData) {
		OverridesForParametersFile overrideParams = new OverridesForParametersFile();
		overrideParams.setContainingClassType(classTypeForTree.getName());
		for(TreeItemData dataForItem : itemData) {
			String fullPathToMethod = dataForItem.getFullPathToMethod();
			if (dataForItem.isSelected() && !fullPathToMethod.endsWith("Parameters")) {
				overrideParams.addOverride(dataForItem.getFullPathToMethod(), "");
			}
		}
		return overrideParams;
	}

	// Setup the selected/de-selected state from override parameters
	public void setFromOverrides(OverridesForParametersFile overrides) {
		List<ParameterOverride> paramOverrides = overrides.getOverrides();

		// Loop over all items in tree, set checked status to true for each one that is present in Override params
		for (TreeItemData treeItemData : treeItemDataList) {
			String methodName = treeItemData.getFullPathToMethod();
			boolean selected = false;

			for(int i=0; i<paramOverrides.size() && selected==false; i++) {
				if (methodName.equals(paramOverrides.get(i).getFullPathToGetter())) {
					selected = true;

					// Make sure parent is checked and expanded.
					TreeItem parentTreeItem = treeItemData.getTreeItem().getParentItem();
					if (parentTreeItem!=null && parentTreeItem.getChecked()==false) {
						parentTreeItem.setChecked(true);
						parentTreeItem.setExpanded(true);
					}
				}
				treeItemData.setSelected(selected);
				treeItemData.getTreeItem().setChecked(selected);
			}
		}
		tree.redraw();
	}

	public void setClassTypeForTree(Class<?> classTypeForTree) {
		this.classTypeForTree = classTypeForTree;
	}

	private void treeViewTest(Composite shell) {

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));

		tree = new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		// tree.setLayout(new GridLayout(1, false));
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// final B18SampleParameters sampleParams = new B18SampleParameters();
		TreeItem treeItem0 = new TreeItem(tree, 0);
		treeItem0.setText("Sample Parameters");
		addMethodNamesToTree(classTypeForTree, treeItem0);

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				TreeItem item = (TreeItem) e.item;
				if (item != null && item.getData() != null) {
					TreeItemData itemData = (TreeItemData) item.getData();
					itemData.setSelected(item.getChecked());
				}

				for (TreeItem treeItem : tree.getSelection()) {
					TreeItemData itemData = (TreeItemData) treeItem.getData();
					if (itemData!=null) {
						String fullPath = itemData.getFullPathToMethod();
						boolean selected = treeItem.getChecked();
						itemData.setSelected(selected);
						TreeItem parentTreeItem = treeItem.getParentItem();
						if (selected && parentTreeItem!=null) {
							parentTreeItem.setChecked(true);
						}
						System.out.println(treeItem.getText()+", checked = "+treeItem.getChecked()+", item data = "+treeItem.getData()+", full path = "+fullPath);
					}
				}
			}
		});
		treeItem0.setExpanded(true);
		tree.redraw();
	}

	// Comparator for sorting array of Methods by name
    private class MethodComparator implements Comparator<Method> {
        @Override
        public int compare(Method e1, Method e2) {
            return e1.getName().compareTo(e2.getName());
        }
    }

	private void addMethodNamesToTree(Class<?> clazz, TreeItem parentTreeItem) {
		System.out.println("Methods for class " + clazz.getName() + " (" + clazz.getSimpleName() + ")");

		Method[] declaredMethods = clazz.getDeclaredMethods();
		// Sort into alphabetical order
		Arrays.sort(declaredMethods, new MethodComparator());

		for (Method method : declaredMethods) {
			String methodName = method.getName();
			boolean getterMethod = false;
			boolean parametersReturnType = false;
			// method is a 'getter' ?
			if (methodName.startsWith("get")) {
				getterMethod = true;
			}
			// method returns some more parameters ?
			if (methodName.endsWith("Parameters")) {
				parametersReturnType = true;
			}

			if (getterMethod) {
				// Name of entry in tree view (remove 'get' from start)
				String menuItemName = methodName.substring(3);

				// Create TreeItem for entry
				TreeItem treeItem = new TreeItem(parentTreeItem, 0);
				treeItem.setText(menuItemName);

				// Create data associated with tree item (i.e. parameter name, full path to get method, selection status)
				TreeItemData itemData = new TreeItemData(treeItem);
				itemData.setParameterName(methodName);
				itemData.setSelected(false);

				// Method has a parent, make sure the parent is also 'checked'
				String parentMethod = "";
				if (parentTreeItem.getData()!=null) {
					TreeItemData parentItemData = (TreeItemData) parentTreeItem.getData();
					parentMethod = parentItemData.getParameterName()+".";
				}
				// Full path to get method, including 'get' of parent parameter
				itemData.setFullPathToMethod(parentMethod+methodName);

				treeItem.setData(itemData);

				// Store ref to item data in a list, so can conveniently access it afterwards
				treeItemDataList.add(itemData);

				// the recursive bit - add methods to the sub menus
				if (parametersReturnType) {
					addMethodNamesToTree(method.getReturnType(), treeItem);
				}

			}
		}
		System.out.println("End of methods for class " + clazz.getName());
	}

	static private class TreeItemData {
		private TreeItem treeItem;

		private String parameterName = "";
		private String fullPathToMethod = "";

		// Constructor
		public TreeItemData(TreeItem treeItem) {
			this.treeItem = treeItem;
		}

		private boolean isSelected;


		public TreeItem getTreeItem() {
			return treeItem;
		}

		public String getFullPathToMethod() {
			return fullPathToMethod;
		}
		public void setFullPathToMethod(String fullPathToMethod) {
			this.fullPathToMethod = fullPathToMethod;
		}

		public String getParameterName() {
			return parameterName;
		}
		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
		}
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
	}
}
