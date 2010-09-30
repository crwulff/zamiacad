/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 3, 2007
 */

package org.zamia.plugin.views.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGLibraryImport;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGPackageImport;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGType;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.PackageBody;
import org.zamia.vhdl.ast.VHDLPackage;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZamiaLabelProvider extends LabelProvider {

	private Image fEntityIcon;

	private Image fArchIcon;

	private Image fPackageIcon;

	private Image fPackageBodyIcon;

	private Image fErrorIcon;

	private Image fRedArchIcon;

	private Image fLocalsIcon;

	private Image fSignalIcon;

	private Image fTypeIcon;

	private Image fConstantIcon;

	private Image fMiscIcon;

	private Image fLibraryIcon;

	private Image fProcessIcon;

	private Image fInIcon;

	private Image fInoutIcon;

	private Image fOutIcon;

	private Image fVarIcon;

	public ZamiaLabelProvider() {
		fEntityIcon = ZamiaPlugin.getImage("/share/images/entity.png");
		fArchIcon = ZamiaPlugin.getImage("/share/images/arch.gif");
		fRedArchIcon = ZamiaPlugin.getImage("/share/images/redArch.gif");
		fPackageIcon = ZamiaPlugin.getImage("/share/images/package.gif");
		fPackageBodyIcon = ZamiaPlugin.getImage("/share/images/package_body.gif");
		fErrorIcon = ZamiaPlugin.getImage("/share/images/error.png");
		fLocalsIcon = ZamiaPlugin.getImage("/share/images/folder.gif");
		fSignalIcon = ZamiaPlugin.getImage("/share/images/signal.gif");
		fTypeIcon = ZamiaPlugin.getImage("/share/images/type.gif");
		fConstantIcon = ZamiaPlugin.getImage("/share/images/constant.gif");
		fMiscIcon = ZamiaPlugin.getImage("/share/images/unknown.png");
		fLibraryIcon = ZamiaPlugin.getImage("/share/images/library.gif");
		fProcessIcon = ZamiaPlugin.getImage("/share/images/process.gif");
		fInIcon = ZamiaPlugin.getImage("/share/images/in.gif");
		fInoutIcon = ZamiaPlugin.getImage("/share/images/inout.gif");
		fOutIcon = ZamiaPlugin.getImage("/share/images/out.gif");
		fVarIcon = ZamiaPlugin.getImage("/share/images/var.gif");
	}

	@Override
	public Image getImage(Object anElement) {
		if (anElement == null) {
			return fErrorIcon;
		}

		if (anElement instanceof Architecture)
			return fArchIcon;
		if (anElement instanceof IGModuleWrapper) {
			IGModuleWrapper wrapper = (IGModuleWrapper) anElement;

			switch (wrapper.getOp()) {
			case ITEM:
				IGItem item = wrapper.getItem();

				if (item instanceof IGObject) {
					IGObject obj = (IGObject) item;
					switch (obj.getDirection()) {
					case IN:
						return fInIcon;
					case INOUT:
						return fInoutIcon;
					case OUT:
						return fOutIcon;
					}
					switch (obj.getCat()) {
					case CONSTANT:
						return fConstantIcon;
						//					case FILE: FIXME
						//						return fFileIcon;
					case SIGNAL:
						return fSignalIcon;
					case VARIABLE:
						return fVarIcon;
					}
				}

				if (item instanceof IGType) {
					return fTypeIcon;
				} else if (item instanceof IGProcess) {
					return fProcessIcon;
				} else if (item instanceof IGPackageImport) {
					return fPackageIcon;
				} else if (item instanceof IGLibraryImport) {
					return fLibraryIcon;
				}

				return fMiscIcon;

			case LOCALS:
			case GLOBALS:
				return fLocalsIcon;

			case BLUEIG:
				return fArchIcon;

			case PROCESS:
				return fProcessIcon;
			}

			return fRedArchIcon;
		}
		if (anElement instanceof Entity)
			return fEntityIcon;
		if (anElement instanceof VHDLPackage)
			return fPackageIcon;
		if (anElement instanceof PackageBody)
			return fPackageBodyIcon;
		return super.getImage(anElement);
	}

	@Override
	public String getText(Object element) {

		if (element instanceof Entity) {
			Entity entity = (Entity) element;
			return entity.getId();
		} else if (element instanceof IGModuleWrapper) {
			IGModuleWrapper wrapper = (IGModuleWrapper) element;

			return wrapper.toString();
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			return file.getProjectRelativePath().toString();
		} else if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			return folder.getProjectRelativePath().toString();
		} else if (element instanceof IProject) {
			IProject prj = (IProject) element;
			return prj.getName();
		}

		return super.getText(element);
	}
}
