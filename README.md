AnTrack is a simple application for tracking and managing your device via cloud file services with three goals in mind:

1. _Following the KISS_ principle. AnTrack is as simple as possible and has only two direct dependencies: the standard Kotlin language library and the Dropbox SDK.

2. _Files as a single source of truth_. AnTrack does not use ViewModels to store current state and databases to store persistent state. All data is always stored in plain text files. For example, dynamic UI updates are performed by updating files on the service side and lastly redrawing on the UI layer side. The service and UI layer are not connected in any way, the UI layer just subscribes to file system updates and redraws the UI when the files are updated.

3. _Modularity_. Antrack is just a framework, needed to load and coordinate modules. All useful functionality is in the form of modules, packed into standard JAR files. Anyone can write a module to implement additional functionality. The cloud services support layer is also abstracted, but at this point requires recompiling the application to add a new service.

## How it works

The two cornerstones of AnTrack are *files* and *commands*. Each command consists of a module name and its arguments. A module launched with a command does the work and writes the result to files (each module has its own set of files).

Immediately after starting AnTrack asks you for access to your Dropbox storage, and then starts the service, which executes several bootstrap commands and starts monitoring changes in the internal application storage and changes in the application directory inside Dropbox (AnTrack only has access to its internal directory: `/Applications/antrack`).

When internal files are changed, the service queues them to be sent to Dropbox; when the file `/Applications/antrack/ctl` is changed on the Dropbox side, AnTrack downloads that file and executes the command written in it. As a result of executing the command, the files are updated and sent back to Dropbox.

Example commands:

- `locate` - determines the current position of the device and writes the coordinates to the location file;
- `contacts` - reads contacts database and writes it to contacts file;
- `cmd` - executes the given console command and writes the result to the file cmdout.

All these commands are implemented by separate modules with the same name. Moreover, modules can respond to commands not only given at application startup or written to file `ctl`. They can also be executed on a timer (the `locate` module does that) or, for example, when a call comes in (the `calls` module does that, recording the history of the calls in a separate directory.

See the source code of the [[modules/ModuleInterface.java]] interface for details.

## How to use it

Once launched and successfully connected to the cloud service, AnTrack will run in the background, executing scheduled commands and waiting for commands from the cloud.

The application is managed remotely entirely through the cloud service. For example, in the case of Dropbox, you can find out the list of recent locations using the following command (DEVICE is the name of the device):

```
$ cd ~/Dropbox/Applications/antrack/DEVICE/
$ cat location
```

You can use the following command to take a picture with the front camera:

```
$ echo camera front > ctl
```

The photo will appear in the `camera` subdirectory.

Or you can run the console command:

```
$ echo uname -a > ctl
```

The result will appear in the `cmdout` file.

See the FIXME repository for more information about working with modules.

## Limitations

Unfortunately, there are no official mechanisms in Android that allow a network app to work in real time without using push notifications. Therefore, it can take minutes or even hours between sending a remote command and its execution.

This could be solved by tying the app to Google's push services and using push notifications to wake up the phone when the command arrives. However, this would complicate the mechanism of sending the commands (now you only need to write the command to a file) and create unnecessary ties to proprietary Google services.

## TODO

- Direct Boot support;
- Other cloud providers;
- More tests.

