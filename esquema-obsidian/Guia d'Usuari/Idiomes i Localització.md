# Idiomes i Localització

El joc està totalment localitzat. Pots canviar d'idioma des del [[Menú Principal]] amb el desplegable **🌍 Idioma**.

## Idiomes suportats

| Codi | Idioma | Etiqueta al menú |
|------|--------|------------------|
| `en` | Anglès (per defecte) | 🌍 English |
| `es` | Espanyol | 🌍 Español |
| `ca` | Català | 🌍 Català |
| `fr` | Francès | 🌍 Français |
| `pt` | Portuguès | 🌍 Português |
| `ro` | Romanès | 🌍 Română |
| `ar` | Àrab | 🌍 العربية |
| `ru` | Rus | 🌍 Русский |
| `uk` | Ucraïnès | 🌍 Українська |
| `ff` | Pulaar | 🌍 Pulaar |
| `jp` | Japonès (transliterat) | 🌍 Japanese |

> [!info] Per què Pulaar i Japonès en alfabet llatí?
> Les fonts *pixel-art* del joc (`pixel-game.regular.otf`) no contenen els glifs Adlam (U+1E900+) ni CJK, per això aquests idiomes es renderitzen amb caràcters llatins.

## Com canviar d'idioma

1. A la pantalla del [[Menú Principal]], fes clic al desplegable groc al final dels botons.
2. Tria un dels onze idiomes.
3. **Tots els textos** es refresquen a l'instant — botons, títol de finestra, etiquetes, diàlegs.

> [!tip] Persistència
> El canvi és per a la sessió actual; en obrir el joc una altra vegada torna a l'idioma per defecte (`en`).

## Fitxers de traducció

Les traduccions són **fitxers YAML** a `src/assets/lang/`:

```
ca.yml   en.yml   es.yml   fr.yml   pt.yml   ro.yml
ar.yml   ru.yml   uk.yml   ff.yml   jp.yml
```

Estructura (extracte de `ca.yml`):

```yaml
text.game.title: "El Joc del Pingüí"
menu.button.newgame: "Nova Partida"
menu.button.loadgame: "Carregar Partida"
menu.button.stats: "📊 Estadístiques"
alert.wrongpassword.title: "Contrasenya incorrecta"
alert.wrongpassword.message: "La contrasenya del jugador '%s' és incorrecta..."
```

Les claus s'identifiquen amb l'enum **`Lang`** ([[Paquet model.config]]) que mapa cada constant Java al seu key YAML:

```java
LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME);  // → "Nova Partida" (en català)
```

## Refresc dinàmic

Cada pantalla registra un *listener* a l'inicialització:

```java
LangConfig.addLanguageChangeListener(this::refreshTexts);
```

Quan algú canvia d'idioma, `LangConfig.loadLang(code)` notifica tots els *listeners* i cadascun crida el seu mètode `refreshTexts()` per redibuixar etiquetes. Veure [[Sistema d'Idiomes]] per a la implementació detallada.

## Què està traduït i què no

| Traduït | No traduït |
|---------|-----------|
| Botons del menú | Missatges del log de joc (anglès) |
| Títol de la finestra | Logs de la consola |
| Camps de configuració | Noms dels jugadors |
| Diàlegs d'alerta | — |
| Pantalla d'estadístiques | — |

> [!warning] Log de joc en anglès
> Els missatges com *"Pingu rolled 4 (Normal die)"* o *"Bear attacked!"* només existeixen en anglès al codi (formatActionMessage de `GameBoardController`).

## Enllaços relacionats

- [[Sistema d'Idiomes]] — detalls tècnics
- [[Paquet model.config]] — `Lang` i `LangConfig`
