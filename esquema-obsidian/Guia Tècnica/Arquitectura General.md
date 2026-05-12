# Arquitectura General

El projecte segueix una variant **MVC** clàssica adaptada a JavaFX amb FXML:

- **Model** — paquets `model.*` (lògica, dades, persistència).
- **View** — fitxers FXML + CSS sota `view.fxml/` i `assets/css/`.
- **Controller** — classes Java sota `controller.*` que connecten els FXML amb el model.

## Vista de mòduls

```mermaid
flowchart TB
    subgraph view["view (FXML + CSS)"]
        FXML[mainMenu.fxml<br/>playerSetup.fxml<br/>gameBoard.fxml<br/>playerStats.fxml]
        CSS[style.css<br/>gameBoardStyle.css]
    end

    subgraph controller["controller"]
        MM[MainMenu - Application]
        MMC[MainMenuController]
        PSC[PlayerSetupController]
        GBC[GameBoardController]
        PST[PlayerStatsController]
    end

    subgraph model_game["model.game"]
        GM[GameManager]
        BM[BoardManager]
        PM[PlayerManager]
        TC[TurnController]
        SLS[SaveLoadService]
        SM[SoundManager]
        Game
    end

    subgraph model_other["model"]
        Board[model.board]
        Entity[model.entity]
        Item[model.item]
        Config[model.config]
        DB[model.db]
    end

    MM --> FXML
    FXML --> controller
    controller --> model_game
    model_game --> model_other
    DB --> Oracle[(Oracle BBDD)]
```

## Cicle de vida d'una partida

```mermaid
sequenceDiagram
    participant U as Usuari
    participant MM as MainMenu (Application)
    participant MMC as MainMenuController
    participant PSC as PlayerSetupController
    participant GBC as GameBoardController
    participant GM as GameManager
    participant BM as BoardManager
    participant PM as PlayerManager
    participant TC as TurnController

    U->>MM: java MainMenu
    MM->>MMC: load mainMenu.fxml
    U->>MMC: Nova Partida
    MMC->>PSC: load playerSetup.fxml
    U->>PSC: omple fitxes + Iniciar
    PSC->>GameSetupConfig: setPlayers, setSealEnabled
    PSC->>GBC: load gameBoard.fxml

    GBC->>GM: new GameManager("LOCAL_MATCH", 0)
    GBC->>Board: new Board() + createNewBoard()
    GBC->>TC: new TurnController + addPlayer per cada
    GBC->>GBC: drawBoard() + updateHUD()

    loop fins gameOver
        U->>GBC: rollDice()
        GBC->>GM: playTurn(diceResult)
        GM->>PM: movePlayer
        GM->>BM: executeSquareAction
        BM-->>GBC: ActionResult
        GBC->>TC: nextTurn()
    end

    GBC->>SaveLoadService: recordGameResult(...)
```

## Capes i responsabilitats

| Capa | Paquets | Responsabilitat |
|------|---------|-----------------|
| **Vista** | `view.fxml`, `assets/css` | Descripció declarativa de la UI |
| **Controlador UI** | [[Paquet controller]] | Bind FXML ↔ model, gestió d'events JavaFX |
| **Coordinador** | [[Paquet model.game]] (`GameManager`, `BoardManager`, `PlayerManager`) | Regles del joc |
| **Estat del joc** | [[Paquet model.game]] (`Game`, `TurnController`) | Estructures vives |
| **Domini** | [[Paquet model.board]], [[Paquet model.entity]], [[Paquet model.item]] | Tauler, jugadors, objectes |
| **Configuració** | [[Paquet model.config]] | Idiomes (`Lang`/`LangConfig`), criptografia (`CryptoUtil`), bag de setup (`GameSetupConfig`) |
| **Persistència** | [[Paquet model.db]] (`BBDD`) + `SaveLoadService` | Connexió Oracle + serialització YAML |

## Punts d'entrada

- **`controller.main.MainMenu.main()`**: bootstrap. Carrega l'idioma per defecte i invoca `Application.launch()`.
- **`view.ui.BBDDPanel.main()`**: utilitat secundària per provar les operacions CRUD contra Oracle (no és part del joc).

## Patrons utilitzats

| Patró | On |
|-------|----|
| **MVC** | Global |
| **Singleton** | `LangConfig`, `SoundManager` |
| **Template Method** | `Square.action(player)` abstracte amb subclasses `S_*` |
| **Strategy** (via *switch*) | `BoardManager.executeSquareAction` |
| **Observer / Listener** | `LangConfig.addLanguageChangeListener` |
| **Data Transfer Object** | `ActionResult` (DTO entre lògica i UI) |

## Decisions de disseny destacades

> [!info] Per què `GameSetupConfig` és estàtic?
> Els controllers JavaFX són instanciats pel `FXMLLoader`, així que passar estat directament entre `PlayerSetupController` i `GameBoardController` és incòmode. Una classe estàtica fa de "bossa" temporal.

> [!warning] Una sola finestra
> Tot el joc viu en una **única `Stage`** que canvia d'`Scene` segons la pantalla. Això facilita la lògica de música (`SoundManager`) i el redimensionament.

> [!tip] Cap *thread* de fons
> Tota la lògica corre al **JavaFX Application Thread**. Les animacions utilitzen `Timeline` / `PauseTransition`, no fils separats.

## Enllaços relacionats

- [[Estructura de Paquets]] — mapa detallat de paquets
- [[Diagrama de Classes]] — UML general
- [[Flux de Joc]] — seqüències detallades
