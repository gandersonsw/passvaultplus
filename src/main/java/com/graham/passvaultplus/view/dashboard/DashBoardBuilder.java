/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.OtherTabBuilder;

public class DashBoardBuilder implements OtherTabBuilder {

	public String getTitle() {
		return "Dashboard";
	}

	public Component build(PvpContext context) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0014");
		final JPanel p = new JPanel(new BorderLayout());
		final UpcomingDatesTableModel tm = new UpcomingDatesTableModel(context);
		p.add(buildUpcomingDatesHeader(context, tm), BorderLayout.NORTH);
		p.add(buildUpcomingDatesPanel(context, tm), BorderLayout.CENTER);
		return p;
	}

	public void dispose() {
	}
	
	private static JPanel buildUpcomingDatesHeader(final PvpContext context, final UpcomingDatesTableModel tm) {
		
		final JComboBox<Integer> daysOut = new JComboBox<>(new Integer[]{1,2,3,5,7,10,14,30,60,100});
		daysOut.setMaximumRowCount(20);
		daysOut.setSelectedIndex(6); // 2 weeks is the default
		daysOut.addActionListener(new NumberOfDaysActionListener(context, tm));
		
		final JPanel eastPanel = new JPanel(new FlowLayout());
		eastPanel.add(new JLabel("Number of Days Shown:"));
		eastPanel.add(daysOut);
		
		final JPanel p = new JPanel(new BorderLayout());
		p.add(new JLabel("Upcoming Dates:"), BorderLayout.WEST);
		p.add(eastPanel, BorderLayout.EAST);
		return p;
	}
	
	private static JScrollPane buildUpcomingDatesPanel(final PvpContext context, final UpcomingDatesTableModel tm) {
		JTable t = new JTable(tm);
		
		t.getColumnModel().getColumn(0).setHeaderValue("Days Away");
		t.getColumnModel().getColumn(1).setHeaderValue("Date");
		t.getColumnModel().getColumn(2).setHeaderValue("Type");
		t.getColumnModel().getColumn(3).setHeaderValue("Title");
		
		t.addMouseListener(new UpcomingDatesTableMouseAdpater(context, t));
		
		JScrollPane sp = new JScrollPane(t);
		
		return sp;
	}
	
	static class NumberOfDaysActionListener implements ActionListener {
		final private UpcomingDatesTableModel tm;
		final private PvpContext context;
		
		public NumberOfDaysActionListener(final  PvpContext contextParam, final UpcomingDatesTableModel tmParam) {
			tm = tmParam;
			context = contextParam;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
				com.graham.passvaultplus.PvpContextUI.checkEvtThread("0015");
			JComboBox cb = (JComboBox)e.getSource();
			tm.setNumberOfDays(context, (Integer)cb.getSelectedItem());
		}
		
	}
}
