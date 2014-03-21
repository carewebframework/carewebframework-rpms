# RPMS Adapter for CareWeb Framework
This project allows the CareWeb Framework to run on top of the Indian Health Service [Resource and Patient Management System (RPMS)](http://www.ihs.gov/rpms/).  It is pre-configured to run on the  [FOIA GOLDB database](http://sourceforge.net/projects/foiarpms/)  (not provided), but can be run on other RPMS distros as well.

The project builds upon the [VistA adapter](https://github.com/carewebframework/carewebframework-vista) and contains
additional plugins specific to the RPMS environment.  The ultimate goal is to produce plugins that run in both environments.

There are several clinical plugins in this project, some fully functional while others are in the early
stages of development.

Installation:

1. Using KIDS, install the provided build <b>kids/cwf-rpms-1.0.kid</b>.
2. At the M command line, execute <b>D STARTALL^CIANBLIS</b> to start the broker on port 9300.
3. If TaskMan is not running, it may be started from the M command line by entering <b>D ^ZTMB</b>.

Running:

1. Using Maven, build and install the separate <b>carewebframework-core</b>, <b>carewebframework-icons</b> and <b>carewebframework-vista</b> projects.
2. In the <b>carewebframework-rpms</b> project under the <b>ihs.gov.cwf.web.impl.wvehr</b> artifact, edit the <b>cwf.properties</b> file to reflect your broker and user authentication settings.
3. Using Maven, build the <b>carewebframework-rpms</b> project.  A war file will be created under the <b>target</b> folder of the <b>gov.ihs.cwf.web.impl.wvehr</b> artifact.
4. Deploy and run the created war file using Tomcat or other servlet container.

Additional configuration:

1. To access the design mode feature of the CWF, assign the <b>RGCWF DESIGNER</b> security key.
2. To enable patient selection access, assign the <b>RGCWPT PATIENT SELECT</b> security key.
