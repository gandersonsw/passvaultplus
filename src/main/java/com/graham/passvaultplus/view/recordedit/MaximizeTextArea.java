/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.graham.passvaultplus.PvpContext;

public class MaximizeTextArea extends AbstractAction {

	final private RecordEditContext editContext;
	final private JTextArea smallTextArea;
	
	MaximizeTextArea(final ImageIcon icon, final RecordEditContext editContextParam, final JTextArea smallTextAreaParam) {
		super(icon == null ? "M" : null, icon);
		editContext = editContextParam;
		smallTextArea = smallTextAreaParam;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0078");
		editContext.maximizedTextAreaPanel = new JPanel(new BorderLayout());
		
		// the parent will always be the ScrollPane JViewport
		Container taParent = smallTextArea.getParent();
		taParent.remove(smallTextArea);
		
		JScrollPane sp = new JScrollPane(smallTextArea);
		editContext.maximizedTextAreaPanel.add(sp, BorderLayout.CENTER);
		
		JButton minButton = new JButton(new MinimizeTextArea(PvpContext.getIcon("panel-minimize-small"), editContext, smallTextArea, taParent));
		minButton.setFocusable(false);
		minButton.setToolTipText("return text to normal size");
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(minButton, BorderLayout.NORTH);
		editContext.maximizedTextAreaPanel.add(westPanel, BorderLayout.WEST);
		
		editContext.panelInTabPane.remove(editContext.centerPaneWithFields);
		editContext.panelInTabPane.add(editContext.maximizedTextAreaPanel, BorderLayout.CENTER);
		
		editContext.panelInTabPane.revalidate();
		editContext.panelInTabPane.repaint();
	}

}
