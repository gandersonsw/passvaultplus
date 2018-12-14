/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;

public class NewRecordTypeChangedAction  extends AbstractAction {
	final private PvpContext context;
	final private RecordEditContext editContext;

	public NewRecordTypeChangedAction(final PvpContext contextParam, final RecordEditContext editContextParam) {
		context = contextParam;
		editContext = editContextParam;
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			JComboBox typeSelector = (JComboBox)arg0.getSource();
			PvpType type = (PvpType)typeSelector.getSelectedItem();
			context.ui.getTabManager().removeRecordEditor(editContext);

			PvpRecord newRecord = new PvpRecord(type);
			final RecordEditContext newContext = RecordEditBuilder.buildEditor(context, newRecord, true);
			context.ui.getTabManager().addRecordEditor("New", newContext);
			context.ui.getTabManager().setSelectedComponent(newContext.getPanelInTabPane());
		} catch (Exception e) {
			context.ui.notifyBadException(e, true, PvpException.GeneralErrCode.OtherErr);
		}
	}

}
