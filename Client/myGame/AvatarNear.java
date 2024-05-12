package myGame;

import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition{
        NPC npc;
        public AvatarNear(NPC n, boolean toNegate)
        {
                super(toNegate);
                npc = n;
        }
        protected boolean check()
        {
                return true;
        }
}
