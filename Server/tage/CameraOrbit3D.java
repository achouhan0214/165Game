package tage;

import tage.Camera;
import tage.GameObject;
import org.joml.Vector3f;
import tage.input.InputManager;
import tage.input.IInputManager;
import tage.*;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;
import org.joml.*;
import java.lang.Math;

import java.util.Vector;

/**
* Creates a camera that orbits the dol Object. 
* Finds the location and the direction it is facing with the action.
* Must call the updateCameraPosition for it to work.
* @author Anish Chouhan
*/

public class CameraOrbit3D {

        private GameObject dol;
        private Engine engine;
        private Camera cam;
        private float cameraAzimuth;
        private float cameraElevation;
        private float cameraRadius;
        
        /**
        * Creates a camera that orbits the dol Object. 
        * Uses the gpName to setupInputs for game pad.
        */
        public CameraOrbit3D(Camera cam, GameObject dol, String gpName, Engine e)
        {
                this.engine = e;
                this.cam = cam;
                this.dol = dol;
                cameraAzimuth = 0.0f;
                cameraElevation = 20.0f;
                cameraRadius = 2.0f;
                setupInput(gpName);
                updateCameraPosition();

        }
        
        /**
        * Uses gpName to setup Gamepad.
        * If gamepad is not provided setsup keybord controlls
        */
        private void setupInput(String gpName)
        {
                OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
                OrbitElevationAction elevationAction = new OrbitElevationAction();
                OrbitRadiusAction radiusAction = new OrbitRadiusAction();
                InputManager im = engine.getInputManager();



                if(gpName != null)
                {
                        im.associateAction(gpName,
                                net.java.games.input.Component.Identifier.Axis.RX, azmAction,
                                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                        im.associateAction(gpName,
                                net.java.games.input.Component.Identifier.Axis.RY, elevationAction,
                                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                        im.associateAction(gpName,
                                net.java.games.input.Component.Identifier.Axis.Z, radiusAction,
                                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                }

                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.LEFT,
                        azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.RIGHT,
                        azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.UP,
                        elevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.DOWN,
                        elevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.LBRACKET,
                        radiusAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllKeyboards(
                        net.java.games.input.Component.Identifier.Key.RBRACKET,
                        radiusAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);


        }
        /**
         * Updares the position of the camera
        */
        public void updateCameraPosition()
        {
                Vector3f avatarRot = dol.getWorldForwardVector();
                double avatarAngle = Math.toDegrees((double) avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
                float totalAz = cameraAzimuth-(float)avatarAngle;
                double theta = Math.toRadians(totalAz);
                double phi = Math.toRadians(cameraElevation);
                float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
                float y = cameraRadius * (float)(Math.sin(phi));
                float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
                cam.setLocation(new
                        Vector3f(x,y,z).add(dol.getWorldLocation()));
                cam.lookAt(dol);
        }

        /** <code>OrbitAzimuthAction</code> extends AbstractInputAction, it gets the game
         * and performs the action of finding the orbiting camera's Azimuth.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class OrbitAzimuthAction extends AbstractInputAction
        {
        
               /** See if the input is Left for rotatin the camera left or
               * Right for rotating the camera right,if not looks for a gamepad
               * and does the same with the given input.
               */
                public void performAction(float time, Event event) {
                        float rotAmount;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("Left"))
                        {
                                rotAmount = -0.2f;
                        }
                        else {
                                if(eventComp.equals("Right"))
                                {
                                        rotAmount = 0.2f;
                                }
                                else {
                                        if (event.getValue() < -0.2) {
                                                rotAmount = -0.2f;
                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        rotAmount = 0.2f;
                                                } else {
                                                        rotAmount = 0.0f;
                                                }
                                        }
                                }

                        }

                        cameraAzimuth += rotAmount;
                        cameraAzimuth = cameraAzimuth % 360;
                        updateCameraPosition();
                }
        }


         /** <code>OrbitElevationAction</code> extends AbstractInputAction, it gets the game
         * and performs the action of finding the orbiting camera's Elevation.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class OrbitElevationAction extends AbstractInputAction
        {
               /** See if the input is down for rotatin the camera down or
               * up for rotating the camera up,if not looks for a gamepad
               * and does the same with the given input.
               * Also checks to see the the elevation is below floor, 
               * If it is then it dosen't change.
               */
                public void performAction(float time, Event event) {
                        float rotAmount;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("Down"))
                        {
                                rotAmount = -0.2f;
                        }
                        else {
                                if (eventComp.equals("Up")) {
                                        rotAmount = 0.2f;
                                } else {
                                        if (event.getValue() < -0.2) {
                                                rotAmount = -0.2f;
                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        rotAmount = 0.2f;
                                                } else {
                                                        rotAmount = 0.0f;
                                                }
                                        }
                                }
                        }
                        if(0.03f< cameraRadius * (float)(Math.sin(Math.toRadians(cameraElevation+ rotAmount))))
                        {
                                cameraElevation += rotAmount;
                                cameraElevation = cameraElevation % 360;
                        }
                        updateCameraPosition();
                }
        }

         /** <code>OrbitRadiusAction</code> extends AbstractInputAction, it gets the game
         * and performs the action of finding the orbiting camera's radius.
         *
         * @author Anish Chouhan
         *
         * @see IAction
         * @see AbstractInputAction
         * @see InputManager#associateAction(net.java.games.input.Component.Identifier,
         * 					IAction, tage.input.IInputManager.INPUT_ACTION_TYPE)
         *
         */
        private class OrbitRadiusAction extends AbstractInputAction
        {
        
               /** See if the input is "[" for zooming in or
               * "]" for zooming out,if not looks for a gamepad
               * and does the same with the given input.
               * Also checks to see the radous is less then 0.1f if yes dosn't change
               */
                public void performAction(float time, Event event) {
                        float radius;
                        String eventComp = event.getComponent().toString();
                        if(eventComp.equals("["))
                        {
                                radius = -0.02f;
                        }
                        else {
                                if (eventComp.equals("]")) {
                                        radius = 0.02f;
                                } else {
                                        if (event.getValue() < -0.2) {
                                                radius = -0.02f;
                                        } else {
                                                if (event.getValue() > 0.2) {
                                                        radius = 0.02f;
                                                } else {
                                                        radius = 0.0f;
                                                }
                                        }
                                }
                        }
                        if(0.1f < (cameraRadius + radius) && 20 > (cameraRadius + radius))
                        {
                                cameraRadius += radius;
                        }
                        updateCameraPosition();
                }
        }

}
