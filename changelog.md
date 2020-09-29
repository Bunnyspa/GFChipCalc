# Change Log

## 1.0.0 (10/16/2018)

## 1.1.0 (10/22/2018)

### Added
- Stat number on chip images
- "Used" marker (Used chips will not be used in the calculations.)
- Save/Load

### Changed
- Divided the result table into two (Excluding/Including Resonance) columns

### Fixed
- A bug that caused the result table not getting sorted correctly

## 1.2.0 (10/26/2018)

### Added
- Open/Close the pool panel
- Save as
- Highlight inventory chip with green background when selected in the result panel

### Changed
- 5-6 cells -> 5B/6
- Stat number as options instead of PT
- Optimized calculation

## 1.3.0 (11/7/2018)

### Added
- Save/Load results

### Changed
- Optimized all calculations
- Sorted the pool to match the in-game sorting order
- Adding from the pool now inserts after the selected inventory chip

## 1.4.0 (11/13/2018)

### Added
- Drag-and-drop order switching in the inventory
- Red color on result stats exceeding the maximum

## 1.5.0 (11/19/2018)

### Added
- "Use calibration ticket" option

### Changed
- Optimized calibration ticket count

## 1.6.0 (11/22/2018)

### Added
- Counter-clockwise turn buttons
- Invert pool order button

### Changed
- Optimized 5B/6 calculation
- Result chip list moved to the right

## 1.7.0 (12/7/2018)

### Added
- 5/6 option (slow calculations)
- Red color and explanation on the slow calculation options
- PT on results

### Changed
- Optimized the slow calculations
- Backgrounds in inventory chips blink when selected (orange color) and does not have the white background

### Deleted
- Stat with resonance

### Fixed
- A bug that caused calculations using incorrect inventories after reordering or deleting their chips
- A bug that caused less number of results showing than intended

## 1.8.0 (1/4/2019)

### Added
- Inventory sort
- Inventory filter (Rarity/Color/Cell)

### Fix Attempt
- Interface glitch

## 1.9.0 (1/11/2019)

### Added
- More inventory filter (Used/Enhancement level range)
- Stat visibility between stat and PT
- Settings - Stat option based on PT
- "Help" section
- "5-6 cell" fast calculation with 1-4 star boards

### Deleted
- Stat percentage with resonance
- Settings - # of cells (Filter feature is used instead)

### Fixed
- A bug that caused chips being added while filters are on

## 1.10.0 (1/19/2019)

### Added
- Option - "Exclude used chips"
- Calculation options pop up when the inventory contains any 1-4 cell chips

## 2.0.0 (1/19/2019)

### Changed
- Stat calculation changes based on the game client version 2.03

## 2.1.0 (1/29/2019)

### Added
- Font option
- Some options will be saved with settings.dat file

### Changed
- File extensions changed: Chip files (.dat -> .gfci), Calculation files (.sav -> gfcc)

### Removed
- Calculation option - 5-6 Cell option temporarily removed

### Fixed
- A bug that caused files saving with incorrect extensions
- A bug that caused chip files not getting saved correctly after reordering by dragging and dropping.

## 2.2.0 (2/7/2019)

### Added
- Color option for colorblind users
- Shortcuts
- PT filter for each stat category

### Changed
- Chip color toggle button instead of a list

### Removed
- PT setting (Filters take care of this instead)

## 2.3.0 (2/11/2019)

### Added
- 5-6 Cell Fast Calculation

### Changed
- Chip background color priority

### Fixed
- A bug with tickets shown incorrectly
- A bug that caused each calculation process producing less number of results

## 2.4.0 (2/20/2019)

### Added
- Settings: Mark percentage
- Inventory: "Apply All" menu
- Pop-ups can be closed with escape key

### Changed
- Used -> Marked

### Fixed
- A bug with ticket calculation

## 3.0.0 (3/8/2019)

### Added
- "M2" HOC from a new game version

### Removed
- 5B+6 option (5-6 Cell option and filters take care of this)

### Fixed
- A bug with max stat for HOCs with 4 stars or less

## 4.0.0 (3/22/2019)

### Added
- "AT4" HOC from a new game version
- Tag feature
- Chip stats can be put with a keyboard (Enter lets you focus on the next category. PT support with PT display)

### Changed
- File data structure changed
- Help - App

### Removed
- Result chip list: Unmark all

## 4.1.0 (4/26/2019)

### Added
- M2: 4-6 Cell calculation option
- 5 Star M2: "Fill 35-36 cells with 5B+6 chips" calculation option
- Apply All: Add/Remove tag

### Changed
 - Inventory: Several chips selectable for drag and drop, rotation, deletion, and tagging
 
### Fixed
- A bug that caused adding tags from the result section not applying to the inventory filter

## 4.2.0 (5/14/2019)

### Added
- Proxy Chip Extraction (reads chips from login data)
- Open: JSON (.json and .txt)
- Setting - Stat: saved for each HOC and star
- Filter: Minimum PT
- Filter: Exclude tag

