/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.GoToUrlAction;
import com.graham.swingui.dashdoc.DdParser;
import com.graham.swingui.dashdoc.DdUiBuilder;
import com.graham.swingui.dashdoc.model.DashDoc;
import com.graham.swingui.dashdoc.model.DdContainer;
import com.graham.swingui.dashdoc.model.DdSection;

public class HelpBuilder implements OtherTabBuilder {

		final public static String EMAIL = "gandersonsw@gmail.com";

	//JScrollPane sp55;
	DdUiBuilder ddBuilder;
	DashDoc ddHelp;
	HelpHome helpHome;
	JPanel mainPanel;

	public String getTitle() {
				return "Help";
		}

		public void dispose() {
		}

		public Component build(PvpContext context) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("00151");

			ddHelp = PvpContext.processResourceTextStream("pvp-help", new DdParser(context));
			ddBuilder = new DdUiBuilder(ddHelp, new DdLinkClickAdaptor());


		//	final JPanel p55 = ddBuilder.buildContainerUI(ddHelp.getStartingSection());
			//final JPanel p55 = ddBuilder.buildContainerUI((DdContainer)ddHelp.parts.get(4));

			mainPanel = new JPanel(new BorderLayout());

				{
						final JPanel cp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					helpHome = new HelpHome(context);
					final JButton hh = new JButton(helpHome);
					cp.add(hh);
						final JButton ch = new JButton(new CloseHelp(context));
						cp.add(ch);
						mainPanel.add(cp, BorderLayout.SOUTH);
				}

			JScrollPane sp55 = new JScrollPane(buildHelpHome());
				mainPanel.add(sp55, BorderLayout.CENTER);


