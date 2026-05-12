/*ALTER TABLE ENTITY ADD GAMES_WON    NUMBER DEFAULT 0 NOT NULL;
ALTER TABLE ENTITY ADD GAMES_PLAYED NUMBER DEFAULT 0 NOT NULL;*/


/*CREATE SEQUENCE SEQ_ENTITY    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BOARD     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_GAME      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_INVENTORY START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OBJECT    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_EVENT     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SQUARE    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;*/


/*CREATE OR REPLACE TRIGGER TRG_GAME_PK
    BEFORE INSERT ON GAME
    FOR EACH ROW
BEGIN
    IF :NEW.GAMEID IS NULL THEN
        :NEW.GAMEID := SEQ_GAME.NEXTVAL;
    END IF;
END;
/*/


/*CREATE OR REPLACE TRIGGER TRG_GAME_FINISHED
    AFTER UPDATE OF GAMESTATE ON GAME
    FOR EACH ROW
WHEN (NEW.GAMESTATE = 'FINISHED')
DECLARE
    v_winner_id ENTITY.ENTITYID%TYPE;
BEGIN
    -- Incrementar GAMES_PLAYED para todos los jugadores de esta partida
    UPDATE ENTITY
       SET GAMES_PLAYED = GAMES_PLAYED + 1
     WHERE GAMEID = :NEW.GAMEID
       AND ENTITYTYPE = 'PLAYER';

    -- Obtener el ganador (el jugador con ENTITYTYPE='WINNER' o equivalente)
    -- Ajusta esta lógica según cómo tu juego marca al ganador
    SELECT ENTITYID
      INTO v_winner_id
      FROM ENTITY
     WHERE GAMEID = :NEW.GAMEID
       AND ENTITYTYPE = 'WINNER'
       AND ROWNUM = 1;

    -- Incrementar GAMES_WON del ganador
    UPDATE ENTITY
       SET GAMES_WON = GAMES_WON + 1
     WHERE ENTITYID = v_winner_id;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL; -- No hay ganador marcado todavía, no hacer nada
END;
/*/


/*CREATE OR REPLACE FUNCTION FN_MAX_GAMES_WON
    RETURN NUMBER
IS
    v_max NUMBER;
BEGIN
    SELECT MAX(GAMES_WON)
      INTO v_max
      FROM ENTITY
     WHERE ENTITYTYPE = 'PLAYER';

    RETURN NVL(v_max, 0);
END;
/*/


/*CREATE OR REPLACE PROCEDURE SP_PLAYERS_WITH_RECORD
IS
    v_max    NUMBER := FN_MAX_GAMES_WON();
    v_count  NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== JUGADORES CON EL RÉCORD (' || v_max || ' victorias) ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND GAMES_WON  = v_max
         ORDER BY PLAYERNAME
    ) LOOP
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.PLAYERNAME
                             || ' — Victorias: ' || rec.GAMES_WON);
    END LOOP;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay jugadores registrados.');
    END IF;
END;
/*/


/*CREATE OR REPLACE FUNCTION FN_AVG_GAMES_WON
    RETURN NUMBER
IS
    v_avg NUMBER;
BEGIN
    SELECT AVG(GAMES_WON)
      INTO v_avg
      FROM ENTITY
     WHERE ENTITYTYPE = 'PLAYER';

    RETURN NVL(v_avg, 0);
END;
/*/


/*CREATE OR REPLACE PROCEDURE SP_PLAYERS_ABOVE_AVERAGE
IS
    v_avg   NUMBER := FN_AVG_GAMES_WON();
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Media de victorias: ' || ROUND(v_avg, 2));
    DBMS_OUTPUT.PUT_LINE('=== JUGADORES POR ENCIMA DE LA MEDIA ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND GAMES_WON  > v_avg
         ORDER BY GAMES_WON DESC, PLAYERNAME
    ) LOOP
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.PLAYERNAME
                             || ' — Victorias: ' || rec.GAMES_WON);
    END LOOP;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Ningún jugador supera la media.');
    END IF;
END;
/*/


/*CREATE OR REPLACE FUNCTION FN_PCT_PLAYERS_BELOW(p_games_won IN NUMBER)
    RETURN NUMBER
IS
    v_total  NUMBER;
    v_below  NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_total
      FROM ENTITY
     WHERE ENTITYTYPE = 'PLAYER';

    IF v_total = 0 THEN
        RETURN 0;
    END IF;

    SELECT COUNT(*)
      INTO v_below
      FROM ENTITY
     WHERE ENTITYTYPE = 'PLAYER'
       AND GAMES_WON  < p_games_won;

    RETURN ROUND((v_below / v_total) * 100, 2);
END;
/*/



