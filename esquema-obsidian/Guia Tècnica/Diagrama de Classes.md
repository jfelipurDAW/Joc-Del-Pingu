# Diagrama de Classes

UML general (simplificat) de les classes principals del joc. Per veure detalls per paquet, consulta les pàgines individuals.

## Visió completa

```mermaid
classDiagram
    %% =============== ENTITIES ===============
    class Entity {
        <<abstract>>
        -int entityId
        -int numSquare
        -EntityType type
        -boolean skipNextTurn
        -boolean facingRight
        -boolean damaged
        -boolean frozen
        +advance(int) bool
        +setSquare(int)
    }
    class Player {
        -String name
        -String colour
        -String password
        -String avatarPath
        -Inventory inventory
        -List eventHistory
        +loseHalfInventory()
        +recordEvent(String)
    }
    class Seal {
        -int blockedTurns
        +playTurn(allPlayers)
        +interact(Player)
        +bribeSeal(Player)
        +hitPlayer(Player)
        +passThrough(Player)
    }

    Entity <|-- Player
    Entity <|-- Seal

    %% =============== ITEMS ===============
    class GameObject {
        <<abstract>>
        -int objectId
        -String name
        -ObjectType type
    }
    class Dice { -int minValue; -int maxValue; +roll() int }
    class Fish
    class SnowBall
    GameObject <|-- Dice
    GameObject <|-- Fish
    GameObject <|-- SnowBall

    class Inventory {
        +MAX_SNOWBALLS = 6
        +MAX_FISH = 2
        +MAX_DICE = 3
        +addSnowballs(int)
        +addFish()
        +addDice(ObjectType)
        +useObject(ObjectType,int)
        +removeRandomItem()
        +removeHalf()
        +getTotalItemCount()
    }
    Player "1" *-- "1" Inventory

    %% =============== BOARD ===============
    class Board {
        +MAX_SQUARES = 50
        -Square[] board
        -ArrayList IceHole_Array
        -ArrayList Sled_Array
        +createNewBoard()
        +loadBoard(List)
        +getDestination(int) int
        +convertBrokenFloorToIceHole(int)
    }
    class Square {
        <<abstract>>
        -SquareType type
        -int SquareID
        +action(Player)* String
    }
    Square <|-- S_Normal
    Square <|-- S_Start
    Square <|-- S_End
    Square <|-- S_Bear
    Square <|-- S_IceHole
    Square <|-- S_Sled
    Square <|-- S_Event
    Square <|-- S_BrokenFloor
    Board "1" *-- "50" Square

    class EventManager {
        +triggerEvent(Player, Board) EventResult
    }

    %% =============== GAME ===============
    class Game {
        -List players
        -Board board
        -int currentTurn
        -Player winner
        -boolean gameOver
        -Seal seal
    }
    class GameManager {
        -BoardManager boardManager
        -PlayerManager playerManager
        -TurnController turnController
        +playTurn(int) ActionResult
        +saveGame() bool
    }
    class BoardManager {
        +executeSquareAction(Player, Board) ActionResult
        +validateTurn(Player) bool
    }
    class PlayerManager {
        +movePlayer(Player,int,Board)
        +snowballWar(Player,Player)
        +throwSnowball(Player,Player)
        +handleSealInteraction(Seal,Player)
    }
    class TurnController {
        -ArrayList players
        -int turn
        +nextTurn()
        +getCurrentTurn()
        +getPlayersAtSquare(int,Entity)
    }
    class ActionResult {
        -ActionType actionType
        -String playerName
        -String targetName
        -int value
        -int value2
    }

    GameManager *-- BoardManager
    GameManager *-- PlayerManager
    GameManager o-- TurnController
    GameManager *-- Game
    Game o-- Board
    Game o-- Seal
    BoardManager ..> ActionResult
    PlayerManager ..> ActionResult

    %% =============== CONFIG / DB ===============
    class Lang {
        <<enum>>
        +getKey() String
    }
    class LangConfig {
        <<singleton>>
        -Map data
        -List listeners
        +loadLang(String)
        +getLang(Lang)
    }
    class GameSetupConfig {
        <<static>>
        -static List players
        -static boolean sealEnabled
        -static boolean isLoadedGame
    }
    class CryptoUtil {
        +encrypt(String)
        +decrypt(String)
    }
    class SaveLoadService {
        +saveGame(...) bool
        +loadGame(String) bool
        +registerPlayer(...)
        +verifyPassword(...)
        +recordGameResult(...)
    }
    class BBDD {
        +conectarBaseDatos(Scanner)
        +select / insert / update / delete
    }
    class SoundManager {
        <<singleton>>
        +playTitleMusic()
        +playGameMusic()
        +playDiceSound()
    }

    LangConfig ..> Lang
    SaveLoadService ..> CryptoUtil
    SaveLoadService ..> BBDD
    SaveLoadService ..> GameSetupConfig

    %% =============== CONTROLLERS ===============
    class MainMenu {
        +start(Stage)
        +main(String[])
    }
    class MainMenuController
    class PlayerSetupController
    class PlayerStatsController
    class GameBoardController {
        +rollDice() · rollFastDice() · rollSlowDice()
        +throwSnowball() · saveGame() · handleWin()
        -drawBoard() · toggleDebugMode()
    }

    MainMenu --> MainMenuController
    MainMenuController --> PlayerSetupController
    MainMenuController --> GameBoardController
    MainMenuController --> PlayerStatsController
    PlayerSetupController --> GameBoardController
    GameBoardController --> GameManager
    GameBoardController --> Board
    GameBoardController --> TurnController
    GameBoardController --> Seal
```

