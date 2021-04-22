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


import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.RandomDice;

import java.util.*;

import com.codenjoy.dojo.services.Point;

/**
 * User: Dmytro Moroz
 * Link to play the game:
 * http://46.101.224.244/codenjoy-contest/board/player/9edms0fr5h6k57i21xh0?code=8627537874525682193
 */
public class YourSolver implements Solver<Board> {

    private Dice dice;
    private Board board;

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    @Override
    public String get(Board board) {
        this.board = board;

        Point head = board.getHead(); // starting point for movement
        Point apple = board.getApples().get(0);// finish point for movement
        Point stone = board.getStones().get(0); // stone - obstacle

        SinglyLinkedList<Point> snakeLL = board.getSnakeAsLinkedList();

        AStarAlgo aStarAlgo = new AStarAlgo(15, 15);
        Optional<Point> nextStep = aStarAlgo.trace(
                head,
                apple,
                snakeLL,
                stone
        );

//        System.out.printf("nextStep: %s \n", nextStep.get());
        String result = getDirection(head, nextStep.get());
        return result;
    }

    private String getDirection(Point head, Point next) {
        if (head.getX() == next.getX() && head.getY() < next.getY()) return Direction.UP.toString();
        if (head.getX() == next.getX() && head.getY() > next.getY()) return Direction.DOWN.toString();
        if (head.getY() == next.getY() && head.getX() > next.getX()) return Direction.LEFT.toString();
        return Direction.RIGHT.toString();
    }


    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                "http://46.101.224.244/codenjoy-contest/board/player/9edms0fr5h6k57i21xh0?code=8627537874525682193",
                new YourSolver(new RandomDice()),
                new Board());
    }

}
