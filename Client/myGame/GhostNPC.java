package myGame;
import org.joml.*;
import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;


public class GhostNPC extends GameObject {
        private int id;
        public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p)
        { super(GameObject.root(), s, t);
                this.id = id;
                setPosition(p);
        }
        public void setSize(boolean big)
        {
                if (!big) {
                        this.setLocalScale((new Matrix4f()).scaling(0.5f));
                }
                else {
                        this.setLocalScale((new Matrix4f()).scaling(1.0f));
                }
        }

        public void setPosition(Vector3f position)
        {
                setLocalLocation(position);
        }
}
