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

import com.graham.passvaultplus.actions.TextFieldChangeForwarder;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.util.ResourceUtil;

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
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0089");
		final PvpField field = reb.record.getType().getField(name);

		JTextComponent tf;
		JComponent textComponent;
		JComponent rightWidget = null;
		if (field.isTypeLongString()) {
			JTextArea textArea = new JTextArea(reb.record.getCustomField(name));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane sp = new JScrollPane(textArea);
			sp.setPreferredSize(new Dimension(100, 120));
			tf = textArea;
			textComponent = sp;
		} else {
			JTextField textField = new JTextField(reb.record.getCustomField(name));
			new TextFieldPopUpHandler(reb.context.uiMain.getMainFrame(), textField, field, reb.record.getType(), reb.context.data.getDataInterface(), reb.context.uiMain.getUndoManager());
			tf = textField;
			textComponent = textField;
			reb.rcPopup.addListener(tf);
		}

		RecordEditFieldText ref;
		if (field.isClassificationSecret() && !reb.isNewRecord) {
			final RecordEditFieldSecret refs = new RecordEditFieldSecret(tf, name, reb.editContext);
			ref = refs;
			JButton showSecretFieldButton = new JButton(new UnlockFieldAction(ResourceUtil.getIcon("unlock-small"), refs, reb.editContext));
			showSecretFieldButton.setFocusable(false);
			showSecretFieldButton.setToolTipText("show value");
			
			JButton generatePasswordMenu = GenPasswordMenu.createButton(ref);

			FlowLayout layout = new FlowLayout();
			layout.setVgap(0);
			rightWidget = new JPanel(layout);
			rightWidget.add(showSecretFieldButton);
			rightWidget.add(generatePasswordMenu);
		} else {
			ref = new RecordEditFieldJTextComponent(tf, name, reb.editContext);
		}

		tf.getDocument().addUndoableEditListener(reb.context.uiMain.getUndoManager());
		tf.addCaretListener(reb.context.uiMain.getUndoManager());
		tf.getDocument().addDocumentListener(new TextFieldChangeForwarder(ref.afcAction));

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
		CopyFieldToClipboardAction copyAction = new CopyFieldToClipboardAction(ResourceUtil.getIcon("copy-small"), ref);
		JButton copyButton = new JButton(copyAction);
		copyButton.setFocusable(false);
		if (field.isTypeLongString()) {
			JPanel leftButtons = new JPanel(new BorderLayout());
			leftButtons.add(copyButton, BorderLayout.SOUTH);
			JButton max = new JButton(new MaximizeTextArea(ResourceUtil.getIcon("panel-maximize-small"), reb.editContext, (JTextArea)tf)); // TODO
			max.setFocusable(false);
			max.setToolTipText("maximize this text");
			leftButtons.add(max, BorderLayout.NORTH);
			leftPanel.add(leftButtons);
		} else {
			leftPanel.add(copyButton);
		}
	}

}
