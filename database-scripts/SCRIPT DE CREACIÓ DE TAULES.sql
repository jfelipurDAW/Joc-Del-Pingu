--SCRIPT DE CREACIÓ DE TAULES
--SCRIPT D'INSERCIÓ DE REGISTRES (mín. 1 registre per taula, especificant per a cada taula tots els seus camps)
--SCRIPT D'ELIMINACIÓ DE LES TAULES
--SCRIPT AMB ELS PRINCIPALS SELECTS (els que sabeu segur que es necessitaran pel joc, com podria ser el d'obtenir el ranking de puntuacions, etc.)

CREATE TABLE BOARD (
    boardID     NUMBER(5)       PRIMARY KEY
);

CREATE TABLE EVENT(
    eventID     NUMBER(5)       PRIMARY KEY,
    type        VARCHAR(15)     NOT NULL

    --CONSTRAINT ck_event_type CHECK (type IN ('-', '-', '-', '-', '-'))

);

CREATE TABLE GAME (
    gameID      NUMBER(5)       PRIMARY KEY,
    state       VARCHAR(20)     NOT NULL,
    date        DATE            DEFAULT SYSDATE,
    boardID     NUMBER(5)       ,
    
    CONSTRAINT  fk_game_board           FOREIGN KEY (boardID) REFERENCES BOARD(boardID)
);

CREATE TABLE SQUARE(
    squareID    NUMBER(2)       PRIMARY KEY,
    type        VARCHAR(10)     NOT NULL,
    destination NUMBER(2)       ,
    eventID     NUMBER(5)       ,
    boardID     NUMBER(5)       ,

    CONSTRAINT fk_square_destination    FOREIGN KEY (destination)   REFERENCES SQUARE(squareID),
    CONSTRAINT fk_square_event          FOREIGN KEY (eventID)       REFERENCES EVENT(eventID),
    CONSTRAINT fk_square_board          FOREIGN KEY (boardID)       REFERENCES BOARD(boardID),

    CONSTRAINT ck_square_type           CHECK (type IN ('BEAR', 'ICE_HOLE', 'SLED', 'EVENT', 'NORMAL'))
);

CREATE TABLE INVENTORY(
    inventoryID NUMBER(5)       PRIMARY KEY,
    entityID    NUMBER(5)
);

CREATE TABLE ENTITY(
    entityID    NUMBER(5)       PRIMARY KEY,
    squareID    NUMBER(2)       ,
    type        VARCHAR(10)     NOT NULL,
    gameID      NUMBER(5)       ,
    inventoryID NUMBER(5)       ,
    name        VARCHAR(20)     ,
    password    VARCHAR(50)     ,
    colour      VARCHAR(6)      ,

    CONSTRAINT fk_entity_square         FOREIGN KEY (squareID)      REFERENCES SQUARE(squareID),
    CONSTRAINT fk_entity_game           FOREIGN KEY (gameID)        REFERENCES GAME(gameID),
    CONSTRAINT fk_entity_inventory      FOREIGN KEY (inventoryID)   REFERENCES INVENTORY(inventoryID),

    CONSTRAINT ck_entity_type           CHECK (type IN ('PLAYER', 'CPU')),
    CONSTRAINT ck_player_name           CHECK ((type = 'PLAYER' AND name IS NOT NULL) OR (type != 'PLAYER' AND name IS NULL)),
    CONSTRAINT uq_entity_name           UNIQUE (name),
    CONSTRAINT ck_player_password       CHECK ((type = 'PLAYER' AND password IS NOT NULL) OR (type != 'PLAYER' AND password IS NULL)),
    CONSTRAINT ck_player_colour         CHECK ((type = 'PLAYER' AND colour IS NOT NULL) OR (type != 'PLAYER' AND colour IS NULL))
);

ALTER TABLE INVENTORY ADD CONSTRAINT fk_inventory_entity FOREIGN KEY (entityID) REFERENCES ENTITY(entityID);

CREATE TABLE OBJECT(
    objectID    NUMBER(5)       PRIMARY KEY,
    inventoryID NUMBER(5)       ,
    type        VARCHAR(10)     NOT NULL,
    diceType    VARCHAR(4)      ,
    quantity    NUMBER(1)       ,

    CONSTRAINT fk_object_inventory      FOREIGN KEY (inventoryID)   REFERENCES INVENTORY(inventoryID),
    
    CONSTRAINT ck_object_diceType       CHECK ((type = 'DICE' AND diceType IN ('FAST', 'SLOW')) OR (type != 'DICE' AND diceType IS NULL)),
    CONSTRAINT ck_object_quantity       CHECK ((type = 'FISH' AND quantity BETWEEN 0 AND 2) OR (type = 'SNOWBALL' AND quantity BETWEEN 0 AND 6) OR (type = 'DICE' AND quantity BETWEEN 0 AND 3))
);


INSERT INTO BOARD (boardID)
    VALUES (1);
 
INSERT INTO EVENT (eventID, type)
    VALUES (1, 'GET A FISH');
 
INSERT INTO SQUARE (squareID, type, destination, boardID, eventID)
    VALUES (1, 'NORMAL', NULL, 1, NULL);
INSERT INTO SQUARE (squareID, type, destination, boardID, eventID)
    VALUES (2, 'BEAR', NULL, 1, NULL);
INSERT INTO SQUARE (squareID, type, destination, boardID, eventID)
    VALUES (3, 'ICE_HOLE', NULL, 1, NULL);
INSERT INTO SQUARE (squareID, type, destination, boardID, eventID)
    VALUES (4, 'SLED',     4,    1,    1);
INSERT INTO SQUARE (squareID, type, destination, boardID, eventID)
    VALUES (5, 'EVENT', NULL, 1, 1);

    INSERT INTO GAME (gameID, state, date, boardID)
    VALUES (1, 'IN_PROGRESS', SYSDATE, 1);
 
-- INVENTORY sense entityID fins que ENTITY existeixi
INSERT INTO INVENTORY (inventoryID, entityID)
    VALUES (1, NULL);
 
INSERT INTO ENTITY (entityID, type, name, password, colour, gameID, squareID, inventoryID)
    VALUES (1, 'PLAYER', 'Jugador1', 'hash1234', 'FF5733', 1, 1, 1);
 
-- Actualització d'INVENTORY
UPDATE INVENTORY i SET entityID = (SELECT e.entityID FROM ENTITY e WHERE e.inventoryID = i.inventoryID);
 
INSERT INTO OBJECT (objectID, inventoryID, type, diceType, quantity)
    VALUES (1, 1, 'DICE',     'FAST', 2);
INSERT INTO OBJECT (objectID, inventoryID, type, diceType, quantity)
    VALUES (2, 1, 'FISH',     NULL,   1);
INSERT INTO OBJECT (objectID, inventoryID, type, diceType, quantity)
    VALUES (3, 1, 'SNOWBALL', NULL,   3);

-- Jugadors d'una partida amb posició actual
SELECT e.entityID, e.name, e.colour, s.squareID, s.type, g.state
FROM ENTITY e
JOIN GAME g   ON e.gameID   = g.gameID
JOIN SQUARE s ON e.squareID = s.squareID
WHERE g.gameID = 1
ORDER BY e.name;
 
-- Inventari complet d'un jugador
SELECT e.name, o.type, o.diceType, o.quantity
FROM ENTITY e
JOIN INVENTORY i ON e.inventoryID = i.inventoryID
JOIN OBJECT o    ON o.inventoryID = i.inventoryID
WHERE e.entityID = 1;
 
-- Caselles especials del tauler
SELECT squareID, type, destination, eventID
FROM SQUARE
WHERE boardID = 1
  AND type != 'NORMAL'
ORDER BY squareID;
 
-- Partides en curs
SELECT gameID, state, date, boardID
FROM GAME
WHERE state = 'IN_PROGRESS'
ORDER BY date DESC;
 
-- Totes les entitats al tauler
SELECT s.squareID, s.type, e.name
FROM SQUARE s
LEFT JOIN ENTITY e ON e.squareID = s.squareID
WHERE s.boardID = 1
ORDER BY s.squareID;
 
-- Esdeveniments disponibles
SELECT eventID, type
FROM EVENT
ORDER BY eventID;
