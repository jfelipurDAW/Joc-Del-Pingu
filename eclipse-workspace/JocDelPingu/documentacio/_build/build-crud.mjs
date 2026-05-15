/////////////////////////////////////////////////////
///                                               ///
///   CRUD DOCUMENT GENERATOR (Word)              ///
///                                               ///
/////////////////////////////////////////////////////

import {
  Document, Packer, Paragraph, TextRun, HeadingLevel,
  AlignmentType, BorderStyle, ShadingType,
  Table, TableRow, TableCell, WidthType,
  PageOrientation,
} from "docx";
import fs from "fs";
import path from "path";

const COLOR_TITLE   = "0D3B66";
const COLOR_ACCENT  = "1565C0";
const COLOR_MUTED   = "555555";
const COLOR_CODE_BG = "F4F6FB";
const COLOR_SHOT_BG = "EEF6FF";

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 200, after: 80 },
    children: [new TextRun({ text, bold: true, color: COLOR_TITLE, size: 30 })],
  });
}
function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 160, after: 60 },
    children: [new TextRun({ text, bold: true, color: COLOR_TITLE, size: 24 })],
  });
}
function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 120, after: 40 },
    children: [new TextRun({ text, bold: true, color: COLOR_ACCENT, size: 20 })],
  });
}

function p(text, opts = {}) {
  return new Paragraph({
    spacing: { after: opts.after ?? 80, line: 260 },
    children: Array.isArray(text) ? text : [new TextRun({ text, size: opts.size ?? 19 })],
  });
}

function label(name, value) {
  return new Paragraph({
    spacing: { after: 30, line: 240 },
    children: [
      new TextRun({ text: `${name}: `, bold: true, size: 18, color: COLOR_TITLE }),
      new TextRun({ text: value, size: 18 }),
    ],
  });
}

function bullet(text) {
  return new Paragraph({
    spacing: { after: 30, line: 240 },
    bullet: { level: 0 },
    children: [new TextRun({ text, size: 18 })],
  });
}

function code(text, opts = {}) {
  const lines = text.replace(/\r\n/g, "\n").split("\n");
  return lines.map((ln, i) =>
    new Paragraph({
      spacing: { after: i === lines.length - 1 ? 60 : 0, line: 220 },
      shading: { type: ShadingType.CLEAR, fill: opts.fill ?? COLOR_CODE_BG, color: "auto" },
      border: i === 0
        ? { top: { style: BorderStyle.SINGLE, size: 4, color: "C2C8D2" } }
        : (i === lines.length - 1
            ? { bottom: { style: BorderStyle.SINGLE, size: 4, color: "C2C8D2" } }
            : undefined),
      children: [new TextRun({ text: ln || " ", font: "Consolas", size: opts.size ?? 16 })],
    }));
}

// Placeholder for screenshots: a labelled bordered box the user can replace
// with a real image via Word's Insert > Picture.
function screenshotPlaceholder(caption) {
  return [
    new Paragraph({
      spacing: { before: 60, after: 20, line: 240 },
      alignment: AlignmentType.CENTER,
      shading: { type: ShadingType.CLEAR, fill: COLOR_SHOT_BG, color: "auto" },
      border: {
        top:    { style: BorderStyle.DASHED, size: 6, color: COLOR_ACCENT },
        bottom: { style: BorderStyle.DASHED, size: 6, color: COLOR_ACCENT },
        left:   { style: BorderStyle.DASHED, size: 6, color: COLOR_ACCENT },
        right:  { style: BorderStyle.DASHED, size: 6, color: COLOR_ACCENT },
      },
      children: [new TextRun({
        text: `[ CAPTURA · ${caption} — substituir aquesta caixa per la captura real (Inserir > Imatge) ]`,
        italics: true, color: COLOR_ACCENT, size: 17,
      })],
    }),
  ];
}

/////////////////////////////
///    CRUD SECTIONS      ///
/////////////////////////////

