const fs = require('fs');
const path = require('path');

const src = 'd:\\Usuarios\\martavoytk\\Joc-Del-Pingu\\eclipse-workspace\\JocDelPingu\\src';

function walk(dir, done) {
  let results = [];
  fs.readdir(dir, function(err, list) {
    if (err) return done(err);
    let i = 0;
    (function next() {
      let file = list[i++];
      if (!file) return done(null, results);
      file = path.resolve(dir, file);
      fs.stat(file, function(err, stat) {
        if (stat && stat.isDirectory()) {
          walk(file, function(err, res) {
            results = results.concat(res);
            next();
          });
        } else {
          results.push(file);
          next();
        }
      });
    })();
  });
}

const replacements = [
  { old: 'package board;', new: 'package model.board;' },
  { old: 'package board.squares;', new: 'package model.board.squares;' },
  { old: 'package config;', new: 'package model.config;' },
  { old: 'package ConnectionDDBB;', new: 'package model.db;' },
  { old: 'package entity;', new: 'package model.entity;' },
  { old: 'package main;', new: 'package model.game;' }, // Will be overridden for controllers
  { old: 'package ObjectManagers;', new: 'package model.item;' },
  { old: 'package ObjectManagers.objects;', new: 'package model.item.objects;' },
  { old: 'package gamePanel;', new: 'package controller.ui;' },
  { old: 'package com.fontgenerator;', new: 'package view.font.generator;' },
  { old: 'package CustomBitmapFont;', new: 'package view.font;' },
  
  { old: 'import board.', new: 'import model.board.' },
  { old: 'import config.', new: 'import model.config.' },
  { old: 'import ConnectionDDBB.BBDDPanel;', new: 'import view.ui.BBDDPanel;' },
  { old: 'import ConnectionDDBB.', new: 'import model.db.' },
  { old: 'import entity.', new: 'import model.entity.' },
  { old: 'import main.GameManager;', new: 'import model.game.GameManager;' },
  { old: 'import main.SaveLoadService;', new: 'import model.game.SaveLoadService;' },
  { old: 'import ObjectManagers.', new: 'import model.item.' },
  { old: 'import com.fontgenerator.', new: 'import view.font.generator.' },
  { old: 'import CustomBitmapFont.', new: 'import view.font.' },
  { old: 'import gamePanel.GameSetupConfig;', new: 'import model.config.GameSetupConfig;' },
  { old: 'import gamePanel.MainMenu;', new: 'import controller.main.MainMenu;' },
  { old: 'import gamePanel.', new: 'import controller.ui.' },
  
  { old: 'ObjectManagers.Object', new: 'model.item.GameObject' },
  { old: 'import model.item.Object;', new: 'import model.item.GameObject;' },
  { old: 'List<Object>', new: 'List<GameObject>' },
  { old: 'ArrayList<Object>', new: 'ArrayList<GameObject>' },
  { old: 'public Object get', new: 'public GameObject get' },
  { old: 'public void addObject(Object ', new: 'public void addObject(GameObject ' },
  { old: 'public void removeObject(Object ', new: 'public void removeObject(GameObject ' },
  { old: 'for (Object ', new: 'for (GameObject ' },
  
  { old: 'fx:controller="gamePanel.', new: 'fx:controller="controller.ui.' },
  { old: 'gameBoardStyle.css', new: '/view/css/gameBoardStyle.css' },
  { old: 'style.css', new: '/view/css/style.css' },
  { old: 'getResource("mainMenu.fxml")', new: 'getResource("/view/fxml/mainMenu.fxml")' },
  { old: 'getResource("gameBoard.fxml")', new: 'getResource("/view/fxml/gameBoard.fxml")' },
  { old: 'getResource("playerSetup.fxml")', new: 'getResource("/view/fxml/playerSetup.fxml")' }
];

walk(src, (err, files) => {
  if (err) throw err;
  
  files.forEach(file => {
    if (file.endsWith('.java') || file.endsWith('.fxml') || file.endsWith('.css') || file.endsWith('.xml') || file.endsWith('.yml')) {
      let content = fs.readFileSync(file, 'utf8');
      let originalContent = content;
      
      replacements.forEach(r => {
        content = content.split(r.old).join(r.new);
      });
      
      // File-specific overrides!
      if (file.endsWith('GameSetupConfig.java')) {
        content = content.replace('package controller.ui;', 'package model.config;');
      }
      if (file.endsWith('MainMenu.java')) {
        content = content.replace('package controller.ui;', 'package controller.main;');
      }
      if (file.endsWith('MainConsole.java')) {
        content = content.replace('package model.game;', 'package controller.main;');
        content = content.replace('public class Main ', 'public class MainConsole ');
      }
      if (file.endsWith('BBDDPanel.java')) {
        content = content.replace('package model.db;', 'package view.ui;');
      }
      
      if (content !== originalContent) {
        fs.writeFileSync(file, content, 'utf8');
        console.log('Updated: ' + file);
      }
    }
  });
});
