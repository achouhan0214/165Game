package tage;
import tage.*;
import org.joml.*;

import org.joml.Vector3f;
import tage.input.InputManager;
import tage.input.IInputManager;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

/**
* Creates a control for a camera to pan and zoom in and out. 
* @author Anish Chouhan
*/
public class ViewportCamera {

        private Engine engine;
        private Camera cam;

        Vector3f camLocation;
        /**
        * Creates a camera that can be panned and zoomed in and out.
        * Uses the gpName to setupInputs for gamepad.
        */
        public ViewportCamera(Camera cam, String gpName, Engine e)
        {
                engine = e;
                this.cam = cam;
                camLocation = cam.getLocation();
                this.setupInput(gpName);
        }
        
        /**
        * Uses gpName to setup Gamepad.
        * If gamepad is not provided setsup keybord controlls
        */
        private void setupInput(String gpName)
        {
                ViewportXPan xPan = new ViewportXPan();
                ViewportZPan zPan = new ViewportZPan();
                ViewportZoom zoom = new ViewportZoom();
                InputManager im = engine.getInputManager();
                

                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.I,
                        zPan, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.K,
                        zPan, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.J,
                        xPan, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.L,
                        xPan, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.U,
                        zoom, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.O,
                        zoom, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }

        /**
        * Updates the camera's location
        */
        public void updateViewportCamera()
        {
                cam.setLocation(camLocation);
        }


         /** <code>ViewportXPan</code> extends AbstractInputAction, it lets the user pan in the x-axes.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class ViewportXPan extends AbstractInputAction
        {
        
               /** See if the input is "J" for paning the negative x-axes or
               * "L" in the positive x-axes,if not looks for a gamepad
               * and does the same with the given input.
               */
                public void performAction(float time, Event event) {
                        float x;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("J"))
                        {
                                x = -0.2f;
                        }
                        else {
                                if(eventComp.equals("L"))
                                {
                                        x = 0.2f;
                                }
                                else{
                                        if (event.getValue() < -0.2) {
                                                x = -0.2f;
                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        x = 0.2f;
                                                } else {
                                                        x = 0.0f;
                                                }
                                        }
                                }
                        }
                        camLocation.add(x, 0, 0);
                }
        }
        
        /** <code>ViewportZPan</code> extends AbstractInputAction, it lets the user pan in the z-axes.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class ViewportZPan extends AbstractInputAction
        {
        
               /** See if the input is "I" for paning the negative z-axes or
               * "K" in the positive z-axes,if not looks for a gamepad
               * and does the same with the given input.
               */
                public void performAction(float time, Event event) {
                        float z;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("I"))
                        {
                                z = -0.2f;
                        }
                        else {
                                if(eventComp.equals("K"))
                                {
                                        z = 0.2f;
                                }else {
                                        if (event.getValue() < -0.2) {
                                                z = -0.2f;

                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        z = 0.2f;
                                                } else {
                                                        z = 0.0f;
                                                }
                                        }
                                }
                        }
                        camLocation.add(0, 0, z);
                }
        }
        
        /** <code>ViewportZoom</code> extends AbstractInputAction, it lets the user pan in the y-axes.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class ViewportZoom extends AbstractInputAction
        {
        
               /** See if the input is "O" for paning the negative y-axes or
               * "U" in the positive y-axes,if not looks for a gamepad
               * and does the same with the given input.
               */
                public void performAction(float time, Event event) {
                        float y;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("O"))
                        {
                                y = -0.2f;
                        }
                        else {
                                if(eventComp.equals("U"))
                                {
                                        y = 0.2f;
                                }
                                else{
                                        if (event.getValue() < -0.2) {
                                                y = -0.2f;
                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        y = 0.2f;
                                                } else {
                                                        y = 0.0f;
                                                }
                                        }
                                }
                        }
                        if(0 < (camLocation.y()+y)){
                                camLocation.add(0, y, 0);
                        }
                }
        }


}