function crudSection({ idx, title, fileRef, kind, sql, java, explanation, screenshot }) {
  return [
    h3(`${idx}. ${title}`),
    label("Tipus", kind),
    label("Localització", fileRef),
    new Paragraph({ spacing: { after: 20 }, children: [new TextRun({ text: "Sentència SQL incrustada:", bold: true, size: 18, color: COLOR_TITLE })] }),
    ...code(sql),
    new Paragraph({ spacing: { before: 30, after: 20 }, children: [new TextRun({ text: "Fragment del codi Java on s'utilitza:", bold: true, size: 18, color: COLOR_TITLE })] }),
    ...code(java),
    new Paragraph({ spacing: { before: 30, after: 20 }, children: [new TextRun({ text: "Descripció i explicació:", bold: true, size: 18, color: COLOR_TITLE })] }),
    ...explanation.map((line) => bullet(line)),
    ...screenshotPlaceholder(screenshot),
  ];
}

/////////////////////////////
///    CONTENT            ///
/////////////////////////////

const COVER = [
  new Paragraph({
    spacing: { before: 0, after: 60 },
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "JOC D'EN PINGU — CRUD a Java", bold: true, size: 32, color: COLOR_TITLE })],
  }),
  new Paragraph({
    spacing: { after: 80 },
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "Documentació de les sentències SQL incrustades al codi", bold: true, size: 22, color: COLOR_ACCENT })],
  }),
  new Paragraph({
    spacing: { after: 200 },
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "Grup DW2526_GR06 · Mòdul M0484", size: 17, color: COLOR_MUTED })],
  }),

  h2("Introducció"),
  p("Aquest document recopila totes les sentències SQL que s'executen contra la base de dades Oracle des del codi Java del joc. Per a cadascuna s'indica el fitxer i mètode on apareix, el codi SQL exacte, el fragment Java que la dispara, l'explicació del seu objectiu, i una caixa per inserir la captura corresponent de la seva execució en temps real."),
  p("Les sentències s'agrupen pel tipus d'operació CRUD que realitzen (Create / Read / Update / Delete). Es documenta també l'ús del MERGE (operació mixta UPDATE-o-INSERT) emprat al registre de jugadors."),

  h2("Capa d'accés a dades"),
  p("Tot l'accés a Oracle passa per la classe estàtica model.db.BBDD, que encapsula la connexió i exposa quatre mètodes genèrics: insert(con, sql), update(con, sql), delete(con, sql) i select(con, sql). saveGame() trenca aquest patró i fa servir PreparedStatement directament perquè el camp GAME_DATA és un CLOB superior als 4000 caràcters (limit literal SQL d'Oracle, error ORA-01704)."),
  p("Totes les cadenes que arriben de l'usuari es protegeixen amb un escapament simple de cometes (.replace(\"'\", \"''\")), i les contrasenyes es xifren amb AES-128 (CryptoUtil) abans d'enviar-les a la BD."),
];

/////////////////////////////
///    CREATE OPERATIONS  ///
/////////////////////////////

