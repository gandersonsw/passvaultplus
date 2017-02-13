/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import javax.swing.Action;

public class PvpException extends Exception {
	private static final long serialVersionUID = 1L;

	public interface ErrCode {
		public String getTitle();
		public String getDescription();
		public String getHelpId();
	}
	
	public enum SpecificErrCode implements ErrCode {
		// Here are the specific errors - for creating exceptions
		EncryptionHeaderNotRead("There was an error that prevented the data from loading", "The encryption information could not be read from the file."), // encrypt header not read
		ZipEntryNotFound("There was an error that prevented the data from loading", "The entry in the zip file does not exist"); // zip file not as expected
		final String title;
		final String description;
		SpecificErrCode(final String titleParam, final String descParam) {
			title = titleParam;
			description = descParam;
		}
		@Override
		public String getTitle() {
			return title;
		}
		@Override
		public String getDescription() {
			return description;
		}
		@Override
		public String getHelpId() {
			return name().toLowerCase();
		}
	}
	
	public enum GeneralErrCode implements ErrCode {
		// Here are the general errors - not to be used with an exception.
		GeneralErr("There was an error", "No more information is available. This message should never be displayed"),
		CantOpenDataFile("There was an error that prevented the data from loading", "Cant open the data file stream"), // trying to open the data file
		CantParseXml("There was an error that prevented the data from loading", "Can't parse the XML format"), // trying to parse the xml for the database
		CantWriteDataFile("There was an error that prevented the data from saving", null), // any error when writing the database to the file
		InvalidKey("There was an error that prevented the encryption from working", "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy"), // Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
		CantOpenMainWindow("There was an error that prevented the application from starting", null);
		final String title;
		final String description;
		GeneralErrCode(final String titleParam, final String descParam) {
			title = titleParam;
			description = descParam;
		}
		@Override
		public String getTitle() {
			return title;
		}
		@Override
		public String getDescription() {
			return description;
		}
		@Override
		public String getHelpId() {
			return name().toLowerCase();
		}
	}
	
	final private SpecificErrCode secode;
	final private GeneralErrCode gecode;
	private Action optionalAction;
	private String additionalDescription;
	
	public PvpException(final SpecificErrCode c, final String technicalMessage) {
		super(technicalMessage);
		secode = c;
		gecode = null;
	}
	
	public PvpException(final GeneralErrCode c, final Exception rootCause) {
		super(rootCause);
		secode = null;
		gecode = c;
	}
	
	public PvpException setOptionalAction(final Action a) {
		optionalAction = a;
		return this;
	}
	
	public PvpException setAdditionalDescription(final String d) {
		additionalDescription = d;
		return this;
	}
	
	private ErrCode getErrCode() {
		if (secode != null) {
			return secode;
		} else if (gecode != null) {
			return gecode;
		}
		// Should never get here
		return GeneralErrCode.GeneralErr;
	}
	
	public String getPvpErrorTitle() {
		return getErrCode().getTitle();
	}
	
	public String getPvpErrorDescription() {
		if (additionalDescription == null) {
			return getErrCode().getDescription();
		} else {
			return getErrCode().getDescription() + "\n" + additionalDescription;
		}
	}
	
	public String getPvpHelpId() {
		return getErrCode().getHelpId();
	}
	
	public Action getPvpOptionalAction() {
		return optionalAction;
	}

}
