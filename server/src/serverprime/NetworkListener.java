package serverprime;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import serverprime.Packet.*;

//That will be polling for any conections etc
public class NetworkListener extends Listener {

    //Some public flags
    public int readyForNextPoints = 0;     // Used to check if snake can be moved
    public int connectionCounter = 0;       // Number of connetced players
    public int deadPlayers = 0;            // Number of dead players
    public int tour = 1;                   // Number of tour
    public int readyPlayers = 0;           // Used to check number of ready players ( beggining of the next tour )

    private final List<Player> listOfPlayers = new ArrayList<>(); // List of Players, helps with EVERYTHINGGGG <3
    private final PacketHead heads = new PacketHead(); //Private packet that is sent only on the beginning of each tour

    private final static int sizeWidth = 120; // Width of our mask borad
    private final static int sizeHeight = 60; // Height of our mask borad
    private int tabDeadPlayer[] = new int[4];
    private int[] rewards = {0, 1, 2, 3};
    private int rewardPosition;
    private int playersAnsered;

    //collor
    private int blue = 1;
    private int red = 1;
    private int orange = 1;
    private int pink = 1;
    
    boolean colorTaken = false;

    @Override
    public void connected(Connection c) {
        rewardPosition = 0;
        connectionCounter++; // Someone has connected
        Player player = new Player();
        // more than 4 players are not allowed
        if (connectionCounter < 5) {
            //set players x,y and updating PacketHead
            switch (connectionCounter) {
                case 1:
                    player.x = 60;
                    player.y = 9;
                    heads.x1 = player.x;
                    heads.y1 = player.y;

                    break;
                case 2:
                    player.x = 60;
                    player.y = 51;
                    heads.x2 = player.x;
                    heads.y2 = player.y;
                    break;
                case 3:
                    player.x = 4;
                    player.y = 30;
                    heads.x3 = player.x;
                    heads.y3 = player.y;
                    break;
                case 4:
                    player.x = 115;
                    player.y = 30;
                    heads.x4 = player.x;
                    heads.y4 = player.y;
                    break;
                default:
                    break;
            }
            ServerPrime.mask[player.x][player.y] = BarrierType.SNAKE;
            heads.count = connectionCounter; //helps in setting up heads       
            player.c = c;
            player.color = "bg";
            player.id = c.getID();
            player.isAlive = true;
            player.score = 0;
            listOfPlayers.add(player); //adding next snake to list

            //ZMIENIC NAZWE NA PacketSetImage
            PacketAddPlayer p = new PacketAddPlayer();
            p.x = player.x;
            p.y = player.y;
            p.id = player.id;
            c.sendTCP(p);
            ServerPrime.server.sendToAllTCP(heads);
            
            //sentNames();
            Log.info("[SERVER] Someone has connected.");

        } else {
            Log.info("[SERVER] Too much!");
        }
    }

    @Override
    public void disconnected(Connection cnctn) {
        connectionCounter--;
        Log.info("[SERVER] Someone has disconnected.");
    }

    private void endGame() {
        PacketEndGame endGame = new PacketEndGame();
        ServerPrime.server.sendToAllTCP(endGame);
        playersAnsered = 0;
    }

    public void newTour(Connection c) {

        //Resurrect all snakes !!
        for (Player pl : listOfPlayers) {
            pl.isAlive = true;

        }
        Log.info("New Tour");
        tour += 1;
        for (int i = 0; i < connectionCounter; i++) {
            listOfPlayers.get(i).score += tabDeadPlayer[i];
        }
        Random generator = new Random();
        PacketNewTour newTour = new PacketNewTour();
        newTour.x1 = generator.nextInt(sizeWidth - 2) + 1;
        newTour.y1 = generator.nextInt(sizeHeight - 2) + 1;
        newTour.x2 = generator.nextInt(sizeWidth - 2) + 1;
        newTour.y2 = generator.nextInt(sizeHeight - 2) + 1;
        newTour.x3 = generator.nextInt(sizeWidth - 2) + 1;
        newTour.y3 = generator.nextInt(sizeHeight - 2) + 1;
        newTour.x4 = generator.nextInt(sizeWidth - 2) + 1;
        newTour.y4 = generator.nextInt(sizeHeight - 2) + 1;
        for(Player player : listOfPlayers)
            {
                switch (player.color){
                        case "rozowy" :   
                            newTour.score4 = player.score;
                                break;
                        case "niebieski" :
                            newTour.score1 = player.score;
                            break;
                        case "pomaranczowy":
                            newTour.score3 = player.score;
                            break;
                        case "czerwony" :
                             newTour.score2 = player.score;
                             break;
                }
            }
        
        newTour.count = connectionCounter;
        newTour.tour = tour;
        ServerPrime.initBoard();

        ServerPrime.server.sendToAllTCP(newTour);
        readyPlayers = 0;
        deadPlayers = 0;
        if (tour > 10) {
            endGame();
            System.out.println("END");
        }

    }
    public void sentNames(){
         PacketNames pNames = new PacketNames();
           
          for(Player player : listOfPlayers)
            {
                switch (player.color){
                        case "rozowy" :                      
                            pNames.name4 = player.name;
                                break;
                        case "niebieski" :
                            pNames.name1 = player.name;
                            break;
                        case "pomaranczowy":
                            pNames.name3 = player.name;
                            break;
                        case "czerwony" :
                             pNames.name2 = player.name;
                             break;
                }
            }
    
           ServerPrime.server.sendToAllTCP(pNames);
    
    
    }

