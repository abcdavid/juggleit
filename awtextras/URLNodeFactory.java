package awtextras;

import java.io.*;
import java.net.*;

public class URLNodeFactory extends BaseFileNodeFactory {
	URL documentBase;
	String nameParam="name";
	String dataParam="data";
	String cgiScript="io.cgi";

	class PostDataWriter extends StringWriter {
		String path;
		URLConnection connection;
		PostDataWriter(URLConnection connection,String path) throws IOException {
			this.path=path;
			this.connection=connection;
			connection.setDoOutput(true);
		}
		public void close() throws IOException {
			super.close();
			try {
				BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				writer.write(nameParam+"="+path+"&data=");
				writer.write(URLEncoder.encode(toString()));
				writer.flush();
				writer.close();
				connection.getContent();
			} catch (IOException e) {
				throw new IOException("Cannot write "+path);
			}
		}
	}

	public URLNodeFactory(URL documentBase) {
		this.documentBase=documentBase;
	}
	public Reader getReader(FileNode file) throws IOException,SecurityException {
		try {
			URL url=new URL(documentBase,cgiScript+"?"+nameParam+"="+URLEncoder.encode(getPath(file)));
			InputStream input=url.openConnection().getInputStream();
			return new InputStreamReader(input);
		} catch (MalformedURLException e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}
	protected String getPath(FileNode file) {
		if (file.isRoot()) return file.getName();
		return getPath(file.getParent())+"/"+file.getName();
	}
	public Writer getWriter(FileNode file) throws IOException,SecurityException {
		try {
			URL url=new URL(documentBase,cgiScript);
			return new PostDataWriter(url.openConnection(),getPath(file));
		} catch (MalformedURLException e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}
}
