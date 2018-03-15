# [VFT Capture](http://www.videofirst.co) &middot; [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/videofirst/vft-capture/blob/master/LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/videofirst/vft-capture/blob/README.md)



_VFT Capture_ is a cross-platform tool which enables testers and engineers to capture video during
user interface testing.  It has the following features: -

* **JAVA:** implemented in Java, runs on systems which supports a JRE (Java Runtime Environment).
* **REST API:** simple API enables programmatic access from most programming languages.
* **Upload:** captured videos can be uploaded to a configurable endpoint.



## Installation

* Ensure [Java](https://java.com/en/download/) is installed (minimum Java 8).
* Download the latest [release](https://github.com/videofirst/vft-capture/releases) of _VFT Capture_.
* Extract the zip (or tar) file somewhere e.g. `c:\vft` (Windows) or `/opt/vft` (Linux).
* The minimum configuration required is setting the password in the `vft.yaml` file : -

    ```yaml
    vft_config:
        ...
        security:
            ...
            user: test
            # Set the following property to a password of your choice
            pass:
    ```

* Run _VFT Capture_ by executing `vft.bat` / `vft.sh`.  Once loaded, you can test its working
  correctly by visiting [http://localhost:1357](http://localhost:1357) in a browser (using the
  `user` and `pass` declared in `vft.yaml`).
* You can test the API by running the following curl command (e.g. if your user is `test` and your
  password is `password`): -

    ```bash
    curl -u "test:password" http://localhost:1357/api
    ```

  ... will display top-level information  ...

    ```json
    {
      "info" : {
        "started" : "2018-03-12T10:15:14.464",
        "uptimeSeconds" : 184,
        "categories" : [ "organisation", "product" ],
        "storage" : {
          "tempFolder" : "C:\\Users\\bob\\AppData\\Local\\Temp\\vft\\temp",
          "videoFolder" : "C:\\Users\\bob\\vft\\videos"
        }
      },
      "defaults" : {
        "categories" : {
          "organisation" : "",
          "product" : ""
        }
      },
      "captureStatus" : {
        "state" : "idle"
      },
      "uploads" : [ ]
    }
    ```



## Your First Capture

You are now ready to perform your first capture!  You can use something like
[Postman](https://www.getpostman.com) or `curl` commands are also fine.  To capture a UI test you
must supply the following information: -

1. **Categories:** the default categories are defined in the `vft.yaml` and default to
   `organisation` and `product`.  These can be changed if required.  The order is important i.e. an
   `organisation` contains 1 or more `product`'s.
2. **Feature:** this is the feature you are testing on your user interface e.g. `search`.
3. **Scenario:** a feature contains various scenarios you are testing of the feature e.g. if the
   feature is `search` a potential scenario is `search by city`.
4. **Test Status:** the outcome of the test.  Available values include `pass`, `fail`, `error` or
   `aborted`.

> **Note:** the **Test Status** is the only field which is set _after_ the test finishes, the others
  must be set _before_.

#### Start recording

To start capturing a test, use the `/api/captures/start` (POST) endpoint e.g.: -

```bash
curl -X POST http://localhost:1357/api/captures/start \
  -u 'test:password' \
  -H 'content-type: application/json' \
  -d '{
        "categories" : {
          "organisation": "Google",
          "product": "Search"
        },
        "feature": "Home Page Search",
        "scenario": "Search for Belfast"
      }'
```

These are the minimum required parameters to start a test and this will return the following ...

```json
{
  "state" : "recording",
  "categories" : {
    "organisation" : "Google",
    "product" : "Search"
  },
  "feature" : "Home Page Search",
  "scenario" : "Search for Belfast",
  "started" : "2018-03-12T13:56:28.528",
  "durationSeconds" : 0.053,
  "folder" : "google/search/home-page-search/search-for-belfast/2018-03-12_13-56-28_vxva1z",
  "id" : "2018-03-12_13-56-28_vxva1z",
  "capture" : {
    "x" : 0,
    "y" : 0,
    "width" : 1920,
    "height" : 1200
  },
  "format" : "avi",
  "environment" : {
    "java.awt.graphicsenv" : "sun.awt.Win32GraphicsEnvironment"
  }
}
```

If you are constantly specifying the same categories, these can be defined in the `vft.yaml` file: -

```yaml
vft_default:
    categories:
        organisation: Google
        product: Search
```

The categories are then no longer required in the JSON body of the `/api/captures/start` (POST)
endpoint.


#### Finish recording

To finish the test capture, use the `/api/captures/finish` (POST) endpoint: -

```bash
curl -X POST http://localhost:1357/api/captures/finish \
  -u 'test:password' \
  -H 'content-type: application/json' \
  -d '{ "testStatus" : "fail" }'
```

... will return ...

```json
{
  "state" : "finished",
  "started" : "2018-03-12T13:56:28.528",
  "finished" : "2018-03-12T13:56:35.699",
  "durationSeconds" : 7.171,
  "folder" : "google/search/home-page-search/search-for-belfast/2018-03-12_13-56-28_vxva1z",
  "id" : "2018-03-12_13-56-28_vxva1z",
  "testStatus" : "fail"
}
```

The _VFT Capture_ app captures video / test data to the folder defined by the
`vft_config.storage.videoFolder` property (defaults to `${user.home}/vft/videos`).  The example
above will save the 2 files (video + JSON file) into a sub-folder structure using the provided
`categories`, `feature`, `scenario` parameters and the generated `id` of each test. This can be
identified from the `folder` property in the JSON output e.g.

```
    "folder" : "google/search/home-page-search/search-for-belfast/2018-03-12_13-56-28_vxva1z",
```

If you look inside this folder you'll see 2 files e.g.: -

1. `2018-03-12_13-56-28_vxva1z.avi` - video of screen capture.
2. `2018-03-12_13-56-28_vxva1z.json` - data file containing various information on the test (similar
   to the JSON output of the `/api/captures/finish` (POST) endpoint).

Congratulations - you've captured your first test!

#### Upload the Capture

Once a test has been captured, the generated video and test can be uploaded to an HTTP endpoint.
This endpoint must satisfy the following: -

- **HTTP Method:** must be of type `POST`.
- **HTTP Params:** must contain 2 multi-part parameters - `video` and `data`.

An example using Java (Spring MVC) is as follows: -

```java
@RestController
public UploadControler {
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadVideo(@RequestParam("video") MultipartFile videoFile,
        @RequestParam("data") MultipartFile dataFile) {
        // do something with videoFile / dataFile
        return ResponseEntity.ok().build();
    }
}
```

A working test example is available [here](https://github.com/videofirst/vft-capture/blob/master/src/test/java/co/videofirst/vft/capture/mock/MockUploadController.java)
in the _VFT Capture_ source code.

Once deployed, edit the `vft.yaml` file and set `upload` to `true` and set the `url` parameter to
the deployed URL e.g.

```yaml
vft_config:
    ...
    upload:
        ...
        enable: true
        url: http://www.example.com/upload
```

> **Note:** additional HTTP headers can also be specified in the `vft.yaml`configuration file e.g.
for Basic Auth.

A capture can now be uploaded the configured URL via the `/api/captures/uploads/<id>` (POST) endpoint
e.g.: -

```bash
curl -X POST http://localhost:1357/api/captures/uploads/2018-03-12_13-56-28_vxva1z\
  -u 'test:password'
```

... will return ...

```
[
  {
    "id" : "2018-03-12_13-56-28_vxva1z",
    "state" : "uploading",
    "url" : "http://localhost:1234/upload",
    "scheduled" : "2018-03-13T09:58:47.51",
    "started" : "2018-03-13T09:58:47.537"
  }
]
```

If the upload is big (or connection is slow) you can view the status of the upload using the
`/api/captures/uploads` (GET) endpoint: -


## Configuration

The `vft.yaml` configuration file contains 2 types of configuration properties: -

1. **Start-up:** these all start with `vft_config`.  These configuration properties can only be set
at start-up and once the application is running they CANNOT be changed/overridden e.g. security
username, storage locations, upload URL etc.

2. **Defaults:** these all start with `vft_default`. These configuration properties set default
values.  These can all be overridden at run-time via parameters in various REST calls e.g. setting
the default category values or display capture area.

Comments exist beside each configuration property in the `vft.yaml` file to explain it's purpose and
possible values.

#### Updating the Configuration

There are 3 main ways to set the configuration of _VFT Capture_.  These are: -

1. **YAML:** edit the `vft.yaml` file which comes bundled with _VFT Capture_ e.g. to update the
temporary capture storage folder, locate and update the `tempFolder` property in this file.

    ```javascript
    vft_config:
        ...
        storage:
            # Temporary folder where videos get stored when they are being recorded
            tempFolder: ${java.io.tmpdir}/vft/temp
    ```

2. **Java Properties:** update configuration via `-D` Java options.  Locate the property to
configure in the `vft.yaml` file and join the parts together using dots e.g.

   ```bash
   java -Dvft_config.storage.tempFolder=/tmp -jar vft.jar
   ```

3. **Environment:** updates the configuration via system environment variables. Locate the property
to configure in the `vft.yaml` file and join the parts together using underscores, then convert to
upper-case e.g.

   ```bash
   VFT_CONFIG_STORAGE_TEMPFOLDER=/tmp
   ```



## Documentation

Documentation is still in progress - you can view [Swagger](https://swagger.io) documentation in a
browser at: -

> [http://localhost:1357/swagger-ui.html](http://localhost:1357/swagger-ui.html)



## Development

_VFT Capture_ is implemented in Java using the Spring Boot framework.  Ensure
[Maven](https://maven.apache.org) and [Git](https://git-scm.com/) are installed.  An IDE, such as
[IntelliJ IDEA](https://www.jetbrains.com/idea) or
[Eclipse](https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygen2) is also
recommended.

Download the source code as follows: -

```bash
git clone https://github.com/videofirst/vft-capture.git
```

Change directory to `vft-capture` and build using Maven as follows: -

```bash
cd vft-capture
mvn clean install
```

If the build is successful you should see something like: -

```
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 31.192 s
[INFO] Finished at: 2018-03-13T11:39:57Z
[INFO] Final Memory: 40M/610M
[INFO] ------------------------------------------------------------------------
```

You are now ready to start contributing code.


## License

_VFT Capture_ is [MIT licensed](./LICENSE).