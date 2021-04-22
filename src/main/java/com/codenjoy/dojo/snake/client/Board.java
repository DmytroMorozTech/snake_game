package com.codenjoy.dojo.snake.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snake.model.Elements;

import java.util.Arrays;
import java.util.List;

public class Board extends AbstractBoard<Elements> {

    @Override
    public Elements valueOf(char ch) {
        return Elements.valueOf(ch);
    }

    public List<Point> getApples() {
        return get(Elements.GOOD_APPLE);
    }

    @Override
    protected int inversionY(int y) {
        return size - 1 - y;
    }

    public Direction getSnakeDirection() {
        Point head = getHead();
        if (head == null) {
            return null;
        }
        if (isAt(head.getX(), head.getY(), Elements.HEAD_LEFT)) {
            return Direction.LEFT;
        } else if (isAt(head.getX(), head.getY(), Elements.HEAD_RIGHT)) {
            return Direction.RIGHT;
        } else if (isAt(head.getX(), head.getY(), Elements.HEAD_UP)) {
            return Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }

    public Point getHead() {
        List<Point> result = get(
                Elements.HEAD_UP,
                Elements.HEAD_DOWN,
                Elements.HEAD_LEFT,
                Elements.HEAD_RIGHT);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public List<Point> getBarriers() {
        List<Point> result = getSnake();
        result.addAll(getStones());
        result.addAll(getWalls());
        return result;
    }

    public List<Point> getSnake() {
        Point head = getHead();
        if (head == null) {
            return Arrays.asList();
        }
        List<Point> result = get(
                Elements.TAIL_END_DOWN,
                Elements.TAIL_END_LEFT,
                Elements.TAIL_END_UP,
                Elements.TAIL_END_RIGHT,
                Elements.TAIL_HORIZONTAL,
                Elements.TAIL_VERTICAL,
                Elements.TAIL_LEFT_DOWN,
                Elements.TAIL_LEFT_UP,
                Elements.TAIL_RIGHT_DOWN,
                Elements.TAIL_RIGHT_UP);
        result.add(0, head);
        return result;
    }

    public boolean isGameOver() {
        return getHead() == null;
    }

    @Override
    public String toString() {
        return String.format("Board:\n%s\n" +
                        "Apple at: %s\n" +
                        "Stones at: %s\n" +
                        "Head at: %s\n" +
                        "Snake at: %s\n" +
                        "Current direction: %s",
                boardAsString(),
                getApples(),
                getStones(),
                getHead(),
                getSnake(),
                getSnakeDirection());
    }

    public List<Point> getStones() {
        return get(Elements.BAD_APPLE);
    }

    public List<Point> getWalls() {
        return get(Elements.BREAK);
    }

    /**
     * @return SinglyLinkedList<Point> -> all Points of snake's body, that were sequentially wrapped into a
     * SinglyLinkedList data-structure.
     * This enables us to "walk through" the body of the snake, taking into account every turn.
     */
    public SinglyLinkedList<Point> getSnakeAsLinkedList() {
        Point headOfSnake = getHead();
        SinglyLinkedList<Point> snakeLinkedList = new SinglyLinkedList<>();
        snakeLinkedList.addFirst(headOfSnake);

//        System.out.printf("Current direction of SNAKE: %s \n", getSnakeDirection());

        Direction currentDirection = getSnakeDirection().inverted();
//        System.out.printf("INVERTED Current direction of SNAKE: %s \n", currentDirection);

        // Why do we have to invert snakeDirection?
        // Because we need to traverse through all Points of snake's body from head to tail.
        // For example: if current snake's direction is LEFT, then we should start moving to the RIGHT.
        // This will lead us either to the next turn of snake's body ( '╗','╔','╝','╚'),
        // or to horizontal body fragment('═'), or to vertical body fragment('║'),
        // or to the snake's tail ('╙','╘','╓','╕').
        // Having understood this concept, now we can parse the body of the snake from the Board and wrap
        // it into a SinglyLinkedList, where the sequence of all nodes from head to tail will be preserved.

        int counter = getSnake().size() - 1;
        // counter equals to the length of snake minus 1.
        // We subtract 1(node), because head of the snake was already added to the snakeLinkedList.

        Point current = headOfSnake;
        for (int i = 0; i < counter; i++) {
//            System.out.println("Entered FOR LOOP for finding the body of snake! >>>>>>>>>");

            switch (currentDirection.value()) {
                case 0: { // Direction.LEFT
                    current = new PointImpl(current.getX() - 1, current.getY());
                    break;
                }
                case 1: { // Direction.RIGHT
                    current = new PointImpl(current.getX() + 1, current.getY());
                    break;
                }
                case 2: { // Direction.UP
                    current = new PointImpl(current.getX(), current.getY() + 1);
                    break;
                }
                case 3: { // Direction.DOWN
                    current = new PointImpl(current.getX(), current.getY() - 1);
                    break;
                }
            }
            snakeLinkedList.addLast(current); // add one more Point to the snake Linked List

            Elements elem = getAt(current);
            // we find out what kind of snake's element is in "current" variable.
            // This is important, because it'll allow us to define the subsequent direction of snake's body.

            // if we've reached the tail element, then we should stop performing the loop and return snakeLinkedList.
            if (isSnakeTail(elem)) return snakeLinkedList;

            currentDirection = getNextSnakeDirection(currentDirection, elem);
        }
        return snakeLinkedList;
    }

    private Direction getNextSnakeDirection(Direction currentDirection, Elements elem) {
//        System.out.println("************************************************");
//        System.out.println("Arguments passed into getNextSnakeDirection(): ");
//        System.out.println("currentDirection: " + currentDirection);
//        System.out.println("elem: " + elem);
//        System.out.println("************************************************");

        Direction newDirection = null;

        if ((currentDirection == Direction.LEFT || currentDirection == Direction.RIGHT)
                && elem == Elements.TAIL_HORIZONTAL)
            newDirection = currentDirection;

        if ((currentDirection == Direction.UP || currentDirection == Direction.DOWN)
                && elem == Elements.TAIL_VERTICAL)
            newDirection = currentDirection;

        if (elem == Elements.TAIL_LEFT_UP) {
            newDirection = currentDirection == Direction.DOWN ? Direction.LEFT : Direction.UP;
        }

        if (elem == Elements.TAIL_LEFT_DOWN) {
            newDirection = currentDirection == Direction.RIGHT ? Direction.DOWN : Direction.LEFT;
        }

        if (elem == Elements.TAIL_RIGHT_DOWN) {
            newDirection = currentDirection == Direction.UP ? Direction.RIGHT : Direction.DOWN;
        }

        if (elem == Elements.TAIL_RIGHT_UP) {
            newDirection = currentDirection == Direction.LEFT ? Direction.UP : Direction.RIGHT;
        }

        return newDirection;
    }

    /**
     * @param elem -> an element, that belongs to enum Elements.
     * @return boolean -> whether the given elem represents the tail of the snake.
     */
    private boolean isSnakeTail(Elements elem) {
        boolean isSnakeTail = (elem == Elements.TAIL_END_DOWN || elem == Elements.TAIL_END_LEFT
                || elem == Elements.TAIL_END_UP || elem == Elements.TAIL_END_RIGHT);
        return isSnakeTail;
    }

}
