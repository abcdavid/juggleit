import java.applet.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

import awtextras.*;

public class LaunchApplet extends Applet {

	JugglingApp app;
	
	synchronized void showJugglingApp(FileNode fileNode) {
		if (app==null) {
			app=new JugglingApp(fileNode,false);
		}
		app.setVisible(true);
	}
	public void init() {
		String dataInfo=getParameter("datainfo");
		if (dataInfo!=null) {
			try {
				URL url=new URL(getDocumentBase(),dataInfo);
				URLNodeFactory factory=new URLNodeFactory(getDocumentBase());
				FileNode dataDir=FileTreeParser.buildFileTree(factory,url.openConnection().getInputStream());
				showJugglingApp(dataDir);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		} else {
			System.err.println("datainfo not specified");
		}
	}
	public void destroy() {
		if (app!=null) {
			// doesn't prompt to save or confirm exit
			app.doExit();
			app=null;
		}
	}
}
