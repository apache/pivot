#!/bin/sh
java -cp $CLASSPATH:pivot-core.jar:pivot-web.jar:pivot-wtk.jar:pivot-wtk.terra.jar pivot.wtk.DesktopApplicationContext pivot.wtk.ScriptApplication src:$1
