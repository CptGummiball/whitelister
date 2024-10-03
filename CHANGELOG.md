# CHANGELOG

## 1.1.7 [03.10.2024]

### Changed:
- Refactored UUID handling in the API request handler to improve clarity and maintainability.
- Added the whitelist check to return appropriate messages if the user is already on the whitelist. (API)

## 1.1.6 [25.09.2024]

### Changed:
- Replaced console command for whitelisting with direct API calls to improve performance and reduce overhead
- Optimized whitelist logic with fallback to UUID if username fails

### Fixed:
- Improved error handling for failed whitelist attempts, now provides proper console feedback

## 1.1.5 [18.09.2024]

### Added:
- added deny command
- Console Response if API or Frontend is used
### Changed:
- changed some messages
### Fixed:
- PlayerJoinListener now really checks if List is empty

## 1.1.4 [17.09.2024]

### Fixed:
- Commands now working for console

## 1.1.3 [12.09.2024]
### Changed:
- rules ar now set in ``config.yml``

## 1.1.2 [12.09.2024]
### Fixed:
- Lang Error
  - removes messages files and placed the translation into ``config.yml``
- Default Port is now ``8013``
- Rules-URL is now empty

## 1.1.1 [11.09.2024]
### Added:

### Changed:
- Changed UUID Error Response in API

### Fixed:
- api usage was not available in config.yml

## 1.1.0 [10.09.2024]
### Added:
- API Support (enable in config.yml)
- JSON Responses

### Changed:
- Command rework for new json format
