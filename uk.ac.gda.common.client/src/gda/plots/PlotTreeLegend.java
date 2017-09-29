/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jfree.util.ShapeUtilities;

public class PlotTreeLegend extends JPanel implements XYDataHandlerLegend {
	private TreeListenerArea treeListenerArea;
	private TreeArea treeArea;
	private JCheckBoxMenuItem menuAutoCollapseTreeOnAdd;
	private JCheckBoxMenuItem menuUnshowLast, menuUnshowNew;
	private ScanTree model;

	/**
	 * @param simplePlot
	 */
	public PlotTreeLegend(XYDataHandler simplePlot) {
		treeListenerArea = new TreeListenerArea();
		model  = new ScanTree(new ScanTreeM(), simplePlot);
		treeArea = new TreeArea( treeListenerArea,model);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JMenuBar menuBar = new JMenuBar();
		menuAutoCollapseTreeOnAdd = new JCheckBoxMenuItem("Auto-update Tree");
		menuAutoCollapseTreeOnAdd.setMnemonic(KeyEvent.VK_T);
		menuAutoCollapseTreeOnAdd.setToolTipText("Auto-update will cause tree structure to change as lines are added");
		menuAutoCollapseTreeOnAdd.setSelected(true);
		menuAutoCollapseTreeOnAdd.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (menuAutoCollapseTreeOnAdd.isSelected()) {
						treeArea.getModel().reload();
					}
				} catch (Exception ex) {
					// do nothing - sometimes since if tree is being modified
				}
			}

		});
		menuUnshowLast = new JCheckBoxMenuItem("Auto-hide last scan");
		menuUnshowLast.setMnemonic(KeyEvent.VK_L);
		menuUnshowLast.setToolTipText("Hide plots from previous scan.");
		menuUnshowLast.setSelected(false);

		menuUnshowNew = new JCheckBoxMenuItem("Auto-hide new scan");
		menuUnshowNew.setMnemonic(KeyEvent.VK_N);
		menuUnshowNew.setToolTipText("Hide plots from new scans.");
		menuUnshowNew.setSelected(false);

		JMenuItem menuHideAll = new JMenuItem("Hide all");
		menuHideAll.setMnemonic(KeyEvent.VK_H);
		menuHideAll.setToolTipText("Hide all plots");
		menuHideAll.setSelected(false);
		menuHideAll.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				treeArea.getModel().makeAllVisible(false);
			}
		});

		JMenuItem menuTreeVisible = new JMenuItem("Show All");
		menuTreeVisible.setMnemonic(KeyEvent.VK_S);
		menuTreeVisible.setSelected(true);
		menuTreeVisible.setToolTipText("Show all plots");
		menuTreeVisible.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				treeArea.getModel().makeAllVisible(true);
			}
		});

		menuBar.add(menuHideAll);
		JMenu optionsMenu = new JMenu("Options");
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		optionsMenu.add(menuUnshowNew);
		optionsMenu.add(menuUnshowLast);
		optionsMenu.add(menuAutoCollapseTreeOnAdd);
		optionsMenu.add(menuTreeVisible);
		menuBar.add(optionsMenu);
		setLayout(new BorderLayout());
		add(menuBar,BorderLayout.NORTH);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.add(treeArea);
		panel.add(treeListenerArea);
		treeListenerArea.setMinimumSize(new Dimension(100,100));
		add(panel,BorderLayout.CENTER);
	}

	@Override
	public void addScan(String currentFilename, String topGrouping, String [] subGrouping,
			String itemName, boolean visible, String id, int lineNumber,
			Color color, Marker marker, boolean onlyOne, String xLabel, boolean reloadLegendModel){
		boolean menuUnshowNewVal = getMenuUnShowNewVal();
		DefaultMutableTreeNode node = model.addScanLine(currentFilename, topGrouping, subGrouping,
				new ScanLine(itemName, !menuUnshowNewVal & visible, id, lineNumber, color, marker, xLabel),
				onlyOne, getMenuAutoCollapseTreeOnAdd(), getMenuUnshowLast(),
				menuUnshowNewVal, reloadLegendModel);

		treeArea.addScan(node, getMenuAutoCollapseTreeOnAdd());
	}
	private boolean getMenuUnshowLast() {
		return menuUnshowLast.isSelected();
	}

	private boolean getMenuAutoCollapseTreeOnAdd() {
		return menuAutoCollapseTreeOnAdd.isSelected();
	}

	private boolean getMenuUnShowNewVal() {
		return  menuUnshowNew.isSelected();
	}

	@Override
	public void removeAllItems(){
		treeArea.removeAllItems();
	}

	@Override
	public Vector<String> getNamesOfLinesInPreviousScan(boolean visibility) {
		return model.getNamesOfLinesInPreviousScan(visibility);
	}

}

