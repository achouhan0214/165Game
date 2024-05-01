import tage.ai.behaviortrees.BTCondition;

public class GetBig extends BTCondition {
        NPC npc;
        GetBig(NPC npc)
        {
                super(false);
                npc = npc;

        }

        @Override
        protected boolean check() {
                return false;
        }
}
