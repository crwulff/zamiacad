/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia.plugin.build;

import org.eclipse.core.runtime.IProgressMonitor;
import org.zamia.ExceptionLogger;
import org.zamia.IZamiaMonitor;
import org.zamia.ZamiaLogger;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZamiaMonitor implements IZamiaMonitor {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IProgressMonitor fPM;

	private int fWorked = 0;

	public ZamiaMonitor(IProgressMonitor aPM) {
		fPM = aPM;
	}

	public boolean isCanceled() {
		return fPM.isCanceled();
	}

	public void worked(int aUnits) {

		fWorked += aUnits;

		//logger.info("ZamiaMonitor: %d done (+%d)", fWorked, aUnits);

		fPM.worked(aUnits);
	}

	@Override
	public void setTaskName(String aName) {
		fPM.setTaskName(aName);
	}

}
