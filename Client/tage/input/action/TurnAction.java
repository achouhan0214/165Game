package tage.input.action;
import myGame.*;
import net.java.games.input.Event;
import tage.*;
import tage.input.action.AbstractInputAction;
import org.joml.*;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** <code>TurnAction</code> extends AbstractInputAction, it gets the game
 * and performs the action of turning left or right.
 *
 * @author Anish Chouhan
 *
 * @see IAction
 * @see AbstractInputAction
 * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
 * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
 *
 */

public class TurnAction extends AbstractInputAction{
        private GameObject dol;
        private Matrix4f oldRotation, rotAroundDolUp, newRotation;
        private Vector4f oldUp;
        private MyGame game;
        /** Gets the Game to apply the action to
         */
        public TurnAction(MyGame game)
        {
                this.game = game;
        }

        /** See if the input is A for turning left or
         * D for turning right,if not looks for a gamepad
         * and does the same with the given input.
         */
        @Override
        public void performAction(float time, Event e)
        {
                String event = e.getComponent().toString();
                if(event.equals("A"))
                {
                        dol = game.getDol();
                        dol.yaw(-.025f);
                }else{
                        if(event.equals("D"))
                        {
                                dol = game.getDol();
                                dol.yaw(.025f);
                        }else{
                                float keyValue = e.getValue();
                                if (keyValue > -.2 && keyValue < .2) return; // deadzone
                                dol = game.getDol();
                                dol.yaw((keyValue)*.025f);
                        }
                }
        }
}
