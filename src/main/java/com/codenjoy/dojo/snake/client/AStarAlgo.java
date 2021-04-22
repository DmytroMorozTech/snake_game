package com.codenjoy.dojo.snake.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codenjoy.dojo.snake.client.DoublyLinkedList.Node;

public class AStarAlgo {
    private final int dimX;
    private final int dimY;
    private final int[][] board;
    private Point head;
    private Point goal;
    private Point stone;

    private DoublyLinkedList<Point> path = new DoublyLinkedList<>();
    private SinglyLinkedList snake = new SinglyLinkedList();

    public AStarAlgo(int dimX, int dimY) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.board = new int[dimY][dimX];
    }

    private boolean isOnBoard(Point p) {
        return p.getX() >= 1 && p.getY() >= 1 && p.getX() < dimX - 1 && p.getY() < dimY - 1;
    }

    private Point move(Point pt, int dx, int dy) {
        return new PointImpl(pt.getX() + dx, pt.getY() + dy);
    }

    private Stream<Point> neighbours(Point pt) {
        return Stream.of(
                move(pt, -1, 0),
                move(pt, +1, 0),
                move(pt, 0, +1),
                move(pt, 0, -1)
        )
                .filter(this::isOnBoard);
    }

    public Optional<Point> trace(Point head,
                                 Point goal,
                                 SinglyLinkedList<Point> snakeLL,
                                 Point stone) {
        this.head = head;
        this.goal = goal;
        this.snake = snakeLL;
        this.stone = stone;

//        printSnakeCoords();

        return doTraceAStar();
    }

    // PATHFINDING ALGORITHM
    public Optional<Point> doTraceAStar() {
        path = new DoublyLinkedList<>();
        path.addFirst(head);

        ArrayList<Point> forbiddenPoints = new ArrayList<>();
        forbiddenPoints.add(stone);

        boolean continueToSearch = true;
        int steps = 1;

        while (continueToSearch) {
            Node<Point> current = path.getHeader();
            Point nextPathPoint = getClosest(current.getElement(), forbiddenPoints, steps);
            while (nextPathPoint == null) {
                forbiddenPoints.add(current.getElement());
                // if the function getClosest did not manage to find some suitable Point nextPathPoint
                // then we should add this Point to forbiddenPoints array
                // and not consider it when trying to build an alternative path

                current = current.getNext();
                // in this case current is the very first Point of path, that was calculated at this moment;
                // When we run getNext() method on current this means that we move 1 step BACK
                // in currently calculated path.
                // This is, so to say, SAFETY BACKUP mechanism: if snake is unable to find path from this point
                // it goes 1 step back and tries to find path from that previous point.
                steps -= 1;
                if (steps < 1) {  // there is no way to find route
                    System.out.println("exited");
                    continueToSearch = false;
                    break;
                }
                nextPathPoint = getClosest(current.getElement(), forbiddenPoints, steps);
            }

            // if the new Point for path was found (nextPathPoint) then we should add it to calculated new path.
            // and update steps counter +=1
            if (nextPathPoint != null && current != null) {
                path.newHead(nextPathPoint, current);
                steps += 1;
                if (nextPathPoint.itsMe(goal))
                    // if our path has reached the goal (apple),
                    // then we should stop searching for the path, because it was just found
                    continueToSearch = false;
            }
        }
//        System.out.println("Finished");
//        System.out.println("Path Size " + steps);

        return Optional.of(path.getTrailer().getPrev().getElement());
    }

    /**
     * @param current         -> the Point that the function considers as current for calculating the next Point in path;
     * @param forbiddenPoints -> an ArrayList of Points that contains all the Points, that are not allowed to
     *                        be included into the path (e.g. Point stone; other Points that proved to be
     *                        useless for finding efficient path to the goal (apple);
     * @param steps           ->  number of steps that our "virtual snake" has taken during the process of path modelling,
     *                        starting from the initial position of snake's head (before calculating any path).
     * @return Point -> a single Point that was found among neighbours of Point current, that satisfies all filtering
     * conditions (it doesn't collide with body of the snake; it doesn't collide with currently
     * calculated path for the snake; it doesn't collide with all forbiddenPoints, that were discovered),
     * and it has the shortest distance to the goal.
     */
    public Point getClosest(Point current, ArrayList<Point> forbiddenPoints, int steps) {
        Point closest = null;

        List<Point> verifiedNeighbours = neighbours(current)
                .filter(
                        potentialHead -> !snakeCollision(potentialHead, steps) &&
                                !pathCollision(potentialHead) &&
                                !forbiddenPointsCollision(potentialHead, forbiddenPoints))
                .collect(Collectors.toList());

        for (Point p : verifiedNeighbours) {
            if (closest == null) {
                closest = p;
            } else if (p.distance(goal) <= closest.distance(goal)) {
                closest = p;
            }
        }

        return closest;
    }

    /**
     * @param potentialHead -> a Point that we want to examine on whether it is an appropriate next head for the snake
     *                      (i.e. an appropriate next Point in the path);
     * @param steps         -> the number of steps that the "virtual snake" has moved forward during the process of path modelling;
     * @return boolean value, that answers the question:
     * does the Point potentialHead overlaps with the body of the snake, that it will have in N steps
     * (it will be shorter by N steps from it's tail);
     */
    public boolean snakeCollision(Point potentialHead, int steps) {
        SinglyLinkedList.Node<Point> node = snake.getHead();
        int count = 0;

        // why do we traverse the snake's body only until node with (count < snake.size() - steps)  ?
        // Because the tail of the snake will no longer be in the same position after N steps;
        // It will move forward by N steps. And we should consider this issue.
        // These N nodes should migrate somewhere. If snake became shorter at the end, then it should
        // become longer at the start! That's right. These "new nodes", that represent the path,
        // will be added to the path variable (DoublyLinkedList).
        while (node != null && count < snake.size() - steps) {
//            System.out.println("Checking for snake collision Point with coords:");
//            System.out.printf("X:%d Y:%d \n", potentialHead.getX(), potentialHead.getY());
            if (potentialHead.itsMe(node.getElement())) {
                return true;
            }
            node = node.getNext();
            count++;
        }
        return false;
    }

    /**
     * @param potentialHead -> a Point that we want to examine on whether it is an appropriate next head for the snake
     *                      (i.e. an appropriate next Point in the path);
     * @return boolean value, that answers the question:
     * does the Point potentialHead overlaps with currently calculated path.
     */
    public boolean pathCollision(Point potentialHead) {
        DoublyLinkedList.Node<Point> node = path.getHeader();
        while (node != null) {
            if (potentialHead.itsMe(node.getElement())) {
                return true;
            }
            node = node.getNext();
        }
        return false;
    }

    /**
     * @param potentialHead -> a Point that we want to examine on whether it is an appropriate next head for the snake
     *                      (i.e. an appropriate next Point in the path);
     * @return boolean -> value, that answers the question:
     * does the Point potentialHead overlaps with some forbidden Points.
     */
    public boolean forbiddenPointsCollision(Point potentialHead, ArrayList<Point> forbiddenPoints) {
        for (int i = 0; i < forbiddenPoints.size(); i++) {
            Point forbiddenPoint = forbiddenPoints.get(i);
            if (potentialHead.itsMe(forbiddenPoint)) {
                return true;
            }
        }
        return false;
    }


    public void printSnakeCoords() {
        System.out.println("Current snake:");

        SinglyLinkedList.Node<Point> node = snake.getHead();
        System.out.print("[");
        while (node != null) {
            System.out.printf(" [%d,%d],", node.getElement().getX(), node.getElement().getY());
            node = node.getNext();
        }
        System.out.print("]\n");
        System.out.printf("Length of snake: %d \n", snake.size());
    }

}