class TreeArea extends JScrollPane {
	private JTree tree;
	private ScanTree model;

	TreeArea(TreeListenerArea area, ScanTree model) {
		this.model = model;
		tree = new JTree(model);
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new ScanTreeRenderer());
		tree.setShowsRootHandles(true);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setCellEditor(new ScanTreeCellEditor(tree));
		tree.setEditable(true);
		area.setTree(tree);
		setViewportView(tree);
	}

	void addScan(DefaultMutableTreeNode node, boolean autoCollapseTree) {
		if (autoCollapseTree)
			tree.makeVisible(new TreePath(node.getPath()));
	}

	public void removeAllItems(){
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
		while(rootNode.getChildCount()>0){
			model.removeNodeFromParent((DefaultMutableTreeNode)rootNode.getChildAt(0));
		}
	}

	public ScanTree getModel() {
		return model;
	}

}

class ScanTreeRenderer implements TreeCellRenderer {
	private SelectableNodeRenderer selectableNodeRenderer = new SelectableNodeRenderer();
	private ScanPairRenderer scanPairRenderer = new ScanPairRenderer();
	private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	ScanTreeRenderer() {
		Font fontValue;
		fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) {
			selectableNodeRenderer.setFont(fontValue);
			scanPairRenderer.setFont(fontValue);
		}
		Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		selectableNodeRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
		scanPairRenderer.getCheckBox().setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		Component returnValue = null;
		if (value != null && value instanceof ScanPair) {
			scanPairRenderer.configure((ScanPair) value);
			returnValue = scanPairRenderer;
		}
		else if (value != null && value instanceof ISelectableNode) {
			selectableNodeRenderer.configure((ISelectableNode) value);
			returnValue = selectableNodeRenderer;
		}
		if (returnValue == null) {
			returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
					hasFocus);
		}
		return returnValue;
	}
}


class SelectableNodeRenderer extends JCheckBox implements ISelectable {
	private final static int maxLength = 25;
	private static Icon savedIcon;
	private static Icon icon;
	static {
		icon = new ImageIcon(SelectableNodeRenderer.class.getResource("partialSelection.gif"));
	}

	SelectableNodeRenderer() {
		if (savedIcon == null) {
			savedIcon = getSelectedIcon();
		}
	}

	void configure(ISelectableNode node) {
		String text = node.toLabelString(maxLength);
		if (text.length() > maxLength) {
			text = "." + text.substring(text.length() - maxLength + 1);
		}
		setText(text);
		Selected visibility = node.getSelected();
		setSelected(visibility == Selected.All);
		setIcon(visibility == Selected.Some ? icon : savedIcon);
		setOpaque(false);
	}

}

class ScanPairRenderer extends JPanel implements ISelectable {
	private static final int size = 4;
	private JCheckBox checkBox = new JCheckBox();
	private MButton btn1 = new MButton();
	private static final int maxLength = 25;

	ScanPairRenderer() {
		setLayout(new FlowLayout());
		add(btn1);
		btn1.setOpaque(false);
		add(checkBox);
		checkBox.setOpaque(false);
	}

	void configure(ScanPair node) {
		String text = node.toLabelString(maxLength);
		checkBox.setText(text);
		checkBox.setSelected(node.scanLine.visible);
		checkBox.setForeground(node.scanLine.lineColor);
		btn1.setShape(node.scanLine.marker.getShape(size));
		Rectangle rect = btn1.getShape().getBounds();
		btn1.setShape(ShapeUtilities.createTranslatedShape(btn1.getShape(), size, size));
		btn1.setColor(node.scanLine.lineColor);
		btn1.setPreferredSize(new Dimension(rect.width + 1, rect.height + 1));
		validate();
		setOpaque(false);
	}

	@Override
	public boolean isSelected() {
		return checkBox.isSelected();
	}

	public JCheckBox getCheckBox() {
		return checkBox;
	}

}