    @Override
    public void received(Connection c, Object o) {

        if (o instanceof PacketSendColor) {
            PacketSendColor p = (PacketSendColor) o;
            listOfPlayers.get(c.getID() - 1).color = p.color;
            if ("niebieski".equals(p.color)) {
                blue = 0;
            }
            if ("czerwony".equals(p.color)) {
                red = 0;
            }
            if ("pomaranczowy".equals(p.color)) {
                orange = 0;
            }
            if ("rozowy".equals(p.color)) {
                pink = 0;
            }

                PacketPlayersColors p1 = new PacketPlayersColors();
                if (connectionCounter == 1) {
                    p1.c1 = listOfPlayers.get(0).color;
                    p1.c2 = "bg";
                    p1.c3 = "bg";
                    p1.c4 = "bg";
                }

                if (connectionCounter == 2) {
                    p1.c1 = listOfPlayers.get(0).color;
                    p1.c2 = listOfPlayers.get(1).color;
                    p1.c3 = "bg";
                    p1.c4 = "bg";
                }

                if (connectionCounter == 3) {
                    p1.c1 = listOfPlayers.get(0).color;
                    p1.c2 = listOfPlayers.get(1).color;
                    p1.c3 = listOfPlayers.get(2).color;
                    p1.c4 = "bg";
                }

                if (connectionCounter == 4) {
                    p1.c1 = listOfPlayers.get(0).color;
                    p1.c2 = listOfPlayers.get(1).color;
                    p1.c3 = listOfPlayers.get(2).color;
                    p1.c4 = listOfPlayers.get(3).color;
                }
                ServerPrime.server.sendToAllTCP(heads); //send to ALL connected players packet with heads using TCP protocol
                ServerPrime.server.sendToAllTCP(p1);
                sentNames();                          

        }

        if (o instanceof PacketAskForColors) {
            PacketColors p = new PacketColors();
            p.blue = blue;
            p.orange = orange;
            p.pink = pink;
            p.red = red;
            c.sendTCP(p);
        }//        PacketEndGame endGame = new PacketEndGame();

        if (o instanceof PacketReadyPlayer) {
            readyPlayers += 1;
            if (readyPlayers == connectionCounter) {
                PacketStart odp = new PacketStart();
                ServerPrime.server.sendToAllTCP(odp);

            }

        }
        if (o instanceof PacketWantAgain) {
            playersAnsered++;
            if (playersAnsered == connectionCounter) {
                tour = 0;
                for (int i = 0; i < connectionCounter; i++) {
                    listOfPlayers.get(i).score = 0;
                }
                for (int i = 0; i < 4; i++) {
                    tabDeadPlayer[i] = 0;
                }
                newTour(c);
            }
        }
        if (o instanceof PacketAskForColor) {
            PacketAskForColor p = (PacketAskForColor) o;
            for(Player player : listOfPlayers)
            {
                System.out.println(player.color);

                if(player.color.equals(p.colorName))
                {
                    System.out.println(p.colorName);
                    PacketColorRefused pcf = new PacketColorRefused();
                    c.sendTCP(pcf);
                    System.out.println("wychodze nie akceptuje koloru");
                    return;
                }
            }
                 System.out.println("nie wychodze bo akceptuje kolor");
                    PacketColorAccepted pca = new PacketColorAccepted();
                    c.sendTCP(pca);
            
         }
        
        
        if (o instanceof PacketNotWantAgain) {
            PacketExit p = new PacketExit();
            ServerPrime.server.sendToAllTCP(p);
            System.exit(0);

        }
        if (o instanceof PacketLoginRequested) {
            PacketLoginAccepted loginAnswer = new PacketLoginAccepted();
            if (connectionCounter < 5) {
                String name = ((PacketLoginRequested) o).name;               
                listOfPlayers.get(c.getID()-1).name=name;
                loginAnswer.accepted = true;
            } else {
                loginAnswer.accepted = false;
            }

            c.sendUDP(loginAnswer);
 

        }

        //PacketPoint handle. Used to check collision
        if (o instanceof PacketPoint) {
            //getting x,y from packet
            int x = ((PacketPoint) o).x;
            int y = ((PacketPoint) o).y;

            //checking collisiom
            if (ServerPrime.mask[x][y] == BarrierType.EMPTY) {
                ServerPrime.mask[x][y] = BarrierType.SNAKE; //updating mask woth new point

                //Updating head with new points
                listOfPlayers.get(c.getID() - 1).x = x;
                listOfPlayers.get(c.getID() - 1).y = y;
                readyForNextPoints++; //snake is ready for sending points

                //If for example 4 snakes are ready for sending points, then
                //points will be send to all of them using TCP
                if (readyForNextPoints == connectionCounter - deadPlayers) {
                    for (Player pl : listOfPlayers) {
                        if (pl.isAlive) {
                            readyForNextPoints = 0;
                            PacketPoint p = new PacketPoint();
                            p.x = pl.x;
                            p.y = pl.y;
                            p.id = pl.id;
                            ServerPrime.server.sendToAllTCP(p);
                            rewardPosition = deadPlayers;
                        }
                    }
                }

            } else {
                //Snake which connection in c.getID() is dead.
                listOfPlayers.get(c.getID() - 1).isAlive = false;
                // Moving the rest of snakes
                for (Player pl : listOfPlayers) {
                    if (pl.isAlive) {
                        readyForNextPoints = 0;
                        PacketPoint p = new PacketPoint();
                        p.x = pl.x;
                        p.y = pl.y;
                        p.id = pl.id;
                        ServerPrime.server.sendToAllTCP(p);

                    }
                }
                tabDeadPlayer[((PacketPoint) o).id - 1] = rewards[rewardPosition];
                deadPlayers += 1;
                if (deadPlayers == connectionCounter) {
                    newTour(c);
                    // deadPlayers = connectionCounter;
                }

                //packet which informs snake that it is dead
                PacketDead dead = new PacketDead();
                c.sendTCP(dead);

            }

        }
    }

}
