# Joc del Pingu — Spec Compliance Report

Snapshot date: 2026-05-12

Cross-check of the current codebase (`eclipse-workspace/JocDelPingu/src/`) against
the requirements in **JOC DEL PINGU - Enunciat.pdf**, plus the internal coding
rules: **no `continue;`**, **no `break;` outside `switch`**, **all code and
comments in English**.

Legend: ✅ done · ⚠️ partial · ❌ missing

---

## 1. Development tasks (entorns de desenvolupament)

| Requirement | Status | Evidence / Gap |
|---|---|---|
| Requirements analysis document (formal) | ❌ | Not present in `Joc-Del-Pingu/`. Need a separate `docs/REQUIREMENTS.md` (or PDF) with the 5 sections listed in the spec. |
| UML Class Diagram | ❌ | Not in repo. To produce (e.g., PlantUML in `docs/uml/classes.puml` or PNG). |
| UML Use Case Diagram | ❌ | Not in repo. Same as above (`docs/uml/usecases.puml`). |
| JavaDoc on every class / method / attribute | ⚠️ | Some classes have it (`Seal`, `EventManager`, `SoundManager`, `SaveLoadService`). Many do not (`Board`, `GameManager`, `Inventory`, `Player`, `Entity`, square subclasses, `BBDD`, `PlayerManager`, `TurnController`, controllers). Needs a full pass. |
| GitHub version control with frequent commits | ✅ | Project is a git repo (`git log` shows multiple commits). |
| Digitalization Plan (`Pla de Digitalització`) | ❌ | Not in repo (this is a separate deliverable for the presentation). |

---

## 2. Basic level

