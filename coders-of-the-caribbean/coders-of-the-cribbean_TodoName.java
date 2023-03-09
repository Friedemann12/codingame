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

        // game loop
        while (true) {
            Cube target = new Cube(50, 50);
            //       myShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            cannonballs.clear();
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
                    case "CANNONBALL":
                        cannonballs.add(new Cube(x, y));
                        break;

                }
            }

            if (barrels.isEmpty()) {
                target = enemyShips.get(0).cube;
            }

            List<Ship> allShips = new ArrayList<>();
            allShips.addAll(myShips);
            allShips.addAll(enemyShips);

            // Befehle für jedes eigene Schiff aus der Liste
            for (Ship myShip : myShips) {

                String collision = myShip.preventCollsion(allShips);

                if (!collision.equals("")) {
                    System.out.println(collision);
                    continue;
                }

                if (preventCannonball(myShip) && myShip.speed != 0 || mineAhead(myShip)) {
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

                // schieße auf feind in der nähe
                if (!myShip.isReloading) {
                    String order = myShip.getShootingOrder(enemyShips, myShips, barrels, mines);
                    if (order != null) {
                        myShip.isReloading = true;
                        System.out.println(order + " HAIL TO THE KONG!");
                    }
                } else {
                    myShip.isReloading = false;
                }

                if (!myShip.isReloading) {
                    System.out.println("MOVE " + target.toString() + " RUM IS WHAT WE NEED!");
                }
            }
        }
    }


    public static boolean mineAhead(Ship s) {
        for (Cube mine : mines) {
            //y : 1 and 4 | x: 2 and 5 | z: 0 and 3
            if (s.speed == 0) {
                return false;
            }

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

    String preventCollsion(List<Ship> allShips) {
        if (this.speed == 0) {
            for (Ship otherShip : allShips) {
                if (!this.equals(otherShip)) {
                    Cube futruePosMyShip = this.futurePosition(2, 1);
                    Cube frontOtherShip = otherShip.futurePosition(1, 1);
                    Cube midOtherShip = otherShip.cube;
                    Cube backOtherShip = otherShip.futurePosition(-1, 1);
                    Cube blockingFuturePos = otherShip.futurePosition(1, 2);
                    if (futruePosMyShip.equals(frontOtherShip) || futruePosMyShip.equals(midOtherShip) || backOtherShip.equals(futruePosMyShip) || blockingFuturePos.equals(futruePosMyShip)) {
                        return "STARBOARD";
                    }
                } else {
                    if (!(this.futurePosition(1, 1)).outOfArena()) {
                        return "FASTER";
                    }
                }
            }
        }
        return "";
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

    String getShootingOrder(List<Ship> enemyShips, List<Ship> myShips, Set<Cube> barrels, Set<Cube> mines) {
        Cube cannon = futurePosition(1, 1);

        Ship targetShip = this;  // die IDE meckert, wenn targetShip nicht sicher initialisiert wird
        int targetPrio = 0;
        for (Ship enemyShip : enemyShips) {
            int shipPrio = 0;
            Cube futurePosition = enemyShip.futurePosition();
            int enemySpeed = enemyShip.speed;

            if (futurePosition.distanceTo(cannon) == 1) {
                shipPrio = 1000;
                System.err.println("Sicherer treffer!  - ?");
            } else if (futurePosition.distanceTo(cannon) <= 4) {
                shipPrio = 20;
            } else if (futurePosition.distanceTo(cannon) <= 7) {
                shipPrio = 12;
            } else if (futurePosition.distanceTo(cannon) <= 10) {
                // shipPrio = 7;
                shipPrio = 5;
            }

            if (enemySpeed == 0) {
                shipPrio *= 2;
            } else {
                shipPrio /= enemySpeed;
            }

            int roundsToTarget = ((enemyShip.distanceTo(cannon) + 1) / 3) + 1;
            Cube possibleTarget = enemyShip.futurePositionInNTurns(roundsToTarget);
            boolean isThereAFriendlyShip = shipDetection(myShips, roundsToTarget, possibleTarget);

            if (isThereAFriendlyShip) {
                System.err.println("POSSIBLE FRIENDLY FIRE DETECTED!");
                //        myShips.get(20);
            }

            if (shipPrio > targetPrio && !enemyShip.futurePositionInNTurns(roundsToTarget).outOfArena() && !isThereAFriendlyShip) {
                targetShip = enemyShip;
                targetPrio = shipPrio;
            }
        }

/*        // shoot barrels:
        Cube target = null;
        for (Cube bar : barrels) {
            int tempDist;
            if (cannon.distanceTo(bar) <= 4) {
                int distanceToEnemy = 100;
                int distanceToFriend = 100;
                for (Ship enemy : enemyShips) {
                    tempDist = enemy.futurePosition().distanceTo(bar);
                    if (tempDist < distanceToEnemy) {
                        distanceToEnemy = tempDist;
                    }
                }
                for (Ship friendShip : myShips) {   // höhöhö "FriendShip" ^^
                    tempDist = friendShip.futurePosition().distanceTo(bar);
                    if (tempDist < distanceToFriend) {
                        distanceToFriend = tempDist;
                    }
                }
                if (distanceToEnemy < distanceToFriend && distanceToEnemy > 2) {
                    target = bar;
                    break;
                }
            }
        }

// Shoot Mines
        for (Cube mine : mines) {
            if (cannon.distanceTo(mine) <= 4) {
                for (Ship enemy : enemyShips) {
                    if (enemy.futurePosition(2, 1).distanceTo(mine) <= 1) {
                        target = mine;
                        System.err.println("SHOOT MINE");
                        //             myShips.get(20);
                        break;
                    }
                }
            }
        }
        if (target != null) {
            System.err.println("SHOOT BARREL");
            //      myShips.get(20);
            return ("FIRE " + target.toString());
        }*/

        if (targetShip != this) {
            Cube futurePos = targetShip.futurePositionInNTurns((targetShip.distanceTo(cannon) + 1) / 3 + 1);
            return ("FIRE " + futurePos.toString());
        }
        return null;
    }


    private boolean shipDetection(List<Ship> myShips, int roundsToTarget, Cube possibleTarget) {
        if (roundsToTarget <= 2) {
            for (Ship myShip : myShips) {
                if (myShip.futurePosition(1, (-1 + roundsToTarget * myShip.speed)).equals(possibleTarget)
                        || myShip.futurePosition(1, (roundsToTarget * myShip.speed)).equals(possibleTarget)
                        || myShip.futurePosition(1, (1 + roundsToTarget * myShip.speed)).equals(possibleTarget)) {
                    return true;
                }
            }
        }
        return false;
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

    boolean outOfArena() {
        int x = this.toCoord().x;
        int y = this.toCoord().y;

        if (x > 22 || x < 0) {
            return true;
        }
        return y > 20 || y < 0;
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
