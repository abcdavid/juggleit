import awtextras.*;
import java.io.File;

class Main {
	public static void main(String[] args) {
		File dir=new File("."); // current directory
		if (args.length>0) {
			File folder=new File(args[0]);
			if (folder.exists() && folder.isDirectory())
				dir=folder;
		}
		FileNode fileTree=new FileNodeImp(dir);
		JugglingApp app=new JugglingApp(fileTree,true);
		app.setVisible(true);
	}
}
