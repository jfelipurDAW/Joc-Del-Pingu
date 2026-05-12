SET SERVEROUTPUT ON;

CREATE OR REPLACE TRIGGER TRG_SHOW_PCT_ON_WIN
    FOR UPDATE OF GAMES_WON ON ENTITY
    COMPOUND TRIGGER

    TYPE t_change IS RECORD (
        player_name ENTITY.PLAYERNAME%TYPE,
        new_won     ENTITY.GAMES_WON%TYPE
    );
    TYPE t_changes IS TABLE OF t_change INDEX BY PLS_INTEGER;
    g_changes t_changes;

    AFTER EACH ROW IS
    BEGIN
        IF :NEW.GAMES_WON > NVL(:OLD.GAMES_WON, -1)
           AND :NEW.ENTITYTYPE = 'PLAYER' THEN
            g_changes(g_changes.COUNT + 1).player_name := :NEW.PLAYERNAME;
            g_changes(g_changes.LAST).new_won         := :NEW.GAMES_WON;
        END IF;
    END AFTER EACH ROW;

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