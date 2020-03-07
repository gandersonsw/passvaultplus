/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.gdocs;

import com.graham.passvaultplus.PvpContext;
import com.graham.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ToLocalCopier {

		/**
		 * note that context may not have all the correct settings at this time.
		 */
		public static PvpBackingStoreGoogleDocs.NewChecks doIt(PvpContext context) {
				// TODO we open the stream in doChecksForNewFile, and open again here. There is probably a way to only open it once.
				PvpBackingStoreGoogleDocs bs = new PvpBackingStoreGoogleDocs(context);
				bs.loadFileProps(true);

				if (bs.nchecks.sameFormatExists) {
						BufferedInputStream inStream = null;
						try {
								inStream = new BufferedInputStream(bs.openInputStream());
								//canCancel = false;
								FileUtil.copyFile(inStream, context.prefs.getDataFile());
						} catch (Exception e) {
								bs.nchecks.excep = e;
						} finally {
								if (inStream != null) {
										try { inStream.close(); } catch (IOException e) { }
								}
						}
				}

				if (bs.nchecks.excep == null && bs.getException() != null) {
					//	bs.nchecks.errorMsg = bs.getErrorMessageForDisplay(); TODO sometimes we want a more freindly message if it is a "cant connect" or something
						bs.nchecks.excep = bs.getException();
				}
				return bs.nchecks;
		}

}
