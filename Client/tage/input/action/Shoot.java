package tage.input.action;

import myGame.MyGame;
import myGame.ProtocolClient;
import net.java.games.input.Event;

public class Shoot extends AbstractInputAction{

        private MyGame game;

        private ProtocolClient protClient;


        public Shoot(MyGame game, ProtocolClient p) {
                this.game = game;
                this.protClient = p;
        }


        @Override
        public void performAction(float time, Event e)
        {
                game.shoot();
        }
}
