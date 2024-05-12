package tage.input.action;

import myGame.MyGame;
import myGame.ProtocolClient;
import net.java.games.input.Event;
import tage.GameObject;

public class ThrowGernade extends AbstractInputAction{

        private MyGame game;

        private ProtocolClient protClient;


        public ThrowGernade(MyGame game, ProtocolClient p) {
                this.game = game;
                this.protClient = p;
        }


        @Override
        public void performAction(float time, Event e)
        {
                game.throwGerande();
        }
}
