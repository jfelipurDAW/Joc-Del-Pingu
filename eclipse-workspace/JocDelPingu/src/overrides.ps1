 = Get-Content -Path model\config\GameSetupConfig.java -Raw
 =  -replace "package gamePanel;", "package model.config;"
Set-Content -Path model\config\GameSetupConfig.java -Value 

 = Get-Content -Path controller\main\MainMenu.java -Raw
 =  -replace "package gamePanel;", "package controller.main;"
 =  -replace "getResource\("mainMenu.fxml"\)", "getResource("/view/fxml/mainMenu.fxml")"
Set-Content -Path controller\main\MainMenu.java -Value 

 = Get-Content -Path controller\main\MainConsole.java -Raw
 =  -replace "package main;", "package controller.main;"
 =  -replace "public class Main\b", "public class MainConsole"
Set-Content -Path controller\main\MainConsole.java -Value 

 = Get-Content -Path view\ui\BBDDPanel.java -Raw
 =  -replace "package ConnectionDDBB;", "package view.ui;"
 =  -replace "import ConnectionDDBB.BBDD;", "import model.db.BBDD;"
Set-Content -Path view\ui\BBDDPanel.java -Value 

