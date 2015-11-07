package fr.mossroy.tomcat.tools;

import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Unit tests for ExternalPropertySource.
 * 
 * License LGPL v3
 * 
 * @author Mossroy
 */
public class ExternalPropertySourceTest {
    
    private static final String PASSWORD_THAT_SHOULD_BE_FOUND = "my_top_secret_password";
    private static final String PROPERTY = "com.mycompany.package.mydatasource.jdbcpassword";
    
    /**
     * Checks the normal case : the property is found in the external file
     * @throws Exception
     */
    @Test
    public void should_find_external_property() throws Exception {
        // Let's find the absolute path where the test files are stored,
        // and set the cataline.base property to this path
        URL testEncryptionKeyFile = Thread.currentThread().getContextClassLoader().getResource("externalproperties.properties");
        String absolutePathDir = new File(testEncryptionKeyFile.toURI()).getParentFile().getAbsolutePath();
        System.setProperty("catalina.base", absolutePathDir);
        
        // Instanciante the ExternalPropertySource like Tomcat would
        ExternalPropertySource externalPropertySource = new ExternalPropertySource();
        // Read a property like Tomcat would when it finds a ${property} in conf/server.xml
        String property = externalPropertySource.getProperty(PROPERTY);

        // Check that the value found matches the expected password
        assertEquals(PASSWORD_THAT_SHOULD_BE_FOUND, property);
    }
    
    /**
     * Checks an error : the catalina.properties file is not found
     * @throws Exception 
     */
    @Test
    public void should_return_null_if_external_property_file_not_found() throws Exception {
        // Let's give a wrong catalina_base property
        System.setProperty("catalina.base", "zzz");
        
        ExternalPropertySource externalPropertySource = new ExternalPropertySource();
        String property = externalPropertySource.getProperty(PROPERTY);

        // The property should not be found, and return a null value
        assertNull(property);
    }
    
    /**
     * Checks an error : the external file is found, but the property is not in it
     * @throws Exception 
     */
    @Test
    public void should_return_null_if_external_property_not_found() throws Exception {
        URL testEncryptionKeyFile = Thread.currentThread().getContextClassLoader().getResource("externalproperties.properties");
        String absolutePathDir = new File(testEncryptionKeyFile.toURI()).getParentFile().getAbsolutePath();
        System.setProperty("catalina.base", absolutePathDir);
        
        ExternalPropertySource externalPropertySource = new ExternalPropertySource();
        // Read a property that does not exist in the external property file
        String property = externalPropertySource.getProperty("zzz");

        // It should return a null value
        assertNull(property);
    }
    
}
