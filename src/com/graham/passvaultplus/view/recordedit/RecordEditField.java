/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import com.graham.passvaultplus.model.core.PvpRecord;

public abstract class RecordEditField {
	
	/**
	 * @return has this one field been edited compared to given record.
	 */
	public abstract boolean isEdited(final PvpRecord r);
	
	public abstract void populateRecordFieldFromUI(final PvpRecord r);
	
	public abstract void populateUIFromRecordField(final PvpRecord r);
	
	public abstract String getFieldTextForCopy();

}
