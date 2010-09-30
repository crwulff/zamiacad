/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.vhdl.ast.AliasDeclaration;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.AttributeDeclaration;
import org.zamia.vhdl.ast.BlockDeclarativeItem;
import org.zamia.vhdl.ast.ComponentDeclaration;
import org.zamia.vhdl.ast.ConstantDeclaration;
import org.zamia.vhdl.ast.DesignUnit;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.PackageBody;
import org.zamia.vhdl.ast.SequentialProcess;
import org.zamia.vhdl.ast.SignalDeclaration;
import org.zamia.vhdl.ast.SubProgram;
import org.zamia.vhdl.ast.TypeDeclaration;
import org.zamia.vhdl.ast.VHDLPackage;
import org.zamia.vhdl.ast.VariableDeclaration;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZamiaOutlineLabelProvider extends LabelProvider {
	final Image fLibraryIcon, fEntityIcon, fArchIcon, fInIcon,
			fInoutIcon, fOutIcon, fErrorIcon, fUnknownIcon, fComponentIcon,
			fPackageIcon, fPackageBodyIcon, fProcessIcon, fSignalIcon, fTypeIcon,
			fSubtypeIcon, fUseIcon, fBodyIcon, fAttributeIcon, fVariableIcon,
			fSubprogramIcon, fConstantIcon, fFolderIcon, fAliasIcon;

	public ZamiaOutlineLabelProvider(Composite aParent) {

		fLibraryIcon = ZamiaPlugin.getImage("/share/images/library.gif");
		fEntityIcon = ZamiaPlugin.getImage("/share/images/entity.png");
		fArchIcon = ZamiaPlugin.getImage("/share/images/arch.gif");
		fInIcon = ZamiaPlugin.getImage("/share/images/in.gif");
		fInoutIcon = ZamiaPlugin.getImage("/share/images/inout.gif");
		fOutIcon = ZamiaPlugin.getImage("/share/images/out.gif");
		fErrorIcon = ZamiaPlugin.getImage("/share/images/error.png");
		fUnknownIcon = ZamiaPlugin.getImage("/share/images/unknown.png");
		fComponentIcon = ZamiaPlugin.getImage("/share/images/component.gif");
		fPackageIcon = ZamiaPlugin.getImage("/share/images/package.gif");
		fPackageBodyIcon = ZamiaPlugin
				.getImage("/share/images/package_body.gif");
		fProcessIcon = ZamiaPlugin.getImage("/share/images/process.gif");
		fSignalIcon = ZamiaPlugin.getImage("/share/images/signal.gif");
		fSubtypeIcon = ZamiaPlugin.getImage("/share/images/subtype.png");
		fSubprogramIcon = ZamiaPlugin.getImage("/share/images/subprogram.gif");
		fTypeIcon = ZamiaPlugin.getImage("/share/images/type.gif");
		fUseIcon = ZamiaPlugin.getImage("/share/images/use.png");
		fBodyIcon = ZamiaPlugin.getImage("/share/images/body.png");
		fAttributeIcon = ZamiaPlugin.getImage("/share/images/attribute.gif");
		fVariableIcon = ZamiaPlugin.getImage("/share/images/var.gif");
		fConstantIcon = ZamiaPlugin.getImage("/share/images/constant.gif");
		fFolderIcon = ZamiaPlugin.getImage("/share/images/folder.gif");
		fAliasIcon = ZamiaPlugin.getImage("/share/images/alias.gif");
	}

	public Image getImage(Object aElement) {
		if (aElement == null) {
			return fErrorIcon;
		}

		if (aElement instanceof Architecture)
			return fArchIcon;
		if (aElement instanceof Entity)
			return fEntityIcon;
		if (aElement instanceof VHDLPackage)
			return fPackageIcon;
		if (aElement instanceof PackageBody)
			return fPackageBodyIcon;
		if (aElement instanceof SequentialProcess)
			return fProcessIcon;

		if (aElement instanceof InterfaceDeclaration) {
			InterfaceDeclaration isd = (InterfaceDeclaration) aElement;
			switch (isd.getDir()) {
			case IN:
				return fInIcon;
			case OUT:
				return fOutIcon;
			case INOUT:
				return fInoutIcon;
			}
		}

		if (aElement instanceof ComponentDeclaration)
			return fComponentIcon;

		if (aElement instanceof TypeDeclaration)
			return fTypeIcon;

		if (aElement instanceof SubProgram)
			return fSubprogramIcon;
		if (aElement instanceof SignalDeclaration)
			return fSignalIcon;
		if (aElement instanceof ConstantDeclaration)
			return fConstantIcon;
		if (aElement instanceof VariableDeclaration)
			return fVariableIcon;
		if (aElement instanceof SequentialProcess)
			return fProcessIcon;
		if (aElement instanceof SubProgram)
			return fSubprogramIcon;
		if (aElement instanceof InstantiatedUnit)
			return fComponentIcon;
		if (aElement instanceof ZamiaOutlineFolder)
			return fFolderIcon;
		if (aElement instanceof AttributeDeclaration)
			return fAttributeIcon;
		if (aElement instanceof AliasDeclaration)
			return fAliasIcon;
		return fUnknownIcon;
	}

	public String getText(Object aElement) {
		if (aElement instanceof Architecture) {
			return aElement.toString();
		} else if (aElement instanceof DesignUnit) {
			DesignUnit du = (DesignUnit) aElement;
			return du.getId();
		} else if (aElement instanceof InterfaceDeclaration) {
			InterfaceDeclaration isd = (InterfaceDeclaration) aElement;
			// return isd.getId()+":"+isd.getLocation().line;
			return isd.getId();

		} else if (aElement instanceof SequentialProcess) {
			SequentialProcess cp = (SequentialProcess) aElement;
			String id = cp.getLabel();
			if (id == null)
				id = "unnamed process";
			return id;
		} else if (aElement instanceof InstantiatedUnit) {
			InstantiatedUnit iu = (InstantiatedUnit) aElement;
			return iu.toString();
		} else if (aElement instanceof BlockDeclarativeItem) {
			BlockDeclarativeItem bdi = (BlockDeclarativeItem) aElement;
			return bdi.getId();
		} else if (aElement instanceof ZamiaOutlineFolder) {
			ZamiaOutlineFolder folder = (ZamiaOutlineFolder) aElement;
			return folder.id;
		}

		return "FIXME VHDLOutlineLabelProvider";
	}
}