package myGame;

import tage.ai.behaviortrees.BTCondition;

public class GetSmall extends BTCondition {
        NPC npc;
        GetSmall(NPC npc)
        {
                super(false);
                npc = npc;
        }

        @Override
        protected boolean check() {
                return false;
        }
}
