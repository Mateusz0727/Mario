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
                    javafx.application.Platform.runLater(this::showLoginDialog);
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

    private void showLoginDialog() {
        javafx.scene.control.Dialog<javafx.util.Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Logowanie");
        dialog.setHeaderText("Podaj login i hasło:");

        javafx.scene.control.ButtonType loginButtonType = new javafx.scene.control.ButtonType("Zaloguj", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType registerButtonType = new javafx.scene.control.ButtonType("Zarejestruj", javafx.scene.control.ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        javafx.scene.control.TextField username = new javafx.scene.control.TextField();
        username.setPromptText("Login");
        javafx.scene.control.PasswordField password = new javafx.scene.control.PasswordField();
        password.setPromptText("Hasło");

        grid.add(new javafx.scene.control.Label("Login:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new javafx.scene.control.Label("Hasło:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);
        
        javafx.scene.Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        javafx.scene.Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean disable = newValue.trim().isEmpty() || password.getText().trim().isEmpty();
            loginButton.setDisable(disable);
            registerButton.setDisable(disable);
        });
        password.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean disable = username.getText().trim().isEmpty() || newValue.trim().isEmpty();
            loginButton.setDisable(disable);
            registerButton.setDisable(disable);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new javafx.util.Pair<>("LOGIN:" + username.getText(), password.getText());
            } else if (dialogButton == registerButtonType) {
                return new javafx.util.Pair<>("REGISTER:" + username.getText(), password.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(usernamePassword -> {
            String userAction = usernamePassword.getKey();
            String pass = usernamePassword.getValue();
            String user = userAction.substring(userAction.indexOf(":") + 1);
            boolean isRegister = userAction.startsWith("REGISTER");

            String authResult = Game.authenticate(user, pass, isRegister);
            if ("SUCCESS".equals(authResult)) {
                if (isRegister) {
                    javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    info.setTitle("Rejestracja");
                    info.setHeaderText(null);
                    info.setContentText("Konto utworzone! Możesz się teraz zalogować.");
                    info.showAndWait();
                    showLoginDialog();
                } else {
                    Game.playerName = user;
                    Game.fetchPlayerStats();
                    
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
                            javafx.scene.control.TextInputDialog joinDialog = new javafx.scene.control.TextInputDialog();
                            joinDialog.setTitle("Dołącz do istniejącej gry");
                            joinDialog.setHeaderText("Wpisz 4-znakowy kod Lobby podany przez kolegę");
                            joinDialog.showAndWait().ifPresent(code -> {
                                Game.initOnlineClient(false, code.toUpperCase());
                            });
                        }
                    });
                }
            } else {
                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                errorAlert.setTitle("Błąd");
                errorAlert.setHeaderText("Operacja nie powiodła się");
                errorAlert.setContentText(authResult);
                errorAlert.showAndWait();
                showLoginDialog();
            }
        });
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
