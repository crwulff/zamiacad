/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 22, 2008
 */
package org.zamia.plugin.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.zamia.ASTNode;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.analysis.SourceLocation2IG;
import org.zamia.analysis.ast.ASTDeclarationSearch;
import org.zamia.analysis.ast.ASTReferencesSearch;
import org.zamia.analysis.ig.IGReferencesSearch;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.util.Pair;
import org.zamia.vhdl.ast.DeclarativeItem;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ReferencesSearchQuery implements ISearchQuery {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private ZamiaSearchResult fSearchResult;

	private boolean fSearchUpward, fSearchDownward, fDeclOnly;

	private boolean fUsePath;

	private ZamiaProject fZPrj;

	private ToplevelPath fTLP;

	private SourceLocation fLocation;

	private boolean fWritersOnly, fReadersOnly;

	public ReferencesSearchQuery(ZamiaProject aZPrj, ToplevelPath aTLP, SourceLocation aLocation, boolean aSearchUpward, boolean aSearchDownward, boolean aDeclOnly, boolean aUsePath, boolean aWritersOnly, boolean aReadersOnly) {

		fZPrj = aZPrj;
		fTLP = aTLP;
		fLocation = aLocation;
		fSearchUpward = aSearchUpward;
		fSearchDownward = aSearchDownward;
		fDeclOnly = aDeclOnly;
		fUsePath = aUsePath;
		fWritersOnly = aWritersOnly;
		fReadersOnly = aReadersOnly;
	}

	public ReferencesSearchQuery(StaticAnalysisAction aSAA, boolean aSearchUpward, boolean aSearchDownward, boolean aDeclOnly, boolean aUsePath, boolean aWritersOnly, boolean aReadersOnly) {
		this(aSAA.getZamiaProject(), aSAA.getPath(), aSAA.getLocation(), aSearchUpward, aSearchDownward, aDeclOnly, aUsePath, aWritersOnly, aReadersOnly);
	}

	public boolean isSearchUpward() {
		return fSearchUpward;
	}

	public boolean isSearchDownward() {
		return fSearchDownward;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public String getLabel() {
		return "Searching for references...";
	}

	public ISearchResult getSearchResult() {
		if (fSearchResult == null)
			fSearchResult = new ZamiaSearchResult(this);
		return fSearchResult;
	}

	public IStatus run(IProgressMonitor aMonitor) throws OperationCanceledException {

		final AbstractTextSearchResult result = (AbstractTextSearchResult) getSearchResult();
		result.removeAll();

		try {

			DeclarativeItem declaration = null;
			ZamiaProject zprj = fZPrj;
			ToplevelPath tlp = fTLP;

			if (fUsePath && tlp != null) {

				Pair<IGItem, ToplevelPath> nearest = SourceLocation2IG.findNearestItem(fLocation, tlp, zprj);

				if (nearest != null) {

					IGItem item = nearest.getFirst();
					ToplevelPath path = nearest.getSecond();

					logger.info("ReferencesSearchQuery: nearest item: %s, path: %s", item, path);

					if (item != null) {

						IGReferencesSearch rs = new IGReferencesSearch(zprj);

						IGObject object = null;

						if (item instanceof IGOperationObject) {
							object = ((IGOperationObject) item).getObject();
						} else if (item instanceof IGObject) {
							object = (IGObject) item;
						}
						if (object != null) {
							ReferenceSearchResult rsr = rs.search(object, path, fSearchUpward, fSearchDownward, fWritersOnly, fReadersOnly);

							if (rsr != null) {
								addMatches(result, rsr);
							} else {
								ZamiaPlugin.showError(null, "IG-based reference search failed", "Search returned no result.", "");
							}
						} else {
							ZamiaPlugin.showError(null, "IG-based reference search failed", "Failed to map cursor location to IG Object", "Mapped to non-object " + item);
						}
					} else {
						ZamiaPlugin.showError(null, "IG-based reference search failed", "Failed to map cursor location to IG Object", "Mapped to no IG item at all");
					}
				} else {
					ZamiaPlugin.showError(null, "IG-based reference search failed", "Failed to map cursor location to IG Object", "Mapped to no IG item at all");
				}

			} else {

				/*
				 * AST based reference search in case we do not have path
				 * information or the user requested ir
				 */

				ASTNode nearest = SourceLocation2AST.findNearestASTNode(fLocation, true, zprj);

				if (nearest != null) {
					declaration = ASTDeclarationSearch.search(nearest, zprj);

					if (declaration != null) {

						ReferenceSearchResult results = ASTReferencesSearch.search(declaration, fSearchUpward, fSearchDownward, zprj);

						if (fDeclOnly) {

							ReferenceSearchResult filteredResults = new ReferenceSearchResult("Initial Signal Declarations of " + declaration, declaration.getLocation(), declaration.toString().length(), zprj);

							int n = results.getNumChildren();

							for (int i = 0; i < n; i++) {

								ReferenceSearchResult res = results.getChild(i);

								if (!(res instanceof ReferenceSite))
									continue;

								ReferenceSite ref = (ReferenceSite) res;

								if (ref.getRefType() == RefType.Declaration) {
									filteredResults.add(ref);
								}
							}

							addMatches(result, filteredResults);

						} else {
							addMatches(result, results);

							// int n = results.size();
							//
							// for (int i = 0; i < n; i++) {
							//
							// ReferenceSearchResult res = results.get(i);
							//
							// addMatches(result, res);
							// }
						}
					} else {
						ZamiaPlugin.showError(null, "AST-based reference search failed", "Reference search failed.", "Failed to find declaration of " + nearest);
					}
				} else {
					ZamiaPlugin.showError(null, "AST-based reference search failed", "Failed to map cursor location " + fLocation + " to and AST object", "");
				}
			}
		} catch (Throwable e) {
			el.logException(e);
			ZamiaPlugin.showError(null, "Exception caught while executing reference search", "Caught an unexpected exception during reference search", "" + e);
		}

		aMonitor.done();
		return Status.OK_STATUS;
	}

	private void addMatches(AbstractTextSearchResult aResult, ReferenceSearchResult aRSR) {

		aResult.addMatch(new Match(aRSR, 0, 1));

		int n = aRSR.getNumChildren();
		for (int i = 0; i < n; i++) {
			ReferenceSearchResult child = aRSR.getChild(i);
			addMatches(aResult, child);
		}
	}
}