## Enums

```mermaid
classDiagram
    class SquareType {
        <<enum>>
        ICE_HOLE
        SLED
        BEAR
        EVENT
        BROKEN_FLOOR
        NORMAL
        START
        END
    }
    class EntityType {
        <<enum>>
        PLAYER
        SEAL
    }
    class ObjectType {
        <<enum>>
        SNOWBALL
        FISH
        DICE
        FASTDICE
        SLOWDICE
    }
    class ActionType {
        <<enum>>
        ICE_HOLE
        SLED_FOUND
        SLED_LAST
        BEAR_SAFE
        BEAR_ATTACK
        EVENT
        BROKEN_FLOOR_FALL
        BROKEN_FLOOR_CRACK
        BROKEN_FLOOR_SAFE
        BROKEN_FLOOR_LOSE_ITEM
        NORMAL_SQUARE
        START_SQUARE
        END_SQUARE
        WIN
        SEAL_BRIBED
        SEAL_NO_FISH
        SEAL_HIT_HOLE
        SEAL_HIT_START
        SEAL_PASS
        SEAL_EATING
        SEAL_ACTIVE
        SEAL_ROLL
        SEAL_MOVE
        SNOWBALL_WAR_WIN
        SNOWBALL_WAR_TIE
        SNOWBALL_WAR_EMPTY
        SNOWBALL_THROW
    }
    class EventType {
        <<enum>>
        GET_FISH
        GET_SNOWBALLS
        GET_FAST_DICE
        GET_SLOW_DICE
        LOSE_TURN
        LOSE_ITEM
        SNOWMOBILE
    }
```

## Patrons identificats

| Patró | Implementació |
|-------|---------------|
| **MVC** | `view/`, `controller/`, `model/` |
| **Singleton** | `LangConfig`, `SoundManager` |
| **Template Method** | `Square` abstracta amb `action()` redefinit per subclasses |
| **Strategy via switch** | `BoardManager.executeSquareAction` triant handler per `SquareType` |
| **Observer / Listener** | `LangConfig.listeners` notifica controllers en canviar idioma |
| **DTO** | `ActionResult` viatja de motor a UI |
| **Static Configuration Bag** | `GameSetupConfig` |
| **Lazy initialization** | `SoundManager.getInstance()`, `LangConfig.getInstance()` |

## Enllaços relacionats

- [[Estructura de Paquets]] — mapping disc → classes
- [[Paquet model.board]] · [[Paquet model.entity]] · [[Paquet model.item]] · [[Paquet model.game]]
- [[Arquitectura General]]
