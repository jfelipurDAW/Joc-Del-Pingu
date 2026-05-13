# Joc del Pingu — Manual d'usuari

**Com instal·lar, jugar i resoldre problemes**

---

## Índex

1. Què és el Joc del Pingu
2. Instal·lació i inici
3. Funcionament bàsic
4. Normes del joc
5. Interfície
6. Possibles errors i solucions

---

## 1. Què és el Joc del Pingu

**Joc del Pingu** és un joc de taula per ordinador per a 1-4 jugadors, ambientat a l'Àrtic. Cada jugador controla un pingüí que ha d'arribar el primer a la casella final, esquivant forats de gel, óssos polars i una foca controlada per IA (opcional). En el camí pot recollir peixos, boles de neu i daus alternatius per guanyar avantatge sobre els rivals.

### 1.1. Característiques principals

- Mode 1 a 4 jugadors locals (a la mateixa pantalla, per torns).
- Tauler generat aleatòriament cada partida (50 caselles).
- Combat de boles de neu entre jugadors a la mateixa casella.
- Foca antagonista opcional controlada per IA.
- Sistema de save/load amb encriptació.
- Estadístiques globals (rànquing amb medalles).
- Suport per a 12 idiomes, canviables en runtime.
- Avatars personalitzables per jugador.

---

## 2. Instal·lació i inici

### 2.1. Requisits del sistema

- **Sistema operatiu**: Windows 10 / 11, macOS 11+ o Linux modern.
- **Java**: JDK 17 o superior instal·lat.
- **Memòria RAM**: mínim 512 MB lliures.
- **Connexió a internet**: necessària per a la BBDD Oracle (registres i partides guardades).

### 2.2. Instal·lació

1. Descomprimeix el ZIP del joc en una carpeta de la teva elecció.
2. Comprova que tens Java 17+ instal·lat obrint una terminal i executant:
   ```
   java -version
   ```
3. Si no tens Java, descarrega'l des de [adoptium.net](https://adoptium.net/) i instal·la'l.

### 2.3. Iniciar el joc

#### Opció A: Des d'Eclipse

1. Importa el projecte (*File → Import → Existing Projects*).
2. Obre `controller.main.MainMenu`.
3. Botó dret → *Run As → Java Application*.

#### Opció B: Des de l'executable

Si tens un JAR empaquetat, fes doble clic o executa:

```
java -jar JocDelPingu.jar
```

---

## 3. Funcionament bàsic

### 3.1. Començar una partida nova

1. Al menú principal, prem **New Game**.
2. Tria el nombre de jugadors al desplegable (1-4).
3. Per a cada jugador, omple:
   - **Nom** (obligatori, identifica el jugador).
   - **Contrasenya** (opcional; protegeix les estadístiques).
   - **Color** del pingüí (selector de color).
   - **Avatar** personalitzat (opcional — botó "Choose Avatar").
4. Si vols reutilitzar un jugador registrat, prem **Select Existing Player**.
5. Marca **Enable Seal** si vols afegir la foca antagonista.
6. Prem **Start Game**.

### 3.2. Carregar una partida guardada

1. Al menú principal, prem **Load Game**.
2. Apareixerà una llista amb totes les partides guardades. Selecciona'n una.
3. Si els jugadors d'aquella partida tenen contrasenya, et demanarà que la introdueixis un per un.
4. Un cop validades, es restaurarà l'estat exacte de la partida.

### 3.3. Veure estadístiques

Prem **Stats** al menú principal per veure el rànquing global:

- 🥇 Gold per al primer classificat.
- 🥈 Silver per al segon.
- 🥉 Bronze per al tercer.
- Ordenat per partides guanyades, després per partides jugades.

### 3.4. Canviar d'idioma

Al menú principal hi ha un desplegable d'idiomes. Selecciona'l i tota la interfície es traduirà a l'instant (sense reiniciar el joc).

Idiomes disponibles: Català, Castellà, Anglès, Francès, Portuguès, Romanès, Rus, Ucraïnès, Àrab, Japonès, i variants.

---

## 4. Normes del joc

### 4.1. Objectiu

Arribar el primer a la casella **END** (l'última del tauler). Si la foca està activada, també pot guanyar si "menja" tots els jugadors (perden tots la meitat de l'inventari i no poden completar el tauler).

### 4.2. Estructura del torn

