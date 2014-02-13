# GlobalPlatform from [OpenKMS](http://openkms.org)

Load and manage applets on compatible JavaCards from command line or from your Java project with a [DWIM](http://en.wikipedia.org/wiki/DWIM) approach.

> Provides a **high level** and **easy to use** interface that most of the time **JustWorks<sup>(TM)</sup>** yet is as **flexible** as GPShell. Fully **[open source](#license)**.

#### Jump to ...
 * [Download](#get-it-now)
 * [Usage](#usage)
 * [Supported cards](#supported-cards)
 * [Contact](#contact)
 * [Similar projects](#similar-projects)
 * [About OpenKMS & legal](#about-openkms)


### Get it now!
 * Download latest pre-built JAR or .EXE from [release area](https://github.com/martinpaljak/GlobalPlatform/releases)
 * Or fetch from github and build it yourself, it is really easy:

        git clone https://github.com/martinpaljak/GlobalPlatform
        cd GlobalPlatform
        ant

### Usage

*Beware: [until v1.0 is released](#upcoming-releases), both command line and Java API are subject to change without notice. Check back often.*

Command line samples assume default test keys of ```40..4F```. If you need custom keys, specify them with any or all of the following options: ```-keyid``` ```-keyver``` and ```-enc``` ```-mac``` ```-kek``` (you need to know the details or ask your card provider). Some cards require key diversification with ```-emv``` or ```-visa2``` (you should be notified if that's the case).

 * Show some basic information about a card (failsafe):

        java -jar gp.jar -info
        
   * On Windows just replace ```java -jar gp.jar``` with ```gp.exe``` like this:

            gp.exe -info

 * List applets (this and following commands can brick your card with wrong keys):

        java -jar gp.jar -list

 * Delete current default applet:

        java -jar gp.jar -delete -default

 * Install applet.cap as default applet (with AID information from the CAP):

        java -jar gp.jar -install applet.cap -default
 
 * Show APDU-s sent to the card:
   
   add ```-debug``` to your command

 * Don't use MAC on commands (plain GlobalPlatform commands):

   add ```-mode clr``` to your command (not supported on all cards)

##### Usage from Java
 * For now consult the [command line utility source code](https://github.com/martinpaljak/GlobalPlatform/blob/master/src/openkms/gpj/GPJTool.java)
 * [Javadoc](http://martinpaljak.github.io/GlobalPlatform/) is in a bad shape but shall be improved near v1.0
 
### Supported cards
 * See [TestedCards](https://github.com/martinpaljak/GlobalPlatform/wiki/TestedCards)
 * Generally speaking any modern JavaCard that speaks GlobalPlatform 2.1.1+

### Contact 

 * martin@martinpaljak.net
 * File an issue on Github. Better yet - a pull request!
 * For general conversation: [google forum](https://groups.google.com/forum/#!forum/openkms)

### History

The ancestor of this code is GPJ (Global Platform for SmartCardIO)
available from http://gpj.sourceforge.net.


### Credits (from GPJ):
 *  Wojciech Mostowski <woj@cs.ru.nl>,
 *  Francois Kooman <F.Kooman@student.science.ru.nl>
 *  Martijn Oostdijk <martijn.oostdijk@gmail.com>
 *  Martin Paljak <martin@martinpaljak.net>
 *  Hendrik Tews
 *  Dusan Kovacevic

### Similar projects
 * gpj (the grandparent) - http://gpj.sf.net (LGPL)
   * written in Java 
   * not maintained
   * harder to use from the command line
 * GPShell + globalplatform library - http://sourceforge.net/projects/globalplatform/ (LGPL)
   * written in C
   * often referred to as the de facto open source GlobalPlatform implementation.
   * several components need to be installed and compiled before usage
   * requires more complex "script files" and does not provide a command line utility
 * jcManager - http://www.brokenmill.com/2010/03/java-secure-card-manager/ (LGPL)
   * written in Java  
   * has a GUI
   * old and not maintained
 * gpjNG - https://github.com/SimplyTapp/gpjNG (LGPL)
   * fork of gpj with minor additions, mostly a "script mode" that makes it similar to GPShell
 * JCOP tools, RADIII, JCardManager4 etc
   * not publicly available open source projects and thus not suitable for this comparision

## Upcoming releases
 * T+1 (v0.2.4)
  * ~~Re-written command line utility~~
  * ~~Windows .exe for ease of use~~
  * ```-lock``` and ```-unlock``` commands for changing secure channel keys (moved to T+2)
 * T+2
  * Support for storing card management keys in PKCS#11 tokens (HSM)
  * Simple GUI
 * T+X (wishlist)
  * SCP03
  * GPShell-style scripts

## About OpenKMS
The promise of OpenKMS is similar<sup>*</sup> to OpenSSL: 
    
> Why buy a smart card **software kit as a black box** when you can get an **open one for free**?

In regard to GlobalPlatform, the goal is to make simple operations like installing and removing applets and locking the card with new keys as easy as next-next-done - you don't have to know the whole Global Platform specification by heart for that or buy a piece of proprietary software for a few hundred euros! For all those features that are not describe in the GlobalPlatform specification that actually make your card work... you still have to use those proprietary commands, but OpenKMS GlobalPlatform toolkit's flexibility (and its license) should allow you to do that as well.

\* <sub>With the difference that OpenKMS thrives for a easily usable and pleasantly readable, auditable and secure codebase. And yes, you have probably already sold your soul to the devil...</sub>

### License

 * [LGPL 3.0](http://www.gnu.org/licenses/lgpl-3.0.html) for derived code and MIT/LGPL3 for original code.

### Included/used open source projects

 * [BouncyCastle](http://pholser.github.io/jopt-simple/) for OID parsing (MIT)
 * [JOpt Simple](http://pholser.github.io/jopt-simple/) for parsing command line (MIT)
 * [Launch4j](http://launch4j.sourceforge.net/) for generating .exe (BSD/MIT)
 * [jnasmartcardio](https://github.com/jnasmartcardio/jnasmartcardio) for PC/SC access (CC0 / public domain)

#### Legal disclaimer
 The casual: trademarks to their owners, copyrights to authors, patents to hell, legal letters to ~~/dev/null~~ PGP key 0x307E3452. Everything is provided as-is and there is a constant risk of death from lightning.

----
OpenKMS - open source key management - [openkms.org](http://openkms.org)
