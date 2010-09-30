/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.rtl;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.zamia.plugin.ZamiaPlugin;


/**
 * 
 * @author guenter bartsch
 *
 */
public class ZoomWidget extends Composite {

	private ArrayList<ZoomObserver> observers;
	private Text text;
	private double min, max;

	public ZoomWidget(
		Composite parent_,
		double min_,
		double max_,
		double current_) {
		super(parent_, SWT.NONE);

		min = min_;
		max = max_;

//		Display display = getDisplay();
		
		observers = new ArrayList<ZoomObserver>();

		GridLayout gl = new GridLayout();
		setLayout(gl);
		gl.numColumns = 4;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		ToolBar tb = new ToolBar(this, SWT.FLAT);

		ToolItem ti = new ToolItem(tb, SWT.NONE);
		Image icon = ZamiaPlugin.getImage("/share/images/zoomin.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom in");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				double f = getFactor() * 1.5;
				if (f < max)
					setFactor(f);
				else
					setFactor(max);
			}
		});

		GridData gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		tb.setLayoutData(gd);

		text = new Text(this, SWT.BORDER);
		setFactor(current_);
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.character) {
					case SWT.CR :
						notifyObservers();
						break;
				}
			}
		});
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		text.setLayoutData(gd);

		tb = new ToolBar(this, SWT.FLAT);

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoomout.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom out");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				double f = getFactor() / 1.5;
				if (f >= min)
					setFactor(f);
				else
					setFactor(min);
			}
		});
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		tb.setLayoutData(gd);

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoom100.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom 100%");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setFactor(1.0);
			}
		});
	}

	public void addZoomObserver(ZoomObserver o) {
		observers.add(o);
	}

	public void notifyObservers() {
		double f = getFactor();
		for (Iterator<ZoomObserver> i = observers.iterator(); i.hasNext();) {
			ZoomObserver o = i.next();
			o.updateZoom(f);
		}
	}
	public void setFactor(double f_) {
		setFactor(f_, true);
	}

	public void setFactor(double f_, boolean doNotify_) {
		text.setText(Integer.toString((int) (f_*100.0)) + "%");
		if (doNotify_)
			notifyObservers();
	}

	public double getFactor() {
		try {
			String s = text.getText().replace('%', ' ');
			return Double.valueOf(s).doubleValue() / 100.0;
		} catch (NumberFormatException e) {
			text.setText("100%");
		}
		return 1.0;
	}
}
