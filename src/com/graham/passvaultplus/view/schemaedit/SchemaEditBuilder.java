/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.schemaedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;

public class SchemaEditBuilder {
	final private PvpContext context;
	private JPanel panelInTabPane;
	
	public static JPanel buildEditor(final PvpContext contextParam) {
		return new SchemaEditBuilder(contextParam).build();
	}

	public SchemaEditBuilder(final PvpContext contextParam) {
		context = contextParam;
	}

	private JPanel build() {
		panelInTabPane = new JPanel(new BorderLayout());
		panelInTabPane.add(buildSchemaTop(), BorderLayout.NORTH);
		//panelInTabPane.add(buildEditorTop(), BorderLayout.CENTER);
		//panelInTabPane.add(buildEditorBottom(), BorderLayout.SOUTH);
		return panelInTabPane;
	}
	
	private JPanel buildSchemaTop() {
		List<PvpType> types = context.getDataInterface().getTypes();
		
		JComboBox<PvpType> cb = new JComboBox<>();
		for (PvpType t : types) {
			cb.addItem(t);
		}
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("Type:"));
		p.add(cb);
		p.add(new JButton(new EditTypeAction(cb, panelInTabPane)));
		p.add(new JButton("Create New"));
		p.add(new JButton("Save Changes"));
		p.add(new JButton("Close"));
		
		return p;
	}
	
	private static Component buildSchemaEditor(final PvpType type) {
		if (type == null) {
			System.out.println("TODO: new type");
		}
		
		
		List<PvpField> fields = type.getFields();
		final JPanel p = new JPanel(new GridLayout(fields.size() + 1, 1));
		
		System.out.println("buildSchemaEditor at 1");
		
		final JPanel firstAddP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		firstAddP.add(new JButton("Add Field"));
		p.add(firstAddP);
		for (PvpField f : fields) {
			System.out.println("schema editor field=" + f.getName());
			final JTextField tf = new JTextField(16);
			tf.setText(f.getName());
			final JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			fieldPanel.add(tf);
			fieldPanel.add(new JButton("Delete Field"));
			fieldPanel.add(new JButton("Add Field"));
			p.add(fieldPanel);
		}
		
		JScrollPane sp = new JScrollPane(p);
		
		
		return sp;
		
	}
	
	static class EditTypeAction extends AbstractAction {
		private final JComboBox<PvpType> typeCB;
		private final JPanel tabPanel;
		public EditTypeAction(final JComboBox<PvpType> typeCBParam, final JPanel tabPanelParam) {
			super("Edit");
			typeCB = typeCBParam;
			tabPanel = tabPanelParam;
		}
		public void actionPerformed(ActionEvent e) {
			tabPanel.add(buildSchemaEditor((PvpType)typeCB.getSelectedItem()), BorderLayout.CENTER);
			
		}
	}
	
	
	
	
	
}
