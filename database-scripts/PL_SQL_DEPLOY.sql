-- =====================================================================
--  JOC DEL PINGU — DESPLEGAMENT D'OBJECTES PL/SQL (Opció A)
--
--  Aquest script crea TOTS els objectes PL/SQL de la pràctica, en l'ordre
--  correcte de dependències. Executa'l de dalt a baix en SQL Developer
--  amb F5 (run script). Cada bloc PL/SQL acaba amb "/" en una línia
--  pròpia perquè SQL Developer el compili.
--
--  REQUISIT PREVI: la taula ENTITY ha d'existir (segons SCRIPT DE
--  CREACIÓ DE TAULES.sql) i tenir les columnes GAMES_PLAYED i GAMES_WON.
--  Si encara no les tens, descomenta la secció 0.
--
--  NOTA SOBRE TRG_GAME_FINISHED: aquest trigger NO s'inclou aquí
--  intencionalment. Veure secció final per al motiu i per què es manté
--  l'increment a Java (SaveLoadService.recordGameResult amb NVL).
-- =====================================================================


-- =====================================================================
--  SECCIÓ 0 (OPCIONAL) — Afegir columnes si no existeixen
--  Descomenta només si la taula ENTITY no té encara aquestes columnes.
-- =====================================================================

-- ALTER TABLE ENTITY ADD GAMES_WON    NUMBER DEFAULT 0 NOT NULL;
-- ALTER TABLE ENTITY ADD GAMES_PLAYED NUMBER DEFAULT 0 NOT NULL;


-- =====================================================================
--  SECCIÓ 1 — SEQÜÈNCIES
--  Sense dependències. Es poden crear primer.
-- =====================================================================

CREATE SEQUENCE SEQ_ENTITY    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BOARD     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_GAME      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_INVENTORY START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OBJECT    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_EVENT     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SQUARE    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- =====================================================================
--  SECCIÓ 2 — FUNCIONS
--  Han d'existir abans dels triggers/procediments que les usen.
-- =====================================================================

-- 2.1 Récord de victòries
CREATE OR REPLACE FUNCTION FN_MAX_GAMES_WON
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
/

-- 2.2 Mitjana de victòries
CREATE OR REPLACE FUNCTION FN_AVG_GAMES_WON
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
/

-- 2.3 Percentatge de jugadors per sota d'un llindar
CREATE OR REPLACE FUNCTION FN_PCT_PLAYERS_BELOW(p_games_won IN NUMBER)
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
/


-- =====================================================================
--  SECCIÓ 3 — PROCEDIMENTS
--  Depenen de les funcions anteriors.
-- =====================================================================

-- 3.1 Llista de jugadors amb el récord
CREATE OR REPLACE PROCEDURE SP_PLAYERS_WITH_RECORD
IS
    v_max    NUMBER := FN_MAX_GAMES_WON();
    v_count  NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== JUGADORS AMB EL RÉCORD (' || v_max || ' victòries) ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND GAMES_WON  = v_max
         ORDER BY PLAYERNAME
    ) LOOP
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.PLAYERNAME
                             || '  Victòries: ' || rec.GAMES_WON);
    END LOOP;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hi ha jugadors registrats.');
    END IF;
END;
/

-- 3.2 Jugadors per sobre de la mitjana
CREATE OR REPLACE PROCEDURE SP_PLAYERS_ABOVE_AVERAGE
IS
    v_avg   NUMBER := FN_AVG_GAMES_WON();
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Mitjana de victòries: ' || ROUND(v_avg, 2));
    DBMS_OUTPUT.PUT_LINE('=== JUGADORS PER SOBRE DE LA MITJANA ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND GAMES_WON  > v_avg
         ORDER BY GAMES_WON DESC, PLAYERNAME
    ) LOOP
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.PLAYERNAME
                             || '  Victòries: ' || rec.GAMES_WON);
    END LOOP;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Cap jugador supera la mitjana.');
    END IF;
END;
/

-- 3.3 Rànquing per partides jugades, amb gestió d'errors
CREATE OR REPLACE PROCEDURE SP_RANKING_BY_PLAYED(p_player_name IN VARCHAR2 DEFAULT NULL)
IS
    v_exists    NUMBER;
    v_has_games NUMBER;
    v_rank      NUMBER := 0;
