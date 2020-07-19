package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MyFileDialog {

	public static String openDialog(Shell shell) {	
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
        fd.setText("Save");
        fd.setFilterPath("C:/");
        String[] filterExt = { "*.bpmn", "*.xml" };
        fd.setFilterExtensions(filterExt);
        String selected = fd.open();
        System.out.println(selected);
		return  selected;
	}
}