/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

import com.graham.framework.PasswordGenerator;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpRecord;

public class RightClickPopup extends MouseAdapter {
  private PvpContext context;

  RightClickPopup(PvpContext c) {
    context = c;
  }

  void addListener(JTextComponent tc) {
    tc.addMouseListener(this);
  }

  @Override
  public void mousePressed(MouseEvent e) {
      showPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
      showPopup(e);
  }

  private void showPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        JTextComponent tc = (JTextComponent)e.getComponent();
        getPopupMenu(tc).show(tc, e.getX(), e.getY());
      }
  }

  JPopupMenu getPopupMenu(JTextComponent tc) {
		JPopupMenu popupMenu = new JPopupMenu("Edit");
  	popupMenu.add(new JMenuItem(new ClearFieldAction(tc)));
  	popupMenu.add(new JMenuItem(new ClearAndPasteAction(tc)));

		JMenu passwordMenu = new JMenu("Generate Password");
    addPasswordGen(passwordMenu, tc);
    //passwordMenu.addSeparator();
  	//passwordMenu.add(new JMenuItem("Create New Password Generator"));

		popupMenu.add(passwordMenu);

		return popupMenu;
	}

  void addPasswordGen(JMenu passwordMenu, JTextComponent tc) {
    //List<PvpRecord> pwGenList = context.getDataInterface().getRecordsOfType(PvpDataInterface.TYPE_PASSWORD_GEN);
    //if (pwGenList.size() == 0) {
    PasswordGenerator.PwGenParams params = new PasswordGenerator.PwGenParams();
    passwordMenu.add(new GenPasswordAction("Default Password Generator", 1, tc));
    passwordMenu.add(new GenPasswordAction("8 Basic", 2, tc));
    passwordMenu.add(new GenPasswordAction("12 Strong", 3, tc));
    passwordMenu.add(new GenPasswordAction("20 Strong", 4, tc));
    passwordMenu.add(new GenPasswordAction("10 Upper+Lower+Digit+Special", 5, tc));
    //} else {
    //}
  }

	class ClearFieldAction extends AbstractAction {
    JTextComponent tc;
		public ClearFieldAction(JTextComponent tcParam) {
			super("Clear Field");
      tc = tcParam;
		}
		public void actionPerformed(ActionEvent e) {
      tc.setText("");
		}
	}

  class ClearAndPasteAction extends AbstractAction {
    JTextComponent tc;
    public ClearAndPasteAction(JTextComponent tcParam) {
      super("Paste (Replace)");
      tc = tcParam;
    }
    public void actionPerformed(ActionEvent e) {
      tc.setText("");
      Action a = tc.getActionMap().get("paste");
      if (a != null) {
        ActionEvent ae = new ActionEvent(tc, ActionEvent.ACTION_PERFORMED, "");
        a.actionPerformed(ae);
      }
    }
  }

  class GenPasswordAction extends AbstractAction {
    JTextComponent tc;
    int id;
    public GenPasswordAction(String name, int idParam, JTextComponent tcParam) {
      super(name);
      id = idParam;
      tc = tcParam;
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
      tc.setText(PasswordGenerator.makePassword(pwParams));
    } catch (Exception eee) {
      context.notifyWarning("GenPasswordAction", eee);
    }
    }
  }
  /*
  class GenPasswordAction extends AbstractAction {
    JTextComponent tc;
    PasswordGenerator.PwGenParams pwParams;
    public GenPasswordAction(String name, PasswordGenerator.PwGenParams pw, JTextComponent tcParam) {
      super(name);
      pwParams = pw;
      tc = tcParam;
    }
    public void actionPerformed(ActionEvent e) {
      tc.setText(PasswordGenerator.makePassword(pwParams));
    }
  }
*/
}
