/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.Block;
import org.zamia.vhdl.ast.BlockDeclarativeItem;
import org.zamia.vhdl.ast.ConcurrentStatement;
import org.zamia.vhdl.ast.Entity;
import org.zamia.vhdl.ast.GenerateStatement;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.PackageBody;
import org.zamia.vhdl.ast.SequentialProcess;
import org.zamia.vhdl.ast.SubProgram;
import org.zamia.vhdl.ast.VHDLPackage;


/**
 * Outline page content provider, uses the zamia compilers to generate syntax
 * tree nodes in the outline view
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaOutlineContentProvider implements ITreeContentProvider {

	private boolean fHierarchicalMode = true;

	private boolean fDoSort = true;

	private ZamiaEditor fEditor;

	public ZamiaOutlineContentProvider(ZamiaEditor aEditor) {
		fEditor = aEditor;
	}

	public void dispose() {
	}

	@SuppressWarnings("unchecked")
	private void sort(ArrayList aList) {
		Collections.sort(aList, new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
		});
	}

	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object aElement) {

		if (aElement instanceof Entity) {
			Entity entity = (Entity) aElement;

			if (fHierarchicalMode) {
				ZamiaOutlineHierarchyGen hier = new ZamiaOutlineHierarchyGen();

				int n = entity.getNumDeclarations();

				for (int i = 0; i < n; i++) {
					BlockDeclarativeItem decl = entity.getDeclaration(i);
					hier.add(decl);
				}

				n = entity.getNumInterfaceDeclarations();
				for (int i = 0; i < n; i++) {
					hier.add(entity.getInterfaceDeclaration(i));
				}

				return hier.toArray();
			} else {

				int n = entity.getNumInterfaceDeclarations();
				ArrayList l = new ArrayList(n);
				for (int i = 0; i < n; i++) {
					l.add(entity.getInterfaceDeclaration(i));
				}
				if (fDoSort) {
					sort(l);
				}
				return l.toArray();
			}
		} else if (aElement instanceof Architecture) {
			Architecture arch = (Architecture) aElement;

			if (fHierarchicalMode) {
				ZamiaOutlineHierarchyGen hier = new ZamiaOutlineHierarchyGen();

				int n = arch.getNumDeclarations();

				for (int i = 0; i < n; i++) {
					BlockDeclarativeItem decl = arch.getDeclaration(i);
					hier.add(decl);
				}

				n = arch.getNumConcurrentStatements();
				for (int i = 0; i < n; i++) {
					ConcurrentStatement cs = arch.getConcurrentStatement(i);
					extractCS(cs, hier);
				}

				return hier.toArray();
			} else {
				int n = arch.getNumDeclarations();

				ArrayList l = new ArrayList(n);

				for (int i = 0; i < n; i++) {
					BlockDeclarativeItem decl = arch.getDeclaration(i);
					l.add(decl);
				}

				n = arch.getNumConcurrentStatements();
				for (int i = 0; i < n; i++) {
					ConcurrentStatement cs = arch.getConcurrentStatement(i);
					extractCS(cs, l);
				}

				if (fDoSort) {
					sort(l);
				}
				return l.toArray();
			}

		} else if (aElement instanceof VHDLPackage) {
			VHDLPackage pkg = (VHDLPackage) aElement;

			if (fHierarchicalMode) {
				ZamiaOutlineHierarchyGen hier = new ZamiaOutlineHierarchyGen();

				int n = pkg.getNumDeclarations();

				for (int i = 0; i < n; i++) {
					BlockDeclarativeItem decl = pkg.getDeclaration(i);
					hier.add(decl);
				}

				return hier.toArray();
			} else {
				int m = pkg.getNumDeclarations();

				ArrayList l = new ArrayList(m);

				for (int i = 0; i < m; i++) {
					BlockDeclarativeItem decl = pkg.getDeclaration(i);
					l.add(decl);
				}
				if (fDoSort) {
					sort(l);
				}
				return l.toArray();
			}
		} else if (aElement instanceof SequentialProcess) {
			SequentialProcess proc = (SequentialProcess) aElement;

			int m = proc.getNumDeclarations();

			Object ret[] = new Object[m];
			int j = 0;

			for (int i = 0; i < m; i++) {
				BlockDeclarativeItem decl = proc.getDeclaration(i);
				ret[j++] = decl;
			}
			return ret;
		} else if (aElement instanceof SubProgram) {
			SubProgram sub = (SubProgram) aElement;

			int m = sub.getNumDeclarations();

			Object ret[] = new Object[m];
			int j = 0;

			for (int i = 0; i < m; i++) {
				BlockDeclarativeItem decl = sub.getDeclaration(i);
				ret[j++] = decl;
			}
			return ret;
		} else if (aElement instanceof PackageBody) {
			PackageBody pkg = (PackageBody) aElement;

			int m = pkg.getNumDeclarations();
			Object ret[] = new Object[m];
			int j = 0;
			for (int i = 0; i < m; i++) {
				BlockDeclarativeItem decl = pkg.getDeclaration(i);
				ret[j++] = decl;
			}
			return ret;

		} else if (aElement instanceof ZamiaOutlineFolder) {
			ZamiaOutlineFolder folder = (ZamiaOutlineFolder) aElement;
			return folder.items.toArray();
		}

		return null;
	}

	private void extractCS(ConcurrentStatement aCS, ZamiaOutlineHierarchyGen aHier) {
		if (aCS instanceof SequentialProcess) {
			aHier.add(aCS);
		} else if (aCS instanceof InstantiatedUnit) {
			aHier.add(aCS);
		} else if (aCS instanceof GenerateStatement) {

			GenerateStatement gs = (GenerateStatement) aCS;

			int n = gs.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs2 = gs.getConcurrentStatement(i);
				extractCS(cs2, aHier);
			}
		} else if (aCS instanceof Block) {
			Block block = (Block) aCS;
			int n = block.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs2 = block.getConcurrentStatement(i);
				extractCS(cs2, aHier);
			}
			
		} 
	}

	@SuppressWarnings("unchecked")
	private void extractCS(ConcurrentStatement aCS, ArrayList aResult) {
		if (aCS instanceof SequentialProcess) {
			aResult.add(aCS);
		} else if (aCS instanceof InstantiatedUnit) {
			aResult.add(aCS);
		} else if (aCS instanceof GenerateStatement) {

			GenerateStatement gs = (GenerateStatement) aCS;

			int n = gs.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs2 = gs.getConcurrentStatement(i);
				extractCS(cs2, aResult);
			}
		} else if (aCS instanceof Block) {
			Block block = (Block) aCS;
			int n = block.getNumConcurrentStatements();
			for (int i = 0; i < n; i++) {
				ConcurrentStatement cs2 = block.getConcurrentStatement(i);
				extractCS(cs2, aResult);
			}
			
		} 
	}

	public boolean hasChildren(Object aElement) {

		if (aElement instanceof Entity)
			return true;
		if (aElement instanceof Architecture)
			return true;
		if (aElement instanceof SequentialProcess) {
			SequentialProcess proc = (SequentialProcess) aElement;
			return proc.getNumDeclarations() > 0;
		}
		if (aElement instanceof SubProgram)
			return true;
		if (aElement instanceof VHDLPackage)
			return true;
		if (aElement instanceof PackageBody)
			return true;
		if (aElement instanceof ZamiaOutlineFolder)
			return true;
		return false;
	}

	public Object[] getElements(Object aInputElement) {

		ZamiaReconcilingStrategy strategy = fEditor.getReconcilingStrategy();
		return strategy.getRootElements();
	}

	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
	}

	public Object getParent(Object aElement) {
		// FIXME return (anElement instanceof ITreeNode) ? ((ITreeNode)
		// anElement).getParent() : null;
		return null;
	}

}