# Obsidian Todo Widget
Android Widget to display an interactive todo list from a markdown file

## Install
1. Download the .apk file from the [latest release](https://github.com/YukiGasai/obsidian-todo-widget/releases/latest)
1. Install with package manager
1. Run the main app once
1. Allow the storage permission to read and write external files  

## Setup
1. Select widget from the widget list
1. Drag to Homescreen
1. Enter the Obsidian Vault name to use
1. Configure the file to use
  1. Press "Select" to define the folder where the file(s) is/are stored.
  1. Enter the single filename or enter a date pattern
1. If you want to limit the the tasks to a specific header enter the header name into the "Header" field
  1. If you want to include sub Headers check the "Include Sub Headers" switch
1. Press Create

Note:
- The widget only work for Android Oreo(SDK 26) and up.
- Make sure the date format inside the curly brackets exists. Here are some [options](https://www.datetimeformatter.com/how-to-format-date-time-in-java-8/)

## Example
![preview](https://github.com/YukiGasai/obsidian-todo-widget/assets/38146192/49e5f95d-7ffa-4883-a13c-1f878a9908fe)

## Filename examples
### Static file
- `Quicknote`
- `Todos`

### Daily note
- `{{yyyy-MM-dd}}`
- `Daily-{{dd}}-{{MM}}-{{yyyy}}`

## Demonstration
https://github.com/YukiGasai/obsidian-todo-widget/assets/38146192/cfc415c2-cdaa-43b9-a005-42ed3df12608

## Disclaimer
Due to limited android / kotlin experience, this project is certainly messy.
