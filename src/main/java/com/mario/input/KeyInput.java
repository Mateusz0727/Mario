package com.mario.input;

import com.mario.Game;
import com.mario.entity.Entity;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyInput {

    public void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();

        if (Game.state == Game.GameState.MENU) {
            if (key == KeyCode.UP || key == KeyCode.DOWN) {
                Game.menuIndex = (Game.menuIndex == 0) ? 1 : 0;
            } else if (key == KeyCode.ENTER) {
                if (Game.menuIndex == 0) { // Single Player
                    Game.currentLevel = 1;
                    Game.resetLevel();
                    Game.state = Game.GameState.PLAYING;
                } else if (Game.menuIndex == 1) { // Online Mode
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Tryb Multiplayer");
                        alert.setHeaderText("Wybierz opcję dołączenia");
                        javafx.scene.control.ButtonType buttonCreate = new javafx.scene.control.ButtonType("Stwórz Pokój");
                        javafx.scene.control.ButtonType buttonJoin = new javafx.scene.control.ButtonType("Dołącz z Kodem");
                        alert.getButtonTypes().setAll(buttonCreate, buttonJoin, javafx.scene.control.ButtonType.CANCEL);
                        
                        alert.showAndWait().ifPresent(type -> {
                            if (type == buttonCreate) {
                                Game.initOnlineClient(true, "");
                            } else if (type == buttonJoin) {
                                javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
                                dialog.setTitle("Dołącz do istniejącej gry");
                                dialog.setHeaderText("Wpisz 4-znakowy kod Lobby podany przez kolegę");
                                dialog.showAndWait().ifPresent(code -> {
                                    Game.initOnlineClient(false, code.toUpperCase());
                                });
                            }
                        });
                    });
                }
            }
            return;
        }

        for (Entity entity : Game.handler.entity) {
            if (entity.getId() == com.mario.Id.player) {
                switch (key) {
                    case LEFT:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            ((com.mario.entity.mob.Player) entity).leftPressed = true;
                        }
                        break;
                    case RIGHT:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            ((com.mario.entity.mob.Player) entity).rightPressed = true;
                        }
                        break;
                    case UP:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            com.mario.entity.mob.Player p = (com.mario.entity.mob.Player) entity;
                            p.upPressed = true;
                            if (!p.jumping && !p.falling) {
                                p.jumping = true;
                                p.gravity = 14.0;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        if (Game.state == Game.GameState.MENU) return;
        
        KeyCode key = e.getCode();
        for (Entity entity : Game.handler.entity) {
            if (entity.getId() == com.mario.Id.player) {
                switch (key) {
                    case LEFT:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            ((com.mario.entity.mob.Player) entity).leftPressed = false;
                        }
                        break;
                    case RIGHT:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            ((com.mario.entity.mob.Player) entity).rightPressed = false;
                        }
                        break;
                    case UP:
                        if (entity instanceof com.mario.entity.mob.Player) {
                            ((com.mario.entity.mob.Player) entity).upPressed = false;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
