# Changelog
All notable changes to the VFT-Capture project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Create super simple user interface (should we use React / quicker to do a template for now).
- Add `* **Simple UI:** enables manual screen capture, useful when e.g. performing exploratory UI testing.` to README.md
- Investigate keyboard shortcuts to start / stop video capture.
- Do GatsbyJS? - https://scotch.io/tutorials/zero-to-deploy-a-practical-guide-to-static-sites-with-gatsbyjs
- Check out [ https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html ]
- Implement count-down to end.
- Investigate [ https://hexo.io/docs ]
- Investigate `sid` (scenario ID) - means categories, feature and scenario will all be ignored.
- Check out Netlify ( https://www.netlify.com/ )
- Package front-end into the Spring Boot application
     - [ https://www.blackpepper.co.uk/what-we-think/blog/using-create-react-app-with-spring ]
     - [ https://github.com/pugnascotia/spring-cra-demo ]
- Create annotation (separate project / folder maybe)
- Create `vft-capture-lib-java` ?
- `vft-capture-lib-js`
- `vft-capture-lib-cs`
- `vft-capture-lib-go`
- `vft-capture-lib-php`
- Create something useful for the display text.
- Create test Linux / Windows service (https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html)

## [0.2.0] -
### Added
- fill in

### Changed
- fill in


## [0.1.0] - 2018-03-13
### Added
- First release of VFT-Capture, implemented in Java 8 using Spring Boot (in headless mode)
- API via 2 Spring MVC controllers `/api` (top level info) and `/`
- Implementation of screen capture using the Monte library.