Depixelizer
===========

A Java command-line utility providing a partial implementation of the
[Kopf-Lischinski image vectorization algorithm](https://johanneskopf.de/publications/pixelart/), based on
[TheAppGuys'](https://theappguys.de/) [Android app](https://apkpure.com/depixelizer/de.theappguys.depixelizer)
of the same name.

![dolphin with nearest neighbour resampling](dolphin-nn.png "nearest neighbour")
![dolphin with Kopf-Lischinski algorithm](dolphin-depix.png "Kopf-Lischinski")

Why?
----

Kopf-Lischinski is a tricky algorithm. There are at least 20 independent implementations of it on GitHub, most of them
being broken or incomplete in some way. Yet the most feature-complete version I've found is hidden inside a freeware,
discontinued Android app. I guess it might get a bit more visibility over here.

License
-------

This is an unofficial patch to a freeware program to make it more useful.
The patch itself is released under the [MIT license](https://opensource.org/licenses/MIT).
I am not a lawyer, but I think you should be fine to download the archive, apply the patch and use the patched
software on your own machine. Distributing the patched binary is probably not ok. In particular,
- `depixelizer.apk` is &copy; [TheAppGuys GmbH](https://theappguys.de/).
It is closed source but free of charge, with an optional donation feature that no longer works.
You might be able to find some other way to support them.
- `dex2jar-min.jar` is a copy of [dex2jar](https://github.com/pxb1988/dex2jar) by Bob Pan and contributors,
released under the [Apache 2.0 license](https://opensource.org/licenses/Apache-2.0).
It is a tool used in the patching process to translate code from the Android VM to the Java VM.
It was minified by stripping the classes we don't need.
- `Depixelizer.java` and `build.sh` was written by me and I am releasing them under the
[MIT license](https://opensource.org/licenses/MIT).
In addition to providing a command-line entry point they contain a bug fix for shapes with holes.

Prerequisites
-------------

To build `depix.jar` you'll need a minimal Linux-like environment with an installed JDK.
It was tested both on native Linux and Windows using WSL, with OpenJDK versions 8, 11 and 16.

You can run `depix.jar` on any platform with a Java runtime.

Building
--------

Run the included `./build.sh`. A new `depix.jar` archive will be created in the repository root.

Running
-------
`java -jar depix.jar [-s] [-c] input.png [output.svg]`

Switches:
`-s`: skip polygon smoothing,
`-c`: skip combining paths.

