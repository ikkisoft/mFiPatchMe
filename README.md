**mFiPatchMe** is the *unofficial* security patch for [Ubiquiti Networks mFi][1] Controller 2.1.11

### Background

On the 3rd September 2015, [SecuriTeam][2] disclosed a vulnerability in the Ubiquiti Networks mFi Controller, a software to configure and monitor control and automation devices such as power outlets, light/motion/temperature sensors, etc.  To understand the capabilities of the machine-to-machine platform, please have a look at the [vendor page][3].

The security flaw allows an attacker to retrieve the current admin password due to a bypass in the authentication mechanism used by the mFi Controller Server.

Just few hours after the public release of the [SSD Advisory – Ubiquiti Networks mFi Controller Server Authentication Bypass][4], the page was removed to accommodate the vendor's request since a patch was not available for download. According to the advisory and [Noam Rathaus][5]'s tweet, the vendor  was aware of this critical vulnerability since the beginning of July 2015. 

### Digital Self-Defense

Considering that the advisory published on 09/03/2015 contained a technical description of the security vulnerability, including a **reliable exploit**, it is reasonable to assume that the security flaw can be easily abused by unsophisticated attackers. While the information was removed from the [SecuriTeam website][6] and [/r/netsec][7], a quick search on Google is sufficient to find the exploit for this bug.

Despite the public exposure, **Ubiquiti has yet to publish a patch**. 

After waiting patiently for a few weeks, I created my own patch. Using **mFiPatchMe**, you will be able to easily patch your controller and leave it running without worries. As it took me one hour to create a patch without preliminary knowledge of the codebase, it's surprising that Ubiquiti has not released a fix for this critical vulnerability.

> **Disclaimer:** 
> This is NOT an official patch provided by [Ubiquiti Networks][8]. Use at your own risk!
The patch tool does NOT contain any Ubiquiti Networks' code, library or resources.

### How to patch your Ubiquiti mFi Controller 2.1.11 (Linux)

1. Download the [patch tool JAR][9], or compile it yourself using the code in this repository. ```da7fdf210ef91c0910f72184e553618e  UbntmFiPatchMe.jar```
2. Stop the mFi controller service ```$sudo /etc/init.d/mfi stop```
3. Go to the mFi controller directory ```$cd /usr/lib/mfi/lib/```
4. *Optionally*, make a backup of the mFi controller JAR ```$cp ace.jar aceORI.jar```
5. Run the patch tool on the mFi controller JAR (*ace.jar*) ```$java -jar UbntmFiPatchMe.jar -file ace.jar``` and follow the instructions
6. Once completed, start again the mFi controller service ```$sudo /etc/init.d/mfi start```

At this point, the controller should work as usual. From my preliminary tests, the patch seems to work without side effects. *While the instructions are for Linux only, the same patch tool can be used for Ubiquiti mFi Controller 2.1.11 on Mac OS X and Windows.*

```java
java -jar UbntmFiPatchMe.jar -file ace.jar 

--[ UbntmFiPatchMe v0.1 - @_ikki ]
  [*] Opening Ubiquiti mFi Controller's jar "ace.jar"
  [*] Patching "com/ubnt/ace/view/AuthFilter" class
  [*] Ready to update "ace.jar". Are you sure? (yes/no)
  >yes
  [*] Executing "jar -uvf ace.jar -C /tmp/1442714249482-0 com/ubnt/ace/view/AuthFilter.class"
  [*] Patched
```
### Under the hood 

Since the vulnerability details have been removed from the Internet (*oh, sure!*), I will refrain from providing specific details here. At high level, the patch tool is simply looking for the vulnerable code and modifying the original Java class to perform URL decoding and normalization. [Javassist][10] is used here to manipulate the JVM bytecode. Thanks to the [ExprEditor][11] implementation, it is possible to modify the body of a method with a simple strategy pattern.

  [1]: https://www.ubnt.com/mfi/mport/
  [2]: http://www.securiteam.com/
  [3]: https://www.ubnt.com/mfi/mport/
  [4]: http://blogs.securiteam.com/index.php/archives/2580
  [5]: https://twitter.com/nrathaus/status/644404584081956864
  [6]: http://www.securiteam.com/
  [7]: https://www.reddit.com/r/netsec/
  [8]: https://www.ubnt.com
  [9]: https://github.com/ikkisoft/mFiPatchMe/releases/download/0.1/UbntmFiPatchMe.jar
  [10]: http://jboss-javassist.github.io/javassist/
  [11]: http://jboss-javassist.github.io/javassist/html/javassist/expr/ExprEditor.html
