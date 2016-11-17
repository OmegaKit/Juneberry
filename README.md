
# Setting Up Juneberry Servlet

### Step 0: Copy The Required Files
- Copy the entire Junebery into your home directory
- Find and copy the Apache tomcat tarball
- Copy "crossdomain.xml"
- Untar the Juneberry library
  - `tar -xvf 'juneberry-lib.dir.full-0.10.0.tar.gz.0'`

### Step 1: Build the .war File
- Go into the Juneberry directory and enter the `ant war` command. This will automatically run the build.xml file. We need this war file to run on Apache tomcat.

### Step 2: Setting Up The Tomcat Servlet Engine
- Create a directory folder where you will keep the all of the servlet files.
- Go into that directory and `tar zxcf 'pathtoservertarball'` Our goal is to unzip the server file (apache-tomcat-7.0.41.tar.gz) into this directory.
- Add a 'Juneberry' directory to the 'webapps' folder that exists within the newer apache-server-folder
- Uncompress the war file into the newly created juneberry folder
  - `jar xvf '~/4zac/juneberry-0.10.0/build/juneberry-0.10.0.war'`

### Step 3: Configure the Tomcat Server
- Go into our newly Tomcat server folder and open the server.xml file so that we may configure it.
  - `vim ./apache-tomcat-7.0.41/conf/server.xml`
  - Do these steps:
    - `<Context path="/juneberry" allowLinking="true" privileged="true"/>` (add this)
    - Change the similar one to this...
      - `	<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
	               prefix="localhost_access_log." suffix=".txt" pattern="combined" resolveHosts="false"/>`
- Now that we configured server.xml, now we need to configure tomcat-user.xml
  - `		a. Vim ./apache-tomcat-7.0.41/conf/tomcat-users.xml
		Inside <tomcat-users>...</tomcat-users> element, add
		  <role rolename="demo"/>
  <user username="demo" password="demo" roles="demo"/>`

### Step 4: Copy Icons
- Copy Apache http icons to Tomcat's ROOT webapp
  - `	$ cd ./apache-tomcat-7.0.41/webapps/ROOT
$ cp -avi /var/www/icons`

### Step 5: Setup Juneberry
- Copy crossdomain.xml from home folder into our apache-server-folders webapps/ROOT
- NOW WE POINT JUNEBERRY TO THE DATA!
  - First, we `./apache-tomcat-7.0.41/webapps/juneberry/WEB-INF/web.xml` so that we can do some configuration to the web.xml file. We need to make 2 changes.
    - Change 1: change `<servlet> w10n file system directory` to `"fs_dir_store_root"<param-value>`. Our desired data path.
    - Need to do that in both `<servlet>` areas. THis is how we point to the data!
    - Change 2: In the same file!
      - `		<filter>
		  <filter-name>CorsFilter</filter-name>
		  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
		</filter>
		<filter-mapping>
		  <filter-name>CorsFilter</filter-name>
		  <url-pattern>/*</url-pattern>
</filter-mapping>`
- Now change the port! Go into /apache-tomcat-7.0.41/conf/server.xml
  - `<connector port="3333"...` (70,25)
