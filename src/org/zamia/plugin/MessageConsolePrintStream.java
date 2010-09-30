/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 25, 2008
 */
package org.zamia.plugin;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class MessageConsolePrintStream extends PrintStream {

	private final MessageConsoleStream consoleOutput;
	private ExecutorService service;

	public MessageConsolePrintStream(MessageConsole console_) {
		super(System.out);
		consoleOutput = console_.newMessageStream();
		service = Executors.newSingleThreadExecutor();
	}

	class MyPrintJob implements Runnable {

		private String msg;
		
		public MyPrintJob(String msg_) {
			msg = msg_;
		}
		
		public void run() {
			if (!consoleOutput.isClosed()) {
				consoleOutput.print(msg);
			}
		}
		
	}
	
	public void print(String str_) {
		//consoleOutput.print(str_);
		service.execute(new MyPrintJob(str_));
	}

	public void println() {
		//consoleOutput.println();
		service.execute(new MyPrintJob("\n"));
	}

	public void println(String str_) {
		//super.println(str_);
//		print (str_);
//		consoleOutput.println();
		service.execute(new MyPrintJob(str_+"\n"));
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = off; i<(off+len); i++) {
			sb.append((char) buf[i]);
		}
		
		print(sb.toString());
	}

	@Override
	public void write(int b) {
		print (""+(char)b);
	}
}