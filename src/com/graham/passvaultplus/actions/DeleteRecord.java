/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class DeleteRecord extends AbstractAction {
	final private PvpContext context;

	public DeleteRecord(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("delete"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		Collection<PvpRecord> records = null;
		ArrayList<RecordEditContext> editContexts = new ArrayList<>();
		String message = null;
		if (context.getTabManager().isCurrentTabList()) {
			records = context.getViewListContext().getAllSelectedRecords();

			for (PvpRecord r : records) {
				final RecordEditContext re2 = context.getTabManager().getRecordEditor(r);
				if (re2 != null) {
					editContexts.add(re2);
				}
			}

			if (records.size() == 1) {
				message = "Are you sure you want to delete the record?";
			} else if (records.size() > 1) {
				message = "Are you sure you want to delete " + records.size() + " records?";
			}

		} else {
			RecordEditContext ec2 = context.getTabManager().getCurrentTabRecordEditContext();

			if (ec2 != null) {
				editContexts.add(ec2);
				ArrayList<PvpRecord> r2 = new ArrayList<>();
				r2.add(ec2.getRecord());

				if (ec2.getRecord().isPersisted()) {
					message = "Are you sure you want to delete the record?";
				}
				records = r2;
			}
		}

		if (message != null) {
			int v = JOptionPane.showConfirmDialog(context.getMainFrame(), message, "Delete", JOptionPane.OK_CANCEL_OPTION);
			if (v == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}

		for (RecordEditContext ec2 : editContexts) {
			context.getTabManager().removeRecordEditor(ec2);
		}
		if (records != null) {
			for (PvpRecord r : records) {
				if (r.isPersisted()) {
					context.getDataInterface().deleteRecord(r);
				}
			}
		}

	}
}
