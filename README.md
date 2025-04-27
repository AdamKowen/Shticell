# Shticell

An advanced spreadsheet management system built with JavaFX, featuring a rich graphical user interface and full client-server communication.


## Description

Shticell is a dynamic spreadsheet platform enabling real-time data editing, multi-user collaboration, dynamic analysis with sliders, and version management.  
The system utilizes JSON and XML serialization to manage and optimize data communication between client and server modules.  
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

- Java
- JavaFX (FXML + CSS)
- OkHttp (HTTP client library for server communication)
- GSON (JSON parsing)
- JAXB (XML serialization and deserialization)
- Multi-Module Architecture (ClientApp and ServerApp separation)

## System Architecture

- ClientApp — UI layer, containing FXML files, CSS, and JavaFX controllers
- ServerApp - Engine — Core logic for spreadsheets, ranges, expressions, dependency calculation
- ServerApp - WebEngine — HTTP layer, handling servlet-based communication and user sessions

## Screenshots
<img width="837" alt="Screenshot 2025-04-27 at 11 34 48" src="https://github.com/user-attachments/assets/7284f0a3-63ea-41e7-b287-4a700587363a" />
<img width="837" alt="Screenshot 2025-04-27 at 11 42 44" src="https://github.com/user-attachments/assets/8723793d-368a-4d11-b788-f94e9545999b" />
<img width="837" alt="Screenshot 2025-04-27 at 11 38 41" src="https://github.com/user-attachments/assets/69a4a076-65ed-410f-a10a-0226798ff31a" />
<img width="837" alt="Screenshot 2025-04-27 at 11 39 40" src="https://github.com/user-attachments/assets/ccac4b5c-8077-4217-a15e-23a858d13cf3" />
<img width="837" alt="Screenshot 2025-04-27 at 11 40 32" src="https://github.com/user-attachments/assets/7bed5cd5-1c0e-4fac-a1f4-3dbb9ac07ca7" />
<img width="837" alt="Screenshot 2025-04-27 at 11 40 57" src="https://github.com/user-attachments/assets/0a3860ce-4874-432c-a7ce-c7aeb52981e7" />
<img width="837" alt="Screenshot 2025-04-27 at 11 41 35" src="https://github.com/user-attachments/assets/1f259fce-2890-4fb4-945d-66dc0535eab2" />












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
