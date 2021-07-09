circuit-simulator-2.0
=================

This is a version forked from the improvement available at <https://github.com/fmselab/circuit-simulator> starting from <http://www.falstad.com/circuit/>. The improvements made with this forks are the followings:

* refactored the code structure, by adding packages and correcting the visibility of classes and fields
* now it is possible to integrate the circuit-simulator in other projects, since the simulation can be performed even without gui interaction
* resistors with value 0 are not usable anymore, since they led to exceptions due to division by zero. The default value for null resistors is now 1E-6
* circuit simulation can be now performed step by step by using the method loopAndContinue(false) of the class CirSim
* the project can now be used as an Eclipse project

For more information aboyt how to compile, build and run the simulator, see the original repository at <https://github.com/fmselab/circuit-simulator>.

terms and conditions
--------------------

The terms and conditions for the original code still apply. Check <http://www.falstad.com/licensing.html> before redistributing or modifying the code. You must always consult the original licensing information but, in case the link is unavailable, here is a copy of the original license (as of 2013-05-08):

    You have permission to use these applets in a classroom setting or take
    screenshots as long as the applets are unmodified. Modification or
    redistribution for non-commercial purposes is allowed, as long as you
    credit me (Paul Falstad) and provide a link to my page (the page you
    found the applet(s) on, or http://www.falstad.com/mathphysics.html).

    Contact me for any other uses. The source code for each applet is
    generally available on that applet's web page, but some of the applets
    use third-party source code that has restrictions.

    THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

Rodrigo Hausen's changes are in the public domain (but it would be nice if you credited me alongside Paul if you use this version).
