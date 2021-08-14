/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import com.graham.passvaultplus.model.core.PvpRecord;

public abstract class RecordEditFieldText extends RecordEditField {
	
	public RecordEditFieldText(final RecordEditContext editContextParam) {
		super(editContextParam);
	}
	
	public abstract String getFieldTextForCopy();
	
	public abstract void setFieldText(String t);

}
