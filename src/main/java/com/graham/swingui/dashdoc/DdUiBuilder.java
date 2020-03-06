package com.graham.swingui.dashdoc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.graham.swingui.dashdoc.model.DashDoc;
import com.graham.swingui.dashdoc.model.DdContainer;
import com.graham.swingui.dashdoc.model.DdLink;
import com.graham.swingui.dashdoc.model.DdPart;
import com.graham.swingui.dashdoc.model.DdText;

public class DdUiBuilder {

	private final DashDoc ddoc;
	private final MouseListener linkClickListener;

	public DdUiBuilder(DashDoc d, MouseListener linkClickListenerParam) {
		linkClickListener = linkClickListenerParam;
		ddoc = d;
	}

	public JPanel buildContainerUI(DdContainer dds) {

		JPanel p = new JPanel(new BorderLayout());
		JLabel title = new JLabel(dds.getTitle());
		title.setFont(title.getFont().deriveFont(18.0f));
		p.add(title, BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

		for (DdPart part : dds.parts) {
			p2.add(buildUI(part));
		}

		p.add(p2, BorderLayout.CENTER);

		return p;
	}

	public JComponent buildUI(DdPart part) {
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
			//l.setFont(l.getFont().);
			return lab;

		} else if (part instanceof DdText) {
			JTextArea ta = new JTextArea(part.toString());
			ta.setBackground(new JLabel().getBackground());
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			ta.setEditable(false);
			return ta;

		} else if (part instanceof DdContainer) {
			return buildContainerUI((DdContainer)part);
		} else {
			throw new RuntimeException("unexpected DdPart type: " + part.getClass().getSimpleName());
		}

	}

}
