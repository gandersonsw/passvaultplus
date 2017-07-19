/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.popupwidgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.graham.passvaultplus.MyUndoManager;

public class TypeAheadSelector extends AbstractPopupWidget<JTextField> {
	
	private final MyUndoManager undoManager;
	
	private JTable valueTable;
	private JScrollPane valueTableScroll;
	private TypeAheadTableModel tableModel;
	
	
	/**
	 * @param isAForcedShow if true, the user did something to try to force the dropdown to show, so be less restrictive about if it is shown
	 */
	public static boolean shouldShow(final JTextField tf, List<String> values, boolean isAForcedShow) {
		TypeAheadTableModel m = new TypeAheadTableModel(values);
		m.computeMatches(tf.getText());
		return m.shouldShow(isAForcedShow);
	}
	
	public TypeAheadSelector(final JFrame o, final JTextField tf, List<String> values, MyUndoManager undoManagerParam) {
		super(o, tf);
		
		undoManager = undoManagerParam;
		
		tableModel = new TypeAheadTableModel(values);
		tableModel.computeMatches(parentComponent.getText());
		
		tf.getDocument().addDocumentListener(new TextFieldChangeListener());
	
		popupWindow.add(buildValueTable(), BorderLayout.CENTER);
		popupWindow.pack();
		
		updateLocationRelativeToParent();

		popupWindow.setVisible(true);
	}
	
	@Override
	public void handleEnter() {
		setParentText(tableModel.getValueAt(0, 0).toString());
	}
	
	private void setParentText(final String txt) {
		undoManager.setForceMergeIfSameEditSource(true);
		parentComponent.setText(txt);
		undoManager.setForceMergeIfSameEditSource(false);
	}
	
	private JScrollPane buildValueTable() {
		valueTable = new JTable(tableModel);
		valueTable.setTableHeader(null);
	
		TableColumn column = valueTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(170);
		column.setCellRenderer(new ValueTableCellRenderer());

		valueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		valueTable.getSelectionModel().addListSelectionListener(new TableSelectionChangedListener());
		
		valueTableScroll = new JScrollPane(valueTable);
		valueTableScroll.setPreferredSize(new Dimension(170,100));
		
		return valueTableScroll;
	}
	
	
	public static class TypeAheadTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		
		private final List<String> originalValues;
		private List<MatchedValue> values;
		
		public TypeAheadTableModel(List<String> valuesParam) {
			originalValues = valuesParam;
			values = new ArrayList<>();
			for (String v : originalValues) {
				values.add(new MatchedValue(v));
			}
		}
		
		public void computeMatches(final String inputText) {
			final String inputTextToLower = inputText.toLowerCase();
			for (MatchedValue mv : values) {
				if (mv.textToLower.equals(inputTextToLower)) {
					mv.match = MatchedValue.COMPLETE_MATCH;
				} else if (mv.textToLower.startsWith(inputTextToLower)) {
					mv.match = MatchedValue.STARTSWITH_MATCH;
				} else if (mv.textToLower.indexOf(inputTextToLower) != -1) {
					mv.match = MatchedValue.CONTAINS_MATCH;
				} else {
					mv.match = MatchedValue.NO_MATCH;
				}
			}
			
			Collections.sort(values, matchedComparator);
		}
		
		public boolean shouldShow(final boolean showIfSizeNotZero) {
			if (values.size() == 0) {
				return false;
			}
			if (showIfSizeNotZero) {
				return true;
			}
			int completeMatchCount = 0;
			int containsMatchCount = 0;
			for (MatchedValue mv : values) {
				if (mv.match == MatchedValue.COMPLETE_MATCH) {
					completeMatchCount++;
				} else if (mv.match == MatchedValue.STARTSWITH_MATCH) {
					containsMatchCount++;
				} else if (mv.match == MatchedValue.CONTAINS_MATCH) {
					containsMatchCount++;
				}
			}
			if (completeMatchCount == 1) {
				return false; // there was exactly 1 match, so don't show it
			}
			if (containsMatchCount == 0) {
				return false; // there where no contains matches, so dont show it
			}
			return true;
		}
		
		@Override
		public int getRowCount() {
			return values.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return values.get(rowIndex);
		}
	}
	
	static class MatchedValue {
		final static int NO_MATCH = 3;
		final static int CONTAINS_MATCH = 2;
		final static int STARTSWITH_MATCH = 1;
		final static int COMPLETE_MATCH = 0;
		final String text;
		final String textToLower;
		int match; // 0 - no match, 1 - contains match, 2 - startsWith match, 3 - complete match
		public MatchedValue(String t) {
			text = t;
			textToLower = text.toLowerCase();
		}
		@Override
		public String toString() {
			return text;
		}
	}
	
	static class MatchedComparator implements Comparator<MatchedValue> {
		@Override
		public int compare(MatchedValue o1, MatchedValue o2) {
			int c1 = Integer.compare(o1.match, o2.match);
			if (c1 == 0) {
				return o1.text.compareTo(o2.text);
			}
			return c1;
		}
		
	}
	final static MatchedComparator matchedComparator = new MatchedComparator();
	
	class TableSelectionChangedListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return; 
			}
			if (valueTable.getSelectedRow() >= 0 && valueTable.getSelectedColumn() >= 0) {
				Object val = tableModel.getValueAt(valueTable.getSelectedRow(), valueTable.getSelectedColumn());
				if (val != null) {
					setParentText(val.toString());
				}
			}
		}
	}
	
	class TextFieldChangeListener implements DocumentListener {
		public void checkTextFieldValue() {
			tableModel.computeMatches(parentComponent.getText());
			tableModel.fireTableDataChanged();
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			checkTextFieldValue();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			checkTextFieldValue();
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			checkTextFieldValue();
		}
	}

	static class ValueTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			// Cells are by default rendered as a JLabel.
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (value instanceof MatchedValue) {
				MatchedValue mv = (MatchedValue)value;
				if (mv.match == MatchedValue.COMPLETE_MATCH) {
					l.setBackground(Color.YELLOW);
				} else if (mv.match == MatchedValue.STARTSWITH_MATCH) {
					l.setBackground(Color.WHITE);
				} else if (mv.match == MatchedValue.CONTAINS_MATCH) {
					l.setBackground(Color.LIGHT_GRAY);
				} else {
					l.setBackground(Color.GRAY);
				}
			}
			// Return the JLabel which renders the cell.
			return l;
		}
	}
}
