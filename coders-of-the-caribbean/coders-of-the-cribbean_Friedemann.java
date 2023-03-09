import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/


class Player {

    public static Set<Cube> mines = new HashSet<>();
    public static Set<Cube> cannonballs = new HashSet<>();


    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Set<Cube> barrels = new HashSet<>();
        List<Ship> myShips = new ArrayList<>();
        List<Ship> enemyShips = new ArrayList<>();
        int numberOfEnemyShips;

        // game loop
        while (true) {
            Cube target = new Cube(50, 50);
            //       myShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            int shipCounter = 0;

            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();

                while (myShipCount < myShips.size()) {  // für jedes zerstörte eigene Schiff die Liste verkürzen.
                    // ACHTUNG: FEHLERHAFT. WIRD 1. SCHIFF ZERSTÖRT, VERSCHWINDET DAS 2. SCHIFF AUS LISTE UND DAS 1. ÜBERNIMMT DIE KOORDINATEN DES 2.
                    // das ist ZUNÄCHST kein schwerwiegendes Problem. Macht halt für eine Runde das reloaden kaputt.
                    myShips.remove(myShips.size() - 1);
                }

                switch (entityType) {
                    case "SHIP":
                        if (arg4 == 1) { // eigenes schiff
                            if (myShipCount > myShips.size()) {   // Schiffe in Liste speichern - nur in Runde 1
                                myShips.add(new Ship(new Cube(x, y), arg2, arg1, arg3));
                            } else {  // Schiffe aktualisieren
                                // FEHLERHAFT - IN Z.43 BESCHRIEBEN
                                myShips.get(shipCounter).setCoords(x, y);
                                myShips.get(shipCounter).setSpeed(arg2);
                                myShips.get(shipCounter++).setDirection(arg1);
                            }
                        } else { // feindliches schiff
                            enemyShips.add(new Ship(new Cube(x, y), arg2, arg1, arg3));
                        }
                        break;
                    case "BARREL":
                        barrels.add(new Cube(x, y));
                        break;
                    case "MINE":
                        // System.err.println("Mine to Mine-Set");
                        mines.add(new Cube(x, y));
                        break;
                }
            }
            numberOfEnemyShips = enemyShips.size();


            // Befehle für jedes eigene Schiff aus der Liste
            for (Ship myShip : myShips) {


                if (preventCannonball(myShip) && myShip.speed != 0) {
                    System.out.println("STARBOARD");
                    continue;
                }
                // suche dichtestes Barrel
                for (Cube bar : barrels) {
                    if (myShip.distanceTo(target) > myShip.distanceTo(bar)) {
                        target = bar;
                    }
                }
                barrels.remove(target); // remove barrel, damit weitere schiffe es nicht auch anpeilen


                if (mineAhead(myShip)) {
                    System.out.println("STARBOARD");
                    continue;
                }


                if (barrels.isEmpty()) {
                    Cube futurePosition = enemyShips.get(0).futurePosition();
                    System.out.println("FIRE " + futurePosition.toString());
                    System.out.println("MOVE " + target.toString());
                    continue;
                }
                System.out.println("MOVE " + target.toString());

/*                // schieße auf feind in der nähe
                if (!myShip.isReloading) {
                    Cube cannon = myShip.futurePosition(1, 1);
                    Ship targetShip = myShip;  // die IDE meckert, wenn targetShip nicht sicher initialisiert wird
                    int targetPrio = 0;
                    for (int i = 0; i < numberOfEnemyShips; i++) {
                        int shipPrio = 0;
                        Cube futurePosition = enemyShips.get(i).futurePosition();
                        int enemySpeed = enemyShips.get(i).speed;

                        if (futurePosition.distanceTo(cannon) == 1){
                            shipPrio = 1000;
                            System.err.println("Sicherer treffer!  - ?");
                            // ACHTUNG - "sichere treffer" treffen auch eigenes Schiff ^^
                        } else if (futurePosition.distanceTo(cannon) <= 4){
                            shipPrio = 20;
                        } else if (futurePosition.distanceTo(cannon) <= 7){
                            shipPrio = 12;
                        } else if (futurePosition.distanceTo(cannon) <= 10){
                            shipPrio = 7;
                        }
                        // ACHTUNG - auch andere Kugeln treffen das eigene Schiff ^^
                        if (enemySpeed == 0){
                            shipPrio *= 2;
                        } else {
                            shipPrio /= enemySpeed;
                        }


                        if(shipPrio > targetPrio){
                            targetShip = enemyShips.get(i);
                            targetPrio = shipPrio;
                        }
                    }

                    if (targetShip != myShip){
                        Cube futurePos = targetShip.futurePositionInNTurns((targetShip.distanceTo(cannon)+1)/3 + 1);
                        System.out.println("FIRE " + futurePos.toString());
                        myShip.isReloading = true;
                    } else {
                        //            System.err.println("kein target gefunden");
                    }

                } else {
                    myShip.isReloading = false;
                    //        System.err.println("Ship is reloading");
                }

                if (!myShip.isReloading) {
                    Random ran = new Random();
                    int rann = ran.nextInt(12);
                    if (rann <=9){
                        System.out.println("MOVE " + target.toString());
                    } else if (rann <= 11){
                        System.out.println("FASTER");
                    } else if (rann == 12){
                        System.out.println("STARBOARD");
                    } else if (rann == 13){
                        System.out.println("PORT");
                    }
                }*/

            }
        }
    }


    public static boolean mineAhead(Ship s) {
        for (Cube mine : mines) {

            //y : 1 and 4 | x: 2 and 5 | z: 0 and 3
            if (s.speed == 0) {
                return false;
            }
            ;

            if (s.direction == 0 && s.cube.z == mine.z && s.cube.y > mine.y && s.cube.x < mine.x) {
                System.err.println("MINE DETECTED ZERO");
                return true;

            }
            if (s.direction == 1 && s.cube.y == mine.y && s.cube.x < mine.x && s.cube.z > mine.z) {
                System.err.println("MINE DETECTED ONE");
                return true;
            }
            if (s.direction == 2 && s.cube.x == mine.x && s.cube.y < mine.y && s.cube.z > mine.z) {
                System.err.println("MINE DETECTED TWO");
                return true;
            }
            if (s.direction == 3 && s.cube.z == mine.z && s.cube.y < mine.y && s.cube.x > mine.x) {

                System.err.println("MINE DETECTED THREE");
                return true;
            }
            if (s.direction == 4 && s.cube.y == mine.y && s.cube.z < mine.y && s.cube.x > mine.x) {
                System.err.println("MINE DETECTED FOUR");
                return true;
            }
            if (s.direction == 5 && s.cube.x == mine.x && s.cube.y > mine.y && s.cube.z < mine.z) {
                System.err.println("MINE DETECTED FIVE");
                return true;
            }
        }
        return false;
    }


    public static boolean preventCannonball(Ship s) {
        for (Cube cannon : cannonballs) {

            //y : 1 and 4 | x : 2 and 5 | z: 0 and 3
            if (s.direction == 0 && s.cube.z == cannon.z) {
                return true;
            }
            if (s.direction == 1 && s.cube.y == cannon.y) {
                return true;
            }
            if (s.direction == 2 && s.cube.x == cannon.x) {
                return true;
            }
            if (s.direction == 3 && s.cube.z == cannon.z) {
                return true;
            }
            if (s.direction == 4 && s.cube.y == cannon.y) {
                return true;
            }
            if (s.direction == 5 && s.cube.x == cannon.x) {
                return true;
            }

        }
        return false;
    }
}


