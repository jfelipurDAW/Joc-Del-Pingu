# Walkthrough — All Changes Made to Joc Del Pingu

## Summary of Changes

| # | Feature / Fix | Status |
|---|---------------|--------|
| 1 | Fix DB connection (error logging) | ✅ Done |
| 2 | Encrypt passwords on registration (AES via `CryptoUtil`) | ✅ Done |
| 3 | Decrypt passwords when loading from DB | ✅ Done |
| 4 | Password verification when starting a game | ✅ Done |
| 5 | Player Statistics screen (new FXML + Controller) | ✅ Done |
| 6 | Stats button on Main Menu | ✅ Done |
| 7 | Fix fullscreen → windowed resize | ✅ Done |
| 8 | "Back to Menu" button on game over win screen | ✅ Done |
| 9 | Fix pixel art tile rendering (disable smooth filtering) | ✅ Done |
| 10 | Fallback fonts for all CSS rules | ✅ Done |
| 11 | i18n keys for all 11 language files | ✅ Done |

---

## Files Modified

### `model/db/BBDD.java`
- **DB errors now logged** instead of silently swallowed
- `conectarBaseDatos()`: prints `System.err` with URL and SQL error messages
- `select()`: logs SELECT errors
- `executeInsUpDel()`: logs Insert/Update/Delete errors with label

### `model/game/SaveLoadService.java`
- **`registerPlayer()`**: Password is now encrypted via `CryptoUtil.encrypt()` before storing
- **`getRegisteredPlayers()`**: Password is decrypted via `CryptoUtil.decrypt()` after reading
- **NEW `verifyPassword()`**: Checks input password against encrypted DB record
- **NEW `getPlayerStats()`**: Queries `GAMES_PLAYED` and `GAMES_WON` columns, sorted by wins

### `controller/ui/PlayerSetupController.java`
- **`handleStartGame()`**: Now calls `SaveLoadService.verifyPassword()` for each player before proceeding. Shows alert if password doesn't match.

### `controller/main/MainMenu.java`
- **Fullscreen fix**: Added `fullScreenProperty` listener that saves window dimensions before fullscreen and restores them on exit

### `controller/ui/MainMenuController.java`
- Added `stats_button` FXML binding
- Added `handleStats()` method navigating to `playerStats.fxml`
- `refreshTexts()` now also sets stats button text

### `controller/ui/GameBoardController.java`
- **`handleWin()`**: Stores winner name, removed blocking alert
- **`showWinAnimation()`**: Now includes a styled "Back to Menu" button in the win overlay, with `Congratulations` subtitle. Overlay made interactive for the button.

### `view/fxml/mainMenu.fxml`
- Added `stats_button` between Load Game and Language dropdown

### `assets/css/style.css`
- `.mainmenu_maintitle` font-family: Added fallback chain `"Pixel Game", "FS Pixel Sans Unicode Regular", "Segoe UI", sans-serif`
- `.mainmenu_title_shadow` font-family: Added matching fallback chain

### `assets/css/gameBoardStyle.css`
- `.root` font-family: Changed to `"FS Pixel Sans Unicode Regular", "Segoe UI", "Arial", sans-serif`

### `model/config/Lang.java`
- Added 8 new enum values: `MENU_BUTTON_STATS`, `STATS_TITLE`, `STATS_PLAYER`, `STATS_COLOUR`, `STATS_GAMES_PLAYED`, `STATS_GAMES_WON`, `STATS_NO_DATA`, `GAME_BACK_TO_MENU`

### Language files (all 11)
- Added translations for all new keys in: `en`, `es`, `ca`, `fr`, `pt`, `ro`, `ar`, `ru`, `uk`, `ff`, `jp`, `en_es`

---

## Files Created

### `view/fxml/playerStats.fxml`
- New screen layout with header row, scrollable stats list, and back button

### `controller/ui/PlayerStatsController.java`
- Loads stats via `SaveLoadService.getPlayerStats()`
- Displays ranked list with 🥇🥈🥉 medals for top 3
- Colour swatches for each player
- Full i18n support via `LangConfig`

---

## How the Password Flow Works

```
Registration (New Game → Start):
  1. User types password in PlayerSetup
  2. SaveLoadService.verifyPassword() checks if name exists in DB
     - If exists: decrypt stored password, compare with input → block if mismatch
     - If new: allow through
  3. SaveLoadService.registerPlayer() encrypts password with CryptoUtil.encrypt()
  4. Stored as AES-128 encrypted Base64 string in PLAYERPASSWORD column

Login (Select Existing Player):
  1. SaveLoadService.getRegisteredPlayers() loads players
  2. Passwords are decrypted in-memory for form pre-fill
  3. On Start Game, verifyPassword() re-checks against DB
```

---

## Database Schema Assumption

The `ENTITY` table should have these columns (the code assumes they exist):

```sql
ENTITYID        NUMBER
ENTITYTYPE      VARCHAR2  -- 'PLAYER' or 'SEAL'
PLAYERNAME      VARCHAR2
PLAYERPASSWORD  VARCHAR2  -- AES-128 encrypted, Base64 encoded
COLOUR          VARCHAR2
GAMES_PLAYED    NUMBER    -- defaults to 0
GAMES_WON       NUMBER    -- defaults to 0
```

> [!WARNING]
> If `GAMES_PLAYED` and `GAMES_WON` columns don't exist in your Oracle table, you need to add them:
> ```sql
> ALTER TABLE ENTITY ADD (GAMES_PLAYED NUMBER DEFAULT 0, GAMES_WON NUMBER DEFAULT 0);
> ```
