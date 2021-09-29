package fr.mossroy.tomcat.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils.PropertySource;

/**
 * A PropertySource that allows Tomcat to read properties in an external file,
 * and inject them in conf/server.xml.
 * 
 * To use it, 2 lines have to be added in conf/catalina.properties :
 * org.apache.tomcat.util.digester.PROPERTY_SOURCE=fr.mossroy.tomcat.tools.ExternalPropertySource
 * fr.mossroy.tomcat.tools.ExternalPropertySource.file=path/to/externalproperties.properties
 * 
 * A common use-case is to externalize the password of jdbc datasources, so that
 * they do not appear in the tomcat tree. It's useful when this tomcat tree is
 * readable by people that should not know the jdbc password.
 * 
 * See http://blog.mossroy.fr/2015/11/07/securiser-les-mots-de-passe-jdbc-du-server-xml-dun-tomcat/
 * 
 * License LGPL v3
 * 
 * @author Mossroy
 */
public class ExternalPropertySource implements PropertySource {
    
    private static final String CATALINA_PROPERTIES = "conf/catalina.properties";
    private static final String CATALINA_PROPERTIES_FILE_PROPERTY = "fr.mossroy.tomcat.tools.ExternalPropertySource.file";
    private static final Log LOGGER = LogFactory.getLog(ExternalPropertySource.class);
    private static final Pattern regExp = Pattern.compile("\\$\\{([^\\}]*)\\}");
    private Properties externalProperties;
    
    public ExternalPropertySource() {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            File catalinaPropertiesFile = new File(catalinaBase, CATALINA_PROPERTIES);
            if (!catalinaPropertiesFile.exists()) {
                throw new IOException("Unable to find the file " + CATALINA_PROPERTIES + " in CATALINA_BASE (" + catalinaBase + ")");
            }
            FileInputStream catalinaFileInputStream = new FileInputStream(catalinaPropertiesFile);
            Properties catalinaProperties = new Properties();
            catalinaProperties.load(catalinaFileInputStream);
            String externalPropertiesFile = catalinaProperties.getProperty(CATALINA_PROPERTIES_FILE_PROPERTY);
            if (externalPropertiesFile == null || externalPropertiesFile.isEmpty()) {
                throw new IOException("The external property file location is not set in " + CATALINA_PROPERTIES + " (expected value for " + CATALINA_PROPERTIES_FILE_PROPERTY + ")");
            }
            
            // Replace the Java variables
            Matcher m = regExp.matcher(externalPropertiesFile);
            StringBuffer externalPropertiesFileBuffer = new StringBuffer();
            while (m.find()) {
                if (System.getProperty(m.group(1)) == null) {
                    // unexistant propertie, no replacement
                } else {
                    m.appendReplacement(externalPropertiesFileBuffer, System.getProperty(m.group(1)));
                }
            }
            m.appendTail(externalPropertiesFileBuffer);
            
            FileInputStream fileInputStream = new FileInputStream(externalPropertiesFileBuffer.toString());
            externalProperties = new Properties();
            externalProperties.load(fileInputStream);
        } catch (IOException e) {
            LOGGER.fatal("Unable to read the external property file", e);
            externalProperties = null;
        }
    }

    @Override
    public String getProperty(String string) {
        if (externalProperties != null)  {
            return externalProperties.getProperty(string);
        } else {
            // If the property is not found, we return null (and Tomcat will leave the ${propertyname} )
            // NB : Tomcat uses this PropertySource for each XML file it parses (see IntrospectionUtils source code), not only on server.xml
            return null;
        }
    }
    
}
