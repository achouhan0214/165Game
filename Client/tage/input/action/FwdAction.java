package tage.input.action;

import myGame.*;
import net.java.games.input.Event;
import tage.*;
import tage.input.action.AbstractInputAction;
import org.joml.*;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** <code>FwdAction</code> extends AbstractInputAction, it gets the game
 * and performs the action of moving forward or backward.
 *
 * @author Anish Chouhan
 *
 * @see IAction
 * @see AbstractInputAction
 * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
 * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
 *
 */
public class FwdAction extends AbstractInputAction{
        private GameObject dol;
        private Vector3f oldPosition, newPosition;
        private Vector4f fwdDirection;
        private MyGame game;

        /** Gets the Game to apply the action to
         */
        public FwdAction(MyGame game)
        {
                this.game = game;
        }

        /** See if the input is W for forward or
         * S for backward,if not looks for a gamepad
         * and does the same with the given input.
         */
        @Override
        public void performAction(float time, Event e)
        {
                String event = e.getComponent().toString();
                if(event.equals("W"))
                {
                        dol = game.getDol();
                        oldPosition = dol.getWorldLocation();
                        fwdDirection = new Vector4f(0f,0f,1f,1f);
                        fwdDirection.mul(dol.getWorldRotation());
                        fwdDirection.mul(0.025f);
                        newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                        dol.setLocalLocation(newPosition);
                }else{
                        if(event.equals("S"))
                        {
                                dol = game.getDol();
                                oldPosition = dol.getWorldLocation();
                                fwdDirection = new Vector4f(0f,0f,1f,1f);
                                fwdDirection.mul(dol.getWorldRotation());
                                fwdDirection.mul(-0.025f);
                                newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                                dol.setLocalLocation(newPosition);
                        }else{
                                float keyValue = e.getValue();
                                dol = game.getDol();
                                oldPosition = dol.getWorldLocation();
                                fwdDirection = new Vector4f(0f,0f,1f,1f);
                                fwdDirection.mul(dol.getWorldRotation());
                                fwdDirection.mul((keyValue)*-0.025f);
                                newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                                dol.setLocalLocation(newPosition);
                        }
                }
        }
}
