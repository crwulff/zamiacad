/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.zamia.DUManager;
import org.zamia.ExceptionLogger;
import org.zamia.SFDUInfo;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.vhdl.ast.DesignUnit;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaReconcilingStrategy implements IReconcilingStrategy {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private IDocument fDocument;

	private SFDUInfo fSFDUInfo;

	private ZamiaEditor fEditor;

	private ZamiaProject fZPrj;

	private DUManager fDUM;

	//	private ArrayList<Position> fPositions;

	public ZamiaReconcilingStrategy(ZamiaEditor aEditor) {
		fEditor = aEditor;
	}

	public void setDocument(IDocument aDocument) {
		fDocument = aDocument;
		parse();
	}

	public void reconcile(DirtyRegion aDirtyRegion, IRegion aRegion) {
		parse();
	}

	public void reconcile(IRegion aPartition) {
		parse();
	}

	public void parse() {
		try {
			if (fDocument != null) {
				DocumentReader dr = new DocumentReader(fDocument);

				IEditorInput input = fEditor.getEditorInput();

				IProject prj = null;
				SourceFile sf = null;
				if (input instanceof ExternalReaderEditorInput) {
					ExternalReaderEditorInput esei = (ExternalReaderEditorInput) input;
					sf = new SourceFile(esei.getURI());

					prj = esei.getProject();

				} else {
					IFile file = ResourceUtil.getFile(input);

					if (file == null) {
						logger.error("Input doesn't have a corresponding project file. Will not generate outline.");
						fSFDUInfo = new SFDUInfo();
						return;
					}

					prj = file.getProject();

					sf = ZamiaPlugin.getSourceFile(file);
				}

				fZPrj = ZamiaProjectMap.getZamiaProject(prj);

				fDUM = fZPrj.getDUM();

				fSFDUInfo = fDUM.compileFile(sf, dr);

				//calculateFoldingPositions();
			} else {
				//				fPositions = new ArrayList<Position>();
			}
		} catch (IOException e) {
			el.logException(e);
		} catch (ZamiaException e) {
			el.logException(e);
		}
		
		Shell shell= fEditor.getSite().getShell();
		if (shell == null || shell.isDisposed())
			return;
		
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fEditor.updateOutlinePage();
			}
		});

	}

	//	private Position getIOPosition(ASTObject io_) throws BadLocationException {
	//		int start = getIOOffset(io_);
	//		int end = getNextIOOffset(io_) - 1;
	//
	//		return new Position(start, end - start);
	//	}
	//
	//	private int getNextIOOffset(ASTObject io_) throws BadLocationException {
	//
	//		if (io_ instanceof DesignUnit) {
	//			ASTObject next = null;
	//			int n = dus.size();
	//			boolean foundIt = false;
	//			for (int i = 0; i < n; i++) {
	//
	//				try {
	//					DesignUnit du = dum.getDU(dus.get(i));
	//
	//					if (du == io_) {
	//						foundIt = true;
	//					} else if (foundIt) {
	//						next = du;
	//						break;
	//					}
	//				} catch (ZamiaException e) {
	//					el.logException(e);
	//				}
	//			}
	//
	//			if (next != null) {
	//				return getIOOffset(next);
	//			}
	//
	//		} else {
	//
	//			ASTObject parent = io_.getParent();
	//
	//			if (parent != null) {
	//
	//				int n = parent.getNumChildren();
	//
	//				int closestOffset = Integer.MAX_VALUE;
	//				int ioOffset = getIOOffset(io_);
	//
	//				boolean foundNext = false;
	//
	//				for (int i = 0; i < n; i++) {
	//
	//					ASTObject io = parent.getChild(i);
	//
	//					if (io == null) {
	//						continue;
	//					}
	//
	//					int offset = getIOOffset(io);
	//
	//					if (offset > ioOffset && closestOffset > offset) {
	//						closestOffset = offset;
	//						foundNext = true;
	//					}
	//				}
	//
	//				if (foundNext) {
	//					return closestOffset;
	//				} else {
	//					return getNextIOOffset(parent);
	//				}
	//			}
	//
	//		}
	//		return document.getLength();
	//	}
	//
	//	private int getIOOffset(ASTObject io_) throws BadLocationException {
	//		SourceLocation location = io_.getLocation();
	//
	//		int line = location.line - 1;
	//		line = (line < 0) ? 0 : line;
	//		int col = location.col - 1;
	//		col = (col < 0) ? 0 : col;
	//
	//		return document.getLineOffset(line) + col;
	//	}

	//	private void calculateDUFoldingPositions(DesignUnit du_) {
	//
	//		try {
	//
	//			// first: the whole du
	//
	//			fPositions.add(getIOPosition(du_));
	//
	//			// now declarations 
	//
	//			int n = du_.getNumDeclarations();
	//			int firstOffset = 10000;
	//			int lastOffset = 0;
	//			ASTObject lastDecl = null;
	//
	//			for (int i = 0; i < n; i++) {
	//
	//				BlockDeclarativeItem decl = du_.getDeclaration(i);
	//
	//				int offset = getIOOffset(decl);
	//				if (offset < firstOffset) {
	//					firstOffset = offset;
	//				}
	//
	//				if (offset > lastOffset) {
	//					lastOffset = offset;
	//					lastDecl = decl;
	//				}
	//
	//				if (decl instanceof SubProgram) {
	//					calculateSubFoldingPositions((SubProgram) decl);
	//				}
	//
	//			}
	//
	//			if (lastDecl != null) {
	//				int length = getNextIOOffset(lastDecl) - firstOffset;
	//
	//				fPositions.add(new Position(firstOffset, length));
	//			}
	//
	//			// now concurrent statements
	//
	//			if (du_ instanceof Architecture) {
	//
	//				Architecture arch = (Architecture) du_;
	//
	//				n = arch.getNumConcurrentStatements();
	//				firstOffset = 10000;
	//				lastOffset = 0;
	//				ConcurrentStatement lastStmt = null;
	//				for (int i = 0; i < n; i++) {
	//
	//					ConcurrentStatement stmt = arch.getConcurrentStatement(i);
	//
	//					int offset = getIOOffset(stmt);
	//					if (offset < firstOffset) {
	//						firstOffset = offset;
	//					}
	//
	//					if (offset > lastOffset) {
	//						lastOffset = offset;
	//						lastStmt = stmt;
	//					}
	//
	//					if (stmt instanceof SequentialProcess) {
	//						calculateProcessFoldingPositions((SequentialProcess) stmt);
	//					}
	//
	//				}
	//				if (lastStmt != null) {
	//					int length = getNextIOOffset(lastStmt) - firstOffset;
	//
	//					fPositions.add(new Position(firstOffset, length));
	//				}
	//			}
	//
	//		} catch (BadLocationException e) {
	//			el.logException(e);
	//		}
	//	}

	//	private void calculateProcessFoldingPositions(SequentialProcess proc_) {
	//		try {
	//
	//			// first: the whole process
	//
	//			fPositions.add(getIOPosition(proc_));
	//
	//			// now declarations 
	//
	//			int n = proc_.getNumDeclarations();
	//			int firstOffset = 10000;
	//			int lastOffset = 0;
	//			ASTObject lastDecl = null;
	//
	//			for (int i = 0; i < n; i++) {
	//
	//				BlockDeclarativeItem decl = proc_.getDeclaration(i);
	//
	//				int offset = getIOOffset(decl);
	//				if (offset < firstOffset) {
	//					firstOffset = offset;
	//				}
	//
	//				if (offset > lastOffset) {
	//					lastOffset = offset;
	//					lastDecl = decl;
	//				}
	//
	//				if (decl instanceof SubProgram) {
	//					calculateSubFoldingPositions((SubProgram) decl);
	//				}
	//
	//			}
	//
	//			if (lastDecl != null) {
	//				int length = getNextIOOffset(lastDecl) - firstOffset;
	//
	//				fPositions.add(new Position(firstOffset, length));
	//			}
	//
	//			// now concurrent statements
	//
	//			SequenceOfStatements code = proc_.getStatementSequence();
	//			if (code != null) {
	//				n = code.getNumStatements();
	//				firstOffset = 10000;
	//				lastOffset = 0;
	//				SequentialStatement lastStmt = null;
	//				for (int i = 0; i < n; i++) {
	//
	//					SequentialStatement stmt = code.getStatement(i);
	//
	//					int offset = getIOOffset(stmt);
	//					if (offset < firstOffset) {
	//						firstOffset = offset;
	//					}
	//
	//					if (offset > lastOffset) {
	//						lastOffset = offset;
	//						lastStmt = stmt;
	//					}
	//				}
	//				if (lastStmt != null) {
	//					int length = getNextIOOffset(lastStmt) - firstOffset;
	//
	//					fPositions.add(new Position(firstOffset, length));
	//				}
	//			}
	//
	//		} catch (BadLocationException e) {
	//			el.logException(e);
	//		}
	//	}
	//
	//	private void calculateSubFoldingPositions(SubProgram sub_) {
	//		try {
	//
	//			// first: the whole sub
	//
	//			fPositions.add(getIOPosition(sub_));
	//
	//			// now declarations 
	//
	//			int n = sub_.getNumDeclarations();
	//			int firstOffset = 10000;
	//			int lastOffset = 0;
	//			ASTObject lastDecl = null;
	//
	//			for (int i = 0; i < n; i++) {
	//
	//				BlockDeclarativeItem decl = sub_.getDeclaration(i);
	//
	//				int offset = getIOOffset(decl);
	//				if (offset < firstOffset) {
	//					firstOffset = offset;
	//				}
	//
	//				if (offset > lastOffset) {
	//					lastOffset = offset;
	//					lastDecl = decl;
	//				}
	//
	//				if (decl instanceof SubProgram) {
	//					calculateSubFoldingPositions((SubProgram) decl);
	//				}
	//
	//			}
	//
	//			if (lastDecl != null) {
	//				int length = getNextIOOffset(lastDecl) - firstOffset;
	//
	//				fPositions.add(new Position(firstOffset, length));
	//			}
	//
	//			// now statements
	//
	//			SequenceOfStatements code = sub_.getCode();
	//			if (code != null) {
	//				firstOffset = 10000;
	//				lastOffset = 0;
	//				SequentialStatement lastStmt = null;
	//				n = code.getNumStatements();
	//				for (int i = 0; i < n; i++) {
	//
	//					SequentialStatement stmt = code.getStatement(i);
	//
	//					int offset = getIOOffset(stmt);
	//					if (offset < firstOffset) {
	//						firstOffset = offset;
	//					}
	//
	//					if (offset > lastOffset) {
	//						lastOffset = offset;
	//						lastStmt = stmt;
	//					}
	//
	//					//				if (stmt instanceof SequentialProcess) {
	//					//					calculateProcessFoldingPositions((SequentialProcess) stmt);
	//					//				}
	//
	//				}
	//				if (lastStmt != null) {
	//					int length = getNextIOOffset(lastStmt) - firstOffset;
	//
	//					fPositions.add(new Position(firstOffset, length));
	//				}
	//			}
	//		} catch (BadLocationException e) {
	//			el.logException(e);
	//		}
	//	}
	//
	//	private void calculateFoldingPositions() {
	//		Display.getDefault().asyncExec(new Runnable() {
	//			public void run() {
	//
	//				fPositions = new ArrayList<Position>();
	//				if (dus != null) {
	//					int n = dus.size();
	//
	//					for (int i = 0; i < n; i++) {
	//
	//						try {
	//							DesignUnit du = dum.getDU(dus.get(i));
	////							if (du != null) {
	////								calculateDUFoldingPositions(du);
	////							}
	//						} catch (ZamiaException e) {
	//							el.logException(e);
	//						}
	//
	//					}
	//
	//					//					if (n > 0) {
	//					//
	//					//						try {
	//					//							DesignUnit du = dum.getDU(dus.get(0));
	//					//							DesignUnit du2 = null;
	//					//							int nextStart = document.getLength();
	//					//							if (n > 1) {
	//					//								du2 = dum.getDU(dus.get(1));
	//					//								nextStart = getIOOffset(du2);
	//					//							}
	//					//
	//					//							int start = getIOOffset(du);
	//					//							int i = 1;
	//					//							while (i < n) {
	//					//								Position pos = new Position(start, nextStart - start);
	//					//								fPositions.add(pos);
	//					//								getFoldingPositions(du, nextStart);
	//					//
	//					//								start = nextStart + 1;
	//					//								du = du2;
	//					//								i++;
	//					//
	//					//								if (i < n) {
	//					//									du2 = dum.getDU(dus.get(i));
	//					//									nextStart = getIOOffset(du2);
	//					//								} else {
	//					//									nextStart = document.getLength();
	//					//								}
	//					//							}
	//					//							Position pos = new Position(start, nextStart - start);
	//					//							fPositions.add(pos);
	//					//							getFoldingPositions(du, nextStart);
	//					//
	//					//						} catch (ZamiaException e) {
	//					//							el.logZamiaException(e);
	//					//						}
	//					//}
	//				}
	//
	//				editor.updateFoldingStructure(fPositions);
	//				editor.updateOutlinePage();
	//			}
	//		});
	//	}

	public int getNumRootElements() {
		if (fSFDUInfo == null)
			return 0;
		return fSFDUInfo.getNumDUUIDs();
	}

	public DesignUnit getRootElement(int aIdx) {
		try {
			return fDUM.getDU(fSFDUInfo.getDUUID(aIdx));
		} catch (ZamiaException e) {
			el.logException(e);
		}
		return null;
	}

	public Object[] getRootElements() {

		if (fSFDUInfo == null) {
			logger.error("ZamiaReconcilingStrategie: getRootElements() called, but not parsed yet.");

			Object[] empty = new Object[0];

			return empty;
		}

		int n = fSFDUInfo.getNumDUUIDs();

		ArrayList<DesignUnit> dua = new ArrayList<DesignUnit>(n);

		for (int i = 0; i < n; i++) {
			try {

				DesignUnit du = fDUM.getDU(fSFDUInfo.getDUUID(i));

				if (du != null) {
					dua.add(du);
				}
			} catch (ZamiaException e) {
				el.logException(e);
			}
		}

		return dua.toArray();
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}
}