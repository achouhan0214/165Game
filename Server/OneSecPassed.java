import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
        NPC npc;
        NPCcontroller npcc;

        OneSecPassed(NPCcontroller c, NPC n, boolean toNegate)
        {
                super(toNegate);
                npcc = c;
                npc = n;

        }

        @Override
        protected boolean check() {
                return false;
        }
}
