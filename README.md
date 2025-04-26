# Shticell

An advanced spreadsheet management system built with JavaFX, featuring a rich graphical user interface and full client-server communication.


## Description

Shticell is a dynamic spreadsheet platform enabling real-time data editing, multi-user collaboration, dynamic analysis with sliders, and version management.  
The project emphasizes a responsive and intuitive user interface built with JavaFX and a clean modular architecture.

## Features

- Real-time spreadsheet editing with instant server synchronization
- Integrated group chat functionality
- Dynamic multi-variable analysis using sliders
- Row sorting and filtering based on selected ranges
- Built-in Dark Mode
- Responsive Mode for live cell updates while typing
- Version history management
- Customizable cell styling (background color, text color, alignment, row/column sizing)
- Multi-user management with permission controls (view, edit, admin roles)

## Technologies Used

- Java 17
- JavaFX (FXML + CSS)
- Servlets (HTTP request handling)
- GSON (JSON parsing)
- Multi-Module Architecture (ClientApp and ServerApp separation)

## System Architecture

- ClientApp — UI layer, containing FXML files, CSS, and JavaFX controllers
- ServerApp - Engine — Core logic for spreadsheets, ranges, expressions, dependency calculation
- ServerApp - WebEngine — HTTP layer, handling servlet-based communication and user sessions

## Screenshots



## Installation

```bash
git clone https://github.com/AdamKowen/Shticell.git
```

1. Open the project in an IDE that supports JavaFX (e.g., IntelliJ IDEA, Eclipse).
2. Run the server application (ServerApp) first.
3. Run `AppMain.java` from the ClientApp module.
4. Ensure the server is running for proper client-server interaction.

## Summary

Shticell showcases advanced Java development with an emphasis on real-time data management, collaborative features, modular architecture, and a user-friendly graphical interface built with JavaFX.

## Contact

Adam Gilboa Kowen — [adam.kowen@gmail.com](mailto:adam.kowen@gmail.com)

Ron Alima - [Ronalima256@gmail.com](mailto:Ronalima256@gmail.com)