				return mainPanel;

		}

	JPanel buildHelpHome() {

		JPanel topicsAndBasic = new JPanel(new BorderLayout());
		JPanel topics = ddBuilder.buildContainerUI(ddHelp.getStartingSection());
		topics.setBorder(BorderFactory.createEmptyBorder(8, 8, 3, 8));
		topicsAndBasic.add(topics, BorderLayout.WEST);
		topicsAndBasic.add(buildBasicInfo(), BorderLayout.CENTER);

		return topicsAndBasic;

	//	JPanel p55 = ddBuilder.buildContainerUI(ddHelp.getStartingSection());
	//	JScrollPane sp = new JScrollPane(topicsAndBasic);
	//	return sp;
	}

		JPanel buildBasicInfo() {
			final JLabel spacerLabel1 = new JLabel(" ");

			//com.graham.passvaultplus.PvpContextUI.checkEvtThread("00151");
			//final JPanel mainPanel = new JPanel(new BorderLayout());

			final JPanel p55 = new JPanel();
			p55.setLayout(new BoxLayout(p55, BoxLayout.PAGE_AXIS));
		//	final JLabel spacerLabel1 = new JLabel(" ");
			p55.add(spacerLabel1);

			{
				final JLabel l = new JLabel("Pass Vault Plus " + PvpContext.VERSION);
				l.setHorizontalAlignment(SwingConstants.CENTER);
				l.setFont(l.getFont().deriveFont(20.0f));
				final JPanel cp = new JPanel(new BorderLayout());
				cp.add(l, BorderLayout.CENTER);
				p55.add(cp);
				p55.add(new JLabel(" "));
			}

			{
				final JLabel l = new JLabel("Copyright © 2017 Graham Anderson.");
				l.setHorizontalAlignment(SwingConstants.CENTER);
				final JPanel cp = new JPanel(new BorderLayout());
				cp.add(l, BorderLayout.CENTER);
				p55.add(cp);
			}

			{
				final JLabel l = new JLabel("All rights reserved.");
				l.setHorizontalAlignment(SwingConstants.CENTER);
				final JPanel cp = new JPanel(new BorderLayout());
				cp.add(l, BorderLayout.CENTER);
				p55.add(cp);
				p55.add(new JLabel(" "));
			}

			{
				final JLabel l = new JLabel("Contact author at: " + EMAIL);
				l.setHorizontalAlignment(SwingConstants.CENTER);
				final JPanel cp = new JPanel(new BorderLayout());
				final JPanel emailPanel = new JPanel(new FlowLayout());
				emailPanel.add(l);
				final JButton ce = new JButton(new CopyEmail());
				BCUtil.makeButtonSmall(ce);
				emailPanel.add(ce);
				cp.add(emailPanel, BorderLayout.CENTER);
				p55.add(cp);
			}

			{
				final JTextArea helpLink = new JTextArea();
				final Font f1 = spacerLabel1.getFont().deriveFont(11.0f);
				helpLink.setBackground(spacerLabel1.getBackground());
				helpLink.setForeground(Color.BLUE);
				helpLink.setFont(f1);
				helpLink.setText("http://passvaultplus.com");
				helpLink.setEditable(false);
				helpLink.setBorder(new EmptyBorder(1, 24, 8, 8));
				final JPanel leftAlignPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
				leftAlignPanel2.add(helpLink);

				final JButton copyButton = new JButton(new CopyUrlAction(helpLink.getText()));
				BCUtil.makeButtonSmall(copyButton);
				copyButton.setFocusable(false);
				leftAlignPanel2.add(copyButton);
				GoToUrlAction.checkAndAdd(leftAlignPanel2, helpLink.getText());

				p55.add(leftAlignPanel2);
			}
/*
			{
				final JTextArea helpText = new JTextArea();
				helpText.setBackground(spacerLabel1.getBackground());
				helpText.setEditable(false);
				helpText.setText(PvpContext.getResourceText("help"));
				helpText.setLineWrap(true);
				helpText.setWrapStyleWord(true);
				helpText.setPreferredSize(new Dimension(800, 2000));
				helpText.setMaximumSize(new Dimension(800, 2000));
				helpText.setMinimumSize(new Dimension(800, 2000));
				p55.add(helpText);
			} */

			JPanel p66 = new JPanel(new BorderLayout());
			p66.add(p55, BorderLayout.NORTH);

			return p66;
		}

		static class CopyEmail extends AbstractAction {
				public CopyEmail() {
						super("Copy Email");
				}
				public void actionPerformed(ActionEvent e) {
						StringSelection ss = new StringSelection(EMAIL);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
				}
		}

		class CloseHelp extends AbstractAction {
				final PvpContext context;
				public CloseHelp(PvpContext c) {
						super("Close");
						context = c;
				}
				public void actionPerformed(ActionEvent e) {
						context.uiMain.hideTab(OtherTab.Help);
				}
		}

	class HelpHome extends AbstractAction {
		final PvpContext context;
		public HelpHome(PvpContext c) {
			super("Topics");
			setEnabled(false);
			context = c;
		}
		public void actionPerformed(ActionEvent e) {
			JScrollPane sp55 = new JScrollPane(buildHelpHome());
			mainPanel.remove(1);
			mainPanel.add(sp55, BorderLayout.CENTER);
			mainPanel.revalidate();
			mainPanel.repaint();
			helpHome.setEnabled(false);

		}
	}

		static class CopyUrlAction extends AbstractAction {
				final String url;
				public CopyUrlAction(final String urlParam) {
						super("Copy Link");
						url = urlParam;
				}
				public void actionPerformed(ActionEvent e) {
						final StringSelection ss = new StringSelection(url);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
				}
		}

		class DdLinkClickAdaptor extends MouseAdapter {

			@Override
			public void	mouseClicked(MouseEvent e) {
				Component c = e.getComponent();
				if (c instanceof JLabel) {
					JLabel l = (JLabel)c;
					System.out.println("clicked: " + l.getText());

					DdSection dds = ddHelp.getSection(l.getToolTipText());
					if (dds != null) {
					//	sp55.getComponents()
					//	sp55.removeAll();
					//	sp55.add(ddBuilder.buildContainerUI(dds));
					//	sp55.revalidate();
					//	sp55.repaint();

						JPanel pp23 = ddBuilder.buildContainerUI(dds);
						pp23.setBorder(BorderFactory.createEmptyBorder(8, 8, 3, 8));
						JScrollPane sp55 = new JScrollPane(pp23);
						mainPanel.remove(1);
						mainPanel.add(sp55, BorderLayout.CENTER);
						mainPanel.revalidate();
						mainPanel.repaint();

						helpHome.setEnabled(true);
					}
				}
			}
		}

}
