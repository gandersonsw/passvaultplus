/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import com.graham.util.ResourceUtil;

import javax.swing.text.JTextComponent;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;

public class GenPasswordMenu extends AbstractAction {

    RecordEditFieldText ref;
  
    static public JButton createButton(RecordEditFieldText refParam) {
      GenPasswordMenu action = new GenPasswordMenu(refParam);
      JButton generatePasswordButton = new JButton(action);
      generatePasswordButton.setFocusable(false);
      generatePasswordButton.setToolTipText("generate password");
      return generatePasswordButton;
    }

		public GenPasswordMenu(RecordEditFieldText refParam) {
				super(null, ResourceUtil.getIcon("down-arrow-small"));
        ref = refParam;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JButton) {
						final JPopupMenu popup = new JPopupMenu();
            RightClickPopup.addPasswordGen(popup, ref);
						popup.setFocusable(false);
						JButton b = (JButton)e.getSource();
						popup.show(b, 0, b.getHeight());
				}
		}
}