class MButton extends JComponent {
	private Shape shape;
	private Color color;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			Color old = g2.getColor();
			g2.setColor(getColor());
			g2.draw(getShape());
			g2.setColor(old);
		}
	}

	public Shape getShape() {
		return shape;
	}

	public Color getColor() {
		return color;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}

class ScanTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
	private SelectableNodeRenderer selectableNodeRenderer = new SelectableNodeRenderer();
	private ScanPairRenderer scanPairRenderer = new ScanPairRenderer();
	private JTree tree;
	private Component editor;
	private Object objectBeingEdited;

	ScanTreeCellEditor(JTree tree) {
		this.tree = tree;
		Font fontValue;
		fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) {
			selectableNodeRenderer.setFont(fontValue);
			scanPairRenderer.setFont(fontValue);
		}
		Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		selectableNodeRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
		scanPairRenderer.getCheckBox().setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
	}

	@Override
	public Object getCellEditorValue() {
		if (editor != null && editor instanceof ISelectable) {
			boolean isVisible = ((ISelectable) editor).isSelected();
			if (objectBeingEdited instanceof ISelectableNode) {
				((ISelectableNode) objectBeingEdited).setSelectedFlag(isVisible);
			}
		}
		return objectBeingEdited;
	}

	@Override
	public boolean isCellEditable(EventObject event) {
		boolean returnValue = false;
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (path != null) {
				Object node = path.getLastPathComponent();
				returnValue = ((node != null) && node instanceof DefaultMutableTreeNode);
			}
		}
		return returnValue;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row) {

		Component returnValue = null;
		if (value != null && value instanceof ScanPair) {
			scanPairRenderer.configure((ScanPair) value);
			returnValue = scanPairRenderer;
		} else if (value != null && value instanceof ISelectableNode) {
			selectableNodeRenderer.configure((ISelectableNode) value);
			returnValue = selectableNodeRenderer;
		}
		if (returnValue != null && (returnValue instanceof AbstractButton || returnValue instanceof ScanPairRenderer)) {
			AbstractButton btn;
			if (returnValue instanceof AbstractButton)
				btn = (AbstractButton) returnValue;
			else
				btn = ((ScanPairRenderer) returnValue).getCheckBox();
			ItemListener itemListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					if (stopCellEditing()) {
						fireEditingStopped();
					}
				}
			};
			btn.addItemListener(itemListener);
			objectBeingEdited = value;
		}

		return (editor = returnValue);
	}
}

class TreeListenerArea extends JScrollPane implements TreeSelectionListener {
	private JTextArea area;
	private static final String comment = "Select a line's marker to view details";
	private ScanPair scanPair;
	private JTree tree;

	void setTree(JTree tree){
		this.tree = tree;
		this.tree.addTreeSelectionListener(this);
	}

	TreeListenerArea() {

		JMenuBar menuBar = new JMenuBar();
		JMenuItem menuHideAll = new JMenuItem("Properties");
		menuHideAll.setMnemonic(KeyEvent.VK_P);
		menuHideAll.setToolTipText("Set line properties");
		menuHideAll.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if( scanPair != null && tree != null){
					SimpleXYSeries sxys = new SimpleXYSeries(scanPair.scanLine.name, scanPair.scanLine.line, 0);
					sxys.setPaint(scanPair.scanLine.lineColor);
					sxys.setMarker(scanPair.scanLine.marker);
					Object obj = getTopLevelAncestor();
					@SuppressWarnings("unused")
					LinePropertiesEditor lpe = new LinePropertiesEditor((obj instanceof JFrame) ? (JFrame) obj : null, "Properties", "scanLine.name", sxys,
							false);
					scanPair.scanLine.lineColor = (Color) sxys.getPaint();
					scanPair.scanLine.marker = sxys.getMarker();
					tree.getModel().valueForPathChanged(new TreePath(scanPair.getPath()), scanPair.scanLine);
				}
			}
		});
		menuBar.add(menuHideAll);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createBevelBorder(1));
		panel.setLayout(new BorderLayout());
		panel.add(menuBar,BorderLayout.NORTH);
		area = new JTextArea(comment);
		panel.add(area, BorderLayout.CENTER);
		setViewportView(panel);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		/* if nothing is selected */
		if (node != null && node instanceof ScanPair) {
			scanPair = (ScanPair) node;
			area.setText(scanPair.scanLine.name);
			for (String s : scanPair.getParents()) {
				area.append("\n" + s);
			}
		} else
			area.setText(comment);
		return;
	}
}
