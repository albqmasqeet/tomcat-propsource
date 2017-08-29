package com.foo.bar.secure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils.PropertySource;
import com.foo.bar.secure.CryptoHelper;

public class ExternalPropertySource implements PropertySource {
	
	private static final String CATALINA_PROPERTIES = "conf/catalina.properties";
    private static final String CATALINA_PROPERTIES_FILE_PROPERTY = "com.foo.bar.secure.properties.file";
    private static final Log LOGGER = LogFactory.getLog(ExternalPropertySource.class);
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
            FileInputStream fileInputStream = new FileInputStream(externalPropertiesFile);
            externalProperties = new Properties();
            externalProperties.load(fileInputStream);
        } catch (IOException e) {
            LOGGER.fatal("Unable to read the external property file", e);
            externalProperties = null;
        }
    }

    @Override
    public String getProperty(String propName) {
        if (externalProperties != null)  {
            try {
				return new CryptoHelper().decrypt(externalProperties.getProperty(propName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
        
        // If the property is not found, we return null (and Tomcat will leave the ${propertyname} )
        // NB : Tomcat uses this PropertySource for each XML file it parses (see IntrospectionUtils source code), not only on server.xml
        return null;       
    }
}