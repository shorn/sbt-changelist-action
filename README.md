# Note

This plugin was written for IDEA 12 and is no longer current. The code is left available as an example.

The source code was ported from a BitBucket Mercurial repo to Github on 2020-06-28.

--- 

CLA allows you to execute an operating system command for a specified IDEA changelist (or set of changelists).

The idea behind the basic workflow is that you would:

* configure a ChangeListAction to execute against some script/batch file sitting somewhere on your local system, naming it "MyAction" or somesuch and telling it what parameters to pass to the script (e.g. define it to pass the name of the change list and the list of affected files)
* edit some files in your project, which IDEA automatically groups together as a ChangeList (named Default if you don't rename it)
* right click on the changelist in the IDEA "Changes" view, select "MyAction" and the plugin will execute the script passing in the ChangeList specific parameters that you configured in the first step. 

Configure the plugin by choosing Project Settings\SBT ChangeListAction (ChangeListActions are per-project at the moment, but global application ChangeListActions could easily be added, I just haven't wanted them.)

You can invoke a configured ChangeListAction against a changelist by right-clicking on the changelist in the changes view - a new view will be created that shows the result of executing the command and provides buttons for configuring and re-executing the most recently run command (very useful when debugging your scripts).

Each ChangeListAction is made up of:

* name - this is the name of the ChangeListAction that will be used in the changes window popup menu
* command - absolute path to the OS command you want to execute
* working diretory - the OS-level working directory that the command will executed from
* options - this is where you configure the options that you want to pass the command, this is actually a snippet of groovy script that must evaluate to a list of Strings. The simplest example would be: ["hello", "world"]. See this topic for a better (?) explanation: user/options-help
* clear console - enable this to have CLA clear the console window between each invocation 

This plugin is written in Groovy, but it was orginally copied from Igor Spasic's ChangeListAction plugin: http://code.google.com/p/changelistaction/


