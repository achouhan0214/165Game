package myGame;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;

import java.util.Random;
import org.joml.*;

public class NPCController {
        private NPC[] npc;
        Random rn = new Random();

        boolean nearFlag = false;
        long thinkStartTime, tickStartTime;
        long lastThinkUpdateTime, lastTickUpdateTime;
        double criteria = 2.0;

        private GameObject terr;

        public void updateNPCs(GameObject ava) {
                for(int i = 0; i < npc.length; i++)
                {
                        if(npc[i] != null)
                        {
                                npc[i].updateLocation(ava);
                        }
                }
        }

        public void start(int numberOfNPC, GameObject p, ObjShape s, TextureImage t, GameObject terr) {
                thinkStartTime = System.nanoTime();
                tickStartTime = System.nanoTime();
                lastThinkUpdateTime = thinkStartTime;
                lastTickUpdateTime = tickStartTime;
                npc = new NPC[numberOfNPC];
                this.terr = terr;
                setupNPCs(p, s, t);
        }

        public void updateNPCsSpeed() {
                for(int i = 0; i < npc.length; i++)
                {
                        if(rn.nextInt(3) == 1)
                        {
                                if(npc[i] != null)
                                {
                                        npc[i].speedUp();
                                }
                        }
                }
        }


        public void setupNPCs(GameObject p, ObjShape s, TextureImage t) {
                for (int i = 0; i < npc.length; i++)
                {
                        if(npc[i] == null)
                        {
                                npc[i] = new NPC(p, s, t);
                                npc[i].startNPC(terr);
                                break;
                        }
                }

        }

        public void npcLoop(GameObject ava, GameObject p, ObjShape s, TextureImage t) {
                long currentTime = System.nanoTime();
                float elapsedThinkMilliSecs =
                        (currentTime - lastThinkUpdateTime) / (1000000.0f);
                float elapsedTickMilliSecs =
                        (currentTime - lastTickUpdateTime) / (1000000.0f);
                if (elapsedTickMilliSecs >= 25.0f) {
                        lastTickUpdateTime = currentTime;
                        updateNPCs(ava);
                }
                if (elapsedThinkMilliSecs >= 25000.0f) {
                        updateNPCsSpeed();
                }
                if (elapsedThinkMilliSecs >= 5000.0f) {
                        setupNPCs(p, s, t);
                }

        }

        public int gotHit(Vector3f bullet, float off)
        {
                for(int i = 0; i < npc.length; i++)
                {
                        if(npc[i] != null && npc[i].getLocalLocation().equals(bullet, off))
                        {
                                return i;
                        }
                }
                return -1;
        }

        public boolean isEmpty()
        {
                for(int i = 0; i < npc.length; i++)
                {
                        if(npc[i] != null)
                        {
                                return false;
                        }
                }
                return true;
        }

        public NPC getNPC(int i)
        {
                return npc[i];
        }

        public void setNPC(int i, NPC npc)
        {
                this.npc[i] = npc;
        }
}