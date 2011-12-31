No generated artifacts should be written to this tree - everything generated
goes into ../sbt-changelist-action-gen
The only thing that should need to be put in .hgignore is
ide/.idea/workspace.xml and .gradle, etc.

You can get gradle to put its files in another dir, but I've added the .gradle
dir to the ignore list just in case.  Can't control the location of the
IDEA workspace file though (grrrr).


TODO:SBT create dirs ?


Viewing logs.
Logs from this plugin are written to the normal $LOG_DIR$/idea.log file.

Note that the log.xml file you want is the one in the IDE JDK that you're
using to run the plugin, not the one you're building with.
So if, like me, you're developing in IU but
testing with IC, you want to edit the IC log.xml file, not the IU one.
I keep a reference to this in the favorites list (not sure if that gets
shared yet - might have to make a log dir variable or something).

You can add the following to the idea bin/log.xml file if you want
to log to a separate file as well though.
  <appender name="SBT_CLA_FILE" class="org.apache.log4j.FileAppender">
    <param name="append" value="false"/>
    <param name="file" value="$LOG_DIR$/sbt-cla.idea.log"/>
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%d [%7r] %6p - %30.30c - %m \n"/>
    </layout>
  </appender>

  <category name="#net.intellij.plugins.sbtchangelistaction">
    <level value="DEBUG"/>
    <appender-ref ref="SBT_CLA_FILE"/>
  </category>

Unfortunately, you can't currently tell IDEA that you want to monitor multiple
log files if you're executing via a plugin run configuration (you can with
a normal java application run config though).  So instead, I usually change
the root level category to WARN level, so only my plugin debug statements
show up in the log.