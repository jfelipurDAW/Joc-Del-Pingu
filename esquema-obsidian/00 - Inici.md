# 00 - Inici · El Joc del Pingüí

> [!info] Benvinguda
> Aquest vault d'Obsidian documenta el projecte **Joc del Pingu** (Joc del Pingüí), un joc de taula tipus *snake-board* desenvolupat en **Java + JavaFX** com a pràctica del curs de Desenvolupament d'Aplicacions Multiplataforma.

## Què és aquest joc?

*El Joc del Pingüí* és un joc de **2-4 jugadors per torns** ambientat a l'Àrtic. Els jugadors mouen pingüins al llarg d'un tauler de 50 caselles en forma de serp tirant daus, i han de superar perills (forats al gel, ós polar, terra trencat...) per arribar primers al final. Una **foca CPU** opcional fa de rival comú que pot fer perdre la partida a tothom si arriba al final abans.

## Per on començar

Aquesta documentació té dues guies independents segons el teu perfil:

| Si ets... | Llegeix... | Conté |
|-----------|-----------|-------|
| Jugador / usuari final | [[Visió General]] | Com instal·lar i jugar |
| Desenvolupador / professor | [[Arquitectura General]] | Com està fet per dins |

## Mapa de la documentació

### Guia d'Usuari

- [[Visió General]] — què és el joc i com executar-lo
- [[Menú Principal]] — la pantalla d'inici
- [[Configuració de Partida]] — preparar una partida nova
- [[Tauler de Joc]] — la pantalla principal de joc
- [[Caselles Especials]] — què fa cada tipus de casella
- [[Inventari i Objectes]] — boles de neu, peixos i daus
- [[Guardar i Carregar]] — partides desades i autenticació
- [[Mode Debug]] — drecera per a desenvolupadors
- [[Idiomes i Localització]] — com canviar d'idioma

### Guia Tècnica

- [[Arquitectura General]] — visió MVC global
- [[Estructura de Paquets]] — què conté cada paquet
- [[Paquet model.board]] · [[Paquet model.entity]] · [[Paquet model.item]]
- [[Paquet model.game]] · [[Paquet model.config]] · [[Paquet model.db]]
- [[Paquet controller]] · [[Paquet view]]
- [[Sistema de Sprites]] · [[Sistema de So]] · [[Sistema d'Idiomes]] · [[Sistema de Persistència]]
- [[Diagrama de Classes]] — UML general
- [[Flux de Joc]] — diagrames de seqüència
- [[Mode Debug Tècnic]] — implementació de Ctrl+Shift+D

## Crèdits

> [!example] Tecnologia
> - **Java 21** + **JavaFX 21** (FXML + CSS)
> - **SnakeYAML** per a localització i save-games
> - **Oracle JDBC** per a la persistència (BBDD del curs)
> - **AES-128** per encriptar contrasenyes i partides desades

Per als detalls tècnics complets consulta [[Arquitectura General]].
