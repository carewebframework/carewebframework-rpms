# RPMS Adapter for CareWeb Framework
This project allows the CareWeb Framework to run on top of the Indian Health Service [Resource and Patient Management System (RPMS)](http://www.ihs.gov/rpms/).  It is pre-configured to run on the  [FOIA GOLDB database](http://sourceforge.net/projects/foiarpms/)  (not provided), but can be run on other RPMS distros as well.

The project includes a Java port of the Medsphere RPC broker and integrates broker-based security with
the Spring Security framework.

The project also includes several clinical plugins, some fully functional while others are in the early
stages of development.

Installation:

1. Using KIDS, install the provided build, <b>cwf.kid</b>.
2. Using FileMan, create an entry in the <b>CIA LISTENER</b> file, specifying the desired broker port (the application is pre-configured to run on port 9300).
3. At the M command line, execute <b>D STARTALL^CIANBLIS</b> to start the broker on the port you just specified.
4. If TaskMan is not running, it may be started from the M command line by entering <b>D ^ZTMB</b>.

Running:

1. Using Maven, build and install the separate <b>carewebframework-core</b> and <b>carewebframework-icons</b> projects.
2. In the <b>carewebframework-rpms</b> project under the <b>gov.ihs.cwf.web.impl.goldb</b> artifact, edit the <b>cwf.properties</b> file to reflect your broker and user authentication settings.
3. Using Maven, build the <b>carewebframework-rpms</b> project.  A war file will be created under the <b>target</b> folder of the <b>gov.ihs.cwf.web.impl.goldb</b> artifact.
4. Deploy and run the created war file using Tomcat or other servlet container.
