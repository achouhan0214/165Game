package tage.input.action;

import myGame.MyGame;
import net.java.games.input.Event;
import tage.*;
import tage.input.action.AbstractInputAction;
import org.joml.*;
import org.joml.Vector3f;
import org.joml.Vector4f;
/** <code>Jump</code> extends AbstractInputAction, it gets the game
 * and performs the action of jumping with the dol object.
 *
 * @author Anish Chouhan
 *
 * @see IAction
 * @see AbstractInputAction
 * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
 * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
 *
 */
public class Jump extends AbstractInputAction{

        private GameObject dol;
        private Vector3f oldPosition, newPosition;
        private Vector4f fwdDirection;
        private MyGame game;
        private boolean jump;
        private float cycleTime = 2000.0f;
        private float totalTime = 0.0f;

        /** Gets the Game to apply the action to the dol GameObject
         */
        public Jump(MyGame game, float jumpCycle) {
                this.game = game;
                cycleTime = jumpCycle*100000;
                jump = false;
                dol = game.getDol();
        }

        /** does the action of jumping.
         */
        @Override
        public void performAction(float time, Event e)
        {
                if(!jump)
                {
                        jump = true;
                }
        }
         /** Updates the height location when jumping and falling
         */
        public void jumpUpdate(float time)
        {
                float elapsedTime = time;
                totalTime += elapsedTime/100000000.0f;
                dol = game.getDol();
                oldPosition = dol.getLocalLocation();
                if(totalTime < cycleTime)
                {
                        if (jump == true) {
                                newPosition = oldPosition.add(0, 0.2f,0);
                                dol.setLocalLocation(newPosition);
                        }
                }
                if (oldPosition.y() > 0.25f)
                {
                        newPosition = oldPosition.add(0, -0.075f,0);
                        dol.setLocalLocation(newPosition);
                }
                else {
                        jump = false;
                        totalTime = 0f;
                }

        }

}
