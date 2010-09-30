/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 5, 2010
 */
package org.zamia.util.ehm;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.LLFSHashMap;
import org.zamia.util.ZStack;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class EHMIterator {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private final ExtendibleHashMap fEHM;

	private final ZStack<BreadCrump> fStack;

	private final EHMPageManager fManager;

	class BreadCrump {

		private EHMNode fNode;

		public int fIdx = 0;

		public BreadCrump(EHMNode aNode) {
			fNode = aNode;
		}

		public EHMNode getNode() {
			return fNode;
		}

	}

	private boolean fExhausted = false;

	private long fNext = 0l;

	private final boolean fKeys;

	EHMIterator(ExtendibleHashMap aEHM, boolean aKeys) {
		fEHM = aEHM;
		fManager = fEHM.getManager();
		fKeys = aKeys;

		fStack = new ZStack<BreadCrump>();
		fStack.push(new BreadCrump(fEHM.getRootNode()));

		next();
	}

	public boolean hasNext() {
		return !fExhausted;
	}

	public long next() {

		long res = fNext;

		if (fExhausted) {
			logger.error("EHMKeyIterator: next() called on exhausted index!");
			return res;
		}

		while (true) {

			if (fStack.isEmpty()) {
				fExhausted = true;
				return res;
			}

			BreadCrump bc = fStack.pop();

			EHMNode node = bc.getNode();

			while (node.getId() == -1) {

				fStack.push(new BreadCrump(node.getOneNode()));

				node = node.getZeroNode();
				bc = new BreadCrump(node);
			}

			LLFSHashMap page = fManager.load(node.getId());

			int n = page.getAllocedSize();

			while (bc.fIdx < n && page.getFree(bc.fIdx)) {
				bc.fIdx++;
			}

			if (bc.fIdx < n) {

				fNext = fKeys ? page.getKey(bc.fIdx) : page.getValue(bc.fIdx);
				bc.fIdx++;

				fStack.push(bc);
				return res;
			}
		}
	}
}
