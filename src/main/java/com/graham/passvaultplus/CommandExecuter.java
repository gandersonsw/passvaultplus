/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.graham.passvaultplus.model.core.PvpRecord;

public class CommandExecuter {
	
	private final PvpContext context;
	private SimpleDateFormat df = new SimpleDateFormat();
	
	public CommandExecuter(PvpContext c) {
		context = c;
	}
	
	public String[] getCommands() {
		String[] commands = { "SetAllModDate", "SetAllCreateDate", "Exit" };
		return commands;
	}
	
	public String getDefaultArguments(String command) {
		if (command.equals("SetAllModDate") || command.equals("SetAllCreateDate")) {
			return df.format(new Date());
		}
		if (command.equals("Exit")) {
			return "";
		}
		return "?";
	}
	
	public void execute(String command, String args) {
		try {
			if (command.equals("SetAllModDate")) {
				setModDates(df.parse(args));
			} else if (command.equals("SetAllCreateDate")) {
				setCreationDates(df.parse(args));
			} else if (command.equals("Exit")) {
				doExit();
			} else {
				context.ui.notifyInfo("Unkown Command:" + command);
			}
		} catch (ParseException e) {
			context.ui.notifyInfo("Error:" + e);
		}
	}
	
	private void setModDates(Date d) {
		for (PvpRecord r : context.data.getDataInterface().getRecords()) {
			r.setModificationDate(d);
		}
		context.data.getDataInterface().saveRecords(context.data.getDataInterface().getRecords());
	}
	
	private void setCreationDates(Date d) {
		for (PvpRecord r : context.data.getDataInterface().getRecords()) {
			r.setCreationDate(d);
		}
		context.data.getDataInterface().saveRecords(context.data.getDataInterface().getRecords());
	}
	
	private void doExit() {
		System.exit(0);
	}

}
