import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/ws")
//@ServerEndpoint("/Web_Socket_WebApp-1.0-SNAPSHOT/ws")
public class HelloServlet {
    private static Map<String, String> usernames = new HashMap<String, String>();

    @OnOpen
    public void open(Session session) throws IOException, EncodeException {
        System.out.println("--> opened");

        session.setMaxIdleTimeout(120000);

        session.getBasicRemote().sendText("(Server): Welcome to the chat room. Please state your username to begin.");

    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        if (usernames.containsKey(userId)) {
            String username = usernames.get(userId);
            usernames.remove(userId);
            for (Session peer : session.getOpenSessions())
                peer.getBasicRemote().sendText("(Server): " + username + " left the chat room.");
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        String userId = session.getId();
        if (usernames.containsKey(userId)) {

            String stringArray[] = message.split("/");
            String sessionId = "";
            if (stringArray.length == 3) {
                System.out.println("--> inside if 1");

                System.out.println("->" + stringArray[1] + "<- " +  usernames);
                for (Map.Entry username: usernames.entrySet()) {
                    System.out.println("username.getValue()--" + username.getValue()+"--");
                    if (username.getValue().equals(stringArray[1])) {
                        System.out.println("--> inside if 2");

                        sessionId = (String) username.getKey();
                        break;
                    }
                }

                for (Session peer : session.getOpenSessions()) {
                    if (peer.getId() == sessionId) {
                        System.out.println("--> inside if 3");

                        peer.getBasicRemote().sendText(message);
                    }
                }

            }
            else {

                String username = usernames.get(userId);

                for (Session peer : session.getOpenSessions()) {
                    peer.getBasicRemote().sendText("(" + username + "): " + message);
                }
            }


        } else {
            if (usernames.containsValue(message) || message.toLowerCase().equals("server"))
                session.getBasicRemote().sendText("(Server): That username is already in use. Please try again.");
            else {
                System.out.println(usernames);
                usernames.put(userId, message);
                session.getBasicRemote().sendText("(Server): Welcome, " + message + "!");
                for (Session peer : session.getOpenSessions())
                    if (!peer.getId().equals(userId)) {

                        String usersList = "";
                        Integer index = 0;
                        for (String user : usernames.values()) {
                            index++;
                            usersList += index.toString() + ". " +  user + "\n";
                        }
                        peer.getBasicRemote().sendText("(Server): " + message + " joined the chat room." + usersList);
                    }

            }
        }
    }
}
