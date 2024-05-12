package myGame;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;
import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;

import org.joml.*;


import java.util.Random;

public class NPC extends GameObject {
        BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

        GameObject terr;

        float speed;
        Random rn = new Random();
        long thinkStartTime, tickStartTime;
        long lastThinkUpdateTime, lastTickUpdateTime;
        public NPC(GameObject p, ObjShape s, TextureImage t)
        {
                super(p, s, t);

        }

        public void startNPC(GameObject terr)
        {
                this.terr = terr;
                float x = 5-rn.nextFloat(10);
                float z = 5-rn.nextFloat(10);
                float height = 0;
                Matrix4f initialTranslation = (new Matrix4f()).translation(x, height+ 0.75f, z);
                this.setLocalTranslation(initialTranslation);
                this.getRenderStates().setModelOrientationCorrection(
                        (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
                Matrix4f initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
                this.setLocalRotation(initialRotation);
                Matrix4f initialScale = (new Matrix4f()).scaling(0.2f);
                this.setLocalScale(initialScale);
                thinkStartTime = System.nanoTime();
                tickStartTime = System.nanoTime();
                lastThinkUpdateTime = thinkStartTime;
                lastTickUpdateTime = tickStartTime;
                speed = 0.02f;
        }

        public void updateLocation(GameObject ava)
        {
                Vector3f location = this.getLocalLocation();
                float height = terr.getHeight(location.x(), location.z());
                this.lookAt(ava);
                Vector3f facing = this.getLocalForwardVector();
                float x = (float) location.x()+(float) (facing.x()*speed);
                float z = (float) location.z()+(float) (facing.z()*speed);
                Vector3f newLoc = new Vector3f(x,height+0.75f,z);
                location.add(0.2f, 0f, 0.1f);
                this.setLocalLocation(newLoc);

        }

        public void speedUp()
        {
                speed += 0.002f;
        }

}
