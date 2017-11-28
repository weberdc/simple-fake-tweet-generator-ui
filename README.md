# simple-fake-tweet-generator-ui

Author: **Derek Weber**

Last updated: **2017-11-26**

Simple GUI tool to edit fields of a tweet or create a new one with a (probably
invalid) subset of a Tweet's fields.

**NB** Does not fully comply with the changes made to Tweets recently (late 2017),
described [here](https://developer.twitter.com/en/docs/tweets/tweet-updates). 


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


Twitter credentials are looked for in `"./twitter.properties"`, and proxy info
is looked for in `"./proxy.properties"`. Commandline options for the input file,
the output file, and the Twitter properties are provided, along with a verbose
mode.


## Requirements:

 + Java Development Kit 1.8
 + [Google Guava](https://github.com/google/guava) (Apache 2.0 licence) 
 + [FasterXML](http://wiki.fasterxml.com/JacksonHome) (Apache 2.0 licence)
 + [JXMapViewer2](https://github.com/msteiger/jxmapviewer2) (LGPLv3)
 + [jcommander](http://jcommander.org) (Apache 2.0 licence)

Built with [Gradle 4.3](http://gradle.org), included via the wrapper.


## To Build

The Gradle wrapper has been included, so it's not necessary for you to have
Gradle installed - it will install itself as part of the build process. All that
is required is the Java Development Kit.

By running

`$ ./gradlew installDist` or `$ gradlew.bat installDist`

you will create an installable copy of the app in `PROJECT_ROOT/build/simple-fake-tweet-generator-ui`.


## Configuration

Twitter OAuth credentials must be available in a properties file based on the
provided `twitter.properties-template` in the project's root directory. Copy the
template file to a properties file (the default is `twitter.properties` in the
same directory), and edit it with your Twitter app credentials. For further
information see [http://twitter4j.org/en/configuration.html]().

If running the app behind a proxy or filewall, copy the
`proxy.properties-template` file to a file named `proxy.properties` and set the
properties inside to your proxy credentials. If you feel uncomfortable putting
your proxy password in the file, leave the password-related ones commented and
the app will ask for the password.


## Usage
If you've just downloaded the binary distribution, do this from within the
unzipped archive (i.e. in the `simple-fake-tweet-generator-ui` directory). 
Otherwise, if you've just built the app from source, do this from within
`PROJECT_ROOT/build/install/simple-fake-tweet-generator-ui`:

<pre>
Usage: bin/simple-fake-tweet-generator-ui[.bat] [options]
  Options:
    -c, --credentials
      Properties file with Twitter OAuth credentials
      Default: ./twitter.properties
    -h, -?, --help
      Help
      Default: false
    --skip-date
      Don't bother creating a 'created_at' field.
      Default: false
</pre>

Run the app with no other commandline arguments:
<pre>
prompt> bin/simple-fake-tweet-generator-ui
</pre>


## Rate limits

Attempts have been made to account for Twitter's rate limits, so at times the
app will pause, waiting until the rate limit has refreshed. It reports how long
it will wait when it does have to pause.
