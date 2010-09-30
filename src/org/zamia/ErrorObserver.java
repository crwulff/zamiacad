/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 7, 2009
 */
package org.zamia;

import java.util.EventListener;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public interface ErrorObserver extends EventListener {

	public void notifyErrorsChanged(ZamiaProject aZPrj, SourceFile aSF);

	public void notifyErrorAdded(ZamiaProject aZPrj, ZamiaException aError);

	public void notifyCleaned(ZamiaProject aZPrj);

	public void notifyErrorsChanged(ZamiaProject aZPrj);
}
