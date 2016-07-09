# RemoteVR_AndroidClient
This is the Android client for the RemoteVR project.

This client can communicate with the RemoteVR Server. At the moment the only implementation of the server is based on Unity and can be found here <a href="https://github.com/PierfrancescoSoffritti/RemoteVR_UnityServer">RemoteVR_UnityServer</a>.

<img width="440" src="https://github.com/PierfrancescoSoffritti/RemoteVR_AndroidClient/blob/master/pics/client_screenshot.png" />
<img width="440" src="https://github.com/PierfrancescoSoffritti/RemoteVR_AndroidClient/blob/master/pics/client_connected.png" />

## Client's responsibilities
The client has two responsibilities: 
- Show the output of the 3D application (received from the server) to the user.
- Intercept the input given by the user for the 3D application and send it to the server.

### Input
This application is meant to be used with a Google Cardboard, therefore it currently supports two types of input: rotations to track the user's head and a simple touch on the screen.
That said, it's not hard to implement new types of input.

#### Touch on the screen
Is intercepted by the class `RemoteVRView` and passed outside using RxJava.

#### Head tracking
This app uses the calibrated gyroscope provided by the Android framework to track the user's movements. The rotations are represented as quaternions. All the head tracking components are in the `headtracking` package.

### Output
The images received from the server are shown to the user using the `RemoteVRView` class.

## Communication with the server
The IO layer of the app is based on RxJava and is in the `io` package. Touch events, quaternions from the gyroscope, images from the server.. everything  is encapsulated inside an `Observable`.

The communication protocol is implemented using both TCP and UDP. <b>At the moment the client-server system is meant to run inside a LAN network</b>, so UDP and TCP are good enough.

## Problems
- The `GameFragment` should be refactorend. At the moment it does a little to much.
- The event bus system should be cleaned (or removed :D)
