/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.TextFieldChangeForwarder;
import com.graham.passvaultplus.model.core.PvpField;

/**
 * Builds the UI and the RecordEditField controller
 */
class RecordEditFieldBuilder {
	
	final private String name;
	final private RecordEditBuilder reb;
	private JPanel leftPanel;
	private JComponent rightComponent;

	RecordEditFieldBuilder(String n, RecordEditBuilder rebParam) {
		name = n;
		reb = rebParam;
	}
	
	JComponent getLeftComponent() {
		if (leftPanel == null) {
			build();
		}
		return leftPanel;
	}
	
	JComponent getRightComponent() {
		if (rightComponent == null) {
			build();
		}
		return rightComponent;
	}
	
	private void build() {
		final PvpField field = reb.record.getType().getField(name);
		
		JTextComponent tf;
		JComponent textComponent;
		JComponent rightWidget = null;
		if (PvpField.TYPE_LONG_STRING.equals(field.getType())) {
			JTextArea textArea = new JTextArea(reb.record.getCustomField(name));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane sp = new JScrollPane(textArea);
			sp.setPreferredSize(new Dimension(100, 120));
			tf = textArea;
			textComponent = sp;
		} else {
			JTextField textField = new JTextField(reb.record.getCustomField(name));
			TextFieldPopUpHandler fieldListener = new TextFieldPopUpHandler(reb.context.getMainFrame(), textField, field, reb.record.getType(), reb.context.getDataInterface());
			textField.addFocusListener(fieldListener);
			fieldListener.addInputAndActionMapItems();
			textField.getDocument().addDocumentListener(fieldListener.getDocumentListener());
			tf = textField;
			textComponent = textField;
		}
		
		RecordEditField ref;
		if (field.isClassificationSecret() && !reb.isNewRecord) {
			final RecordEditFieldSecret refs = new RecordEditFieldSecret(tf, name);
			ref = refs;
			JButton showSecretFieldButton = new JButton(new UnlockFieldAction(PvpContext.getIcon("unlock-small"), refs, reb.editContext));
			showSecretFieldButton.setFocusable(false);
			showSecretFieldButton.setToolTipText("show value");
			rightWidget = showSecretFieldButton;
		} else {
			ref = new RecordEditFieldJTextComponent(tf, name);
		}
		
		tf.getDocument().addUndoableEditListener(reb.context.getUndoManager());
		tf.addCaretListener(reb.context.getUndoManager());
		tf.getDocument().addDocumentListener(new TextFieldChangeForwarder(new AnyFieldChangedAction(reb.editContext, ref)));
		
		reb.editContext.editFields.put(name, ref);
		EmptyBorder eBorder = new EmptyBorder(3,3,3,3);
		CompoundBorder cBorder = new CompoundBorder(eBorder, tf.getBorder());
		tf.setBorder(cBorder);
		
		if (rightWidget != null) {
			JPanel p55 = new JPanel(new BorderLayout());
			p55.add(textComponent, BorderLayout.CENTER);
			p55.add(rightWidget, BorderLayout.EAST);
			rightComponent = p55;
		} else {
			rightComponent = textComponent;
		}
		
		// Build the Left Component ******************************
		leftPanel = new JPanel(new FlowLayout());
		leftPanel.add(new JLabel(name + ":", JLabel.RIGHT));
		CopyFieldToClipboardAction copyAction = new CopyFieldToClipboardAction(PvpContext.getIcon("copy-small"), ref);
		JButton copyButton = new JButton(copyAction);
		copyButton.setFocusable(false);
		if (PvpField.TYPE_LONG_STRING.equals(field.getType())) {
			JPanel leftButtons = new JPanel(new BorderLayout());
			leftButtons.add(copyButton, BorderLayout.SOUTH);
			JButton max = new JButton(new MaximizeTextArea(PvpContext.getIcon("panel-maximize-small"), reb.editContext, (JTextArea)tf)); // TODO
			max.setFocusable(false);
			max.setToolTipText("maximize this text");
			leftButtons.add(max, BorderLayout.NORTH);
			leftPanel.add(leftButtons);
		} else {
			leftPanel.add(copyButton);
		}
	}
	
}
