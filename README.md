# tomcat-tools
Tools for Apache Tomcat

## ExternalPropertySource
It's a PropertySource (https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/tomcat/util/IntrospectionUtils.PropertySource.html) that allows Tomcat to read properties in an external file, and inject them in conf/server.xml.

A common use-case is to externalize the password of jdbc datasources, so that they do not appear in the tomcat tree. It's useful when this tomcat tree is readable by people that should not know the jdbc password.

The server.xml looks like :

    <Resource name="jdbc/myDataSource"
                      type="javax.sql.DataSource"
                      factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
                      auth="Container"
                      username="xxx"
                      password="${com.mycompany.package.mydatasource.jdbcpassword}"
                      driverClassName="..."
                      url="..."
                      ...
    />

and the externalproperties.properties looks like :

    com.mycompany.package.mydatasource.jdbcpassword=my_top_secret_password

To use it, 2 lines have to be added at the end of conf/catalina.properties :

    org.apache.tomcat.util.digester.PROPERTY_SOURCE=fr.mossroy.tomcat.tools.ExternalPropertySource
    fr.mossroy.tomcat.tools.ExternalPropertySource.file=path/to/externalproperties.properties

and you need to add the generated jar in the lib folder of tomcat.


Tested with Tomcat 7.0.53. Should work with any Tomcat >=7.

License LGPL v3
