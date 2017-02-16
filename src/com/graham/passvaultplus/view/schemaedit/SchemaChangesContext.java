/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.schemaedit;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.graham.passvaultplus.model.core.PvpType;

public class SchemaChangesContext {
	JPanel panelInTabPane;
	Component currentSchemaEditorPanel;
	JComboBox<PvpType> typeCB;
	PvpTypeModification tm;
}