### Changed
- Result - PT calculation: Percent based on stat instead of PT
- Result files - Default chip rotations are updated to those of inventory chips. Files before the current version does not need to be changed.

### Fixed
- Chip: A bug with color button

## 4.2.1

### Fixed
- M2 4-6 Cell Calculation: A bug that caused 4-cell chips not placing correctly

## 4.2.2

### Fixed
- A bug that caused "5-6 Cell" options for other HOCs not working after the "Fill 35-36 cells with 5B+6 chips" option for the 5-star M2 is used.
- A bug that caused calibration tickets not being counted correctly when loading pre-4.2.0 version calculation files

### Fix Attempt
- A bug with the proxy chip extraction from other servers

## 4.3.0 (5/25/2019)

### Added
- Language feature
- Language: English

### Changed
- Tip text shown at the bottom instead

### Fix Attempt
- A bug that caused the app not starting
- A bug that caused texts not being displayed properly
- A bug with the proxy chip extraction from other servers

## 4.4.0 (6/6/2019)

### Added
- Proxy extraction: "Only 5 stars" and "Only 4-6 cells" filters, "Mark tagged chip" option

### Changed
- Results that do not get added to the list due to conditions are not counted towards the counter

### Fixed
- Calculation setting: A bug that caused the PT list showing instead of their sum when the PT option is on
- A bug with the proxy chip extraction from other servers

### Language File Changed
- <html> and <center> are not required in the text anymore

## 4.5.0 (6/14/2019)

### Added
- Show/Hide calculation process

### Changed
- Help - Chip: Contents are changed. Language support is added.

### Fixed
- A bug with the proxy chip extraction from other servers (Tested with chinese Bilibili server)

## 4.6.0 (7/2/2019)

### Added
- Calculation Setting - Stat option: Presets
- Filter Setting: Apply recommended filter (Only usable when a preset is selected)

## 4.7.0 (7/7/2019)

### Added
- Chip: Previous level is displayed when calculated with max level option
- Result: Enhancement XP required
- Calculation setting: Result list sorting order (Ticket/XP)

### Changed
- Calculation setting - Mark: Range instead of inequality
- Calculation setting - Mark: Cell/Chip count instead of percentage
- Calculation setting: Does not ask to apply filters if applied already
- Calculation: Less strict internal filtering algorithm for presets and PT

### Fixed
- A bug that caused filter presets being changed

## 4.8.0 (7/25/2019)

### Added
- Calculation setting: Auto-generated presets (beta)
- Auto-check newer versions of the app
- Result: Stat details

### Changed
- Result: Original max now displayed with max set by user
- Proxy extraction: Reorder to match descending acquisition order
- Inventory sort method

### Fixed
- A bug that caused saving, canceling overwrite, and saving again not showing the overwrite warning

## 5.0.0 (8/3/2019)

### Changed
- Proxy extraction - Can read game client version 2.04

### Fix Attempt
- A bug that caused the app not starting

## 5.1.0 (8/11/2019)

### Added
- Sort order - PT
- Appearance setting: Generate language files
- Filter and Marking for opening json files

### Changed
- Calcaultion setting - Preset: lower bound lowered for 2B14

### Removed
- Auto-generated presets

### Fixed
- Calculation setting - Mark: A bug that caused the upper bound always setting to 100

## 5.1.1 (8/12/2019)

### Fixed
- A bug with 1-6 cell calculation

## 5.2.0 (8/17/2019)

### Added
- Calculation: Pause and resume

### Fixed
- Misc. bugs

## 5.2.1 (8/18/2019)

### Fixed
- Calculation setting: A bug that caused stat and PT options being disabled

## 5.2.2 (8/20/2019)

### Fixed
- Result: A bug showing 0% for max of 0
- Result: A bug that caused instantly finished calculations not enabling the save button
- Result: A bug that caused less number of results showing than intended

## 5.2.3 (8/27/2019)

### Added
- Calculation setting: Max type shown in the icon
- Calculation setting: Use old version stat calculation

## 5.3.0 (9/7/2019)

### Added
- Paused calculation can be saved, opened, and resumed
- Stat details: Old version stat

### Changed
- 1-6 cell calculation optimization
- Progress bar optimization
- Update ticket count when opening result file while the matching inventory is loaded

### Removed
- Calculation setting: Use old version stat calculation

## 5.3.1 (9/12/2019)

### Fixed
- A bug that caused inventory chip colors being incorrect

## 5.3.2 (9/26/2019)

### Added
- Datamined "QLZ-04" HOC

## 5.4.0 (10/3/2019)

### Added
- 5-6 cell calculation for 1-4 star QLZ-04
- "Data Mining" feature

## 6.0.0 (10/3/2019)

### Added
- Data Mining: Multithread

### Changed
- Confirmed QLZ-04 data and updated to 6.0.0

## 6.1.0 (10/13/2019)

