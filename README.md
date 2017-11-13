# simple-fake-tweet-generator-ui

Author: **Derek Weber**

Last updated: **2017-11-13**

Simple GUI tool to create a selection of fields to generate important
bits of a fake tweet.


## Description

This app runs a small form-based UI into which one specifies a screen name,
text content, and, optionally, geolocation information (latitude and longitude).
The entered information is used to create a JSON instance that mirrors tweet
structures, thus creating an impoverished tweet. This is generated when the
button at the bottom of the UI is pressed, and is subsequently copied to the
global copy buffer/clipboard, ready to be pasted at the user's convenience.

Some effort has been expended to validate values entered in the geolocation
panel, and the generated JSON will include plausibly unique `id` and
corresponding `id_str` fields. Future versions may include a mapping component
to visually select a geolocation.


## Requirements:

 + Java Development Kit 1.8
 + [Google Guava](https://github.com/google/guava) (Apache 2.0 licence) 
 + [FasterXML](http://wiki.fasterxml.com/JacksonHome) (Apache 2.0 licence)

Built with [Gradle 4.3](http://gradle.org), included via the wrapper.


## To Build

The Gradle wrapper has been included, so it's not necessary for you to have
Gradle installed - it will install itself as part of the build process. All that
is required is the Java Development Kit.

By running

`$ ./gradlew installDist` or `$ gradlew.bat installDist`

you will create an installable copy of the app in `PROJECT_ROOT/build/simple-fake-tweet-generator-ui`.


## Usage
If you've just downloaded the binary distribution, do this from within the
unzipped archive (i.e. in the `simple-fake-tweet-generator-ui` directory). 
Otherwise, if you've just built the app from source, do this from within
`PROJECT_ROOT/build/install/simple-fake-tweet-generator-ui`:

<pre>
Usage: bin/fetch-tweets[.bat]
</pre>

Run the app with no other commandline arguments:
<pre>
prompt> bin/simple-fake-tweet-generator-ui
</pre>