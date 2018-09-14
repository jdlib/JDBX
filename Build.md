# Build JDBX

You need a Java JDK 8+, Apache Ant 1.6.0+ and <a href="http://ant.apache.org/ivy/">Apache Ivy.</a><br/>
Unpack the distribution archive in an directory, open a console in
that directory and run

    ant resolve
    
This downloads thirdparty libraries with Ivy. Then run

    ant dist

to build the distribution in the <code>tmp</code> subdirectory.