### Added
- "Add shape tag" option when extracting or loading JSON file
- Pool: shape name as tool tip
- Inventory: chip images now have tag color marks

### Changed
- Mining -> Research

### Fixed
- A bug that caused loading a file and changing a tag not changing the tag from all chips

## 6.1.1 (10/21/2019)

### Added
- Research: The window can be minimized and maximized

### Changed
- Research: Hides/Reopens main window when opening/closing the research window
- Research: Thread number limit based on the spec

## 6.1.2 (10/23/2019)

### Fixed
- Research: A bug that caused the research stopping until the restart whenever the server does not respond, even when the server is restarted
- Language: A bug that caused language exports not in the UTF-8 format

## 6.2.0 (10/25/2019)

### Added
- 5B+6 fast calculation for 5-star QLZ-04 (5-6 fast calculation research is not done yet)

## 6.3.0 (12/8/2019)

### Added
- 5-6 fast calculation for 5-star QLZ-04
- Image scan

### Changed
- Calulation setting - Preset: 5-star QLZ-04 chip filter range is adjusted

### Removed
- App setting: Font (Only font size is configurable)

## 6.3.1 (12/10/2019)

### Changed
- App setting: Use system font first if it supports the language

## 6.4.0 (12/25/2019)

### Added
- Help - Application: Brief usage
- Warning for 5-star boards that cannot have 100% stat

### Changed
- 1-6 cell calculation: Improved algorithm

### Fixed
- A visual bug that caused marked chips not showing proper check marks

## 6.5.0 (1/7/2020)

### Added
- Tool tip text popup
- Estimated calculation time remaining
- Warning popup button appears if the calculation time remaining is too long

### Changed
- (For non-maxable HOCs) warning text -> warning popup button
- Calulation setting - Preset: 5-star QLZ-04 chip filter range is adjusted
- Research: Hidden if server is not up

### Fixed
- Image scan: A bug that caused deleting a rectangle leaving the chip image generated by the app

## 6.5.1 (1/9/2020)

### Fixed
- Font compatibility issue

## 6.5.2 (1/9/2020)

### Fixed
- Proxy: Extraction bug for global server

## 6.5.3 (1/11/2020)

### Changed
- Files: Data extracted chips now have consistent IDs
- Result files: Initial chip levels are (also) updated to those of inventory chips

### Fixed
- A bug that caused result chips not max-leveled when loaded (if max level option is checked)

## 6.5.4 (1/11/2020)

### Fixed
- A possible bug with saving finished calculations

## 6.5.5 (1/11/2020)

### Changed
- Total stat percentage calculation method changed

### Fixed
- A possible bug with saving finished calculations

## 6.5.6 (1/12/2020)

### Fixed
- A stat total percentage calculation bug

## 6.6.0 (1/13/2020)

### Added
- Equipped indicator (based on tag names)
- Rotated indicator in the result chip list

### Changed
- English: "version" is now "iteration"

## 6.7.0 (3/28/2020)

### Added
- Japanese language

### Chaged
- Sort: PT -> depends on the display type

### Removed
- Open inventory file: .txt extension

## 6.7.1 (3/29/2020)

### Fixed
- Text error for non-English settings

## 6.7.2 (4/1/2020)

### Changed
- Update tool will be downloaded automatically when starting an update

## 6.8.0 (4/15/2020)

### Added
- Chip Frequency: Shows the list of chips used in the calculation.

## 6.8.1 (4/16/2020)

### Added
- Missing Japanese language

### Fixed
- A bug that caused the app not reading external language files

### Fix Attempt
- Proxy: The application will try to find the real interal IP address

## 6.8.2 (4/18/2020)

### Fix Attempt
- Proxy: The application will try to find the real interal IP address

## 6.8.3 (4/22/2020)

### Fix Attempt
- Chip: A bug that caused unnecessary rotations

## 6.9.0 (4/28/2020)

### Added
- Setting: Find only symmetric boards (off by default)

## 6.10.0 (5/2/2020)

### Added
- Setting: Advanced mode (if the mode is off, user can only use presets, and filters will be automatically applied.)

## 6.10.1 (5/2/2020)

### Changed
- Setting - Preset: Presets with the most expected number of results are the first on the lists.

## 6.10.2 (5/16/2020)

### Fixed
- A bug that caused the app not reading JSON files from certain game servers

### Fix Attempt
- A bug that caused the interface not working in certain operating systems

## 6.11.0 (5/18/2020)

### Changed
- Normal (non-advanced) mode: the application will ask if you want to apply a filter.

## 6.11.1 (5/23/2020)

### Changed
- Adjusted the window sizes for Filter dialog and Calculation Settings dialog
- A bit of Japanese translation

## 7.0.0 (9/29/2020)

### Added
- New HOC "Mk 153"
- Fast calculations for 1-4 star Mk 153 (5-star Mk 153 is not available yet)

## 7.1.0 (9/30/2020)

### Added
- 5-star Mk 153 fast calculation with 5B-6 chips