1. El jugador actiu tira el dau (1-6).
2. El seu pingüí es mou cap endavant tantes caselles com indiqui el dau.
3. En arribar, s'aplica l'efecte de la casella.
4. Si comparteix casella amb un altre jugador, es pot iniciar combat de boles de neu.
5. El torn passa al següent jugador.
6. Si la foca està activada, té un torn dedicat entre rondes.

### 4.3. Tipus de caselles

| Icona | Nom | Efecte |
|---|---|---|
| 🟦 | **START** | Casella de sortida. Tots els pingüins comencen aquí. |
| ⬜ | **NORMAL** | Casella neutra. No té cap efecte. |
| 🕳 | **ICE HOLE** (forat de gel) | El pingüí cau i és enviat al **forat anterior** de la cadena (o al START si és el primer forat). |
| 🛷 | **SLED** (trineu) | El pingüí avança fins al **trineu següent** de la cadena (si és l'últim, no es mou). |
| 🐻 | **BEAR** (os polar) | El pingüí és atacat per l'os polar. **Salta el següent torn**. |
| ⚡ | **EVENT** (esdeveniment) | Activa un esdeveniment aleatori (guany d'objecte, intercanvi, etc.). |
| 🧊 | **BROKEN FLOOR** (terra trencat) | El pingüí cau i la casella es converteix en un nou **forat de gel** permanent. |
| 🏁 | **END** (meta) | Casella final. El primer que hi arriba **guanya la partida**. |

### 4.4. Objectes recollibles

| Objecte | Funció |
|---|---|
| ❄️ Bola de neu | Es pot llançar contra un altre jugador per fer-li perdre el torn. |
| 🐟 Peix | Aliment per a la foca (l'allunya) o per recuperar inventari. |
| 🎲 Dau ràpid | Tira un dau amb valors més alts (3-8). |
| 🎲 Dau lent | Tira un dau amb valors més baixos (però pot evitar caselles perilloses). |

### 4.5. La foca (opcional)

Si has activat la foca al setup, apareixerà al tauler com un personatge controlat per IA:

- Té el seu propi torn entre cada ronda de jugadors.
- Es mou aleatòriament i intenta apropar-se als pingüins.
- Si comparteix casella amb un pingüí, **li fa perdre la meitat de l'inventari**.
- Es pot "alimentar" amb peixos per fer-la dormir uns torns.

### 4.6. Combat de boles de neu

Quan dos o més jugadors coincideixen en la mateixa casella, es pot iniciar una guerra de boles de neu. El llançador escull l'objectiu i, si l'encerta, l'objectiu salta el següent torn.

---

## 5. Interfície

### 5.1. Menú principal

Quatre opcions:

- **New Game**: comença una partida nova.
- **Load Game**: carrega una partida guardada.
- **Stats**: rànquing global de jugadors.
- **Language**: desplegable per canviar d'idioma.

### 5.2. Pantalla "Player Setup"

- **Nombre de jugadors**: 1-4.
- **Habilitar foca**: checkbox per al mode amb antagonista IA.
- **Targetes de jugador**: una per slot, amb nom, contrasenya, color, avatar.
- **Select Existing Player**: per reutilitzar un jugador registrat.
- **Start Game / Back**: continuar o tornar al menú.

### 5.3. Pantalla del tauler

L'àrea principal és el tauler central de 50 caselles en patró serp. Al voltant hi ha:

#### Panell esquerre — Llista de tots els jugadors

- Retrat (sprite o avatar personalitzat).
- Nom amb el color de fons del jugador.
- Indicador de torn actiu.

#### Panell dret — HUD del jugador actiu

- **Nom** del jugador en torn.
- **Casella actual**.
- **Inventari**: comptadors de boles de neu, peixos, daus.
- **Estat de la foca** (si està activada): posició i torns bloquejats.

#### Barra d'accions inferior

- **🎲 Roll Dice**: tira el dau estàndard.
- **🎲 Fast Dice** / **🎲 Slow Dice**: tira un dau alternatiu (si en tens).
- **❄️ Throw Snowball**: llança una bola contra un altre jugador.
- **💾 Save Game**: guarda la partida amb un nom.
- **📜 History**: mostra el log d'esdeveniments.

### 5.4. Diàleg d'històric

Prement el botó **📜 History** apareix una finestra amb el llistat cronològic de tots els esdeveniments de la partida: tirades, moviments, efectes de casella, atacs, etc.

### 5.5. Pantalla "Stats"

Llista en format taula, ordenada de millor a pitjor:

- Rang amb medalla per al top-3.
- Nom del jugador.
- Color (mostra hexadecimal i quadrat acolorit).
- Partides jugades.
- Partides guanyades.

### 5.6. Drecera oculta: mode debug

> ⚠️ **Per a desenvolupadors**: durant la partida pots prémer `Ctrl + Shift + D` per obrir el panell de debug, que permet teletransportar pingüins arrossegant-los, forçar el resultat del proper dau i editar l'inventari de qualsevol jugador en viu.

---

## 6. Possibles errors i solucions

### 6.1. El joc no arrenca

| Símptoma | Causa | Solució |
|---|---|---|
| "Error: JavaFX runtime components missing" | JavaFX no està al classpath / module-path. | Afegir `--module-path javafx-lib/lib --add-modules javafx.controls,javafx.fxml,javafx.media` als arguments VM. |
| "UnsupportedClassVersionError" | Estàs utilitzant una versió de Java antiga. | Instal·la JDK 17 o superior. |
| "Resource not found: /assets/..." | La carpeta `src/assets/` no està marcada com a Source folder. | A Eclipse: Build Path → Configure Build Path → Source → Add Folder → `assets/`. |

### 6.2. Errors de connexió a la BBDD

| Símptoma | Causa | Solució |
|---|---|---|
| "Oracle JDBC driver not found" | Falta `ojdbc8.jar` o `ojdbc11.jar`. | Afegir el JAR al Build Path del projecte. |
| "Failed to connect to database" | Estàs configurat com a "centro" però treballes des de casa (o viceversa). | Editar `BBDD.java` línia 27: canviar `entorno` a `"fuera"` o `"centro"`. |
| "IO Error: Network adapter could not establish the connection" | Sense connexió a internet o el servidor està caigut. | Verifica la connexió i prova un altre cop. |
| "Invalid username/password" | Credencials canviades. | Demanar al professor les credencials actualitzades i actualitzar `BBDD.java`. |

### 6.3. Errors a la pantalla de setup

| Símptoma | Causa | Solució |
|---|---|---|
| "Wrong password" en intentar reutilitzar un nom | El nom ja existeix a la BBDD amb una altra contrasenya. | Introduir la contrasenya correcta, o canviar el nom del jugador. |
| El botó "Choose Avatar" no fa res | JavaFX no troba el FileChooser nadiu. | Reinstal·la JavaFX o utilitza el joc sense avatar personalitzat. |
| "No more empty slots available" | Intentes carregar més jugadors registrats que slots disponibles. | Augmenta el nombre de jugadors o esborra una targeta. |

### 6.4. Errors durant la partida

| Símptoma | Causa | Solució |
|---|---|---|
| El tauler es veu borrós | El sistema està aplicant un escalat HiDPI. | Redimensiona la finestra: el tauler es redibuixa amb interpolació pixel-perfect. |
| No es reprodueix la música | JavaFX Media no està disponible o el fitxer no es troba. | Afegir `javafx.media` als `--add-modules`. |
| "Save game failed" | Sense connexió a la BBDD en el moment del save. | Verifica la connexió i torna a intentar-ho. |
| Un jugador queda "encallat" | Probablement ha caigut a una cadena de forats / trineus. | És comportament esperat — el seu torn ha funcionat però el resultat ha estat advers. |

### 6.5. Errors carregant partides

| Símptoma | Causa | Solució |
|---|---|---|
| "No saved games found" | Encara no has guardat cap partida. | Comença una partida nova i guarda-la amb **Save Game**. |
| "Error loading the selected saved game" | El YAML encriptat està corrupte o la clau AES ha canviat. | Aquesta partida no es pot recuperar. Comença una nova. |
| "Wrong password" en carregar | Has introduït una contrasenya diferent de la que tenia el jugador. | Recupera la contrasenya original o utilitza el panell d'administració per resetejar-la. |

> ✅ **Consell**: guarda la partida sovint si fas torns llargs. El format de save inclou tot l'estat (tauler, inventaris, foca, historial), així que pots recuperar exactament on l'havies deixat.

> ⚠️ **Atenció**: les contrasenyes dels jugadors estan encriptades amb una clau interna fixa. Si oblides la contrasenya d'un jugador no hi ha manera oficial de recuperar-la des de la UI — només es pot canviar modificant manualment la fila a la BBDD.

---

## Suport i contacte

Si trobes un error que no apareix en aquesta llista o tens propostes de millora, contacta amb el grup de desenvolupament:

- Grup: **DW25-26 GR06**
- Repositori: `github.com/...../Joc-Del-Pingu`
