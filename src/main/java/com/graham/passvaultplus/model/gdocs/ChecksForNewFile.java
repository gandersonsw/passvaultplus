/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.gdocs;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.model.core.PvpInStreamer;
import com.graham.passvaultplus.view.longtask.LongTaskNoException;
import com.graham.passvaultplus.view.longtask.LTManager;

import java.io.BufferedInputStream;
import java.security.InvalidKeyException;

public class ChecksForNewFile implements LongTaskNoException {

		final private PvpContext context;
		final private PvpBackingStoreGoogleDocs bs;

		/**
		 * note that context may not have all the correct settings at this time.
		 */
		public static PvpBackingStoreGoogleDocs.NewChecks doIt(PvpContext contextParam) {
				ChecksForNewFile c = new ChecksForNewFile(contextParam);
				c.runLongTask(); // TODO clean this up
			//	LTManager.runSync(c, "Verifying Google File");
				return c.bs.nchecks;
		}

		private ChecksForNewFile(PvpContext contextParam) {
				context = contextParam;
				bs = new PvpBackingStoreGoogleDocs(context);
		}

		@Override
		public void runLongTask() {
				LTManager.registerCancelFunc(() -> bs.nchecks.wasCanceled = true);
				LTManager.nextStep("loadFileProps");
				bs.nchecks.passwordWorks = true;
				bs.loadFileProps(true);

				if (bs.nchecks.sameFormatExists) {
						final PvpInStreamer fileReader = new PvpInStreamer(bs, context);
						LTManager.nextStep("getStream");
						try {
								BufferedInputStream inStream = fileReader.getStream();
								// do nothing with inStream. Just verifying it can be opened.
						} catch (UserAskToChangeFileException ucf) {
								bs.nchecks.passwordWorks = false;
								context.ui.notifyInfo("doChecksForNewFile: at UserAskToChangeFileException");
						} catch (InvalidKeyException e) {
								context.ui.notifyInfo("doChecksForNewFile: at InvalidKeyException");
								bs.nchecks.passwordWorks = false;
						} catch (Exception e) {
								bs.nchecks.excep = e;
						} finally {
								fileReader.close();
						}
				}

				if (bs.nchecks.excep == null && bs.getException() != null) {
						//bs.nchecks.error = bs.getErrorMessageForDisplay(); TODO sometimes we want a more freindly message if it is a "cant connect" or something
						bs.nchecks.excep = bs.getException();
				}
		}

}
