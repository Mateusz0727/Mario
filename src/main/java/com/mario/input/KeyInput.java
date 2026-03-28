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
                        entity.setVelX(-3);
                        break;
                    case RIGHT:
                        entity.setVelX(3);
                        break;
                    case UP:
                        if (!entity.jumping && !entity.falling) {
                            entity.jumping = true;
                            entity.gravity = 14.0;
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
                        entity.setVelX(0);
                        break;
                    case RIGHT:
                        entity.setVelX(0);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