const SEC_CREATE = [
  h1("CREATE (INSERT)"),

  ...crudSection({
    idx: "1.1",
    title: "Registre/alta de jugadors — MERGE INTO ENTITY (UPSERT)",
    fileRef: "model/game/SaveLoadService.java · registerPlayer() (línies 336-376)",
    kind: "INSERT (via MERGE: upsert idempotent)",
    sql: `MERGE INTO ENTITY e
USING (SELECT '<safeName>' AS pname FROM DUAL) src
   ON (e.PLAYERNAME = src.pname AND e.ENTITYTYPE = 'PLAYER')
WHEN MATCHED THEN
   UPDATE SET e.PLAYERPASSWORD = '<safePassword>',
              e.COLOUR = '<safeColor>'
WHEN NOT MATCHED THEN
   INSERT (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR, GAMES_PLAYED, GAMES_WON)
   VALUES (<newId>, 'PLAYER', '<safeName>', '<safePassword>', '<safeColor>', 0, 0);`,
    java: `// 1) Xifrat AES-128 de la contrasenya abans de res
String encryptedPwd = CryptoUtil.encrypt(password != null ? password : "");
String safePassword = (encryptedPwd != null ? encryptedPwd : "").replace("'", "''");

// 2) Càlcul del següent ID lliure
int newId = 1;
ArrayList<LinkedHashMap<String, String>> maxResult =
    BBDD.select(con, "SELECT NVL(MAX(ENTITYID), 0) + 1 AS NEXTID FROM ENTITY");
if (!maxResult.isEmpty()) newId = Integer.parseInt(maxResult.get(0).get("NEXTID"));

// 3) Sentència MERGE (vegeu SQL a sobre)
BBDD.executeInsUpDel(con, sql, "Merge");`,
    explanation: [
      "S'invoca des de PlayerSetupController quan l'usuari clica «Start Game»: cada jugador definit al formulari es registra (o actualitza) abans d'iniciar la partida.",
      "Es fa servir MERGE perquè el botó «Select existing player» també torna a passar per aquest mètode i no pot fallar amb violació de UNIQUE.",
      "L'ID es calcula amb SELECT NVL(MAX(ENTITYID),0)+1 perquè la columna ENTITYID no és IDENTITY a l'esquema actual.",
      "GAMES_PLAYED i GAMES_WON s'inicialitzen explícitament a 0 per evitar el bug NULL+1=NULL que afectaria al recompte posterior."
    ],
    screenshot: "MERGE executat — registre nou + ENTITY mostra la fila a SQL Developer",
  }),

  ...crudSection({
    idx: "1.2",
    title: "Guardar partida — INSERT a SAVED_GAMES amb CLOB (PreparedStatement)",
    fileRef: "model/game/SaveLoadService.java · saveGame() (línies 158-170)",
    kind: "INSERT amb PreparedStatement (camp CLOB)",
    sql: `INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES (?, ?);`,
    java: `// Serialitzar l'estat de la partida a YAML i xifrar-lo amb AES-128
Yaml yaml = new Yaml();
String yamlString = yaml.dump(state);
String encrypted = CryptoUtil.encrypt(yamlString);

// El payload xifrat supera els 4000 caràcters → PreparedStatement evita
// l'error ORA-01704 ("string literal too long") d'un INSERT concatenat.
String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES (?, ?)";
try (PreparedStatement ps = con.prepareStatement(sql)) {
    ps.setString(1, customName);
    ps.setString(2, encrypted);
    int rows = ps.executeUpdate();
    if (!con.getAutoCommit()) con.commit();
    return rows > 0;
}`,
    explanation: [
      "S'invoca des del menú in-game «Save» del GameBoardController. L'usuari escull un nom i tot l'estat (taller, jugadors, foca, torn actual) es desa com a CLOB encriptat.",
      "És l'única sentència que NO passa per BBDD.insert(): el CLOB requereix ps.setString() per fluxar el contingut via JDBC.",
      "S'inclou commit explícit perquè algunes configuracions JDBC tenen auto-commit desactivat i la fila no es persistiria.",
      "El nom de la partida actua com a PK; un segon save amb el mateix nom fallaria amb violació de PK_SAVED_GAMES (i el missatge es captura amb un try/catch)."
    ],
    screenshot: "Diàleg «Save Game» del joc + SELECT a SAVED_GAMES mostrant la fila nova",
  }),

  ...crudSection({
    idx: "1.3",
    title: "Crear taulell si no existeix — INSERT idempotent",
    fileRef: "model/game/SaveLoadService.java · recordGameResult() (línies 201-205)",
    kind: "INSERT condicional",
    sql: `INSERT INTO BOARD (BOARDID)
SELECT 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM BOARD WHERE BOARDID = 1);`,
    java: `// Garantia mínima abans del INSERT INTO GAME: la FK fk_game_board
// exigeix que BOARD(boardID=1) existeixi. INSERT…SELECT WHERE NOT EXISTS
// és idempotent: si la fila ja hi és no s'insereix res (no llança error).
BBDD.executeInsUpDel(con,
    "INSERT INTO BOARD (BOARDID) " +
    "SELECT 1 FROM DUAL " +
    "WHERE NOT EXISTS (SELECT 1 FROM BOARD WHERE BOARDID = 1)",
    "Insert BOARD");`,
    explanation: [
      "Es crida just abans d'inserir una nova fila a GAME, ja que GAME.BOARDID és FK obligatòria.",
      "Tècnica habitual a Oracle per a inserts idempotents quan no es vol gestionar manualment una excepció DUP_VAL_ON_INDEX.",
      "Si la taula BOARD ja conté la fila (boardID=1), la subconsulta WHERE NOT EXISTS retorna fals i el INSERT no afecta cap fila — el resultat sempre és consistent."
    ],
    screenshot: "Console del joc + SELECT BOARDID FROM BOARD mostrant la fila única",
  }),

  ...crudSection({
    idx: "1.4",
    title: "Registre del resultat d'una partida — INSERT INTO GAME",
    fileRef: "model/game/SaveLoadService.java · recordGameResult() (línia 207)",
    kind: "INSERT (auto-PK via TRG_GAME_PK)",
    sql: `INSERT INTO GAME (GAMESTATE, GAMEDATE, BOARDID) VALUES ('FINISHED', SYSDATE, 1);`,
    java: `// El GAMEID NO es proporciona: el trigger TRG_GAME_PK consulta
// SEQ_GAME.NEXTVAL i omple la clau primària automàticament.
BBDD.insert(con, "INSERT INTO GAME (GAMESTATE, GAMEDATE, BOARDID) " +
                 "VALUES ('FINISHED', SYSDATE, 1)");`,
    explanation: [
      "S'executa des de GameBoardController en finalitzar la partida (tant si guanya un jugador com si guanya la foca).",
      "El GAMEID s'omple automàticament gràcies al trigger PL/SQL TRG_GAME_PK (vegeu documentació PL/SQL).",
      "GAMEDATE = SYSDATE deixa traça de quan va acabar; serveix per ordenar l'historial al PlayerStatsController.",
      "Després d'aquesta inserció es disparen els UPDATEs de comptadors (vegeu seccions 3.2 i 3.3)."
    ],
    screenshot: "SELECT * FROM GAME ORDER BY GAMEDATE DESC mostrant la partida acabada",
  }),

  ...crudSection({
    idx: "1.5",
    title: "INSERT de prova — BBDDPanel (verificació del CRUD)",
    fileRef: "view/ui/BBDDPanel.java · main() (línia 21)",
    kind: "INSERT (script de proves)",
    sql: `INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR)
VALUES (999, 'PLAYER', 'PinguTest', 'SuperPingu1234', 'BLUE');`,
    java: `System.out.println("\\n--- 1. INSERT ---");
model.db.BBDD.insert(con,
    "INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR) " +
    "VALUES (999, 'PLAYER', 'PinguTest', 'SuperPingu1234', 'BLUE')");
model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);`,
    explanation: [
      "Aquest fragment forma part de la plantilla original del mòdul (BBDDPanel) i serveix per verificar que la connexió funciona i que els quatre verbs CRUD bàsics tenen efecte sobre Oracle.",
      "Inserta una entitat fictícia (PinguTest, ID=999) i tot seguit fa un SELECT per imprimir-la per pantalla — la mateixa fila es modifica al pas 2 (UPDATE) i s'elimina al pas 3 (DELETE)."
    ],
    screenshot: "Sortida per consola amb la línia de la fila inserida (PinguTest)",
  }),
];

