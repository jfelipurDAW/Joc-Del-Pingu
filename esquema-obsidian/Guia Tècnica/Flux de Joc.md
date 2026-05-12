# Flux de Joc

Diagrames de seqüència per a les operacions més importants del joc.

## 1. Inicialització de la partida

```mermaid
sequenceDiagram
    participant U as Usuari
    participant FX as FXMLLoader
    participant GBC as GameBoardController
    participant GM as GameManager
    participant B as Board
    participant TC as TurnController
    participant SC as GameSetupConfig
    participant SM as SoundManager

    U->>FX: load gameBoard.fxml
    FX->>GBC: new GameBoardController()
    GBC->>GBC: precarrega 10 sprites
    FX->>GBC: @FXML initialize()
    GBC->>GM: new GameManager("LOCAL_MATCH", 0)
    GBC->>B: new Board()
    GBC->>GM: setBoard(board)

    alt isLoadedGame
        GBC->>SC: getLoadedBoardState()
        GBC->>B: loadBoard(types)
        GBC->>TC: setCurrentTurn(idx)
    else nova partida
        GBC->>B: createNewBoard()
    end

    GBC->>SC: getPlayers()
    loop per cada player
        GBC->>TC: addPlayer(player)
    end

    GBC->>GBC: drawBoard() + updateHUD()
    GBC->>SM: playGameMusic()
```

## 2. Tirada de daus i moviment

```mermaid
sequenceDiagram
    participant U as Usuari
    participant GBC as GameBoardController
    participant D as Dice
    participant GM as GameManager
    participant PM as PlayerManager
    participant BM as BoardManager
    participant P as Player
    participant TC as TurnController
    participant SM as SoundManager

    U->>GBC: rollDice()
    GBC->>D: rollOrForce(defaultDice)
    Note over GBC: showDiceAnimation (1.5s)
    GBC->>SM: playDiceSound() amb delay
    Note over GBC: showDiceResultBadge (1s)
    GBC->>GBC: animatePlayerMovement (250ms/casella)
    loop per cada casella
        GBC->>P: setSquare(currentPos + i)
        GBC->>GBC: drawBoard()
    end
    GBC->>GM: playTurn(diceResult)
    GM->>PM: movePlayer(player, steps, board)
    alt arribat a final
        PM-->>GM: ActionResult(WIN)
        GM-->>GBC: WIN
        GBC->>GBC: handleWin(player)
    else
        PM-->>GM: null
        GM->>BM: executeSquareAction(player, board)
        BM-->>GM: ActionResult
        GM-->>GBC: ActionResult
        GBC->>GBC: log + drawBoard + flashDamage si cal
        GBC->>TC: nextTurn()
    end
```

## 3. Caure en una casella especial (BEAR)

```mermaid
sequenceDiagram
    participant GBC as GameBoardController
    participant GM as GameManager
    participant BM as BoardManager
    participant P as Player
    participant I as Inventory
    participant SM as SoundManager

    GBC->>GM: playTurn(diceResult)
    GM->>BM: executeSquareAction(player, board)
    BM->>BM: switch(SquareType.BEAR) → handleBear()
    BM->>I: getObjectQuantity(FISH)
    alt fish > 0
        BM->>I: useObject(FISH, 1)
        BM-->>GBC: ActionResult(BEAR_SAFE)
    else sense peix
        BM->>P: setSquare(0)
        BM-->>GBC: ActionResult(BEAR_ATTACK)
        GBC->>SM: playBearSound()
        GBC->>GBC: showBearAnimation()
        GBC->>P: setDamaged(true) [flashDamage]
    end
```

## 4. Casella EVENT amb subllançament aleatori

```mermaid
sequenceDiagram
    participant BM as BoardManager
    participant EM as EventManager
    participant P as Player
    participant I as Inventory

    BM->>EM: triggerEvent(player, board)
    EM->>EM: int roll = random.nextInt(100)
    alt roll < 30
        EM->>I: addDice(SLOWDICE)
        EM-->>BM: EventResult(GET_SLOW_DICE)
    else roll < 50
        EM->>I: addSnowballs(1-3)
        EM-->>BM: EventResult(GET_SNOWBALLS)
    else roll < 65
        EM->>I: addFish()
        EM-->>BM: EventResult(GET_FISH)
    else roll < 77
        EM->>P: setSkipNextTurn(true)
        EM-->>BM: EventResult(LOSE_TURN)
    else roll < 87
        EM->>I: removeRandomItem()
        EM-->>BM: EventResult(LOSE_ITEM)
    else roll < 95
        EM->>I: addDice(FASTDICE)
        EM-->>BM: EventResult(GET_FAST_DICE)
    else
        EM->>P: setSquare(nextSled)
        EM-->>BM: EventResult(SNOWMOBILE)
    end
    BM->>P: setLastEvent(result)
    BM-->>BM: retorna ActionResult(EVENT)
```

## 5. Torn de la foca CPU

