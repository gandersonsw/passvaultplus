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
import com.graham.passvaultplus.view.longtask.CancelableLongTask;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.prefs.RemoteBSPrefHandler;
import com.graham.passvaultplus.view.prefs.ResetPrefsAction;
import com.graham.passvaultplus.view.recordedit.RecordEditBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class CommandExecuter {
	
	private final PvpContext context;
	private final LTCallback ltcb;
	private SimpleDateFormat df = new SimpleDateFormat();

	public CommandExecuter(PvpContext c, LTCallback ltcbParam) {
		context = c;
		ltcb = ltcbParam;
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
			System.out.println("testLongTask testLongTask testLongTask testLongTask testLongTask testLongTask");
		try {
			if (LTManager.runSync(new LongTaskTest(bakeTime), "Making a pizza")) {
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
			LTManager.run(new SearchBU(searchText), ltcb);
	}

	class SearchBU implements CancelableLongTask {
			String searchText;
			boolean doCancel;
			public SearchBU(String s) {
					searchText = s;
			}
			@Override
			public void runLongTask() {
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
							if (doCancel) {
									context.ui.notifyInfo("- - - - Canceled Searching. - - - -");
									return;
							}
					}
					context.ui.notifyInfo("- - - - Completed Searching. " + count + " records found. - - - -");
			}
			@Override
			public boolean cancel() {
					doCancel = true;
					return true;
			}
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

	public void normalMethodThatCanTakeLong(int bakeTime) throws Exception {
		LTManager.nextStep("Prepping dough");
		Thread.sleep(3000);
		//LTManager.stepDone("Prepping dough");
		LTManager.nextStep("Rolling out dough");
		Thread.sleep(5000);
		//LTManager.stepDone("Rolling out dough");
		LTManager.nextStep("Adding Sauce");
		Thread.sleep(500);
		//LTManager.stepDone("Adding Sauce");
		LTManager.nextStep("Adding Cheese");
		Thread.sleep(1000);
		//LTManager.stepDone("Adding Cheese");
		LTManager.nextStep("Adding Olives");
		Thread.sleep(1000);
		//LTManager.stepDone("Adding Olives");
		LTManager.nextStep("Baking");
		Thread.sleep(1000 * bakeTime);
		//LTManager.stepDone("Baking");
		LTManager.nextStep("Cutting");
		Thread.sleep(4000);
		LTManager.stepDone("Cutting");
		System.out.println("normalMethodThatCanTakeLong normalMethodThatCanTakeLong normalMethodThatCanTakeLong normalMethodThatCanTakeLong");
	}

	class LongTaskTest implements CancelableLongTask {
		final private int bakeTime;
		public LongTaskTest(int bt) {
			bakeTime = bt;
		}
		@Override
		public void runLongTask() throws Exception {
			normalMethodThatCanTakeLong(bakeTime);
		}
		@Override
		public boolean cancel() {
				System.out.println("CommandExecuter.LongTaskTest.cancel.A");
				return true;
		}
	}

}
