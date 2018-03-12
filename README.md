# [VFT Capture](http://www.videofirst.co) &middot; [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/videofirst/vft-capture/blob/master/LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/videofirst/vft-capture/blob/how-to-contribute.md)

VFT Capture is a cross-platform tool which enables testers and engineers to capture video while user interface testing.  It has the following features: -

* **JAVA:** implemented in Java, will run on any system which supports a JRE (Java Run-time Environment).
* **REST API:** simple API enables programmatic access from most programming languages.
* **Simple UI:** enables manual screen capture, useful when e.g. performing exploratory UI testing.
* **Upload:** captured videos can be uploaded to a specified endpoint.

## Installation

* Ensure [Java](https://java.com/en/download/) is installed (minimum Java 8).
* Download the latest [release](link) of VFT-Capture.
* Unzip the zip (or tar) somewhere e.g. `c:\vft` (Windows) or `/opt/vft` (Linux).
* The minimum configuration required is setting the password in the `vft.yaml` file : -

    ```bash
    vft_config:
        ...
        security:
            ...
            user: test
            # Set following field to a password of your choice
            pass:
    ```

* Run VFT-Capture by executing `vft.bat` / `vft.sh`.  Once loaded, you can test its working by visiting [http://localhost:1357](http://localhost:1357) in a browser (using the `user` and `pass` declared in `vft.yaml`).
* You can test the API by running the following curl command (e.g. if your user is `test` and your password is `password`): -

    ```bash
    curl -u "test:password" http://localhost:1357/api
    ```

  If should see the following: -

    ```javascript
    {
      "info" : {
        "started" : "2018-03-12T10:15:14.464",
        "uptimeSeconds" : 184,
        "categories" : [ "organisation", "product" ],
        "storage" : {
          "tempFolder" : "C:\\Users\\bob\\AppData\\Local\\Temp\\vft\\temp",
          "videoFolder" : "C:\\Users\\bob\\vft\\videos"
        },
        ...
      },
      "defaults" : {
        "categories" : {
          "organisation" : "",
          "product" : ""
        },
        ...
      },
      "video" : {
        "state" : "idle"
      },
      "uploads" : [ ]
    }
    ```

## Your First Capture

Now installation has been successful

## API Record

Todo

## Configuration

Todo

## Configuration

Todo

## License

VFT-Capture is [MIT licensed](./LICENSE).