/////////////////////////////
///    READ OPERATIONS    ///
/////////////////////////////

const SEC_READ = [
  h1("READ (SELECT)"),

  ...crudSection({
    idx: "2.1",
    title: "Llistar partides desades",
    fileRef: "model/game/SaveLoadService.java · getAllSavedGameIds() (línies 57-58)",
    kind: "SELECT (llista)",
    sql: `SELECT GAME_ID FROM SAVED_GAMES ORDER BY GAME_ID DESC;`,
    java: `String sql = "SELECT GAME_ID FROM SAVED_GAMES ORDER BY GAME_ID DESC";
ArrayList<LinkedHashMap<String, String>> result = BBDD.select(con, sql);
// Es transforma a List<String> per alimentar el ChoiceDialog del menú principal
for (LinkedHashMap<String, String> row : result) ids.add(row.get("GAME_ID"));`,
    explanation: [
      "El crida MainMenuController quan l'usuari prem «Load Game»: omple el diàleg de selecció amb tots els noms guardats.",
      "L'ORDER BY DESC mostra les partides més recents al capdamunt, però com que GAME_ID és un text definit per l'usuari, l'ordenació és lexicogràfica, no temporal.",
      "Si la consulta retorna llista buida, el controlador mostra «No saved games found.»."
    ],
    screenshot: "Diàleg «Load Game» del menú principal mostrant les partides disponibles",
  }),

  ...crudSection({
    idx: "2.2",
    title: "Carregar una partida (recuperar el CLOB encriptat)",
    fileRef: "model/game/SaveLoadService.java · loadGame() (línia 258)",
    kind: "SELECT (fila única)",
    sql: `SELECT GAME_DATA FROM SAVED_GAMES WHERE GAME_ID = '<gameId>';`,
    java: `ArrayList<LinkedHashMap<String, String>> result = BBDD.select(con,
    "SELECT GAME_DATA FROM SAVED_GAMES WHERE GAME_ID = '" + gameId + "'");
String encrypted  = result.get(0).get("GAME_DATA");
String yamlString = CryptoUtil.decrypt(encrypted);   // AES-128 invers
Map<String, Object> state = new Yaml().load(yamlString);`,
    explanation: [
      "El CLOB encriptat es desxifra amb AES-128 i es parseja a YAML per reconstruir l'estat de la partida (taulell, jugadors, foca, torn).",
      "Si la fila no existeix, result.isEmpty() i la càrrega retorna false sense modificar res.",
      "Si la desencriptació falla (CLOB corrupte o clau diferent), CryptoUtil.decrypt retorna null i la càrrega també aborta de forma segura."
    ],
    screenshot: "Joc carregant una partida — pantalla del taulell amb la configuració restaurada",
  }),

  ...crudSection({
    idx: "2.3",
    title: "Verificar contrasenya de jugador",
    fileRef: "model/game/SaveLoadService.java · verifyPassword() (línia 397)",
    kind: "SELECT (autenticació)",
    sql: `SELECT PLAYERPASSWORD FROM ENTITY
WHERE PLAYERNAME = '<safeName>' AND ENTITYTYPE = 'PLAYER';`,
    java: `String sql = "SELECT PLAYERPASSWORD FROM ENTITY " +
             "WHERE PLAYERNAME = '" + safeName + "' AND ENTITYTYPE = 'PLAYER'";
ArrayList<LinkedHashMap<String, String>> result = BBDD.select(con, sql);
String storedEncrypted = result.get(0).get("PLAYERPASSWORD");
return CryptoUtil.decrypt(storedEncrypted).equals(inputPassword);`,
    explanation: [
      "S'invoca al login (cada cop que un jugador es vol unir a una partida) i a la càrrega d'una partida desada (autenticació dels participants).",
      "El valor a la base de dades està xifrat amb AES-128; per comparar es desencripta i es compara amb la cadena introduïda.",
      "Casos especials: si la fila no existeix es tracta com a jugador nou (true); si el password emmagatzemat és buit només s'accepta input buit."
    ],
    screenshot: "Diàleg de password al joc + missatge «Authentication OK»",
  }),

  ...crudSection({
    idx: "2.4",
    title: "Obtenir el següent ENTITYID lliure",
    fileRef: "model/game/SaveLoadService.java · registerPlayer() (línies 349-350)",
    kind: "SELECT amb agregació",
    sql: `SELECT NVL(MAX(ENTITYID), 0) + 1 AS NEXTID FROM ENTITY;`,
    java: `int newId = 1;
ArrayList<LinkedHashMap<String, String>> maxResult =
    BBDD.select(con, "SELECT NVL(MAX(ENTITYID), 0) + 1 AS NEXTID FROM ENTITY");
if (!maxResult.isEmpty()) newId = Integer.parseInt(maxResult.get(0).get("NEXTID"));`,
    explanation: [
      "Calcula manualment el següent ID perquè la columna ENTITYID no és IDENTITY ni té sequence_trigger associat (a diferència de GAME, que sí en té).",
      "NVL(MAX(...),0)+1 evita el cas de taula buida (MAX retornaria NULL i NULL+1=NULL).",
      "S'utilitza només a registerPlayer, just abans del MERGE de la secció 1.1."
    ],
    screenshot: "Sortida de la consola del joc amb el missatge del MERGE incloent el nou ID generat",
  }),

  ...crudSection({
    idx: "2.5",
    title: "Estadístiques globals dels jugadors",
    fileRef: "model/game/SaveLoadService.java · getPlayerStats() (línies 440-445)",
    kind: "SELECT amb ORDER BY",
    sql: `SELECT PLAYERNAME, COLOUR,
       NVL(GAMES_PLAYED, 0) AS GAMES_PLAYED,
       NVL(GAMES_WON, 0)    AS GAMES_WON
FROM ENTITY WHERE ENTITYTYPE = 'PLAYER'
ORDER BY GAMES_WON DESC, GAMES_PLAYED DESC;`,
    java: `String sql = "SELECT PLAYERNAME, COLOUR, " +
             "NVL(GAMES_PLAYED, 0) AS GAMES_PLAYED, " +
             "NVL(GAMES_WON, 0) AS GAMES_WON " +
             "FROM ENTITY WHERE ENTITYTYPE = 'PLAYER' " +
             "ORDER BY GAMES_WON DESC, GAMES_PLAYED DESC";
stats = BBDD.select(con, sql);`,
    explanation: [
      "Alimenta la pantalla PlayerStatsController (botó «Stats» del menú principal): mostra el rànquing complet.",
      "Els NVL converteixen comptadors a 0 si la columna és NULL (jugadors antics anteriors a la migració).",
      "L'ordenació per GAMES_WON desempata amb GAMES_PLAYED — un jugador que ha jugat més partides apareix abans amb mateixa puntuació."
    ],
    screenshot: "Pantalla d'estadístiques del joc mostrant la taula amb totes les columnes",
  }),

  ...crudSection({
    idx: "2.6",
    title: "Llistar jugadors registrats (per al diàleg «Select existing»)",
    fileRef: "model/game/SaveLoadService.java · getRegisteredPlayers() (línies 467-468)",
    kind: "SELECT (llista)",
    sql: `SELECT PLAYERNAME, PLAYERPASSWORD, COLOUR
FROM ENTITY WHERE ENTITYTYPE = 'PLAYER';`,
    java: `String sql = "SELECT PLAYERNAME, PLAYERPASSWORD, COLOUR " +
             "FROM ENTITY WHERE ENTITYTYPE = 'PLAYER'";
ArrayList<LinkedHashMap<String, String>> result = BBDD.select(con, sql);
// Es construeixen objectes Player a memòria amb la contrasenya ja desxifrada
for (LinkedHashMap<String, String> row : result) {
    Player p = new Player(row.get("PLAYERNAME"), row.get("COLOUR"));
    p.setPassword(CryptoUtil.decrypt(row.get("PLAYERPASSWORD")));
    players.add(p);
}`,
    explanation: [
      "PlayerSetupController el crida quan l'usuari prem «Select existing player» — així s'omple el ChoiceDialog amb noms ja coneguts en lloc d'haver-los de tornar a escriure.",
      "PLAYERPASSWORD es desxifra per autocompletar el camp password al formulari (el jugador encara haurà d'introduir la mateixa contrasenya per validar)."
    ],
    screenshot: "Diàleg «Select existing player» mostrant la llista de noms recuperats",
  }),
];