BEGIN
    -- Validacions si es passa un nom concret
    IF p_player_name IS NOT NULL THEN

        -- Error 1: el jugador no existeix
        SELECT COUNT(*)
          INTO v_exists
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND PLAYERNAME = p_player_name;

        IF v_exists = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador "' || p_player_name
                                 || '" no existeix.');
            RETURN;
        END IF;

        -- Error 2: el jugador existeix però no té partides
        SELECT GAMES_PLAYED
          INTO v_has_games
          FROM ENTITY
         WHERE ENTITYTYPE = 'PLAYER'
           AND PLAYERNAME = p_player_name
           AND ROWNUM     = 1;

        IF v_has_games = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador "' || p_player_name
                                 || '" no té cap partida guardada.');
            RETURN;
        END IF;
    END IF;

    -- Llistat
    DBMS_OUTPUT.PUT_LINE('=== RÀNQUING PER PARTIDES JUGADES ===');

    FOR rec IN (
        SELECT PLAYERNAME, GAMES_PLAYED, GAMES_WON
          FROM ENTITY
         WHERE ENTITYTYPE   = 'PLAYER'
           AND GAMES_PLAYED > 0
         ORDER BY GAMES_PLAYED DESC, GAMES_WON DESC, PLAYERNAME
    ) LOOP
        v_rank := v_rank + 1;
        DBMS_OUTPUT.PUT_LINE(v_rank || '. ' || rec.PLAYERNAME
                             || '  Jugades: ' || rec.GAMES_PLAYED
                             || ' | Guanyades: ' || rec.GAMES_WON);
    END LOOP;

    IF v_rank = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hi ha jugadors amb partides registrades.');
    END IF;
END;
/


-- =====================================================================
--  SECCIÓ 4 — TRIGGERS
--  Es creen al final perquè poden dependre de seqüències i funcions.
-- =====================================================================

-- 4.1 Auto-assignació de PK a la taula GAME via SEQ_GAME
CREATE OR REPLACE TRIGGER TRG_GAME_PK
    BEFORE INSERT ON GAME
    FOR EACH ROW
BEGIN
    IF :NEW.GAMEID IS NULL THEN
        :NEW.GAMEID := SEQ_GAME.NEXTVAL;
    END IF;
END;
/

-- 4.2 Trigger informatiu: cada vegada que s'incrementa GAMES_WON,
--     mostra per DBMS_OUTPUT el % de jugadors per sota.
--
--     Implementat com COMPOUND TRIGGER per evitar ORA-04091 (mutating table):
--     un row-level trigger que crida una funció que llegeix la mateixa
--     taula que s'està modificant és il·legal a Oracle.
--     Solució: separar la captura (BEFORE EACH ROW) del processament
--     (AFTER STATEMENT, quan la taula ja no està mutant).
CREATE OR REPLACE TRIGGER TRG_SHOW_PCT_ON_WIN
    FOR UPDATE OF GAMES_WON ON ENTITY
    COMPOUND TRIGGER

    -- Estructura per acumular les files modificades durant la sentència
    TYPE t_change IS RECORD (
        player_name ENTITY.PLAYERNAME%TYPE,
        new_won     ENTITY.GAMES_WON%TYPE
    );
    TYPE t_changes IS TABLE OF t_change INDEX BY PLS_INTEGER;
    g_changes t_changes;

    -- Per cada fila afectada, només capturem dades (sense tocar ENTITY)
    AFTER EACH ROW IS
    BEGIN
        IF :NEW.GAMES_WON > NVL(:OLD.GAMES_WON, -1)
           AND :NEW.ENTITYTYPE = 'PLAYER' THEN
            g_changes(g_changes.COUNT + 1).player_name := :NEW.PLAYERNAME;
            g_changes(g_changes.LAST).new_won         := :NEW.GAMES_WON;
        END IF;
    END AFTER EACH ROW;

    -- Quan la sentència ha acabat, la taula ja NO està mutant:
    -- ara podem llegir-la des de la funció sense ORA-04091.
    AFTER STATEMENT IS
        v_pct NUMBER;
    BEGIN
        FOR i IN 1 .. g_changes.COUNT LOOP
            v_pct := FN_PCT_PLAYERS_BELOW(g_changes(i).new_won);
            DBMS_OUTPUT.PUT_LINE('>>> Jugador "' || g_changes(i).player_name
                                 || '" ara té ' || g_changes(i).new_won
                                 || ' victòries.');
            DBMS_OUTPUT.PUT_LINE('>>> El ' || v_pct
                                 || '% de jugadors té menys victòries que ell/ella.');
        END LOOP;
    END AFTER STATEMENT;

