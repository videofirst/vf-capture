# [VFT Capture](http://www.videofirst.co) &middot; [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/videofirst/vft-capture/blob/master/LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/videofirst/vft-capture/blob/how-to-contribute.md)



VFT Capture is a cross-platform tool which enables testers and engineers to capture video while user interface testing.  It has the following features: -

* **JAVA:** implemented in Java, will run on systems which supports a JRE (Java Run-time Environment).
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

  ... will display top-level information ...

    ```json
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

You are now ready to perform your first capture!  You can use something like
[Postman](https://www.getpostman.com) or `curl` commands are also fine.  To capture a UI test you must supply the following information: -

1. **Categories:** the default categories are defined in the `vft.yaml` and default to `organisation` and `product`.  These can be changed if required.  The order is important i.e. an `organisation` contains 1 or more `product`'s.
2. **Feature:** this is the feature you are testing on your user interface e.g. `search`.
3. **Scenario:** a feature contains various scenarios you are testing of the feature e.g. if the feature is `search` a potential scenario is `search by city`.
4. **Test Status:** the outcome of the test.  Available values include `pass`, `fail`, `error` or `aborted`.

> **NOTE:** the **Test Status** is the only field which is set _after_ the test finishes, the others must be set _before_.

#### Start recording

To start capturing a test, use the `/api/videos/start` (POST) endpoint e.g.: -

```bash
curl -X POST http://localhost:1357/api/videos/start \
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

... will return ...

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
    "java.awt.graphicsenv" : "sun.awt.Win32GraphicsEnvironment",
    ...
  }
}
```

This is the minimum required.  If you are constantly using the same categories, these can be set in the `vft.yaml` file.

```yaml
vft_default:
    categories:
        organisation: Google
        product: Search
```

The categories are no required in the JSON body of the `/api/videos/start` (POST) endpoint.

#### Finish recording

To finish the test capture, hit the `/api/videos/finish` (POST) endpoint: -

```bash
curl -X POST http://localhost:1357/api/videos/finish \
  -u 'test:password' \
  -H 'content-type: application/json' \
  -d '{ "testStatus" : "fail" }'
```

... will return ...

```json
{
  "state" : "finished",
  "categories" : {
    "organisation" : "Google",
    "product" : "Search"
  },
  "feature" : "Home Page Search",
  "scenario" : "Search for Belfast",
  "started" : "2018-03-12T13:56:28.528",
  "finished" : "2018-03-12T13:56:35.699",
  "durationSeconds" : 7.171,
  "folder" : "google/search/home-page-search/search-for-belfast/2018-03-12_13-56-28_vxva1z",
  "id" : "2018-03-12_13-56-28_vxva1z",
  "capture" : {
    "x" : 0,
    "y" : 0,
    "width" : 1920,
    "height" : 1200
  },
  "format" : "avi",
  "meta" : { },
  "environment" : {
    "java.awt.graphicsenv" : "sun.awt.Win32GraphicsEnvironment",
    ...
  },
  "testStatus" : "fail"
}
```

The **VFT-Capture** app will have created 2 files in the `${user.home}/vft/videos` folder.  The folder structure is generated from the `categories`, `feature` and `scenario`.

## Configuration

The `vft.yaml` configuration file contains 2 types of configuration properties: -

1. **Start-up:** these all start with `vft_config`.  These configuration properties can only be set at start-up and once the application is running they CANNOT be changed/overridden e.g. security username, storage locations, upload URL etc.

2. **Defaults:** these all start with `vft_default`. These configuration properties set default values.  These can all be overridden at run-time via parameters in various REST calls e.g. setting the default category values or display capture area.

Comments exist beside each configuration property in the `vft.yaml` file to explain it's purpose and possible values.



#### Updating Configuration

There are 3 main ways to set the configuration of VFT-Capture.  These are: -

1. **YAML:** edit the `vft.yaml` file which comes bundled with VFT-Capture e.g. to update the temporary video storage folder, locate and update the `tempFolder` property in this file.

    ```javascript
    vft_config:
        ...
        storage:
            # Temporary folder where videos get stored when they are being recorded
            tempFolder: ${java.io.tmpdir}/vft/temp
    ```

2. **Java Properties:** update configuration via `-D` Java options.  Locate the property to configure in the `vft.yaml` file and join the parts together using dots e.g.

   ```bash
   java -Dvft_config.storage.tempFolder=/tmp -jar vft.jar
   ```

3. **Environment:** updates the configuration via system environment variables. Locate the property to configure in the `vft.yaml` file and join the parts together using underscores, then convert to upper-case e.g.

   ```bash
   VFT_CONFIG_STORAGE_TEMPFOLDER=/tmp
   ```



## Documentation

Documentation is still in progress - you can view [Swagger](https://swagger.io) documentation in a browser at: -

> [http://localhost:1357/swagger-ui.html](http://localhost:1357/swagger-ui.html)



## Contribute

All contributions are welcome.



## License

VFT-Capture is [MIT licensed](./LICENSE).