| Requirement | Status | Evidence |
|---|---|---|
| Board with **50+ squares**, randomly generated | ✅ | [Board.java:15-17](eclipse-workspace/JocDelPingu/src/model/board/Board.java#L15-L17) — `widthBoard=10 × heightBoard=5 = 50`. Randomized in `createNewBoard()`. |
| Square types: **Penguin / Bear / Ice Hole / Sled / Event** | ✅ | [SquareType.java](eclipse-workspace/JocDelPingu/src/model/board/SquareType.java) declares `ICE_HOLE`, `SLED`, `BEAR`, `EVENT`, `BROKEN_FLOOR`, `NORMAL`, `START`, `END`. |
| Penguin token with id (color, name, etc.) | ✅ | `Player` has `name`, `colour`, `avatarPath`. Sprite tinted via `Lighting` per player colour. |
| Bear → back to start | ✅ | [BoardManager.handleBear:54-62](eclipse-workspace/JocDelPingu/src/model/game/BoardManager.java#L54-L62) → `player.setSquare(0)`. |
| Ice Hole → previous hole (or start if first) | ✅ | [Board.getDestination:101-105](eclipse-workspace/JocDelPingu/src/model/board/Board.java#L101-L105) returns `IceHole_Array.get(listIndex-1)` or `0`. |
| Sled → next sled (or nothing if last) | ✅ | [Board.getDestination:106-111](eclipse-workspace/JocDelPingu/src/model/board/Board.java#L106-L111) + `BoardManager.handleSled`. |
| Event square → random event | ✅ | [EventManager.triggerEvent](eclipse-workspace/JocDelPingu/src/model/board/EventManager.java) with weighted probabilities. |
| Roll dice (normal/special) OR throw snowballs per turn | ✅ | `rollDice` / `rollFastDice` / `rollSlowDice` / `throwSnowball` in [GameBoardController.java](eclipse-workspace/JocDelPingu/src/controller/ui/GameBoardController.java). |
| Inventory: max **3 dice** | ✅ | [Inventory.java:17](eclipse-workspace/JocDelPingu/src/model/item/Inventory.java#L17) `MAX_DICE = 3`. |
| Inventory: max **2 fish** | ✅ | [Inventory.java:16](eclipse-workspace/JocDelPingu/src/model/item/Inventory.java#L16) `MAX_FISH = 2`. |
| Inventory: max **6 snowballs** | ✅ | [Inventory.java:15](eclipse-workspace/JocDelPingu/src/model/item/Inventory.java#L15) `MAX_SNOWBALLS = 6`. |
| Inventory in DB (dice persistence) | ✅ | Serialized into the encrypted YAML `SAVED_GAMES.GAME_DATA`. |
| Save / load game **encrypted in DB** | ✅ | AES-128 + Base64 in [CryptoUtil](eclipse-workspace/JocDelPingu/src/model/config/CryptoUtil.java); `SAVED_GAMES.GAME_DATA` is a CLOB. |
| Saved data: player positions | ✅ | `pMap.put("square", p.getSquareIndex())`. |
| Saved data: board state | ✅ | `state.put("board", boardState)` (list of SquareType names). |
| Saved data: each player's inventory | ✅ | `pMap.put("inventory", invMap)`. |
| Saved data: event history | ✅ | `pMap.put("eventHistory", ...)`. |
| Event — get fish | ✅ | `EventManager.handleGetFish`. |
| Event — get 1-3 snowballs | ✅ | `EventManager.handleGetSnowballs` (`random.nextInt(3)+1`). |
| Event — fast dice (5-10, low prob) | ✅ | `Dice.FASTDICE` range 5-10 ([Dice.java:10-11](eclipse-workspace/JocDelPingu/src/model/item/objects/Dice.java#L10-L11)); 8% probability in EventManager. |
| Event — slow dice (1-3, high prob) | ✅ | `Dice.SLOWDICE` range 1-3 ([Dice.java:7-8](eclipse-workspace/JocDelPingu/src/model/item/objects/Dice.java#L7-L8)); 30% probability — the highest. |

**Basic level: 100% complete.**

---

## 3. Intermediate level

| Requirement | Status | Evidence |
|---|---|---|
| Multiplayer min 2 | ✅ | `PlayerSetupController` accepts 1–4 players via `numPlayersCombo`. |
| Turn management persisted in DB | ✅ | `state.put("currentTurn", turnController.getCurrentTurnIndex())` + restored in `SaveLoadService.loadGame`. |
| Broken floor squares | ✅ | [BoardManager.handleBrokenFloor:74-100](eclipse-workspace/JocDelPingu/src/model/game/BoardManager.java#L74-L100). |
| Broken floor: > 5 items → fall to start | ✅ | `if (totalItems > 5) player.setSquare(0); convertBrokenFloorToIceHole(...)`. |
| Broken floor: 1-5 items → lose turn / lose item | ✅ | Same method — 50/50 between `setSkipNextTurn(true)` and `removeRandomItem`. |
| Broken floor: no items → no penalty | ✅ | Returns `BROKEN_FLOOR_SAFE`. |
| Event — lose turn | ✅ | `EventManager.handleLoseTurn` (12% probability). |
| Event — lose random item | ✅ | `EventManager.handleLoseItem` (10% probability). |

**Intermediate level: 100% complete.**

---

## 4. Impossible level

| Requirement | Status | Evidence |
|---|---|---|
| Max 4 players + CPU (seal) | ✅ | 1-4 players + optional `Seal` (toggled by `sealCheckBox`). |
| CPU seal AI — autonomous turn | ✅ | [Seal.playTurn:119-162](eclipse-workspace/JocDelPingu/src/model/entity/Seal.java#L119-L162). |
| Seal passes through player → loses half inventory | ✅ | [Seal.passThrough:78-81](eclipse-workspace/JocDelPingu/src/model/entity/Seal.java#L78-L81) → `player.loseHalfInventory()`. |
| Seal lands on player → tail hit → previous ice hole | ✅ | [Seal.hitPlayer:63-73](eclipse-workspace/JocDelPingu/src/model/entity/Seal.java#L63-L73) + `findPreviousIceHole`. |
| Player war: same square → most snowballs wins | ✅ | [PlayerManager.snowballWar:21-58](eclipse-workspace/JocDelPingu/src/model/game/PlayerManager.java#L21-L58). |
| Player war: both spend ALL snowballs | ✅ | `useObject(SNOWBALL, balls1)` and `useObject(SNOWBALL, balls2)` before any decision. |
| Player war: loser retreats by snowball difference | ✅ | `Math.max(0, target.getSquareIndex() - difference)`. |
| Player war: tie → both spend, nobody retreats | ✅ | `SNOWBALL_WAR_TIE` branch leaves positions unchanged. |
| Seal interaction — feed fish → blocked 2 turns | ✅ | [Seal.bribeSeal:50-58](eclipse-workspace/JocDelPingu/src/model/entity/Seal.java#L50-L58) sets `blockedTurns = 2`. |
| Seal interaction — no fish → previous hole | ✅ | `Seal.interact` → `hitPlayer`. |
| Event — snowmobile (advance to next sled) | ✅ | `EventManager.handleSnowmobile` (5% probability). |

**Impossible level: 100% complete.**

---

## 5. Extras (Digitalization module)

| Requirement | Status | Evidence |
|---|---|---|
| Animations | ✅ | Bear cinematic, seal slide-in, dice GIF, dice number badge, snowball flash, war flash, damage flash, win-screen sprite bobbing. |
| Player avatar customization | ✅ | `PlayerSetupController` `avatarBtn` + `Player.avatarPath`; rendered in hotbar and on win screen. |
| Sound effects on events | ✅ | `SoundManager`: dice, event, bear, seal, snowball. |
| Background music | ✅ | Two looping tracks: `main_screen_music.wav` (menus) + `bg_music.wav` (in-game). |
| Seal sound on movement | ✅ | `playSealSound()` triggered in `playSealTurnAnimated`. |

**Extras: 100% complete.**

---

## 6. English videotutorial

| Requirement | Status |
|---|---|
| Video explaining goal, genre, mechanics, controls, rules | ❌ Not in repo (separate deliverable). |

---

## 7. Internal coding rules

| Rule | Status | Detail |
|---|---|---|
| **No `continue;` anywhere** | ✅ | `grep -r '\bcontinue\s*;' src/**/*.java` → 0 matches. |
| **No `break;` outside `switch`** | ✅ | All remaining 28 `break;` are inside switch-case constructs (verified previously). |
| **All comments & identifiers in English** | ❌ | **30+ violations** — see section below. |

---

## 8. Non-English text inventory (must fix)

### 8a. Spanish/Catalan comments

| File | Line | Snippet |
|---|---|---|
| [BBDD.java](eclipse-workspace/JocDelPingu/src/model/db/BBDD.java) | 10 | `Clase que proporciona métodos para interactuar con una base de datos Oracle.` |
| BBDD.java | 15-16 | `Intenta establecer una conexión a la base de datos Oracle. NO HACE FALTA QUE ENTENDÁIS CÓMO FUNCIONA...` |
| BBDD.java | 40 | `aquí NO hago trim por si la contraseña tuviera espacios` |
| BBDD.java | 62 | `Cierra la conexión con la BBDD.` |
| BBDD.java | 76 | `Realiza una inserción en la base de datos.` |
| [SaveLoadService.java](eclipse-workspace/JocDelPingu/src/model/game/SaveLoadService.java) | 21 | `Recupera todos los IDs de las partidas guardadas para el selector.` |
| SaveLoadService.java | 48 | `Serializar el tablero` |
| SaveLoadService.java | 237 | `Guarda un nuevo perfil de jugador en la tabla ENTITY.` |
| SaveLoadService.java | 256 | `MERGE: actualiza si el nombre ya existe, inserta si es nuevo` |
| SaveLoadService.java | 333 | `Recupera todos los jugadores registrados para poder elegirlos.` |
| [MainMenuController.java](eclipse-workspace/JocDelPingu/src/controller/ui/MainMenuController.java) | 144, 157 | Catalan: `Afegint text bitmap...`, `Text creat amb ... caràcters` |
| [Inventory.java](eclipse-workspace/JocDelPingu/src/model/item/Inventory.java) | 28 | `///   GETTERS I SETTERS    ///` (Catalan-ish header) |

### 8b. Spanish identifiers (methods / variables)

| File | Identifier | Suggested rename |
|---|---|---|
| [BBDD.java:23](eclipse-workspace/JocDelPingu/src/model/db/BBDD.java#L23) | `conectarBaseDatos` | `connectToDatabase` |
| BBDD.java:27 | `entorno` | `environment` |
| BBDD.java:66 | `cerrar` | `close` |
| BBDD.java:200 | `registrarJugadorEnBD` | `registerPlayerInDB` |
| BBDD.java:208 | `encriptarTexto` | `encryptText` |
| BBDD.java:212 | `guardarPartida` | (unused; consider deleting) |
| [PlayerSetupController.java:228](eclipse-workspace/JocDelPingu/src/controller/ui/PlayerSetupController.java#L228) | `existentes` | `existingPlayers` |
| PlayerSetupController.java:244 | `promptExistingPlayerChoice(existentes)` | param → `existingPlayers` |
| PlayerSetupController.java:254 | `nombreSeleccionado` | `selectedName` |
| PlayerSetupController.java:273 | `asignarJugadorAlPrimerInputVacio` | `assignPlayerToFirstEmptyInput` |
| PlayerSetupController.java:291 | `mostrarAlerta` | `showAlert` |
| GameBoardController.java | `mostrarAlerta` | `showAlert` |

### 8c. Spanish user-facing strings (should be English or LangConfig keys)

| File | Line | String |
|---|---|---|
| BBDD.java | 24 | `"Intentando conectarse a la base de datos..."` |
| BBDD.java | 48 | `"Database connection established successfully."` (already English ✅) |
| MainMenuController.java | 207 | `"No hay partidas guardadas."` |
| MainMenuController.java | 220-222 | `"Cargar Partida"`, `"Selecciona la partida..."`, `"ID de partida:"` |
| MainMenuController.java | 230-240 | `"Partida X cargada..."`, `"Autenticación cancelada..."`, `"Error al cargar la partida..."` |
| GameBoardController.java | 664-666 | `"Guardar Partida"`, `"Introduce un nombre..."`, `"Nombre:"` |
| GameBoardController.java | 672-673 | `"Éxito"`, `"Partida '...' guardada correctamente."`, `"Error"`, `"No se pudo guardar..."` |
| [BBDDPanel.java](eclipse-workspace/JocDelPingu/src/view/ui/BBDDPanel.java) | 36 | `"¡Pruebas terminadas y conexión cerrada! Todo funciona de 10."` |

---

## 9. Suggested fix order (proposed)

1. **Rename Spanish methods/variables** across BBDD, SaveLoadService, controllers (rename-safe refactor, won't break behaviour).
2. **Translate all Spanish/Catalan comments** to English.
3. **Translate user-facing strings** — short-term: hard-code English. Mid-term: route through `LangConfig` with new `Lang` enum keys so the new dialogs follow the same i18n path as the rest of the menu.
4. **Add JavaDoc** to every public class / method / attribute that still lacks it (≈25 classes).
5. **Generate UML** (class + use case) with PlantUML — store under `docs/uml/` so the PDF expectation is met.
6. **Write Requirements document** (`docs/REQUIREMENTS.md`) with the 5 sections listed in the spec.
7. **(Out of scope of this repo)** Digitalization plan + English videotutorial — separate deliverables.

---

## 10. Score summary

| Dimension | Status |
|---|---|
| Basic level | ✅ 100% |
| Intermediate level | ✅ 100% |
| Impossible level | ✅ 100% |
| Extras (animations, sound, avatars) | ✅ 100% |
| Coding rule: no `continue;` | ✅ |
| Coding rule: no `break;` outside switch | ✅ |
| Coding rule: code/comments in English | ❌ 30+ issues |
| Documentation: JavaDoc full coverage | ⚠️ partial |
| Documentation: UML diagrams | ❌ missing |
| Documentation: Requirements document | ❌ missing |
| Documentation: Digitalization plan | ❌ missing |
| Documentation: English videotutorial | ❌ missing |

**Gameplay grade level → Excel·lent (Impossible) is functionally reached.**
The remaining gap to a final excellent submission is **documentation** and the
**English-only code-base rule**, both of which are mechanical fixes (no game-logic
risk).
