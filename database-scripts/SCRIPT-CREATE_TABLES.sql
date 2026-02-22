--SCRIPT DE CREACIÓ DE TAULES
--SCRIPT D'INSERCIÓ DE REGISTRES (mín. 1 registre per taula, especificant per a cada taula tots els seus camps)
--SCRIPT D'ELIMINACIÓ DE LES TAULES
--SCRIPT AMB ELS PRINCIPALS SELECTS (els que sabeu segur que es necessitaran pel joc, com podria ser el d'obtenir el ranking de puntuacions, etc.)

CREATE TABLE BOARD (
    boardID     NUMBER(5)       PRIMARY KEY
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

    CONSTRAINT ck_square_type           CHECK (type IN ('BEAR', 'ICEHOLE', 'SELD', 'EVENT', 'NORMAL')),
);

CREATE TABLE ENTITY(
    entityID    NUMBER(5)       PRIMARY KEY,
    squareID    NUMBER(2)       ,
    type        VARCHAR(10)     NOT NULL,
    gameID      NUMBER(5)       ,
    inventoryID NUMBER(5)       ,
    playerID    NUMBER(5)       ,
    name        VARCHAR(20)     ,
    password    VARCHAR(50)     ,
    colour      VARCHAR(6)      ,

    CONSTRAINT fk_entity_square         FOREIGN KEY (squareID)      REFERENCES SQUARE(squareID),
    CONSTRAINT fk_entity_game           FOREIGN KEY (gameID)        REFERENCES GAME(gameID),
    CONSTRAINT fk_entity_inventory      FOREIGN KEY (inventoryID)   REFERENCES INVENTORY(inventoryID)
);

CREATE TABLE INVENTORY(
    inventoryID NUMBER(5)       PRIMARY KEY,
    entityID    NUMBER(5)       ,
    
    CONSTRAINT fk_inventory_entity      FOREIGN KEY (entityID)      REFERENCES ENTITY(entityID)
);

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

CREATE TABLE EVENT(
    eventID     NUMBER(5)       PRIMARY KEY,
    type        VARCHAR(15)     NOT NULL

    --CONSTRAINT ck_event_type CHECK (type IN ('-', '-', '-', '-', '-'))
);