/////////////////////////////
///    UPDATE OPERATIONS  ///
/////////////////////////////

const SEC_UPDATE = [
  h1("UPDATE"),

  ...crudSection({
    idx: "3.1",
    title: "Actualització de password (branca MATCHED del MERGE)",
    fileRef: "model/game/SaveLoadService.java · registerPlayer() (línies 360-362)",
    kind: "UPDATE (dins de MERGE)",
    sql: `WHEN MATCHED THEN
   UPDATE SET e.PLAYERPASSWORD = '<safePassword>',
              e.COLOUR        = '<safeColor>'`,
    java: `// Si el jugador ja existia, MERGE actualitza el password (xifrat) i el color
// preservant ENTITYID, GAMES_PLAYED i GAMES_WON.`,
    explanation: [
      "Branca del MERGE de la secció 1.1. S'activa quan el jugador ja és a ENTITY (clau natural PLAYERNAME + ENTITYTYPE='PLAYER').",
      "Conserva ENTITYID i, sobretot, GAMES_PLAYED/GAMES_WON: l'historial de partides no es perd quan algú canvia el color o re-introdueix la contrasenya."
    ],
    screenshot: "ENTITY abans i després — mateixos ENTITYID/GAMES_WON, PLAYERPASSWORD canviat",
  }),

  ...crudSection({
    idx: "3.2",
    title: "Incrementar GAMES_PLAYED en finalitzar partida",
    fileRef: "model/game/SaveLoadService.java · recordGameResult() (línies 214-215)",
    kind: "UPDATE (NVL anti-null)",
    sql: `UPDATE ENTITY SET GAMES_PLAYED = NVL(GAMES_PLAYED, 0) + 1
WHERE PLAYERNAME = '<safeName>' AND ENTITYTYPE = 'PLAYER';`,
    java: `for (Entity e : allPlayers) {
    if (e instanceof Player) {
        String safeName = ((Player) e).getName().replace("'", "''");
        int rows = BBDD.update(con,
            "UPDATE ENTITY SET GAMES_PLAYED = NVL(GAMES_PLAYED, 0) + 1 " +
            "WHERE PLAYERNAME = '" + safeName + "' AND ENTITYTYPE = 'PLAYER'");
        if (rows == 0) {
            System.err.println("recordGameResult: no row updated for player '" + safeName + "'.");
        }
    }
}`,
    explanation: [
      "Per cada jugador que ha participat en la partida (incloent perdedors), incrementem el comptador de partides jugades.",
      "NVL(GAMES_PLAYED, 0) + 1 evita el bug NULL+1=NULL: sense això, files antigues amb GAMES_PLAYED=NULL es quedarien sempre a NULL.",
      "rows==0 indica que el nom no es trobava a ENTITY (jugador no registrat); s'imprimeix un avís però el flux continua."
    ],
    screenshot: "ENTITY abans/després — la columna GAMES_PLAYED incrementada en +1 per a cada jugador",
  }),

  ...crudSection({
    idx: "3.3",
    title: "Incrementar GAMES_WON del guanyador",
    fileRef: "model/game/SaveLoadService.java · recordGameResult() (línies 225-226)",
    kind: "UPDATE (dispara el trigger TRG_SHOW_PCT_ON_WIN)",
    sql: `UPDATE ENTITY SET GAMES_WON = NVL(GAMES_WON, 0) + 1
WHERE PLAYERNAME = '<safeWinner>' AND ENTITYTYPE = 'PLAYER';`,
    java: `if (winnerName != null && !winnerName.isEmpty()) {
    String safeWinner = winnerName.replace("'", "''");
    int rows = BBDD.update(con,
        "UPDATE ENTITY SET GAMES_WON = NVL(GAMES_WON, 0) + 1 " +
        "WHERE PLAYERNAME = '" + safeWinner + "' AND ENTITYTYPE = 'PLAYER'");
}`,
    explanation: [
      "S'executa una sola vegada, només per al guanyador. Si winnerName és null (cas de victòria de la foca) s'omet el pas.",
      "Aquesta UPDATE és la que dispara el trigger PL/SQL TRG_SHOW_PCT_ON_WIN, que imprimeix per DBMS_OUTPUT el nou comptador i el percentatge de jugadors que ell/ella ha superat.",
      "Tornar a fer servir NVL aquí és imprescindible: sense NVL el guanyador novell tindria GAMES_WON=NULL i no s'incrementaria mai."
    ],
    screenshot: "DBMS_OUTPUT del trigger + ENTITY mostrant GAMES_WON incrementat",
  }),

  ...crudSection({
    idx: "3.4",
    title: "UPDATE de prova — BBDDPanel",
    fileRef: "view/ui/BBDDPanel.java · main() (línia 26)",
    kind: "UPDATE (script de proves)",
    sql: `UPDATE ENTITY SET PLAYERPASSWORD = 'NuevaClave99' WHERE PLAYERNAME = 'PinguTest';`,
    java: `System.out.println("\\n--- 2. UPDATE ---");
model.db.BBDD.update(con,
    "UPDATE ENTITY SET PLAYERPASSWORD = 'NuevaClave99' WHERE PLAYERNAME = 'PinguTest'");
model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);`,
    explanation: [
      "Modifica la contrasenya del jugador de test creat al pas 1.5 i tot seguit l'imprimeix per pantalla per comprovar visualment el canvi.",
      "Pertany a la mateixa plantilla original del mòdul; no s'utilitza en cap moment durant el joc real."
    ],
    screenshot: "Sortida per consola amb la fila després de l'UPDATE",
  }),
];

