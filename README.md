# Simple Password Manager v1.3
![alt text](https://github.com/AlexIII/spm/blob/master/src/resources/ico.png) 

### A simple multiplatform program that locally stores your passwords in an encrypted form.

#### Features
- Strong encryption (AES 128bit)
- Lightweight
- Multi-platform
- Uses UTF-8
- Open-source
- Extremely permissive license

#### Usage
- Windows users<br/>
Just download SPM.exe from `/dist`, put it somewhere, create a shortcut and run.
- Other platforms<br/>
Download SPM.jar from `/dist` and run it with your JVM. You can also find a program icon in `src/resources/`
- Paranoid<br/>
Review the source code thoroughly and built it yourself

On the first run the program creates database file `spmdb.xml` in the current directory.

#### Hints
- Double click puts the password into the clipboard
- The clipboard is automatically cleared in 30 seconds after coping the password or when the programm is being closed
- Filter acts on "Site", "Login" and "Comment" fields
- "Site", "Login" and "Comment" fields are being encrypted too

#### Requirements
- [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

#### Screencaps
![alt text](https://github.com/AlexIII/spm/blob/master/sc1.png)
![alt text](https://github.com/AlexIII/spm/blob/master/sc2.png)

License
[WTFPL v2](http://www.wtfpl.net)

GUI was created with [NetBeans IDE](https://netbeans.org/)<br/>
Executable for Windows was created with [Launch4j](http://launch4j.sourceforge.net/)
