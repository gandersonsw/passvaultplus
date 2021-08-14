/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

import com.graham.passvaultplus.PvpContextUI;
import com.graham.framework.PasswordGenerator;

public class GenPasswordAction extends AbstractAction {
  
  RecordEditFieldText ref;
  int id;
  
  public GenPasswordAction(String name, int idParam, RecordEditFieldText refParam) {
    super(name);
    id = idParam;
    ref = refParam;
  }
  
  public void actionPerformed(ActionEvent e) {
    try {
      PasswordGenerator.PwGenParams pwParams = new PasswordGenerator.PwGenParams();
      switch (id) {
        case 1: // Default Password Generator
          break;
        case 2: // 8 Basic
          pwParams.setLengths(8,8);
          pwParams.setCharCat(PasswordGenerator.CharCategory.digit, 3, false);
          pwParams.setCharCat(PasswordGenerator.CharCategory.lower, 10, false);
          pwParams.setCharCat(PasswordGenerator.CharCategory.upper, 3, false);
          pwParams.setCharCat(PasswordGenerator.CharCategory.special, 0, false);
          break;
        case 3: // 12 Strong
          pwParams.setLengths(12,12);
          pwParams.setCharCat(PasswordGenerator.CharCategory.digit, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.lower, 10, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.upper, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.special, 0, false);
          break;
        case 4: // 20 Strong
          pwParams.setLengths(20,20);
          pwParams.setCharCat(PasswordGenerator.CharCategory.digit, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.lower, 10, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.upper, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.special, 0, false);
          break;
        case 5: // 10 Upper+Lower+Digit+Special
          pwParams.setLengths(10,10);
          pwParams.setCharCat(PasswordGenerator.CharCategory.digit, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.lower, 10, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.upper, 3, true);
          pwParams.setCharCat(PasswordGenerator.CharCategory.special, 2, true);
          break;
      }
      ref.setFieldText(PasswordGenerator.makePassword(pwParams));
    } catch (Exception eee) {
      PvpContextUI.getActiveUI().notifyWarning("GenPasswordAction", eee);
    }
  }
}