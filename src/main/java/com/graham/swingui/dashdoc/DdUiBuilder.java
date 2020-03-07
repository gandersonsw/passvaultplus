/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.dashdoc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.graham.swingui.dashdoc.model.DashDoc;
import com.graham.swingui.dashdoc.model.DdContainer;
import com.graham.swingui.dashdoc.model.DdLink;
import com.graham.swingui.dashdoc.model.DdPart;
import com.graham.swingui.dashdoc.model.DdSection;
import com.graham.swingui.dashdoc.model.DdText;

public class DdUiBuilder {

	private final DashDoc ddoc;
	private final MouseListener linkClickListener;
	private boolean optionDdSectionLabelCentered = true;

	public DdUiBuilder(DashDoc d, MouseListener linkClickListenerParam) {
		linkClickListener = linkClickListenerParam;
		ddoc = d;
	}

	public void setOptionDdSectionLabelCentered(boolean c) {
		optionDdSectionLabelCentered = c;
	}

	public JPanel buildContainerUI(DdContainer ddc) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(createContainerLabel(ddc), BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

		for (DdPart part : ddc.parts) {
			p2.add(buildUI(part));
		}

		p.add(p2, BorderLayout.CENTER);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);

		return p;
	}

	private JLabel createContainerLabel(DdContainer ddc) {
		JLabel title = new JLabel(ddc.getTitle());
		if (ddc instanceof DdSection) {
			title.setFont(title.getFont().deriveFont(20.0f));
			if (optionDdSectionLabelCentered) {
				title.setHorizontalAlignment(SwingConstants.CENTER);
			}
			title.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
		} else {
			title.setFont(title.getFont().deriveFont(16.0f));
			title.setBorder(BorderFactory.createEmptyBorder(7,0,3,0));
		}
		return title;
	}

	private JComponent buildUI(DdPart part) {
		if (part instanceof DdLink) {
			DdLink link = (DdLink)part;
			JLabel lab = new JLabel(part.toString());
			if (link.nestedLevel > 0) {
				lab.setBorder(BorderFactory.createEmptyBorder(0, link.nestedLevel * 24, 0, 0));
			}
			if (ddoc.getSection(link.getLinkToTitle()) != null) {
				lab.addMouseListener(linkClickListener);
				lab.setToolTipText(link.getLinkToTitle());
				lab.setForeground(Color.BLUE);
			}
			return lab;

		} else if (part instanceof DdText) {
			JTextArea ta = new JTextArea(part.toString());
			ta.setBackground(new JLabel().getBackground());
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			ta.setEditable(false);
			ta.setAlignmentX(Component.LEFT_ALIGNMENT); // this is needed so that other components in BoxLayout are left aligned
			return ta;

		} else if (part instanceof DdContainer) {
			return buildContainerUI((DdContainer)part);
		} else {
			throw new RuntimeException("unexpected DdPart type: " + part.getClass().getSimpleName());
		}

	}

}
