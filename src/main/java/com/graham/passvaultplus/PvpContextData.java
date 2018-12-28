/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;

public class PvpContextData {
  private PvpPersistenceInterface rtFileInterface;
  private PvpDataInterface rtDataInterface;
  private PvpContext context;

  PvpContextData(PvpContext c) {
    rtFileInterface = new PvpPersistenceInterface(c);
    rtDataInterface = new PvpDataInterface(c);
  }

	public PvpPersistenceInterface getFileInterface() {
		return this.rtFileInterface;
	}

	public PvpDataInterface getDataInterface() {
		return this.rtDataInterface;
	}

  /**
   * To be called when a big change is made, and the database should be saved to the file, and the data view should be refreshed
   */
  public void saveAndRefreshDataList() {
    getFileInterface().save(getDataInterface(), PvpPersistenceInterface.SaveTrigger.major);
    context.uiMain.getViewListContext().filterUIChanged();
  }
}
