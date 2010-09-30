/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;
import org.zamia.BuildPath;
import org.zamia.BuildPathEntry;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.plugin.build.ZamiaBuilder;
import org.zamia.plugin.editors.ExternalReaderEditorInput;
import org.zamia.plugin.editors.ZamiaEditor;
import org.zamia.plugin.editors.completion.VHDLContext;
import org.zamia.plugin.efs.ZamiaFileStore;
import org.zamia.plugin.views.navigator.IGModuleWrapper;
import org.zamia.util.PathName;


/**
 * The main plugin class
 * 
 * @author Guenter Bartsch
 */
public class ZamiaPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.zamia.plugin";

	public static final String CONSOLE_ID = "Zamia Console";

	private static final String CUSTOM_TEMPLATES_PREFERENCE_NAME = "org.zamia.templatesStore";

	public static final boolean ENABLE_EXPERIMENTAL_FEATURES = false;

	public final static String BP_EXTERNAL_SOURCES = "[BP External Sources]";

	private static FSCache fsCache = FSCache.getInstance();

	// The shared instance.
	private static ZamiaPlugin plugin;

	public static PrintStream out;

	public static ZamiaLogger logger;

	public static ExceptionLogger el;

	public String fVersion;

	// Resource bundle.
	private ResourceBundle fResourceBundle;

	private ContributionContextTypeRegistry fContextTypeRegistry;

	private TemplateStore fTemplateStore;

	/**
	 * The constructor.
	 */
	public ZamiaPlugin() {
		super();
		plugin = this;
		try {
			fResourceBundle = ResourceBundle.getBundle("org.zamia.plugin.ZamiaPlugin");
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
	}

	public ResourceBundle getResourceBundle() {
		return fResourceBundle;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
		super.start(context);

		Dictionary dictionary = context.getBundle().getHeaders();
		fVersion = (String) dictionary.get("Bundle-Version");

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				MessageConsole console = null;
				ConsolePlugin plugin = ConsolePlugin.getDefault();
				IConsoleManager conMan = plugin.getConsoleManager();
				org.eclipse.ui.console.IConsole[] existing = conMan.getConsoles();
				for (int i = 0; i < existing.length; i++) {
					if (CONSOLE_ID.equals(existing[i].getName())) {
						console = (MessageConsole) existing[i];
					}
				}

				if (console == null) {
					console = new MessageConsole(CONSOLE_ID, null);
					console.setWaterMarks(8192, 128000);
					conMan.addConsoles(new IConsole[] { console });
				}

				// MessageConsoleStream stream = console.newMessageStream();
				// console.activate();
				//
				// stream.println("Hello, world!");

				out = new MessageConsolePrintStream(console);
				ZamiaLogger.setConsoleOutput(out);

				logger = ZamiaLogger.getInstance();

				logger.info("ZamiaCAD V" + fVersion);
				logger.info("");
				logger.info("Copyright (C) 2003-2010 G. Bartsch");
				logger.info("");
				logger.info("Author: Guenter Bartsch <guenter@zamia.org>");
				logger.info("");
				logger.info("Online documentation: http://www.zamia.org");
				logger.info("Bug tracking: http://zamia.sf.net");
				logger.info("");

				el = ExceptionLogger.getInstance();

			}
		});

		//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		//		IResourceChangeListener listener = new IResourceChangeListener() {
		//			public void resourceChanged(IResourceChangeEvent event) {
		//
		//				System.out.println("Something changed: " + event);
		//
		//				if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
		//
		//					IResource resource = event.getResource();
		//
		//					if (resource instanceof IProject) {
		//
		//						IProject prj = (IProject) resource;
		//						ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);
		//						zprj.shutdown();
		//						ZamiaProjectMap.remove(prj);
		//					}
		//				}
		//			}
		//		};
		//		workspace.addResourceChangeListener(listener);

	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		ZamiaLogger.setConsoleOutput(null);
		ZamiaProjectMap.shutdown();
		System.out.println("ZamiaCAD shutdown complete.");
		System.out.println("Logfile: " + logger.getLogFileName());
		System.out.println();
		System.out.println("Thank you for using ZamiaCAD.");
	}

	/**
	 * Returns the shared instance.
	 */
	public static ZamiaPlugin getDefault() {
		return plugin;
	}

	public static Image getImage(String path) {
		ImageRegistry registry = getDefault().getImageRegistry();
		Image image = registry.get(path);
		if (image == null) {
			registry.put(path, imageDescriptorFromPlugin(PLUGIN_ID, path));
			image = registry.get(path);
		}
		return image;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	// public static String getResourceString(String key) {
	// ResourceBundle bundle = ZamiaPlugin.getDefault().getResourceBundle();
	// try {
	// return (bundle != null) ? bundle.getString(key) : key;
	// } catch (MissingResourceException e) {
	// return key;
	// }
	// }
	/**
	 * Returns the plugin's resource bundle,
	 */
	// public ResourceBundle getResourceBundle() {
	// return resourceBundle;
	// }
	public static Status makeStatus(int errorLevel, String message, Throwable e) {
		return new Status(errorLevel, PLUGIN_ID, errorLevel, message, e);
	}

	/***************************************************************************
	 * a few helper functions
	 **************************************************************************/

	public static IFile getIFile(SourceFile aSF, IProject aProject) {
		IFile file = null;
		String localPath = aSF.getLocalPath();

		String uri = aSF.getURI();
		if (uri != null) {
			logger.error("getIFile(): Called getIFile() on '%s' (absolute: '%s')", aSF, aSF.getAbsolutePath());
			return null;
		}

		logger.debug("getIFile(): sf = '%s', prj='%s'", aSF, aProject);

		if (localPath != null) {
			file = aProject.getFile(localPath);

		} else {

			logger.debug("getIFile(): Need resource for non-local file '%s'", aSF.getAbsolutePath());

			String absPath = aSF.getAbsolutePath();

			// a quick check whether this resource is local anyway

			IPath prjLocation = aProject.getLocation();
			if (prjLocation != null) {
				String prjAbsPath = prjLocation.toOSString();
				if (absPath.startsWith(prjAbsPath)) {

					localPath = absPath.substring(prjAbsPath.length());

					logger.debug("getIFile(): Warning: sf '%s' claimed to be external, but was local in project '%s', local path is '%s'", absPath, prjAbsPath, localPath);

					file = aProject.getFile(localPath);

					return file;
				}
			}

			ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(aProject);

			if (zprj == null) {
				logger.error("getIFile(): Failed to find zamia project for eclipse project '%s'.", aProject);
				return null;
			}

			BuildPath bp = zprj.getBuildPath();

			if (bp == null) {
				logger.error("getIFile(): No build path found in project '%s'", zprj);
				return null;
			}

			logger.debug("getIFile(): zprj location is '%s', build path came from '%s'", zprj.getBasePath(), bp.getSourceFile().getAbsolutePath());

			BuildPathEntry entry = bp.findEntry(aSF);

			if (entry == null) {
				logger.error("getIFile(): Failed to find bp entry for '%s'.", aSF);
				return null;
			}

			logger.debug("getIFile(): bp entry found: libId='%s', prefix='%s'", entry.fLibId, entry.fPrefix);

			if (entry.fExtern) {

				String pathPrefix = null;
				if (entry.fIsDirectory) {
					//pathPrefix = entry.fPrefix.replace('/', '.').substring(1);
					pathPrefix = entry.fPrefix;
				} else {
					File f = new File(entry.fPrefix);
					//pathPrefix = f.getParentFile().getAbsolutePath().replace('/', '.').substring(1);
					pathPrefix = f.getParentFile().getAbsolutePath();
				}
				String filename = absPath.substring(pathPrefix.length());

				logger.debug("getIFile(): external file, pathPrefix is '%s', filename is '%s'", pathPrefix, filename);

				// a really ugly hack, but effective
				ZamiaBuilder.disableAutoBuild(5000);
				
				IFolder extDir = ZamiaBuilder.linkExternalSource(aProject, pathPrefix, entry.fReadonly);
				file = extDir.getFile(filename);

				//				String path = BP_EXTERNAL_SOURCES + "/" + pathPrefix + "/" + absPath.substring(pathPrefix.length()+1);
				//				file = aPrj.getFile(path);

			} else {

				logger.debug("getIFile(): internal file, absPath is '%s'", absPath);

				file = aProject.getFile(absPath);

				if (!file.exists()) {

					String dirPath = FSCache.getDirPath(absPath);
					String filePath = FSCache.getFilePath(absPath);

					if (dirPath == null || filePath == null)
						return null;

					String path = BP_EXTERNAL_SOURCES + "/" + dirPath.replace('/', '.').substring(1) + "/" + filePath;
					file = aProject.getFile(path);
				}
			}
		}
		return file;
	}

	public static IEditorPart showSource(IWorkbenchPage aPage, ZamiaProject aZPrj, ToplevelPath aTLP) {
		IGManager igm = aZPrj.getIGM();

		Toplevel tl = aTLP.getToplevel();
		PathName path = aTLP.getPath();

		IGItem item = igm.findItem(tl, path);

		IProject prj = ZamiaProjectMap.getProject(aZPrj);

		if (item != null) {

			SourceLocation location = item.computeSourceLocation();

			IEditorPart ep = ZamiaPlugin.showSource(aPage, prj, location, 0);

			if (ep instanceof ZamiaEditor) {
				ZamiaEditor editor = (ZamiaEditor) ep;

				editor.setPath(aTLP);
			}

			return ep;
		}
		return null;
	}

	public static IEditorPart showSource(IWorkbenchPage aPage, IProject aProject, SourceLocation aLocation, int aLength) {

		IEditorPart editor = null;

		logger.debug("showSource(): page='%s', prj='%s', location='%s', length=%d", aPage, aProject, aLocation, aLength);

		if (aProject != null) {
			String path = aProject.getFullPath().toOSString();
			logger.debug("showSource(): project full path is '%s'", path);
		} else {
			logger.debug("showSource(): no prj given.");
		}

		if (aLocation == null) {
			logger.error("ZamiaPlugin: showSource(): location==null!");
			return null;
		}

		SourceFile sf = aLocation.fSF;

		if (sf == null) {
			logger.error("ZamiaPlugin: showSource(): source file == null!");
			return null;
		}

		String uri = sf.getURI();
		if (uri != null) {

			// this is typically used for std libs that reside in the ZamiaCAD
			// plugin jar

			logger.debug("showSource(): URI is '%s'", uri);

			IEditorInput input = null;
			try {
				input = new ExternalReaderEditorInput(fsCache.openFile(sf, false), sf.getFileName(), sf.getURI(), aProject);
				IEditorDescriptor descriptor;
				try {
					descriptor = IDE.getEditorDescriptor(input.getName(), true);
					if (descriptor != null) {
						IWorkbenchPage page = aPage != null ? aPage : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						editor = IDE.openEditor(page, input, descriptor.getId(), true);
					}
				} catch (PartInitException e) {
					el.logException(e);
				}
			} catch (FileNotFoundException e1) {
				el.logException(e1);
			} catch (IOException e) {
				el.logException(e);
			}

		} else {

			IFile file = getIFile(sf, aProject);

			if (file == null) {
				logger.error("showSource(): Couldn't get IFile for sf '%s'.", sf);
				return null;
			}

			try {
				editor = IDE.openEditor(aPage, file, true);
			} catch (PartInitException e) {
				el.logException(e);
			}
		}

		if (!(editor instanceof ITextEditor))
			return editor;

		try {
			ITextEditor textEditor = (ITextEditor) editor;
			IEditorInput input = textEditor.getEditorInput();
			IDocumentProvider provider = textEditor.getDocumentProvider();
			provider.connect(input);
			IDocument document = provider.getDocument(input);

			int nline = aLocation.fLine - 1;
			if (nline < 0)
				nline = 0;

			if (textEditor instanceof ZamiaEditor) {
				ZamiaEditor ze = (ZamiaEditor) textEditor;
				if (ze.isAnnotated()) {
					document = ze.getDocument();
					nline *= 2;
					nline++;
				}
			}

			IRegion line = document.getLineInformation(nline);
			textEditor.selectAndReveal(line.getOffset() + aLocation.fCol - 1, aLength);
		} catch (CoreException e) {
			el.logException(e);
		} catch (BadLocationException e) {
			el.logException(e);
		}

		return editor;
	}

	public static SourceFile getSourceFile(IFile file) {
		IPath rawLocation = file.getRawLocation();

		String path = null;

		if (rawLocation != null) {
			path = rawLocation.toOSString();
		} else {

			// actually, this is a hack:
			// since we know that non-local files are accessed via
			// ZamiaFileStores,

			java.net.URI uri = file.getLocationURI();

			try {
				IFileStore store = EFS.getStore(uri);

				if (store instanceof ZamiaFileStore) {
					ZamiaFileStore zfs = (ZamiaFileStore) store;
					path = zfs.getPath();
				}

			} catch (CoreException e) {
				el.logException(e);
			}
			if (path == null) {
				// FIXME: this is probably wrong - but what should I do?
				path = uri.getPath();
			}
		}

		SourceFile sf = new SourceFile(new File(path));

		if (rawLocation != null) {
			IPath localPath = file.getFullPath();
			localPath = localPath.removeFirstSegments(1);
			localPath = localPath.makeRelative();
			String lp = localPath.toOSString();
			sf.setLocalPath(lp);
		}

		return sf;
	}

	public static void showConsole() {

		Display display = Display.getDefault();

		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IWorkbenchWindow window = getWorkbenchWindow();

					IWorkbenchPage page = window.getActivePage();

					page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
				} catch (Throwable e) {
					el.logException(e);
				}
			}
		});
	}

	public static void showError(Shell aShell, String aTitle, String aMsg, String aReason) {
		Display display = Display.getDefault();
		logger.error("ZamiaPlugin: showError(): title=%s, msg=%s, reason=%s", aTitle, aMsg, aReason);
		display.asyncExec(new ShowMessageJob(aShell, aTitle, aMsg, aReason));
	}

	public static void showInfo(Shell aShell, String aTitle, String aMsg, String aReason) {
		Display display = Display.getDefault();
		logger.info("ZamiaPlugin: showInfo(): title=%s, msg=%s, reason=%s", aTitle, aMsg, aReason);
		display.asyncExec(new ShowMessageJob(aShell, aTitle, aMsg, aReason, false));
	}

	public static int askQuestion(Shell aShell, String aTitle, String aMsg, int aStyle) {
		Display display = Display.getDefault();

		AskQuestionJob aqj = new AskQuestionJob(aShell, aTitle, aMsg, aStyle);

		display.syncExec(aqj);

		return aqj.getRC();
	}

	public static String inputDialog(Shell aShell, String aTitle, String aMsg, String aInitialValue) {
		Display display = Display.getDefault();

		InputDialogJob aqj = new InputDialogJob(aShell, aTitle, aMsg, aInitialValue);

		display.syncExec(aqj);

		if (aqj.getRC() == Window.CANCEL) {
			return null;
		}
		return aqj.getValue();
	}

	public ContextTypeRegistry getContextTypeRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry = new ContributionContextTypeRegistry();
			fContextTypeRegistry.addContextType(VHDLContext.CONTEXT_TYPE);
		}
		return fContextTypeRegistry;
	}

	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			fTemplateStore = new ContributionTemplateStore(getContextTypeRegistry(), getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_PREFERENCE_NAME);
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				el.logException(e);
			}
		}
		return fTemplateStore;
	}

	@SuppressWarnings("unchecked")
	public static ZamiaProject findCurrentProject() {
		ZamiaProject zPrj = null;

		try {
			IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			ISelectionService sserv = window.getSelectionService();

			ISelection selection = sserv.getSelection("org.zamia.plugin.views.navigator.ZamiaNavigator");

			logger.info("ZamiaProject: findCurrentProject(): selection is %s", selection);

			if (selection instanceof IStructuredSelection) {

				IStructuredSelection structSel = (IStructuredSelection) selection;

				Iterator<Object> iterator = structSel.iterator();
				while (iterator.hasNext()) {
					Object element = iterator.next();

					if (element instanceof IGModuleWrapper) {

						IGModuleWrapper wrapper = (IGModuleWrapper) element;

						zPrj = wrapper.getZPrj();
					} else if (element instanceof IAdaptable) {
						IAdaptable adaptable = (IAdaptable) element;
						IResource res = (IResource) adaptable.getAdapter(IResource.class);
						if (res != null) {
							IProject prj = res.getProject();

							zPrj = ZamiaProjectMap.getZamiaProject(prj);
						}
					}
				}

			}

			if (zPrj == null) {

				IEditorPart editor = page.getActiveEditor();

				if (editor instanceof ZamiaEditor) {
					ZamiaEditor ze = (ZamiaEditor) editor;
					zPrj = ze.getZPrj();
				}
			}
		} catch (Throwable t) {
			el.logException(t);
		}
		return zPrj;
	}

	public static IWorkbenchWindow getWorkbenchWindow() {
		IWorkbench wb = ZamiaPlugin.getDefault().getWorkbench();

		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if (window == null) {
			IWorkbenchWindow[] windows = wb.getWorkbenchWindows();

			if (windows.length > 0) {
				window = windows[0];
			}
		}
		return window;
	}

	public static Shell getShell() {
		IWorkbenchWindow window = getWorkbenchWindow();
		return window != null ? window.getShell() : new Shell();
	}

}