```mermaid
sequenceDiagram
    participant GBC as GameBoardController
    participant TC as TurnController
    participant S as Seal
    participant Ps as Players

    GBC->>TC: nextTurn()
    GBC->>GBC: detecta seal turn (turnIndex == 0 && sealEnabled)
    GBC->>S: playTurn(humanPlayers)
    alt blocked > 0
        S->>S: blockedTurns--
        S-->>GBC: [SEAL_EATING]
    else
        S->>S: roll dice 1-6
        loop per cada casella intermèdia
            S->>Ps: passThrough(player) si està al pas
            Note over S,Ps: removeHalf() de l'inventari
        end
        S->>S: setNumSquare(newPos)
        alt jugador a destí
            S->>Ps: hitPlayer(player)
            Note over S,Ps: send to previous ice hole
        end
    end
    S-->>GBC: List<ActionResult> log
    GBC->>GBC: anima log amb Timeline
    alt foca a casella 49
        GBC->>GBC: gameOver = true, winner = "Seal"
    else
        GBC->>GBC: startNextPlayerTurn()
    end
```

## 6. Snowball war (col·lisió de jugadors)

```mermaid
sequenceDiagram
    participant GBC as GameBoardController
    participant TC as TurnController
    participant PM as PlayerManager
    participant A as Attacker
    participant D as Defender

    Note over GBC: després del moviment del jugador
    GBC->>TC: getPlayersAtSquare(current.square, current)
    TC-->>GBC: List<Player> col·lisions
    loop per cada col·lisió
        GBC->>PM: snowballWar(attacker, defender)
        PM->>A: useObject(SNOWBALL, balls1)
        PM->>D: useObject(SNOWBALL, balls2)
        alt balls1 > balls2
            PM->>D: setSquare(d.square - diff)
            PM-->>GBC: SNOWBALL_WAR_WIN (winner=attacker)
        else balls2 > balls1
            PM->>A: setSquare(a.square - diff)
            PM-->>GBC: SNOWBALL_WAR_WIN (winner=defender)
        else empat
            PM-->>GBC: SNOWBALL_WAR_TIE o EMPTY
        end
    end
```

## 7. Victòria

```mermaid
sequenceDiagram
    participant GBC as GameBoardController
    participant GM as GameManager
    participant SLS as SaveLoadService
    participant SM as SoundManager
    participant U as Usuari

    GBC->>GM: playTurn(diceResult)
    Note over GM: detecta newPos >= MAX_SQUARES - 1
    GM->>GM: setGameOver(true), setWinner(player)
    GM-->>GBC: ActionResult(WIN)
    GBC->>GBC: handleWin(winner)
    GBC->>SLS: recordGameResult(allPlayers, winner.name)
    SLS->>BBDD: INSERT INTO GAME (...)
    SLS->>BBDD: UPDATE ENTITY SET GAMES_PLAYED++
    SLS->>BBDD: UPDATE ENTITY SET GAMES_WON++ WHERE name=winner
    GBC->>SM: playTitleMusic() (canvi de música)
    GBC->>U: pantalla / animació de victòria
```

## 8. Guardat de partida

```mermaid
sequenceDiagram
    participant U as Usuari
    participant GBC as GameBoardController
    participant SLS as SaveLoadService
    participant Y as Yaml
    participant CR as CryptoUtil
    participant DB as BBDD

    U->>GBC: saveGame()
    GBC->>U: TextInputDialog "name"
    U->>GBC: "MyGame"
    GBC->>SLS: saveGame("MyGame", board, tc, seal, winner)
    SLS->>SLS: construeix Map state
    SLS->>Y: yaml.dump(state)
    Y-->>SLS: String YAML
    SLS->>CR: encrypt(yaml)
    CR-->>SLS: Base64 ciphertext
    SLS->>DB: conectarBaseDatos(null)
    SLS->>DB: PreparedStatement INSERT (?,?)
    DB->>Oracle: INSERT INTO SAVED_GAMES
    Oracle-->>DB: rows affected
    SLS-->>GBC: true/false
    GBC->>U: Alert "Game saved" / "Error"
```

## 9. Càrrega de partida

```mermaid
sequenceDiagram
    participant U as Usuari
    participant MMC as MainMenuController
    participant SLS as SaveLoadService
    participant CR as CryptoUtil
    participant Y as Yaml
    participant SC as GameSetupConfig

    U->>MMC: handleLoadGame()
    MMC->>SLS: getAllSavedGameIds()
    SLS-->>MMC: List<String> ids
    MMC->>U: ChoiceDialog (ids)
    U->>MMC: tria id
    MMC->>SLS: loadGame(id)
    SLS->>BBDD: SELECT GAME_DATA WHERE GAME_ID=?
    BBDD-->>SLS: encrypted
    SLS->>CR: decrypt(encrypted)
    CR-->>SLS: yaml string
    SLS->>Y: yaml.load(yamlString)
    Y-->>SLS: Map state
    SLS->>SC: setLoadedBoardState, setPlayers, ...
    SLS-->>MMC: true
    loop per cada jugador
        MMC->>U: Dialog password
        U->>MMC: pwd
        MMC->>SLS: verifyPassword(name, pwd)
    end
    alt totes correctes
        MMC->>GameBoardController: load gameBoard.fxml
    else falla
        MMC->>SC: setLoadedGame(false)
        MMC->>MMC: torna al menú
    end
```

## Enllaços relacionats

- [[Arquitectura General]] — visió MVC
- [[Paquet model.game]] — coordinadors
- [[Paquet controller]] — `GameBoardController`
- [[Caselles Especials]] — efecte de cada casella
- [[Sistema de Persistència]] — detalls del guardat
