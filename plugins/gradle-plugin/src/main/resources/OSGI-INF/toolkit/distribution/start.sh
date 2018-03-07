#!/bin/sh

# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
java -cp .:bin/* com.neva.osgi.toolkit.framework.launcher.Launcher
