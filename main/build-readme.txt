No generated artifacts should be written to this tree - everything generated
goes into ../sbt-changelist-action-gen
The only thing that should need to be put in .hgignore is
ide/.idea/workspace.xml and .gradle, etc.


TODO:SBT create dirs ?


I've also added the guava-r09.jar from the idea lib dir, maybe it's not
supposed to be part of the plugin openapi, but I like Guava - I'll bundle it
with the plugin if I have to.


Viewing logs.
Logs from this plugin are written to the normal $LOG_DIR$/idea.log file.
You can also add the following to the idea bin/log.xml file if you want
to log to a separate file as well though.
Note that the log.xml file you want is the one in the IDE JDK that you're
using to run the plugin, not the one you're building with.
So if, like me, you're developing in IU but
testing with IC, you want to edit the IC log.xml file, not the IU one.
Also note that this plugin uses slf4j, but IDEA uses log4j (which I'm bridging
to via the slf4j-jcl binding (used to the use the log4j binding but it seems
not to work in IDEA 11 for some reason).  So you're configuring log4j when
you're editing log.xml and slf4j takes care of delivering the log statements
to log4j.
Note that slf4j TRACE level will map to DEBUG level in log4j.
  <appender name="SBT_CLA_FILE" class="org.apache.log4j.FileAppender">
    <param name="append" value="false"/>
    <param name="file" value="$LOG_DIR$/sbt-cla.idea.log"/>
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%d [%7r] %6p - %30.30c - %m \n"/>
    </layout>
  </appender>

  <logger name="org.xpdtr">
    <level value="TRACE"/>
    <appender-ref ref="SBT_CLA_FILE"/>
  </logger>
