# Changelog
All notable changes to the VF-Capture project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Create a new `/api/captures/preview` endpoint which displays borders etc (but doesn't record). Note, cancel endpoint will also cancel preview.
- Create `/edit` endpoint which can edit the test data associated with a capture?
- New UI screen which only loads of configuration mode is not headless - record, stop
- New UI panel to show a table of uploads.
- Change uploading so it works out of the box with Video First App.
- Remove "alwaysOnTop" - make border always on top and background always in background.
- Split docs into 2 i.e.
   - Make `README.md` more concise.
   - Create a new README.md in `/docs` folder.
- Investigate keyboard shortcuts to start / stop capture.
- Do GatsbyJS? - https://scotch.io/tutorials/zero-to-deploy-a-practical-guide-to-static-sites-with-gatsbyjs
- Check out [ https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html ]
- Implement count-down to end.
- Check out Netlify ( https://www.netlify.com/ )
- Finish off `vf-capture-lib-java` ?
- `vf-capture-lib-js`
- `vf-capture-lib-cs`
- `vf-capture-lib-go`
- `vf-capture-lib-php`
- Create something useful for the display text (or simmply remove)
- Create `ffmpeg` support for custom screen capture.
- Create test Linux / Windows service (https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html)


## [0.2.0] - (IN PROGRESS)
### Added
- Add ability to _force_ a start capture using new `force` parameter (ideal for programmatic access).
- Added new `stackTrace` field to `Capture` object + finish params.
- New redirect controller to handle errors to go to UI.
- User interface is now a Java Swing application which can be minimised to the task bar.
- Make `feature` and `sceario` fields optional and give a default `project`.
- New `sid` field - means categories, feature and scenario will all be ignored.

### Changed
- Changed all the `/api/videos ...` endpoints to `/api/captures ...` so it's more consistent.
- Updated spring security so that the UI doesn't require browser based basic auth.
- Renamed all VFT (Video First Testing) to VF (Video First).  Also refactor `co.videofirst` to
  `io.videofirst`.
- Removed `/api/captures/start` endpoint and merge into `/record` (optional parameter).
- Removed `/api/captures/finish` endpoint and merge into `/stop` (optional parameter).
- Simplify capture state by removing 5 states and having a simple boolean [ recording: true / false ]
- Removed `/api` from API calls (not needed now that the UI is removed).


## [0.1.0] - 2018-03-13
### Added
- First release of VF-Capture, implemented in Java 8 using Spring Boot (in headless mode)
- API via 2 Spring MVC controllers `/api` (top level info) and `/`
- Implementation of screen capture using the Monte library.