/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 21, 2009
 */
package org.zamia.plugin.editors;


/**
 * Just used let editors remember their paths using hibernate
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaEditorPath {

	private Long id;

	private String fPath;

	private String fFilename;

	public ZamiaEditorPath() {

	}
	
	public ZamiaEditorPath (String aPath, String aFilename) {
		fPath = aPath;
		fFilename = aFilename;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long aId) {
		id = aId;
	}

	public String getPath() {
		return fPath;
	}

	public void setPath(String aPath) {
		fPath = aPath;
	}

	public String getFilename() {
		return fFilename;
	}

	public void setFilename(String aFilename) {
		fFilename = aFilename;
	}

}
