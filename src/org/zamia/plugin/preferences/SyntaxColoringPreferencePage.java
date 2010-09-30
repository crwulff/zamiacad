/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */
package org.zamia.plugin.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.editors.ZamiaEditor;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class SyntaxColoringPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SyntaxColoringPreferencePage() {
		super(GRID);
		setPreferenceStore(ZamiaPlugin.getDefault().getPreferenceStore());
		setDescription("ZamiaCAD Syntax Coloring Preference Page");
	}

	public void createFieldEditors() {

		addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD, "Keyword color", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_STRING, "String color", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_COMMENT, "Comment color", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_DEFAULT, "Default text color", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_MATCHING_CHAR, "Matching char color", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_BACKGROUND, "Background color", getFieldEditorParent()));

		/*
		
		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
			*/
	}

	protected void performApply() {
		updateColors();
		super.performApply();
	}

	public boolean performOk() {
		updateColors();
		return super.performOk();
	}

	protected void updateColors() {
		IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorReference[] editorReference = page.getEditorReferences();
				for (int i = 0; i < editorReference.length; i++) {
					IEditorPart editorPart = editorReference[i].getEditor(false);
					if (editorPart instanceof ZamiaEditor) {
						ZamiaEditor editor = (ZamiaEditor) editorPart;
						editor.updateColors();
					}
				}
			}
		}
	}

	public void init(IWorkbench workbench) {
	}

}