/////////////////////////////
///    DELETE OPERATIONS  ///
/////////////////////////////

const SEC_DELETE = [
  h1("DELETE"),

  ...crudSection({
    idx: "4.1",
    title: "DELETE de prova — BBDDPanel",
    fileRef: "view/ui/BBDDPanel.java · main() (línia 31)",
    kind: "DELETE (script de proves)",
    sql: `DELETE FROM ENTITY WHERE PLAYERNAME = 'PinguTest';`,
    java: `System.out.println("\\n--- 3. DELETE ---");
model.db.BBDD.delete(con, "DELETE FROM ENTITY WHERE PLAYERNAME = 'PinguTest'");
model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);`,
    explanation: [
      "Tanca el cicle CRUD del fitxer de proves: després d'inserir (1.5) i actualitzar (3.4) la fila «PinguTest», la elimina.",
      "El joc real NO conté cap operació DELETE incrustada: les partides desades no es poden suprimir des de la interfície (decisió de disseny — s'haurien d'eliminar manualment des de SQL Developer si calgués)."
    ],
    screenshot: "Consola després del DELETE — el SELECT de comprovació retorna 0 files",
  }),
];

/////////////////////////////
///    SUMMARY            ///
/////////////////////////////

const SEC_SUMMARY = [
  h1("Resum"),
  p("Aquesta és la distribució de sentències SQL incrustades al codi Java del joc:"),

  new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    rows: [
      new TableRow({
        tableHeader: true,
        children: [
          tableCell("Operació", true),
          tableCell("Sentències", true),
          tableCell("Fitxer principal", true),
        ],
      }),
      ...[
        ["CREATE (INSERT)", "5 (incloent 1 MERGE-INSERT + 1 INSERT amb PreparedStatement/CLOB)", "SaveLoadService.java"],
        ["READ (SELECT)",   "6 (carregar, autenticar, llistar, estadístiques, MAX ID)",         "SaveLoadService.java"],
        ["UPDATE",          "4 (1 dins MERGE + 2 comptadors + 1 de test)",                      "SaveLoadService.java + BBDDPanel.java"],
        ["DELETE",          "1 (només al fitxer de proves)",                                     "BBDDPanel.java"],
      ].map(([op, sentencies, fitxer]) => new TableRow({
        children: [tableCell(op), tableCell(sentencies), tableCell(fitxer)],
      })),
    ],
  }),

  p(""),
  p("Tota la lògica d'accés a dades es concentra a model.game.SaveLoadService, amb dues úniques excepcions: la classe BBDDPanel.java conté un petit script de proves CRUD del mòdul (no s'invoca des del joc) i la classe BBDD.java conté un mètode auxiliar guardarPartida() obsolet (substituït per saveGame). El patró general és INSERT/UPDATE via concatenació amb escapament de cometes simples, llevat de SAVED_GAMES on cal PreparedStatement per al camp CLOB."),
];

