/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.io.BufferedInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.graham.passvaultplus.actions.ExportXmlFile;
import com.graham.passvaultplus.model.core.*;
import com.graham.passvaultplus.view.EulaDialog;
import com.graham.passvaultplus.view.JceDialog;
import com.graham.passvaultplus.view.LongTask;
import com.graham.passvaultplus.view.LongTaskUI;
import com.graham.passvaultplus.view.prefs.RemoteBSPrefHandler;
import com.graham.passvaultplus.view.prefs.ResetPrefsAction;
import com.graham.passvaultplus.view.recordedit.RecordEditBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class CommandExecuter {
	
	private final PvpContext context;
	private SimpleDateFormat df = new SimpleDateFormat();
	
	public CommandExecuter(PvpContext c) {
		context = c;
	}
	
	public String[] getCommands() {
		String[] commands = { "SearchBackups", "OpenBackupRecord", "TestLongTask", "TestJce", "TestEula", "TestRemoteAsk", "TestGDNF", "TestResetPrefs", "TestExportXml", "SetAllModDate", "SetAllCreateDate", "Exit" };
		return commands;
	}
	
	public String getDefaultArguments(String command) {
		if (command.equals("SetAllModDate") || command.equals("SetAllCreateDate")) {
			return df.format(new Date());
		}
		if (command.equals("TestLongTask") ) {
			return "18";
		}
		if (command.equals("TestJce") ) {
			return "256";
		}
		if (command.equals("TestRemoteAsk") ) {
			return "true,false";
		}
		if (command.equals("TestGDNF") ) {
			return "TestFile.txt";
		}
		if (command.equals("Exit") || command.equals("TestResetPrefs") || command.equals("TestEula") || command.equals("SearchBackups") || command.equals("TestExportXml")) {
			return "";
		}
		if (command.equals("OpenBackupRecord")) {
			return "filename.xml,12";
		}
		return "?";
	}
	
	public void execute(String command, String args) {
		try {
			if (command.equals("TestLongTask")) {
				testLongTask(Integer.parseInt(args));
			} else if (command.equals("TestJce")) {
				testJceDialog(Integer.parseInt(args));
			} else if (command.equals("TestEula")) {
				testEula();
			} else if (command.equals("TestRemoteAsk")) {
				String[] sa = args.split(",");
				testRemoteAsk(Boolean.parseBoolean(sa[0]), Boolean.parseBoolean(sa[1]));
			} else if (command.equals("TestGDNF")) {
				testGDNF(args);
			} else if (command.equals("TestResetPrefs")) {
				testResetPrefs();
			} else if (command.equals("TestExportXml")) {
				testExportXml();
			} else if (command.equals("SetAllModDate")) {
				setModDates(df.parse(args));
			} else if (command.equals("SetAllCreateDate")) {
				setCreationDates(df.parse(args));
			} else if (command.equals("Exit")) {
				doExit();
			} else if (command.equals("SearchBackups")) {
				searchBackups(args);
			} else if (command.equals("OpenBackupRecord")) {
				String[] sa = args.split(",");
				openBackupRecord(sa[0], Integer.parseInt(sa[1]));
			} else {
				context.ui.notifyInfo("Unkown Command:" + command);
			}
		} catch (Exception e) {
			context.ui.notifyInfo("Error:" + e);
		}
	}

	private void testLongTask(int bakeTime) {
		LongTaskUI ui = new LongTaskUI(new LongTaskTest(bakeTime), "Making a pizza");
		try {
			if (ui.runLongTask()) {
				context.ui.notifyInfo("Cancel was pressed!");
			}
		} catch (Exception e) {
			context.ui.notifyWarning("TestLongTask Exception", e);
		}
	}

	private void testJceDialog(int maxKeySize) {
		final JceDialog jced = new JceDialog();
		jced.showDialog(context.ui.getFrame(), maxKeySize);
	}

	private void testEula() {
		final EulaDialog eula = new EulaDialog();
		eula.showEula();
	}

	private void testRemoteAsk(boolean passwordWorks, boolean isNewDB) {
		RemoteBSPrefHandler handler = new RemoteBSPrefHandler();
		handler.askAboutExistingFile(context.ui.getFrame(), passwordWorks, isNewDB);
	}

	private void testGDNF(String fileName) {
		ErrUIGoogleDocFileNotFound d = new ErrUIGoogleDocFileNotFound(context, fileName, null);
		d.buildDialog();
	}

	private void testResetPrefs() {
			new ResetPrefsAction(context).doConfirmDialog();
	}

	private void testExportXml() {
		PvpBackingStoreFile bsFileMain = new PvpBackingStoreFile(context.prefs.getDataFile());
		ExportXmlFile e = new ExportXmlFile(context, bsFileMain);
		e.actionPerformed(null);
	}

	private void setModDates(Date d) {
		for (PvpRecord r : context.data.getDataInterface().getRecords()) {
			r.setModificationDate(d);
		}
		context.data.saveRecords(context.data.getDataInterface().getRecords());
	}
	
	private void setCreationDates(Date d) {
		for (PvpRecord r : context.data.getDataInterface().getRecords()) {
			r.setCreationDate(d);
		}
		context.data.saveRecords(context.data.getDataInterface().getRecords());
	}
	
	private void doExit() {
		System.exit(0);
	}

	private void searchBackups(String searchText) {
			PvpBackingStoreFile bsFileMain = new PvpBackingStoreFile(context.prefs.getDataFile());
			File[] fArr = bsFileMain.getAllFiles(true);
			int count = 0;
			for (File f : fArr) {
					context.ui.notifyInfo("- - - - Searching File: " + f.getName() + " - - - -");
					PvpInStreamer fileReader = null;
					try {
							PvpBackingStoreFile bsFileBackup = new PvpBackingStoreFile(f);
							fileReader = new PvpInStreamer(bsFileBackup, context);
							BufferedInputStream inStream = fileReader.getStream();
							PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
							PvpDataInterface.FilterResults fr = newDataInterface.getFilteredRecords(PvpType.FILTER_ALL_TYPES, searchText, null, false);
							for (PvpRecord r : fr.records) {
									context.ui.notifyInfo(r.getFullText(false));
							}
							count += fr.records.size();
					} catch (Exception e) {
							context.ui.notifyWarning("Failed to search file: " + f.getName(), e);
					} finally {
							if (fileReader != null) {
									fileReader.close();
							}
					}
			}
			context.ui.notifyInfo("- - - - Completed Searching. " + count + " records found. - - - -");
	}

	private void openBackupRecord(String fileName, int id) {
			File f = new File(context.prefs.getDataFile().getParent(), fileName);
			PvpInStreamer fileReader = null;
			try {
					PvpBackingStoreFile bsFileBackup = new PvpBackingStoreFile(f);
					fileReader = new PvpInStreamer(bsFileBackup, context);
					BufferedInputStream inStream = fileReader.getStream();
					PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
					PvpRecord r = newDataInterface.getRecord(id);
					r.clearId();
					final RecordEditContext editor = RecordEditBuilder.buildEditor(context, r, true);
					context.uiMain.addRecordEditor(fileName + ":" + id, editor);
					context.uiMain.setSelectedComponent(editor.getPanelInTabPane());
			} catch (Exception e) {
					context.ui.notifyWarning("Failed to Open Record: " + f.getName(), e);
			} finally {
					if (fileReader != null) {
							fileReader.close();
					}
			}
	}

	static class LongTaskTest implements LongTask {

			final private int bakeTime;
			public LongTaskTest(int bt) {
					bakeTime = bt;
			}

			@Override
			public void runLongTask() throws Exception {
					LongTaskUI.nextStep("Prepping dough");
					Thread.sleep(3000);
					LongTaskUI.stepDone("Prepping dough");
					LongTaskUI.nextStep("Rolling out dough");
					Thread.sleep(5000);
					LongTaskUI.stepDone("Rolling out dough");
					LongTaskUI.nextStep("Adding Sauce");
					Thread.sleep(500);
					LongTaskUI.stepDone("Adding Sauce");
					LongTaskUI.nextStep("Adding Cheese");
					Thread.sleep(1000);
					LongTaskUI.stepDone("Adding Cheese");
					LongTaskUI.nextStep("Adding Olives");
					Thread.sleep(1000);
					LongTaskUI.stepDone("Adding Olives");
					LongTaskUI.nextStep("Baking");
					Thread.sleep(1000 * bakeTime);
					LongTaskUI.stepDone("Baking");
					LongTaskUI.nextStep("Cutting");
					Thread.sleep(4000);
					LongTaskUI.stepDone("Cutting");
			}

			@Override
			public void cancel() {

			}
	}

}
