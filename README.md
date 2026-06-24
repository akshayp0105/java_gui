# Attendance Calculator Pro

A Java Swing-based attendance tracking application for students.

## Features

- Calculate and track attendance for multiple subjects
- See if you're safe or need more classes to meet requirements
- Color-coded rows (green = safe, red = alert)
- Overall attendance summary with min/max/average stats
- Search and filter subjects in the table
- Table column sorting by clicking headers
- Dark mode toggle
- Live date/time status bar

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+S | Save data |
| Ctrl+L | Load data |
| Ctrl+E | Export as CSV |
| Ctrl+A | Select all rows |
| Enter | Calculate & Add subject |
| Delete | Remove selected row |

## File Operations

- **Save**: Save current data to database file
- **Load**: Load previously saved data
- **Export as CSV**: Export table data to CSV file
- **Import from CSV**: Import subjects from a CSV file

## How to Use

1. Enter subject name, total classes, attended classes, and required percentage
2. Click **Calculate & Add** or press **Enter** to add to the table
3. Status column shows if you're safe or need more classes
4. Use File menu to save, load, export, or import data
5. Toggle **Dark Mode** from the View menu
6. Use the **Search** field to filter subjects in the table

## Requirements

- Java JDK 8 or higher