function tableCell(text, header = false) {
  return new TableCell({
    margins: { top: 60, bottom: 60, left: 90, right: 90 },
    shading: header ? { type: ShadingType.CLEAR, fill: "E6EFFF", color: "auto" } : undefined,
    children: [new Paragraph({
      spacing: { after: 0, line: 230 },
      children: [new TextRun({ text, bold: header, size: header ? 17 : 16 })],
    })],
  });
}

/////////////////////////////
///    DOC ASSEMBLY       ///
/////////////////////////////

const doc = new Document({
  creator: "Grup DW2526_GR06",
  title: "Joc d'en Pingu — CRUD incrustat",
  styles: {
    default: {
      document: {
        run: { font: "Calibri", size: 19 },
        paragraph: { spacing: { line: 260 } },
      },
    },
  },
  sections: [
    {
      properties: {
        page: {
          margin: { top: 900, bottom: 900, left: 1000, right: 1000 },
          size: { orientation: PageOrientation.PORTRAIT },
        },
      },
      children: [
        ...COVER,
        ...SEC_CREATE,
        ...SEC_READ,
        ...SEC_UPDATE,
        ...SEC_DELETE,
        ...SEC_SUMMARY,
      ],
    },
  ],
});

const OUT = path.resolve(path.dirname(new URL(import.meta.url).pathname.replace(/^\//, "")), "..", "Documentació CRUD - Joc del Pingu.docx");
const buf = await Packer.toBuffer(doc);
fs.writeFileSync(OUT, buf);
console.log("CRUD docx written:", OUT, "(" + buf.length + " bytes)");
