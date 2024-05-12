package tage.input.action;
import myGame.*;
import net.java.games.input.Event;
import tage.*;
import tage.input.InputManager;
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
        private Vector3f oldPosition, newPosition;
        private Vector4f fwdDirection;
        private MyGame game;
        private ProtocolClient protClient;
        /** Gets the Game to apply the action to
         */
        public TurnAction(MyGame game, ProtocolClient p)
        {
                this.game = game;
                protClient = p;
        }

        /** See if the input is A for turning left or
         * D for turning right,if not looks for a gamepad
         * and does the same with the given input.
         */
        @Override
        public void performAction(float time, Event e)
        {
                String event = e.getComponent().toString();
                float angle = 0;
                if(event.equals("A") )
                {       
                        
                        dol = game.getDol();
                        angle = -0.025f;
                        dol.yaw(angle);
                        
                       /*  dol = game.getDol();
                        oldPosition = dol.getWorldLocation();
                        fwdDirection = new Vector4f(1f,0f,0f,1f);
                        fwdDirection.mul(dol.getWorldRotation());
                        fwdDirection.mul(0.025f);
                        newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                        dol.setLocalLocation(newPosition);*/
                }else{
                        if(event.equals("D"))
                        {       
                               
                                dol = game.getDol();
                                angle = 0.025f;
                                dol.yaw(angle);
                                
                                /* 
                                dol = game.getDol();
                        oldPosition = dol.getWorldLocation();
                        fwdDirection = new Vector4f(1f,0f,0f,1f);
                        fwdDirection.mul(dol.getWorldRotation());
                        fwdDirection.mul(-0.025f);
                        newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                        dol.setLocalLocation(newPosition); */
                        }else{
                                float keyValue = e.getValue();
                                if (keyValue > -.2 && keyValue < .2) return; // deadzone
                                dol = game.getDol();
                                angle = (keyValue)*.025f;
                                dol.yaw(angle);
                        }
                }
                if (protClient != null)
                {
                        //getLocalForwardVector()
                        //getWorldForwardVector()
                      
                        Matrix4f rotation = dol.getLocalRotation();
                        protClient.sendRotationMessage(rotation);
                        
                }
        }
}
