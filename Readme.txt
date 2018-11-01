PassVaultPlus is a secure personal password manager. It can be extended for other data types.

http://passvaultplus.com/

- - -Gradle Build - - -
gradle -q run

gradle -q jar
   Remember: set JAR_BUILD in PvpContext.java

gradle -q tasks

gradle -q compileJava -Xlint:unchecked

gradle test

- - - javac build - - -
$ javac -cp ./lib/jdom.jar -d ./bin ./src/com/graham/framework/*.java ./src/com/graham/passvaultplus/actions/*.java ./src/com/graham/passvaultplus/model/*.java ./src/com/graham/passvaultplus/model/core/*.java ./src/com/graham/passvaultplus/view/*.java ./src/com/graham/passvaultplus/view/prefs/*.java ./src/com/graham/passvaultplus/view/recordedit/*.java ./src/com/graham/passvaultplus/view/recordlist/*.java ./src/com/graham/passvaultplus/view/schemaedit/*.java ./src/com/graham/passvaultplus/*.java

$ java -cp ./bin:./jdom.jar com.graham.passvaultplus.PassVaultPlus

$ jar cfm PassVaultPlus.jar manifest.txt .

$ java -jar PassVaultPlus.jar

- - - Scope - - -
https://www.googleapis.com/auth/drive.file	Per-file access to files created or opened by the app. File authorization is granted on a per-user basis and is revoked when the user deauthorizes the app.


- - - Contact - - -
Author: Graham Anderson
Email: gandersonsw@gmail.com
