/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.gdocs;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.longtask.LongTaskNoException;
import com.graham.passvaultplus.view.longtask.LTManager;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ToLocalCopier implements LongTaskNoException {

		final private PvpContext context;
		final private PvpBackingStoreGoogleDocs bs;
		private volatile boolean canCancel = true;

		/**
		 * note that context may not have all the correct settings at this time.
		 */
		public static PvpBackingStoreGoogleDocs.NewChecks doIt(PvpContext contextParam) {
				ToLocalCopier c = new ToLocalCopier(contextParam);
			//	LTManager.runWithProgress(c, "Verifying Google File");
				c.runLongTask(); // TODO clean up - don't need Task stuff anymore
				return c.bs.nchecks;
		}

		private ToLocalCopier(PvpContext contextParam) {
				context = contextParam;
				bs = new PvpBackingStoreGoogleDocs(context);
		}

		@Override
		public void runLongTask() {
				// TODO we open the stream in doChecksForNewFile, and open again here. There is probably a way to only open it once.
				PvpBackingStoreGoogleDocs bs = new PvpBackingStoreGoogleDocs(context);
				//bs.nchecks.passwordWorks = true;
				bs.loadFileProps(true);

				if (bs.nchecks.sameFormatExists) {
						BufferedInputStream inStream = null;
						try {
								inStream = new BufferedInputStream(bs.openInputStream());
								canCancel = false;
								BCUtil.copyFile(inStream, context.prefs.getDataFile());
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
		}

		// TODO handle cancel
	//	@Override
	//	public boolean cancel() {
	//			return canCancel;
	//	}
}
