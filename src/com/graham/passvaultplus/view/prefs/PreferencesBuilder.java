/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;

import com.graham.passvaultplus.actions.TextFieldChangeForwarder;

public class PreferencesBuilder {
	
	private final Dimension indentDim = new Dimension(30, 2); 
	
	public static Component buildPrefs(final PreferencesConnection connParam) {
		return new PreferencesBuilder(connParam).build();
	}
	
	final private PreferencesConnection conn;
	final private PreferencesContext prefsContext;
	
	private PreferencesBuilder(final PreferencesConnection connParam) {
		conn = connParam;
		prefsContext = new PreferencesContext(conn);
	}
	
	private Component build() {
		final JPanel p = new JPanel(new BorderLayout());
		p.add(buildTop(), BorderLayout.CENTER);
		p.add(buildBottom(p), BorderLayout.SOUTH);
		return p;
	}
	
	private Component buildBottom(final JPanel panelToBeReturned) {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(new JButton(conn.getCancelAction()));
		prefsContext.saveButton = new JButton(new SavePrefsAction(prefsContext));
		p.add(prefsContext.saveButton);
		return p;
	}
	
	private Component buildTop() {
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildActionCombo());
		p.add(buildFileText());
		p.add(buildFileButtons());
		p.add(buildCompressButtons());
		p.add(buildEncryptedButtons());
		p.add(buildPassword());
		p.add(buildPasswordOptions());
		p.add(buildPin());
		p.add(buildAESBits());
		prefsContext.updateBecauseCompressedOrEncryptedChanged();
		
		// set the intial password strength
		final PasswordChangedAction pca = new PasswordChangedAction(prefsContext);
		pca.actionPerformed(null);
		
		JPanel bp = new JPanel(new BorderLayout());
		bp.add(p, BorderLayout.NORTH);
		
		return bp;
	}
	
	private JPanel buildActionCombo() {
		final ConfigAction[] actions = new ConfigAction[conn.supportsChangeDataFileOptions() ? 3 : 2];
		actions[0] = ConfigAction.Create;
		actions[1] = ConfigAction.Open;
		if (conn.supportsChangeDataFileOptions()) {
			actions[2] = ConfigAction.Change;
		}
		final JComboBox<ConfigAction> cb = new JComboBox<>(actions);
		if (conn.supportsChangeDataFileOptions()) { // make assumption here that if it is supported, it should be the default
			cb.setSelectedIndex(2);
			prefsContext.configAction = ConfigAction.Change;
		} else {
			prefsContext.configAction = ConfigAction.Create;
		}
		cb.setFocusable(false);
		cb.addActionListener(new ConfigActionChanged(prefsContext));
		prefsContext.actionCombo = cb;
	
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(cb);
		return p;
	}
	
	private JPanel buildFileText() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("  Data File: "));
		final JLabel jl = new JLabel(prefsContext.getDataFileString());
		prefsContext.setDataFileLabel(jl);
		p.add(jl);
		return p;
	}
	
	private JPanel buildFileButtons() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(Box.createRigidArea(indentDim));
		final Action defaultAction = prefsContext.getDefaultFileAction();
		JButton defBut = new JButton(defaultAction);
		p.add(defBut);
		final Action chooseDirAction = new ChooseDirAction(prefsContext, conn.getSuperFrame());
		final JButton chooseBut = new JButton(chooseDirAction);
		p.add(chooseBut);
		return p;
	}
	
	private JPanel buildCompressButtons() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		prefsContext.compressed = new JCheckBox("Compressed (zip)", prefsContext.compressedFlag);
		prefsContext.compressed.addActionListener(e -> prefsContext.updateBecauseCompressedOrEncryptedChanged());
		p.add(prefsContext.compressed);
		return p;
	}
	
	private JPanel buildEncryptedButtons() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		prefsContext.encrypted = new JCheckBox("Encrypted", prefsContext.encryptedFlag);
		prefsContext.encrypted.addActionListener(e -> prefsContext.updateBecauseCompressedOrEncryptedChanged());
		p.add(prefsContext.encrypted);
		return p;
	}
	
	private JPanel buildPassword() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(Box.createRigidArea(indentDim));
		p.add(new JLabel("  Password:", JLabel.LEFT));
		
		final JTextField pwct = new JTextField(27);
		pwct.setVisible(false);
		JPasswordField pw;
		if (conn.isPasswordSaved()) {
			pw = new JPasswordField(conn.getPassword(), 27);
		} else {
			pw = new JPasswordField(27);
		}
		final PasswordChangedAction pca = new PasswordChangedAction(prefsContext);
		final TextFieldChangeForwarder tfcf = new TextFieldChangeForwarder(pca);
		pw.getDocument().addDocumentListener(tfcf);
		pwct.getDocument().addDocumentListener(tfcf);
		prefsContext.password = pw;
		prefsContext.passwordClearText = pwct;

		p.add(pw);
		p.add(pwct);
		prefsContext.showPassword = new JCheckBox("Show");
		prefsContext.showPassword.addActionListener(new ShowPasswordAction(prefsContext));
		p.add(prefsContext.showPassword);
		return p;
	}
	
	private JPanel buildPasswordOptions() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(Box.createRigidArea(indentDim));
		p.add(Box.createRigidArea(indentDim));
		prefsContext.savePassword = new JCheckBox("Save Password", conn.isPasswordSaved());
		prefsContext.savePassword.setToolTipText("If checked, the password will be saved. If not checked, you must enter the password when starting app.");
		prefsContext.savePassword.addActionListener(e -> prefsContext.setPinEnabled());
		p.add(prefsContext.savePassword);
		p.add(Box.createRigidArea(indentDim));
		p.add(new JLabel("Password Strength: "));
		prefsContext.passwordStrength = new JLabel(" ");
		p.add(prefsContext.passwordStrength);
		return p;
	}
	
	private JPanel buildAESBits() {
		final String[] bits = {"128", "192", "256"};
		final JComboBox<String> cb = new JComboBox<>(bits);
		prefsContext.aesBits = cb;
		prefsContext.setSelectedBits(conn.getAesBits());
		
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(Box.createRigidArea(indentDim));
		p.add(new JLabel("  Key Size in bits:"));
		p.add(cb);
		return p;
	}
	
	private JPanel buildPin() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(Box.createRigidArea(indentDim));
		p.add(Box.createRigidArea(indentDim));
		p.add(Box.createRigidArea(indentDim));
		prefsContext.usePin = new JCheckBox("Use PIN:");
		if (conn.getUsePin()) {
			prefsContext.usePin.setSelected(true);
		}
		p.add(prefsContext.usePin);
		prefsContext.pinClearText = new JTextField(10);
		prefsContext.pinClearText.setVisible(false);
		prefsContext.pin = new JPasswordField(conn.getPin(), 10);
		p.add(prefsContext.pin);
		p.add(prefsContext.pinClearText);
		prefsContext.showPin = new JCheckBox("Show");
		prefsContext.showPin.addActionListener(new ShowPinAction(prefsContext));
		p.add(prefsContext.showPin);
		
		final String[] timeouts = {"2", "5", "10", "15", "20", "30", "45", "60", "120", "300", "Never"};
		prefsContext.timeoutCombo = new JComboBox<>(timeouts);
		prefsContext.timeoutCombo.setToolTipText("Timeout in minutes. You will be asked to enter PIN after this many minutes");
		prefsContext.setPinTimeout(conn.getPinTimeout());
		p.add(new JLabel("Timeout:"));
		p.add(prefsContext.timeoutCombo);
		return p;
	}
	
}
