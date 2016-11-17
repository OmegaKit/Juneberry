
# Setting Up Juneberry Servlet

### Step 0: Copy The Required Files
- Assumptions:
  - There is a user called "doe" on host "mybox" running Linux OS
  - The home directory is `/home/doe`
  - The user "doe" has a data directory called /home/doe/data that contains three (3) collections of mission imagery files:
    - `/home/doe/data/set0`
    - `/home/doe/data/set1`
    - `/home/doe/data/set2`
- Download and unzip this Juneberry directory
  - `$ unzip Juneberry-master.zip`
- Set up an Apache Tomcat servlet directory
- Copy "crossdomain.xml"
- Download and unzip the Juneberry-lib directory from https://github.com/OmegaKit/Juneberry-lib

### Step 1: Build the .war File
- Go into the Juneberry directory and enter the `ant war` command. This will automatically run the build.xml file. We need this war file to run on Apache tomcat.
  - `$ cd /home/doe/Juneberry-master/`
  - `$ ant war`

### Step 2: Setting Up The Tomcat Servlet Engine
- Decide on a port that Tomcat should be listening to, e.g., 8080, then unpack apache-tomcat-7.0.41.tar.gz to its own location:
  - `$ mkdir -p /home/doe/host/mybox/8080`
  - `$ cd /home/doe/host/mybox/8080`
  - `$ tar zxvf /home/doe/host/mybox/pkg/apache-tomcat-7.0.41.tar.gz`
- Add a 'Juneberry' directory to the 'webapps' folder that exists within the newer apache-server-folder
  - `$ cd ./apache-tomcat-7.0.41/webapps`
  - `$ mkdir ./juneberry`
- Uncompress the war file into the newly created juneberry folder
  - `$ cd ./juneberry`
  - `$ jar xvf /home/doe/Juneberry-master/juneberry-0.10.0r1.war`

### Step 3: Configure the Tomcat Server
- Go to the top of our Tomcat server dir and open the server.xml file so that we may configure it.
  - `$ cd /home/doe/host/mybox/8080/apache-tomcat-7.0.41/`
  - `$ vim ./conf/server.xml`
  - Do these steps:
    - Inside the `<Host>...</Host>` element for localhost, add
      - `<Context path="/juneberry" allowLinking="true" privileged="true"/>`
    - In the same area, change the `<Valve../>` element to
      - `<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
	 prefix="localhost_access_log." suffix=".txt" pattern="combined" resolveHosts="false"/>`
    - This provides nice logging in the style of apache httpd
- Now that we configured server.xml, now we need to configure tomcat-users.xml
  - `$ vim ./conf/tomcat-users.xml`
  - Inside `<tomcat-users>...</tomcat-users>` element, add
    - `<role rolename="demo"/>
      <user username="demo" password="demo" roles="demo"/>`
  - This is to set up a demo user for testing purpose only
  - It uses Tomcat's MemoryRealm, a simple demonstration implementation of the Tomcat 6 Realm interface.
  - Warning: in a production system, other types of realms must be used.

### Step 4: Copy Icons
- Copy Apache http icons to Tomcat's ROOT webapp
  - `$ cd ./webapps/ROOT`
  - `$ cp -avi /var/www/icons`
- This is to provide icons for juneberry web GUI

### Step 5: Setup Juneberry
- Copy crossdomain.xml from home folder into our apache-server-folders webapps/ROOT
  - `$ cd ./webapps/ROOT`
  - `$ cp -avi /home/doe/host/mybox/pkg/crossdomain.xml .`
- This is to make Cooliris happy. It is a 3rd party image wall application used by Juneberry.
- NOW WE POINT JUNEBERRY TO THE DATA!
  - First, we need to do some configuration to the web.xml file. We need to make 2 changes.
    - `$ cd /home/doe/host/mybox/8080/apache-tomcat-7.0.41/`
    - `$ vim ./webapps/juneberry/WEB-INF/web.xml`
    - Change 1:
      - Look for the element
        `<servlet> <!-- w10n of imagery -->
	...
	</servlet>`
      - Change the following inside of this servlet element:
		  `<init-param>
		    <description>
		    Root path of fs dir store.
		    Default is /var/www/html.
		    </description>
		    <param-name>fs_dir_store_root</param-name>
		    <param-value>/var/www/html</param-value>
		  </init-param>
		  <init-param>
		    <description>
		    Comma-separated names for entries under fs_dir_store_root
		    that are to be soft-linked.
		    Ignored if w10n_of_fs_dir_not_by_cgi_servlet is true.
		    Default is "".
		    </description>
		    <param-name>entry_list</param-name>
		    <param-value></param-value>
		  </init-param>`
		  
		  to
		  
		  `<init-param>
		    <description>
		    Root path of fs dir store.
		    Default is /var/www/html.
		    </description>
		    <param-name>fs_dir_store_root</param-name>
		    <param-value>/home/doe/data</param-value>
		  </init-param>
		  <init-param>
		    <description>
		    Comma-separated names for entries under fs_dir_store_root
		    that are to be soft-linked.
		    Ignored if w10n_of_fs_dir_not_by_cgi_servlet is true.
		    Default is "".
		    </description>
		    <param-name>entry_list</param-name>
		    <param-value>set0, set1, set2</param-value>
		  </init-param>`

    - Change 2: In the same file!
      - Add the following:
        `<filter>
	  <filter-name>CorsFilter</filter-name>
	  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
	</filter>
	<filter-mapping>
	  <filter-name>CorsFilter</filter-name>
	  <url-pattern>/*</url-pattern>
	</filter-mapping>`
      - This enables CORS
      
- Now change the port if needed (i.e. if you want something besides 8080)
- Go into /apache-tomcat-7.0.41/conf/server.xml
  - `$ vim ./conf/server.xml`
  - Find the element `<Service name="Catalina">`
  - Inside of this element, find the element `<Connector.../>` and change the port to the number you would like
  - `<Connector port="3333"...` (70,22)
