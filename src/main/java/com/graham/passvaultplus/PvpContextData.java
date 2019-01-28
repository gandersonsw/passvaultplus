/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.longtask.LongTask;

import java.util.Collection;

public class PvpContextData {
	private PvpPersistenceInterface rtFileInterface;
	private PvpDataInterface rtDataInterface;
	private PvpContext context;

	PvpContextData(PvpContext c) {
		context = c;
		rtFileInterface = new PvpPersistenceInterface(c);
		rtDataInterface = new PvpDataInterface(c);
	}

	public PvpPersistenceInterface getFileInterface() {
		return rtFileInterface;
	}

	public PvpDataInterface getDataInterface() {
		return rtDataInterface;
	}

	public void saveRecord(final PvpRecord r) {
		rtDataInterface.saveRecord(r);
		dataChanged(PvpPersistenceInterface.SaveTrigger.cud);
	}

	public void saveRecords(final Collection<PvpRecord> rCol) {
		if (rCol == null || rCol.size() == 0) {
			return;
		}
		for (PvpRecord r : rCol) {
			rtDataInterface.saveRecord(r);
		}
		dataChanged(PvpPersistenceInterface.SaveTrigger.cud);
	}

	public void deleteRecords(final Collection<PvpRecord> rCol) {
		if (rCol == null || rCol.size() == 0) {
			return;
		}
		boolean changed = false;
		for (PvpRecord r : rCol) {
			changed = changed || rtDataInterface.deleteRecord(r);
		}
		if (changed) {
			dataChanged(PvpPersistenceInterface.SaveTrigger.cud);
		}
	}

  /**
   * To be called when a big change is made, and the database should be saved to the file, and the data view should be refreshed
   */
  public void saveAndRefreshDataList() {
		dataChanged(PvpPersistenceInterface.SaveTrigger.major);
  }

  private void dataChanged(PvpPersistenceInterface.SaveTrigger trigger) {
		LTManager.run(rtFileInterface.saveLT(rtDataInterface, trigger), new TempCB());
		//rtFileInterface.save(rtDataInterface, trigger);
		if (context.uiMain != null) {
			context.uiMain.getViewListContext().filterUIChanged();
		}
	}

	class TempCB implements LTCallback {
			@Override
			public void taskStarting(LongTask lt) {
					context.ui.notifyInfo("callback for dataChanged - taskStarting.");
			}
			@Override
			public void taskComplete(LongTask lt) {
					context.ui.notifyInfo("callback for dataChanged - taskComplete.");
			}
			@Override
			public void handleException(LongTask lt, Exception e) {
					context.ui.notifyWarning("callback for dataChanged - handleException.", e);
			}
	}
}
