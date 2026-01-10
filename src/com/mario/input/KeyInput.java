package com.mario.input;

import com.mario.Game;
import com.mario.entity.Entity;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyInput implements KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {
      //  int key = e.getKeyCode();


    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        for (Entity entity: Game.handler.entity)
        {
            switch (key) {
                case KeyEvent.VK_LEFT:
                    entity.setVelX(-3);
                    break;
                case KeyEvent.VK_RIGHT:
                    entity.setVelX(3);
                    break;
                case KeyEvent.VK_UP:
                    if(!entity.jumping) {
                        entity.jumping = true;
                        entity.gravity=10.0;
                    }
                    break;
//                case KeyEvent.VK_DOWN:
//                    entity.setVelY(1);
//                    break;

            }
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        for (Entity entity: Game.handler.entity)
        {
            switch (key) {
                case KeyEvent.VK_LEFT:
                    entity.setVelX(0);
                    break;
                case KeyEvent.VK_RIGHT:
                    entity.setVelX(0);
                    break;
                case KeyEvent.VK_UP:
                    entity.setVelY(0);
                    break;
                case KeyEvent.VK_DOWN:
                    entity.setVelY(0);
                    break;

            }
        }
    }
}
