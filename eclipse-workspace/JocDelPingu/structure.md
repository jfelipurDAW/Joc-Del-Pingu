# Project Structure: Joc Del Pingu (Penguin Game)

This document provides a comprehensive overview of the project structure for "Joc Del Pingu," a Java-based board game. It is designed to help AI models and developers navigate the codebase efficiently.

---

## 1. Project Overview
- **Type**: Java Application (JavaFX)
- **Architecture**: Model-View-Controller (MVC)
- **Key Features**: Multiplayer board game, inventory system, event-driven squares, localization support, and save/load functionality.

---

## 2. Directory & File Breakdown

### Root Directory
| File/Folder | Description |
| :--- | :--- |
| `src/` | Main source code directory. |
| `bin/` | Compiled bytecode (standard Java project structure). |
| `overrides.ps1` | PowerShell utility script for environment/build overrides. |
| `refactor.js` | JavaScript utility for codebase refactoring or automation. |
| `external-libraries/` | Third-party dependencies (YAML, JDBC, etc.). |
| `javafx-lib/` | JavaFX specific libraries. |
| `penguin_theme.css` | Global application theme for the UI. |
| `.project` / `.classpath` | Eclipse project configuration files. |

---

### Source Directory (`src/`)

#### 📦 `model/` (Core Logic)
Handles the data and business logic of the game.

- **`model.board`**:
  - `Board.java`: Manages the collection of squares and board generation.
  - `Square.java`: Abstract class/Interface for a single board tile.
  - `SquareType.java`: Enum defining types of squares (Normal, Ice, Bear, etc.).
  - `EventManager.java`: Handles triggers and events when a player lands on a square.
  - **`squares/`**: Specific implementations of square logic:
    - `S_Bear.java`, `S_IceHole.java`, `S_Sled.java`, `S_Event.java`, `S_Normal.java`, `S_Start.java`, `S_End.java`.

- **`model.game`**:
  - `Game.java`: Main game instance controller.
  - `GameManager.java`: Coordinates high-level game flow.
  - `TurnController.java`: Manages player turns and turn order.
  - `PlayerManager.java`: Handles player life cycles and states.
  - `SaveLoadService.java`: Logic for persisting game state (YAML/Binary).
  - `SoundManager.java`: Handles audio playback.

- **`model.entity`**:
  - `Player.java`: Data model for a player (position, inventory, stats).
  - `Seal.java`: Data model for the "Seal" NPC/Obstacle.
  - `Entity.java`: Base class for players and NPCs.

- **`model.item`**:
  - `Inventory.java`: Manages objects held by entities.
  - `GameObject.java`: Base class for items.
  - **`objects/`**: Concrete items like `Dice.java`, `Fish.java`, `SnowBall.java`.

- **`model.config`**:
  - `GameSetupConfig.java`: Static storage for game initialization parameters.
  - `LangConfig.java` / `Lang.java`: Localization engine (i18n).
  - `CryptoUtil.java`: Utility for secure data handling (e.g., password hashing).

- **`model.db`**:
  - `BBDD.java`: Database connection and CRUD operations (likely for scores or user accounts).

---

#### 🎮 `controller/` (UI Logic)
Connects the Model with the View using JavaFX Controller patterns.

- **`controller.ui`**:
  - `MainMenuController.java`: Logic for the home screen.
  - `PlayerSetupController.java`: Handles player selection and game configuration.
  - `GameController.java`: Main gameplay UI logic (HUD, actions).
  - `GameBoardController.java`: Specifically manages the visual rendering of the board.

- **`controller.main`**:
  - `MainMenu.java`: Application entry point (Main class).

---

#### 🖼️ `view/` (Layouts & UI)
- **`fxml/`**: XML definitions for the UI screens.
  - `mainMenu.fxml`, `playerSetup.fxml`, `gameBoard.fxml`.
- **`ui/`**: Custom UI components or panels.
  - `BBDDPanel.java`: Specialized UI for database-related views.

---

#### 🎨 `assets/` (Static Resources)
- **`css/`**: Stylesheets for UI components.
- **`font/`**: Custom typography (e.g., Pixel fonts).
- **`lang/`**: Localization files in YAML format (`en.yml`, `es.yml`, `ca.yml`, etc.).
- **`sprites/`**: Visual assets:
  - `entities/`: Player and NPC images.
  - `squares/`: Individual square tile textures.
  - `objects/`: Item icons.
  - `backgrounds/`: Screen backgrounds.

---

## 3. Technical Stack & Dependencies
Defined in `module-info.java`:
- **JavaFX**: UI Framework.
- **SnakeYAML**: For parsing localization and configuration files.
- **Java SQL (JDBC)**: For database connectivity.
- **Java Desktop/Swing**: Support for legacy components or specialized UI interactions.

---

## 4. Key Workflows for Developers
- **Adding a Square**: Create a class in `model.board.squares` and register its type in `SquareType`.
- **Adding an Item**: Create a class in `model.item.objects` and define its behavior in `GameObject`.
- **Modifying UI**: Edit FXML files in `view/fxml` and update corresponding methods in `controller.ui`.
- **Adding Languages**: Add a new YAML file to `assets/lang/` and update `Lang.java`.