class Ship {
    Cube cube;
    int speed;
    int direction;
    int remainingRum;
    boolean isReloading = false;

    public Ship(Cube cube, int spd, int dir, int rum) {
        this.cube = cube;
        this.speed = spd;
        this.direction = dir;
        this.remainingRum = rum;
    }

    int distanceTo(Cube otherCube) {
        return (this.cube.distanceTo(otherCube));
    }

    int distanceTo(Ship s) {
        return this.cube.distanceTo(s.cube);
    }

    public Cube futurePosition() {
        return futurePosition(this.speed, 1);
    }

    public Cube futurePositionInNTurns(int n) {
        return futurePosition(this.speed, n);
    }

    public Cube futurePosition(int spd, int turns) {
        int speed = spd * turns;
        int x = this.cube.x;
        int y = this.cube.y;
        int z = this.cube.z;
        switch (this.direction) {
            case 0:
                y = y - speed;
                x = x + speed;
                break;
            case 1:
                x = x + speed;
                z = z - speed;
                break;
            case 2:
                y = y + speed;
                z = z - speed;
                break;
            case 3:
                x = x - speed;
                y = y + speed;
                break;
            case 4:
                x = x - speed;
                z = z + speed;
                break;
            case 5:
                y = y - speed;
                z = z + speed;
                break;
            default:
                break;
        }
        return new Cube(x, y, z);
    }

    void setCoords(int x, int y) {
        this.cube = new Cube(x, y);
    }

    void setDirection(int dir) {
        this.direction = dir;
    }

    void setSpeed(int s) {
        this.speed = s;
    }
}

// used for Output-String
class Coordinate {
    int x;
    int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// used for distance:
class Cube {
    int x;
    int y;
    int z;


    public Cube(int x, int y) {
        Cube cube = toCube(x, y);
        this.x = cube.x;
        this.y = cube.y;
        this.z = cube.z;
    }

    public Cube(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    public Cube toCube(int x, int y) {
        if (y % 2 == 0) {
            int x3 = x - y / 2;
            int y3 = -x3 - y;
            return new Cube(x3, y3, y);
        } else {
            int x3 = x - (y - 1) / 2;
            int y3 = -x3 - y;
            return new Cube(x3, y3, y);
        }
    }

    public Coordinate toCoord() {
        if (z % 2 == 0) {
            int col = this.x + (this.z / 2);
            //   int col = (this.x + (this.z) / 2);
            int row = this.z;
            return new Coordinate(col, row);
        } else {
            int col = this.x + ((this.z - 1) / 2);
            //    int col = (this.x + (this.z - 1) / 2);
            int row = this.z;
            return new Coordinate(col, row);
        }
    }

    int distanceTo(Cube otherCube) {
        // source: https://www.redblobgames.com/grids/hexagons/#distances-cube
        return (Math.abs(this.x - otherCube.x) + Math.abs(this.y - otherCube.y)
                + Math.abs(this.z - otherCube.z)) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cube cube = (Cube) o;
        return x == cube.x && y == cube.y && z == cube.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        Coordinate coord = this.toCoord();
        return coord.x + " " + coord.y;
    }
}
