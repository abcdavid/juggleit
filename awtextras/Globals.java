package awtextras;

import java.util.*;

/** even simpler than a resource bundle for ease of use
// on launching application these defaults are set using a resource bundle (for example)
// rather than having to specify a particular resource bundle for every class
*/

public class Globals {
	public static String OK="ok";
	public static String CANCEL="cancel";
	public static String YES="yes";
	public static String NO="no";
	public static String OPEN="open";
	public static String SAVE="save";
	
	static {
		setDefault(OK,"OK");
		setDefault(CANCEL,"Cancel");
		setDefault(YES,"Yes");
		setDefault(NO,"No");
		setDefault(OPEN,"Open");
		setDefault(SAVE,"Save");
	}
	
	private static Hashtable hashtable;
	
	public static String getString(String key) {
		Object o=hashtable.get(key);
		if (o!=null) return o.toString();
		System.err.println("No value defined for key="+key);
		return "";
	}
	public static void setDefault(String key,String value) {
		if (hashtable==null) hashtable=new Hashtable();
		hashtable.put(key,value);
	}
	public static void updateDefaults(ResourceBundle bundle) {
		// just use keys specified here
		Iterator it=hashtable.keySet().iterator();
		while (it.hasNext()) {
			String key=(String)it.next();
			try {
				String value=bundle.getString(key);
				setDefault(key,value);
			} catch (MissingResourceException e) {}
		}
	}
}
