package cz.incad.xsl;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Administrator
 */
public class ResourceBundleService {

    public static ResourceBundle getBundle(String bundle, String locale) {
        return ResourceBundle.getBundle(bundle, new Locale(locale), ResourceBundleService.class.getClassLoader());
    }

    public static String getString(ResourceBundle bundle, String key) {
        if(bundle.containsKey(key)){
            return bundle.getString(key);
        }
        return key;
    }

    public static String getString(ResourceBundle bundle, String prefix, String key) {
        if(bundle.containsKey(prefix + "." +key)){
            return bundle.getString(prefix + "." + key);
        }
        return key;
    }
}
