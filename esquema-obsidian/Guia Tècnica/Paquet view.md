# Paquet `view`

Conté els arxius FXML que descriuen declarativament la UI, els CSS que els estilen, i una classe Java (`BBDDPanel`) que serveix com a script de proves contra Oracle.

## Estructura

```
view/
├── ui/
│   └── BBDDPanel.java
└── fxml/
    ├── mainMenu.fxml      ← MainMenuController
    ├── playerSetup.fxml   ← PlayerSetupController
    ├── gameBoard.fxml     ← GameBoardController
    └── playerStats.fxml   ← PlayerStatsController
```

I els CSS associats:

```
assets/css/
├── style.css              ← menú + setup + estadístiques
└── gameBoardStyle.css     ← pantalla del joc
```

## Fitxers FXML

### `mainMenu.fxml`

Arrel: `StackPane#rootPane.mainmenu_bg`. Conté un `HBox > VBox` amb:

```
StackPane          (rootPane)
└── HBox alignment=CENTER
    └── VBox alignment=CENTER spacing=15
        ├── StackPane
        │   ├── Text  (titleShadowText, classe mainmenu_title_shadow)
        │   └── Text  (titleText, classe mainmenu_maintitle)
        ├── Button   (newGame_button, onAction=#handleNewGame)
        ├── Button   (loadGame_button, onAction=#handleLoadGame)
        ├── Button   (stats_button, onAction=#handleStats)
        └── ComboBox (language_combobox)
```

Controller: **`MainMenuController`** ([[Paquet controller]]).

### `playerSetup.fxml`

Arrel: `StackPane#rootPane.mainmenu_bg`. Estructura:

```
VBox alignment=TOP_CENTER spacing=16
├── HBox.nav-bar       (backButton)
├── Text               (titleText, "setup-screen-title")
├── HBox               (numPlayersCombo + sealCheckBox + labels)
├── ScrollPane
│   └── VBox          (playersContainer — fitxes generades dinàmicament)
└── HBox               (selectExistingButton + startGameButton)
```

Controller: **`PlayerSetupController`**.

### `gameBoard.fxml`

Arrel: `StackPane#mainStack`. Conté un `BorderPane#rootPane`:

```
StackPane#mainStack
└── BorderPane#rootPane
    ├── top:    HBox.top-bar (game title + turn indicator)
    ├── center: StackPane#boardContainer
    │           └── GridPane#grid.game-grid (10x5)
    ├── right:  VBox#rightPanel (player card + inventory + seal status)
    └── bottom: FlowPane.action-bar
                ├── rollDiceButton
                ├── rollFastDiceButton
                ├── rollSlowDiceButton
                ├── throwSnowballButton
                ├── saveGameButton
                ├── historyButton
                ├── backButton
                └── homeButton
```

Controller: **`GameBoardController`**.

> [!info] `right` ocultat en runtime
> El `rightPanel` es deixa al FXML però `GameBoardController.updateHUD()` el fa invisible perquè la informació es renderitza a la *hotbar* dinàmica al `top`.

### `playerStats.fxml`

Pantalla amb una `VBox#statsContainer` on `PlayerStatsController` afegeix files dinàmicament.

## CSS

### `style.css`

Estils per al menú, setup i estadístiques. Classes destacades:
- `mainmenu_bg` — fons del menú principal
- `mainmenu_maintitle` / `mainmenu_title_shadow` — text en capes
- `mainmenu_button_newgame` / `_loadgame` — botons grans
- `mainmenu_language_combo` — dropdown d'idioma
- `setup-card` / `setup-card-title` — fitxa de jugador
- `setup-screen-title` / `setup-control-label` / `setup-combo` / `setup-checkbox`
- `stats-row` / `stats-cell-name` / `stats-cell-colour` / etc.
- `nav-btn-back` / `nav-btn-home` — navegació

### `gameBoardStyle.css`

Estils específics del tauler:
- `game-grid` / `square` — graella i caselles base
- `square-normal` / `square-ice-hole` / `square-sled` / etc. — un per a cada SquareType
- `hotbar` / `hotbar-portrait` / `hotbar-title` / `hotbar-title-shadow` / `hotbar-player-name`
- `inventory-slot` / `inventory-quantity`
- `dice-result-badge` — overlay del resultat del dau
- `btn-roll` / `btn-fast-dice` / `btn-slow-dice` / `btn-snowball` / `btn-save` / `btn-history`
- `top-bar` / `action-bar` / `right-panel` / `seal-card`

## `BBDDPanel`

Classe utilitat (no és UI real, malgrat estar al paquet `view.ui`). Té un `main()` que:

1. Connecta a Oracle.
2. Fa INSERT, UPDATE, DELETE de prova sobre la taula `ENTITY` amb un usuari `PinguTest`.
3. Imprimeix els resultats per consola.

> [!warning] Només per debug
> No es crida des del joc real. Es pot executar manualment des d'Eclipse per validar que la BBDD respon.

## Recursos addicionals

| Tipus | Localització |
|-------|--------------|
| Sprites de jugador | `assets/sprites/entities/player/*.png` |
| Sprites de foca | `assets/sprites/entities/seal/*.png` |
| Fons de casella | `assets/sprites/squares/background/Square-{0..6}.png` |
| Foreground de casella | `assets/sprites/squares/foreground/{TYPE}.png` |
| Fons del menú | `assets/sprites/backgrounds/1.png` |
| Icones d'objecte | `assets/sprites/objects/{snowball,fish,fastdice,slowdice}.png` |
| Música | `assets/sounds/main_screen_music.wav`, `bg_music.wav` |
| SFX | `assets/sounds/{dice,event,bear,seal,snowball}.wav` |
| Fonts | `assets/font/pixel-game.regular.otf` + extrude + unicode |
| Traduccions | `assets/lang/{en,es,ca,fr,pt,ro,ar,ru,uk,ff,jp}.yml` |

## Enllaços relacionats

- [[Paquet controller]] — qui connecta amb cada FXML
- [[Sistema de Sprites]] — com es dibuixen sobre les caselles
- [[Paquet model.db]] — `BBDDPanel` test
