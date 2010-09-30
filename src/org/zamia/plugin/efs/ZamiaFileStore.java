/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 20, 2008
 */
package org.zamia.plugin.efs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.ZamiaLogger;
import org.zamia.plugin.ZamiaPlugin;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaFileStore extends FileStore {

	public final static boolean dump = false;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static FSCache fsCache = FSCache.getInstance();

	private final IFileStore fParent;

	private final String fName;

	private final String fPath;

	private HashMap<String, IFileStore> fChildCache = new HashMap<String, IFileStore>();

	private final boolean fReadonly;

	private URI fURI;

	private boolean fTop;

	public ZamiaFileStore(String aName, IFileStore aParent, String aPath, boolean aReadonly, boolean aTop, URI aURI) {

		if (dump) {
			logger.debug("ZamiaFileStore: new instance name='%s', parent='%s', path='%s', readonly=%b", aName, aParent, aPath, aReadonly);
		}

		fName = aName;
		fParent = aParent;
		fPath = aPath;
		fReadonly = aReadonly;
		fTop = aTop;
		fURI = aURI;
	}

	@Override
	public String[] childNames(int aOptions, IProgressMonitor aMonitor) throws CoreException {

		if (dump) {
			logger.debug("ZamiaFileStore: childNames() options=%d", aOptions);
		}

		if (isDirectory()) {

			String[] l = fsCache.list(fPath, fReadonly);
			if (l != null) {
				return l;
			}
		}
		return new String[0];
	}

	private boolean isDirectory() {
		return fTop || fsCache.isDirectory(fPath, fReadonly);
	}

	@Override
	public IFileInfo fetchInfo(int aOptions, IProgressMonitor monitor) throws CoreException {

		if (dump) {
			logger.debug("ZamiaFileStore: fetchInfo() options=%d", aOptions);
		}

		FileInfo fileInfo = new FileInfo(getName());

		if (!fTop) {
			fileInfo.setDirectory(isDirectory());
			fileInfo.setLastModified(fsCache.getLastModified(fPath, fReadonly));
			fileInfo.setExists(fsCache.exists(fPath, fReadonly));
		} else {
			fileInfo.setDirectory(true);
			fileInfo.setLastModified(System.currentTimeMillis());
			fileInfo.setExists(true);
		}
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, fReadonly);

		return fileInfo;
	}

	@Override
	public IFileStore getChild(String aName) {
		if (dump) {
			logger.debug("ZamiaFileStore: getChild(), name='%s'", aName);
		}

		IFileStore child = fChildCache.get(aName);
		if (child == null) {

			try {
				String childPath = fPath.endsWith("/") ? fPath + aName : fPath + '/' + aName;

				URI uri = new URI(ZamiaFileSystem.ZAMIA_EFS_SCHEME, null, fReadonly ? ZamiaFileSystem.ZAMIA_EFS_HOST_READONLY: ZamiaFileSystem.ZAMIA_EFS_HOST_READWRITE, fURI.getPort(), childPath, null, null);
				child = new ZamiaFileStore(aName, this, fPath + File.separator + aName, fReadonly, false, uri);
				fChildCache.put(aName, child);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}

		}
		return child;
	}

	@Override
	public String getName() {
		if (dump) {
			logger.debug("ZamiaFileStore: getName()");
		}
		return fName;
	}

	@Override
	public IFileStore getParent() {
		if (dump) {
			logger.debug("ZamiaFileStore: getParent()");
		}
		return fParent;
	}

	@Override
	public InputStream openInputStream(int aOptions, IProgressMonitor aMonitor) throws CoreException {
		if (dump)
			logger.debug("ZamiaFileStore: openInputStream(), options=%d", aOptions);

		try {
			return fsCache.openFileStream(fPath, fReadonly);
		} catch (IOException e) {
			el.logException(e);
			throw new CoreException(new Status(IStatus.ERROR, ZamiaPlugin.PLUGIN_ID, e.toString()));
		}
	}

	@Override
	public OutputStream openOutputStream(int aOptions, IProgressMonitor aMonitor) throws CoreException {
		if (dump)
			logger.debug("ZamiaFileStore: openOutputStream(), options=%d", aOptions);

		try {
			return fsCache.openFileWrite(fPath);
		} catch (IOException e) {
			el.logException(e);
			throw new CoreException(new Status(IStatus.ERROR, ZamiaPlugin.PLUGIN_ID, e.toString()));
		}
	}

	@Override
	public URI toURI() {
		if (dump) {
			logger.debug("ZamiaFileStore: toURI()");
		}
		return fURI;
	}

	public String getPath() {
		return fPath;
	}

}