/*CREATE OR REPLACE TRIGGER TRG_SHOW_PCT_ON_WIN
    AFTER UPDATE OF GAMES_WON ON ENTITY
    FOR EACH ROW
WHEN (NEW.GAMES_WON > OLD.GAMES_WON AND NEW.ENTITYTYPE = 'PLAYER')
DECLARE
    v_pct NUMBER;
BEGIN
    v_pct := FN_PCT_PLAYERS_BELOW(:NEW.GAMES_WON);
    DBMS_OUTPUT.PUT_LINE('>>> Jugador "' || :NEW.PLAYERNAME || '" ahora tiene '
                         || :NEW.GAMES_WON || ' victorias.');
    DBMS_OUTPUT.PUT_LINE('>>> El ' || v_pct || '% de jugadores tienen menos victorias que él/ella.');
END;
/*/


/*CREATE OR REPLACE PROCEDURE SP_RANKING_BY_PLAYED(p_player_name IN VARCHAR2 DEFAULT NULL)
IS
    v_exists NUMBER;
    v_has_games NUMBER;
    v_rank   NUMBER := 0;
BEGIN
    -- Si se pasa un jugador concreto, validar errores
    IF p_player_name IS NOT NULL THEN

        -- Error 1: El jugador no existe
        SELECT COUNT(*)
          INTO v_exists
          FROM ENTITY
         WHERE ENTITYTYPE  = 'PLAYER'
           AND PLAYERNAME  = p_player_name;

        IF v_exists = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador "' || p_player_name || '" no existe en la tabla de jugadores.');
            RETURN;
        END IF;

        -- Error 2: El jugador existe pero no ha jugado ninguna partida
        SELECT GAMES_PLAYED
          INTO v_has_games
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND PLAYERNAME = p_player_name
           AND ROWNUM     = 1;

        IF v_has_games = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador "' || p_player_name || '" no tiene ninguna partida guardada.');
            RETURN;
        END IF;

    END IF;

    -- Mostrar ranking completo (o de todos los jugadores)
    DBMS_OUTPUT.PUT_LINE('=== RANKING POR PARTIDAS JUGADAS ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_PLAYED, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE   = 'PLAYER'
           AND GAMES_PLAYED > 0
         ORDER BY GAMES_PLAYED DESC, GAMES_WON DESC, PLAYERNAME
    ) LOOP
        v_rank := v_rank + 1;
        DBMS_OUTPUT.PUT_LINE(v_rank || '. ' || rec.PLAYERNAME
                             || ' — Jugadas: ' || rec.GAMES_PLAYED
                             || ' | Ganadas: ' || rec.GAMES_WON);
    END LOOP;

    IF v_rank = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay jugadores con partidas registradas.');
    END IF;
END;
/*/

/*BEGIN
    DBMS_OUTPUT.PUT_LINE('Récord actual: ' || FN_MAX_GAMES_WON() || ' victorias');
END;
/*/


-- Probar jugadores con récord
/*EXEC SP_PLAYERS_WITH_RECORD;*/



-- Probar media
/*BEGIN
    DBMS_OUTPUT.PUT_LINE('Media de victorias: ' || ROUND(FN_AVG_GAMES_WON(), 2));
END;
/*/

-- Probar jugadores sobre la media
/*EXEC SP_PLAYERS_ABOVE_AVERAGE;*/


-- Probar % con menos victorias que 3
/*BEGIN
    DBMS_OUTPUT.PUT_LINE('% jugadores con menos de 3 victorias: ' || FN_PCT_PLAYERS_BELOW(3) || '%');
END;
/*/



-- Probar ranking completo
/*EXEC SP_RANKING_BY_PLAYED;*/

-- Probar error: jugador inexistente
/*EXEC SP_RANKING_BY_PLAYED('JugadorFalso');*/

-- Probar error: jugador sin partidas
/*EXEC SP_RANKING_BY_PLAYED('NombreRealSinPartidas');*/

/*-- ¿Hay filas en GAME?
SELECT * FROM GAME;

-- ¿Hay filas en ENTITY con tipo PLAYER?
SELECT ENTITYID, PLAYERNAME, GAMES_WON, GAMES_PLAYED, ENTITYTYPE 
FROM ENTITY 
WHERE ENTITYTYPE = 'PLAYER';

-- ¿Hay filas en SAVED_GAMES?
SELECT * FROM SAVED_GAMES;*/

