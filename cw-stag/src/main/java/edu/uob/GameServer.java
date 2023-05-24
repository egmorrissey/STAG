package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class implements the STAG server. */
public final class GameServer {

    ArrayList<Location> locations;
    HashMap<String, HashSet<GameAction>> actionHashList;
    ArrayList<Player> players;
    String playerMessage;
    Integer currentPlayer;

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */

    public GameServer(File entitiesFile, File actionsFile) {

        locations = new ArrayList<>();
        actionHashList = new HashMap<>();
        players = new ArrayList<>();
        addEntitiesFileToServer(entitiesFile);
        addActionFileToServer(actionsFile);

    }

    private void addActionFileToServer(File actionsFile){

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            for (int a = 1; a < actions.getLength() - 1; a+=2) {
                Element firstAction = (Element) actions.item(a);

                Element triggers = (Element) firstAction.getElementsByTagName("triggers").item(0);
                for (int t = 0; t < triggers.getElementsByTagName("keyphrase").getLength(); t++) {
                    //add trigger to hashmap
                    String trigger = triggers.getElementsByTagName("keyphrase").item(t).getTextContent();
                    GameAction action = new GameAction();
                    HashSet<GameAction> actionHashSet = new HashSet<>();

                    //populate hashset
                    Element subjects = (Element) firstAction.getElementsByTagName("subjects").item(0);
                    for (int s = 0; s < subjects.getElementsByTagName("entity").getLength(); s++) {
                        action.addSubject(subjects.getElementsByTagName("entity").item(s).getTextContent());
                    }

                    Element consumed = (Element) firstAction.getElementsByTagName("consumed").item(0);
                    for (int c = 0; c < consumed.getElementsByTagName("entity").getLength(); c++) {
                        action.addConsumed(consumed.getElementsByTagName("entity").item(c).getTextContent());
                    }

                    Element produced = (Element) firstAction.getElementsByTagName("produced").item(0);
                    for (int p = 0; p < produced.getElementsByTagName("entity").getLength(); p++) {
                        action.addProduced(produced.getElementsByTagName("entity").item(p).getTextContent());
                    }

                    action.addNarration(firstAction.getElementsByTagName("narration").item(0).getTextContent());

                    actionHashSet.add(action);
                    actionHashList.put(trigger, actionHashSet);
                }

            }

        } catch(ParserConfigurationException pce) {
            System.out.println("ParserConfigurationException was thrown when attempting to read basic actions file");
        } catch(SAXException saxe) {
            System.out.println("SAXException was thrown when attempting to read basic actions file");
        } catch(IOException ioe) {
            System.out.println("IOException was thrown when attempting to read basic actions file");
        }

    }

    private void addEntitiesFileToServer(File entitiesFile){

        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();

            //locations and entities
            ArrayList<Graph> locationsToAdd = sections.get(0).getSubgraphs();

            //pass location data into arraylist
            addDataToLocations(locationsToAdd);

            //paths
            ArrayList<Edge> paths = sections.get(1).getEdges();

            //pass paths data into arraylist
            addPathsToServer(paths);

        } catch (FileNotFoundException fnfe) {
            System.out.println("FileNotFoundException was thrown when attempting to read basic entities file");
        } catch (ParseException pe) {
            System.out.println("ParseException was thrown when attempting to read basic entities file");
        }

    }

    private void addDataToLocations(ArrayList<Graph> locationsToAdd){

        for (Graph location : locationsToAdd) {
            Node locationDetails = location.getNodes(false).get(0);
            String locationName = locationDetails.getId().getId();
            String locationDescription = locationDetails.getAttribute("description");
            Location newLocation = new Location(locationName, locationDescription);

            //add artifacts, furniture and characters
            ArrayList<Graph> entities = location.getSubgraphs();
            for (Graph entity : entities) {
                String entityType = entity.getId().getId();

                for (int y = 0; y < entity.getNodes(false).size(); y++) {
                    Node entityDetails = entity.getNodes(false).get(y);
                    String entityName = entityDetails.getId().getId();
                    String entityDescription = entityDetails.getAttribute("description");
                    newLocation.addEntity(entityType, entityName, entityDescription);
                }
            }
            locations.add(newLocation);
        }

    }

    private void addPathsToServer(ArrayList<Edge> paths) {

        for (Edge firstPath : paths) {
            Node fromLocation = firstPath.getSource().getNode();
            String fromName = fromLocation.getId().getId();
            Node toLocation = firstPath.getTarget().getNode();
            String toName = toLocation.getId().getId();
            for (Location location : locations) {
                if (location.getName().equals(fromName)) {
                    location.addPath(toName);
                }
            }
        }

    }

    public HashMap<String, HashSet<GameAction>> getActionHashList() {

        return actionHashList;

    }

    public ArrayList<Location> getLocations() {

        return locations;

    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */

    public String handleCommand(String command) {
        // TODO implement your server logic here

        String[] nameSplit = command.split(":");
        if (!playerExistCheck(nameSplit[0])) {
            newPlayer(nameSplit[0]);
        };
        setCurrentPlayer(nameSplit[0]);
        if (nameSplit.length < 2){
            return "Invalid command";
        }
        String commandLC = nameSplit[1].toLowerCase();
        String[] words = commandLC.split("\\s+");

        //command check
        for (String s : words) {
            switch (s) {
                case ("inventory"), ("inv") -> {
                    playerMessage = players.get(currentPlayer).getInventoryList();
                    return playerMessage;
                }
                case ("get") -> {
                    commandGet(words);
                    return playerMessage;
                }
                case ("drop") -> {
                    commandDrop(words);
                    return playerMessage;
                }
                case ("goto") -> {
                    commandGoTo(words);
                    return playerMessage;
                }
                case ("look") -> {
                    commandLook();
                    return playerMessage;
                }
                case ("health") -> {
                    commandHealth();
                    return playerMessage;
                }

            }
        }

        //action check
        for (String word : words) {
            if (actionHashList.containsKey(word)) {
                commandAction(words, word);
                playerHealthCheck();
                return playerMessage;
            }
        }

        return "Not a valid command";
    }

    private void playerHealthCheck() {

        if (players.get(currentPlayer).getHealth() == 0) {
            players.get(currentPlayer).resetHealth();
            resetPlayer();
        }

    }

    private boolean playerExistCheck(String name) {

        for (Player value : players) {
            if (value.getName().equals(name)) {
                return true;
            }
        }

        return false;

    }

    private void newPlayer(String name) {

        Player newPlayer = new Player(name);
        newPlayer.setCurrentLocation(locations.get(0).getName());
        players.add(newPlayer);

    }

    private void setCurrentPlayer(String name) {

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) {
                currentPlayer = i;
            }
        }

    }

    private void commandAction(String[] commandInputs, String keyphrase) {

        //create array for each action in a keyphrase
        GameAction[] array = actionHashList.get(keyphrase).toArray(new GameAction[0]);

        //for each action
        for (GameAction gameAction : array) {
            //is a subject present in command
            for (String word : commandInputs) {
                if (validCommandCheck(gameAction, word)) {
                    commandHelper(gameAction);
                    return;
                }
            }

        }

        playerMessage = "Not a valid command";

    }

    private void commandHelper(GameAction gameAction) {

        //for each subject in a command
        for (int i = 0; i < gameAction.getSubjects().size(); i++) {
            //if it doesn't exist in location or player return fail
            if (!commandSubjectExistCheck(gameAction.getSubjects().get(i))) {
                playerMessage = "Not a valid command";
                return;
            }
        }

        //remove consumed
        for (int i = 0; i < gameAction.getConsumed().size(); i++) {
            consumedHelper(gameAction.getConsumed().get(i));
        }

        //move produced
        for (int i = 0; i < gameAction.getProduced().size(); i++) {
            producedHelper(gameAction.getProduced().get(i));
        }

        playerMessage = gameAction.getNarration();

    }

    private void consumedHelper(String consumed) {

        //travels through player inventory
        for (int i = 0; i < players.get(currentPlayer).getInventory().size(); i++) {
            //if a match inventory item removed
            if (players.get(currentPlayer).getInventory().get(i).getName().equals(consumed)) {
                players.get(currentPlayer).getInventory().remove(i);
                return;
            }
        }

        int locationToSearch = playerLocationArrayPosition();

        //travels through location entity list
        for (int i = 0; i < locations.get(locationToSearch).getEntities().size(); i++) {
            //if a match pass string into remove function
            if (locations.get(locationToSearch).getEntities().get(i).equals(consumed)) {
                locationRemoveConsumed(consumed);
                locations.get(locationToSearch).getEntities().remove(i);
                return;
            }
        }

        if (consumed.equals("health")){
            players.get(currentPlayer).consumeHealth();
        }

    }

    private void producedHelper(String produced) {

        //for each location in server
        for (Location location : locations) {
            for (int j = 0; j < location.getEntities().size(); j++) {
                //if location contains produced
                if (location.getEntities().get(j).equals(produced)) {
                    producedMover(location, produced);
                    return;
                }
            }
        }

        if (produced.equals("health")){

            players.get(currentPlayer).produceHealth();
            return;

        }

        //if produced doesn't exist add as path in current location
        locations.get(playerLocationArrayPosition()).addPath(produced);

    }

    private void producedMover(Location location, String produced) {

        //if artefact equals produced move to inventory
        for (int i = 0; i < location.getArtefacts().size(); i++) {
            if (location.getArtefacts().get(i).getName().equals(produced)) {
                players.get(currentPlayer).addInventory(location.getArtefacts().get(i));

                location.getArtefacts().remove(i);
                return;
            }
        }

        int locationToAdd = playerLocationArrayPosition();

        //if furniture equals produced move to current location
        for (int i = 0; i < location.getFurniture().size(); i++) {
            if (location.getFurniture().get(i).getName().equals(produced)) {
                locations.get(locationToAdd).getFurniture().add(location.getFurniture().get(i));
                locations.get(locationToAdd).getEntities().add(produced);

                location.getFurniture().remove(i);
                location.removeEntityString(produced);
                return;
            }
        }

        //if character equals produced move to current location
        for (int i = 0; i < location.getCharacters().size(); i++) {
            if (location.getCharacters().get(i).getName().equals(produced)) {
                locations.get(locationToAdd).getCharacters().add(location.getCharacters().get(i));
                locations.get(locationToAdd).getEntities().add(produced);

                location.getCharacters().remove(i);
                location.removeEntityString(produced);
                return;
            }
        }

    }

    private void locationRemoveConsumed(String consumed) {

        //travels through each arrayList in location
        //if found remove

        int locationToSearch = playerLocationArrayPosition();

        for (int i = 0; i < locations.get(locationToSearch).getArtefacts().size(); i++) {
            if (locations.get(locationToSearch).getArtefacts().get(i).getName().equals(consumed)) {
                locations.get(locationToSearch).getArtefacts().remove(i);
                return;
            }
        }

        for (int i = 0; i < locations.get(locationToSearch).getFurniture().size(); i++) {
            if (locations.get(locationToSearch).getFurniture().get(i).getName().equals(consumed)) {
                locations.get(locationToSearch).getFurniture().remove(i);
                return;
            }
        }

        for (int i = 0; i < locations.get(locationToSearch).getCharacters().size(); i++) {
            if (locations.get(locationToSearch).getCharacters().get(i).getName().equals(consumed)) {
                locations.get(locationToSearch).getCharacters().remove(i);
                return;
            }
        }

    }

    private boolean commandSubjectExistCheck(String subject) {

        //checks if subject exists in inventory
        for (int i = 0; i < players.get(currentPlayer).getInventory().size(); i++) {
            if (players.get(currentPlayer).getInventory().get(i).getName().equals(subject)) {
                return true;
            }
        }

        int locationToSearch = playerLocationArrayPosition();

        //checks if subject exists in location
        for (int i = 0; i < locations.get(locationToSearch).getEntities().size(); i++) {
            if (locations.get(locationToSearch).getEntities().get(i).equals(subject)) {
                return true;
            }
        }

        return false;

    }

    public Integer playerLocationArrayPosition() {

        for (int i = 0; i < locations.size(); i++){
            if (locations.get(i).getName().equals(players.get(currentPlayer).getCurrentLocation())){
                return i;
            }
        }

        return 0;

    }

    private boolean validCommandCheck(GameAction gameAction, String word) {

        for (int i = 0; i < gameAction.getSubjects().size(); i++) {
            if (gameAction.getSubjects().get(i).equals(word)) {
                return true;
            }
        }
        return false;

    }

    private void commandHealth() {

        playerMessage = "Current health: " + players.get(currentPlayer).getHealth();

    }

    private void commandGet(String[] words) {
        String currentLocation = players.get(currentPlayer).getCurrentLocation();
        int locationToSearch = 0;

        for (int i = 0; i < locations.size(); i++){
            if (locations.get(i).getName().equals(currentLocation)){
                locationToSearch = i;
            }
        }

        for (int i = 0; i < locations.get(locationToSearch).getArtefacts().size(); i++) {
            for (String word : words) {
                if (locations.get(locationToSearch).getArtefactsByID(i).getName().equals(word)) {
                    players.get(currentPlayer).addInventory(locations.get(locationToSearch).getArtefactsByID(i));
                    locations.get(locationToSearch).getArtefacts().remove(locations.get(locationToSearch).getArtefactsByID(i));
                    playerMessage = "Added to inventory: " + word;
                    return;
                }
            }
        }

        playerMessage = "Unable to locate item";

    }

    private void commandDrop(String[] words) {

        String currentLocation = players.get(currentPlayer).getCurrentLocation();
        int locationToSearch = 0;

        for (int i = 0; i < locations.size(); i++){
            if (locations.get(i).getName().equals(currentLocation)){
                locationToSearch = i;
            }
        }

        for (int i = 0; i < players.get(currentPlayer).getInventory().size(); i++) {
            for (String word : words) {
                if (players.get(currentPlayer).getInventory().get(i).getName().equals(word)) {
                    locations.get(locationToSearch).getArtefacts().add(players.get(currentPlayer).getInventory().get(i));
                    players.get(currentPlayer).getInventory().remove(players.get(currentPlayer).getInventory().get(i));

                    playerMessage = "Item dropped: " + word;
                    return;
                }
            }

        }

        playerMessage = "Item not inside inventory";

    }

    private void commandGoTo(String[] words) {

        String currentLocation = players.get(currentPlayer).getCurrentLocation();
        int locationToSearch = 0;

        for (int i = 0; i < locations.size(); i++){
            if (locations.get(i).getName().equals(currentLocation)){
                locationToSearch = i;
            }
        }

        for (int i = 0; i < locations.get(locationToSearch).getPaths().size(); i++) {
            for (String word : words) {
                if (locations.get(locationToSearch).getPathByID(i).equals(word)) {
                    players.get(currentPlayer).setCurrentLocation(locations.get(locationToSearch).getPathByID(i));
                    playerMessage = "Travelled to: " + word;
                    return;
                }
            }
        }

        playerMessage = "Path does not exist";

    }

    private void commandLook() {

        int locationToSearch = playerLocationArrayPosition();

        playerMessage = "You can see: \n";
        String description = locations.get(locationToSearch).getDescription();
        playerMessage = playerMessage.concat(description);

        for (int i = 0; i < locations.get(locationToSearch).getArtefacts().size(); i++) {
            String artefactName = locations.get(locationToSearch).getArtefacts().get(i).getName();
            String artefactDesc = locations.get(locationToSearch).getArtefacts().get(i).getDescription();
            playerMessage = playerMessage.concat("\n" + artefactName + ": " + artefactDesc);
        }

        for (int i = 0; i < locations.get(locationToSearch).getFurniture().size(); i++) {
            String furnitureName = locations.get(locationToSearch).getFurniture().get(i).getName();
            String furnitureDesc = locations.get(locationToSearch).getFurniture().get(i).getDescription();
            playerMessage = playerMessage.concat("\n" + furnitureName + ": " + furnitureDesc);
        }

        for (int i = 0; i< locations.get(locationToSearch).getCharacters().size(); i++) {
            String characterName = locations.get(locationToSearch).getCharacters().get(i).getName();
            String characterDesc = locations.get(locationToSearch).getCharacters().get(i).getDescription();
            playerMessage = playerMessage.concat("\n" + characterName + ": " + characterDesc);
        }

        playerMessage = playerMessage.concat("\nAvailable paths: ");
        for (int i = 0; i< locations.get(locationToSearch).getPaths().size(); i++) {
            playerMessage = playerMessage.concat("\n" + locations.get(locationToSearch).getPaths().get(i));
        }

        if (players.size() > 1) {
            lookAddPlayers();
        }

    }

    private void lookAddPlayers() {

        playerMessage = playerMessage.concat("\nPlayers:");
        for (int i = 0; i < players.size(); i++){
            if(players.get(i).getCurrentLocation().equals(locations.get(playerLocationArrayPosition()).getName()) && i != currentPlayer){
                playerMessage = playerMessage.concat("\n" + players.get(i).getName());
            }
        }

    }

    private void resetPlayer() {

        //drop all items
        for (int i = players.get(currentPlayer).getInventory().size() - 1; i > -1; i--) {
            locations.get(playerLocationArrayPosition()).addEntity("artefacts",
                    players.get(currentPlayer).getInventory().get(i).getName(),
                    players.get(currentPlayer).getInventory().get(i).getDescription());
            players.get(currentPlayer).getInventory().remove(i);
        }
        players.get(currentPlayer).setCurrentLocation(locations.get(0).getName());

    }

    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }

}