END TRG_SHOW_PCT_ON_WIN;
/


-- =====================================================================
--  VERIFICACIÓ — comprova que tot s'ha creat correctament
-- =====================================================================

SELECT object_name, object_type, status
  FROM USER_OBJECTS
 WHERE object_name IN (
        'SEQ_ENTITY','SEQ_BOARD','SEQ_GAME','SEQ_INVENTORY','SEQ_OBJECT','SEQ_EVENT','SEQ_SQUARE',
        'FN_MAX_GAMES_WON','FN_AVG_GAMES_WON','FN_PCT_PLAYERS_BELOW',
        'SP_PLAYERS_WITH_RECORD','SP_PLAYERS_ABOVE_AVERAGE','SP_RANKING_BY_PLAYED',
        'TRG_GAME_PK','TRG_SHOW_PCT_ON_WIN'
       )
 ORDER BY object_type, object_name;


-- =====================================================================
--  PROVES RÀPIDES (descomenta per executar)
-- =====================================================================

-- SET SERVEROUTPUT ON;

-- -- Récord actual
-- BEGIN
--     DBMS_OUTPUT.PUT_LINE('Récord: ' || FN_MAX_GAMES_WON() || ' victòries');
-- END;
-- /

-- -- Llistat de récords
-- EXEC SP_PLAYERS_WITH_RECORD;

-- -- Mitjana
-- BEGIN
--     DBMS_OUTPUT.PUT_LINE('Mitjana: ' || ROUND(FN_AVG_GAMES_WON(), 2));
-- END;
-- /

-- -- Per sobre de la mitjana
-- EXEC SP_PLAYERS_ABOVE_AVERAGE;

-- -- % per sota d'un valor
-- BEGIN
--     DBMS_OUTPUT.PUT_LINE('% < 3 victòries: ' || FN_PCT_PLAYERS_BELOW(3) || '%');
-- END;
-- /

-- -- Rànquing complet
-- EXEC SP_RANKING_BY_PLAYED;

-- -- Errors controlats:
-- EXEC SP_RANKING_BY_PLAYED('JugadorFalso');             -- jugador no existeix
-- EXEC SP_RANKING_BY_PLAYED('NomRealSensePartides');     -- no ha jugat


-- =====================================================================
--  ROLLBACK (per si vols esborrar-ho tot i tornar a començar)
-- =====================================================================

-- DROP TRIGGER   TRG_SHOW_PCT_ON_WIN;
-- DROP TRIGGER   TRG_GAME_PK;
-- DROP PROCEDURE SP_RANKING_BY_PLAYED;
-- DROP PROCEDURE SP_PLAYERS_ABOVE_AVERAGE;
-- DROP PROCEDURE SP_PLAYERS_WITH_RECORD;
-- DROP FUNCTION  FN_PCT_PLAYERS_BELOW;
-- DROP FUNCTION  FN_AVG_GAMES_WON;
-- DROP FUNCTION  FN_MAX_GAMES_WON;
-- DROP SEQUENCE  SEQ_SQUARE;
-- DROP SEQUENCE  SEQ_EVENT;
-- DROP SEQUENCE  SEQ_OBJECT;
-- DROP SEQUENCE  SEQ_INVENTORY;
-- DROP SEQUENCE  SEQ_GAME;
-- DROP SEQUENCE  SEQ_BOARD;
-- DROP SEQUENCE  SEQ_ENTITY;


-- =====================================================================
--  SOBRE TRG_GAME_FINISHED — INTENCIONALMENT EXCLÒS
--
--  Aquest trigger del fitxer original esperava un esquema diferent:
--   1) Es disparava AFTER UPDATE OF GAMESTATE ON GAME, però el codi
--      Java fa INSERT INTO GAME ... VALUES('FINISHED', ...) directament.
--      El trigger no es dispara mai amb un INSERT.
--   2) Cercava ENTITY amb ENTITYTYPE='WINNER', però el CHECK constraint
--      només permet 'PLAYER' o 'SEAL'. Sempre cau a NO_DATA_FOUND.
--
--  L'increment de GAMES_PLAYED i GAMES_WON el fa directament Java a
--  SaveLoadService.recordGameResult() amb NVL(...) per evitar el bug
--  NULL+1=NULL d'Oracle. El trigger TRG_SHOW_PCT_ON_WIN d'aquí sí que
--  es dispara amb aquestes UPDATE i complementa la lògica.
-- =====================================================================
