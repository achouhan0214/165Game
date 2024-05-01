import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public class GameAIServerUDP extends GameConnectionServer<UUID> {
        NPCcontroller npcCtrl;
        public GameAIServerUDP(int localPort, NPCcontroller npc) throws IOException {
                
                super(localPort, ProtocolType.UDP);
                npcCtrl = npc;
        }
        // --- additional protocol for NPCs ----
        public void sendCheckForAvatarNear()
        {
                try {
                        String message = new String("isnr");
                        message += "," + (npcCtrl.getNPC()).getX();
                        message += "," + (npcCtrl.getNPC()).getY();
                        message += "," + (npcCtrl.getNPC()).getZ();
                        message += "," + (npcCtrl.getCriteria());
                        sendPacketToAll(message);
                } catch (IOException e)
                {
                        System.out.println("couldnt send msg"); e.printStackTrace();
                }
        }
        public void sendNPCinfo()
        {
                try {
                        String message = new String("NPCinfo");
                        message += "," + (npcCtrl.getNPC()).getX();
                        message += "," + (npcCtrl.getNPC()).getY();
                        message += "," + (npcCtrl.getNPC()).getZ();
                        message += "," + (npcCtrl.getNPC()).getSize();
                        sendPacketToAll(message);
                } catch (IOException e)
                {
                        System.out.println("couldnt send msg"); e.printStackTrace();
                }
        }
        public void sendNPCstart(UUID clientID)
        {
                NPC npc = npcCtrl.getNPC();
                try {

                        String message = new String("needNPC," + clientID.toString());
                        message += "," + npc.getX();
                        message += "," + npc.getY();
                        message += "," + npc.getZ();
                        sendPacket(message, clientID);
                        //forwardPacketToAll(message, clientID);
                        System.out.println("server telling clients about an NPC");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        @Override
        public void processPacket(Object o, InetAddress senderIP, int port)
        {

                String message = (String) o;
                String[] messageTokens = message.split(",");

                if (messageTokens[0].compareTo("join") == 0) {
                        try {
                                IClientInfo ci;
                                ci = getServerSocket().createClientInfo(senderIP, port);
                                UUID clientID = UUID.fromString(messageTokens[1]);
                                addClient(ci, clientID);
                                System.out.println("Join request received from - " + clientID.toString());
                                sendJoinedMessage(clientID, true);
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                // Case where server receives request for NPCs
                // Received Message Format: (needNPC,id)
                if(messageTokens[0].compareTo("needNPC") == 0)
                {
                        System.out.println("server got a needNPC message");
                        UUID clientID = UUID.fromString(messageTokens[1]);
                        sendNPCstart(clientID);
                }
                // Case where server receives notice that an av is close to the npc
                // Received Message Format: (isnear,id)
                if(messageTokens[0].compareTo("isnear") == 0)
                {
                        UUID clientID = UUID.fromString(messageTokens[1]);
                        handleNearTiming(clientID);
                }

                if (messageTokens[0].compareTo("mNPC") == 0) {
                        UUID clientID = UUID.fromString(messageTokens[1]);
                        String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
                        sendCreateNPCmsg(clientID, pos);
                }
        }

        public void handleNearTiming(UUID clientID)
        {
                npcCtrl.setNearFlag(true);
        }
        // ------------ SENDING NPC MESSAGES -----------------
        // Informs clients of the whereabouts of the NPCs.
        public void sendCreateNPCmsg(UUID clientID, String[] position) {
                try {
                        System.out.println("server telling clients about an NPC");
                        String message = new String("createNPC," + clientID.toString());
                        message += "," + position[0];
                        message += "," + position[1];
                        message += "," + position[2];
                        forwardPacketToAll(message, clientID);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void sendJoinedMessage(UUID clientID, boolean success)
        {
                try
                {	System.out.println("trying to confirm join");
                        String message = new String("join,");
                        if(success)
                                message += "success";
                        else
                                message += "failure";
                        sendPacket(message, clientID);
                }
                catch (IOException e)
                {
                        e.printStackTrace();
                }
        }
}
