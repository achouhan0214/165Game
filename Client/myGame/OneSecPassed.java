package myGame;

import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
        NPC npc;


        OneSecPassed(NPC n, boolean toNegate)
        {
                super(toNegate);
                npc = n;

        }

        @Override
        protected boolean check() {
                return false;
        }
}
