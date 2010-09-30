/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.zamia.plugin.ZamiaPlugin;


/**
 * Class used to initialize default preference values.
 *
 * @author guenter bartsch
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();

		Display display = Display.getCurrent();

		Color color = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_KEYWORD, color.getRGB());
		color = display.getSystemColor(SWT.COLOR_BLUE);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_STRING, color.getRGB());
		color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_COMMENT, color.getRGB());
		color = display.getSystemColor(SWT.COLOR_BLACK);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_DEFAULT, color.getRGB());
		color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_MATCHING_CHAR, color.getRGB());
		color = display.getSystemColor(SWT.COLOR_WHITE);
		PreferenceConverter.setDefault(store, PreferenceConstants.P_BACKGROUND, color.getRGB());

	}

}
