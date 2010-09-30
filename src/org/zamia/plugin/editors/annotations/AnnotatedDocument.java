/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 2, 2009
 */
package org.zamia.plugin.editors.annotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.ITextStore;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.annotations.IGSimAnnotation;
import org.zamia.plugin.views.sim.SimulatorView;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;


/**
 * Based on IBM's DocumentClone
 * 
 * @author Guenter Bartsch
 * 
 */

public class AnnotatedDocument extends AbstractDocument {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static class StringTextStore implements ITextStore {

		private String fContent;

		public StringTextStore(String content) {
			Assert.isNotNull(content);
			fContent = content;
		}

		public char get(int offset) {
			return fContent.charAt(offset);
		}

		public String get(int offset, int length) {
			return fContent.substring(offset, offset + length);
		}

		public int getLength() {
			return fContent.length();
		}

		public void replace(int offset, int length, String text) {
		}

		public void set(String text) {
		}

	}

	private HashMap<Integer, SortedSet<Annotation>> fAnnotations;

	private final int fTabWidth;
	
	private final SimulatorView fSimView;

	private HashSetArray<IGSimAnnotation> fAnns;

	public AnnotatedDocument(String aContent, HashSetArray<IGSimAnnotation> aAnns, int aTabWidth, SimulatorView aSimView) {
		super();

		fAnns = aAnns;
		fTabWidth = aTabWidth;
		fSimView = aSimView;

		computeAnnotations();

		String content = annotateSource(aContent);

		setTextStore(new StringTextStore(content));
		ConfigurableLineTracker tracker = new ConfigurableLineTracker(new String[] { "\n", "\r" });
		setLineTracker(tracker);
		getTracker().set(content);
		completeInitialization();
	}

	private void computeAnnotations() {

		fAnnotations = new HashMap<Integer, SortedSet<Annotation>>();

		int n = fAnns.size();
		for (int i = 0; i < n; i++) {

			IGSimAnnotation ann = fAnns.get(i);

			PathName path = ann.getPath();
			
			IGStaticValue value = ann.getValue();
			
			String annotation = fSimView.formatSignalValue(path, value);
			
			addAnnotation(ann.getLocation(), annotation);
		}
	}

	private void addAnnotation(SourceLocation aLocation, String aV) {

		int line = aLocation.fLine;

		SortedSet<Annotation> al = fAnnotations.get(line);
		if (al == null) {
			al = new TreeSet<Annotation>();
			fAnnotations.put(line, al);
		}

		Annotation a = new Annotation(aLocation.fCol, "[" + aV + "]");

		al.add(a);
	}

	private String annotateSource(String aSource) {

		StringBuilder buf = new StringBuilder();

		try {

			BufferedReader br = new BufferedReader(new StringReader(aSource));

			int l = 0;
			while (true) {
				String line = br.readLine();
				l++;
				if (line == null) {
					break;
				}

				SortedSet<Annotation> al = fAnnotations.get(l);

				if (al == null) {
					buf.append("\n");
					buf.append(line);
					//buf.append("-- no annotations for line "+l+"\n");
					buf.append("\n");
					continue;
				}

				Iterator<Annotation> it = al.iterator();
				Annotation an = it.next();
				int len = line.length();

				StringBuilder bufA = new StringBuilder("-- ");
				StringBuilder bufB = new StringBuilder();

				for (int i = 0; i < len; i++) {

					int col = i + 1;

					if (an != null && col == an.getOffset()) {

						while (bufA.length() > bufB.length()) {
							bufB.append(" ");
						}
						while (bufA.length() < bufB.length()) {
							bufA.append(" ");
						}

						bufA.append(an.getStr());

						if (it.hasNext()) {
							an = it.next();
						} else {
							an = null;
						}
					}

					char c = line.charAt(i);
					if (c == '\t') {
						for (int j = 0; j < fTabWidth; j++) {
							bufB.append(" ");
						}
					} else {
						bufB.append(c);
					}
				}

				buf.append(bufA + "\n");
				buf.append(bufB + "\n");
			}

		} catch (IOException e) {
			el.logException(e);
		}

		return buf.toString();
	}

}
