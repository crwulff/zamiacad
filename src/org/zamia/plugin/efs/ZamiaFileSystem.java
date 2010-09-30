/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.efs;

import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.zamia.ExceptionLogger;


/**
 * This implements the ZamiaEFS3:// type URIs used to create symlinks in [BP External Sources]
 * 
 * We're somewhat abusing the URI mechanism to create our own view on the outside filesystem
 * and encode certain flags for optimization:
 * 
 * The host part of the URI is used to encode the readonly flag. Valid host strings are
 * 
 * READWRITE, READONLY
 * 
 * The port is used to specify the number of characters of the path that make up
 * the section of the path that was given in the BuildPath.txt file.
 * 
 * The path represents to absolute (and hopefully canonical) file system path in unix style
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaFileSystem extends FileSystem {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static String ZAMIA_EFS_SCHEME = "ZamiaEFS3";

	public final static String ZAMIA_EFS_HOST_READONLY = "READONLY";

	public final static String ZAMIA_EFS_HOST_READWRITE = "READWRITE";

	private HashMap<URI, IFileStore> fFileStoreCache = new HashMap<URI, IFileStore>();

	@Override
	public IFileStore getStore(URI aURI) {

		IFileStore store = fFileStoreCache.get(aURI);

		if (store == null) {

			int port = aURI.getPort();
			String host = aURI.getHost();

			boolean readonly = ZAMIA_EFS_HOST_READONLY.equals(host);

			String path = aURI.getPath();

			int l = path.length();
			if (l < port || port <= 0) {
				return null; // invalid
			}

			String firstPart = path.substring(0, port);

			try {
				URI topURI = new URI(ZAMIA_EFS_SCHEME, null, host, port, firstPart, null, null);
				store = fFileStoreCache.get(topURI);
				if (store == null) {
					String name = path.replace('/', '.');
					store = new ZamiaFileStore(name, null, path, readonly, true, topURI);
					fFileStoreCache.put(topURI, store);
				}

				if (port < l) {
					String secondPart = path.substring(port);

					String elements[] = secondPart.split("/");

					int n = elements.length;

					for (int i = 0; i < n; i++) {
						String name = elements[i];
						store = store.getChild(name);
						if (store == null) {
							return null;
						}
					}
				}
			} catch (Throwable t) {
				el.logException(t);
				return null;
			}
			fFileStoreCache.put(aURI, store);
		}

		return store;
	}

}
