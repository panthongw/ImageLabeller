#!/bin/bash
rm -rf ./hci/*.class
javac */*.java
java hci.ImageLabeller ./images/U1003_0000.jpg