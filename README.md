## This project was performed by Dmytro Moroz, as a task within a full-stack course at Dan-It (Kyiv).

# Approach to creating snake AI:
- A modified version of the A-star algorithm was used for pathfinding.

How does algorithm work?
1. Snake's body is obtained from class Board, method getSnakeAsLinkedList().
2. The snake tries to find optimal path to the apple and at the same time it considers all obstacles (itself, stone, borders of the game-board);
3. In case pathfinding algorithm leads to a dead end, it can back up and make another attempt to find appropriate path.
4. When the optimal path is found, snake starts following it.

- Several data-structures were internally created for managing the current state of snake and currently found path:
  * SinglyLinkedList<E> -  this class was used for managing the body of the snake.
  * DoublyLinkedList<E> - this class was used for managing the path. 
    <br>**Both of these generic classes were instantiated using Point as type parameter.

<hr>
General info:
<hr>
We need to present the body of the snake using some suitable data-structure that would enable us to manipulate and model the movement of the snake. Modelling is very important in terms of searching for the optimal path from snake's head to the goal (apple). I decided to use class Board, that was provided with initial task, to get the snake presented in the form of SinglyLinkedList<Point>.<br>
  In class Board there is a method getSnake(), that returns us List<Point>. There is not so much that we can do with this List when it comes to modelling future steps of snake and modelling future location of snake's body in n steps. The reason is that Points are not properly ordered in this List<Point>.<br>
 So, a new method was developed in class Board to tackle this issue:<br>
 <b>public SinglyLinkedList<Point> getSnakeAsLinkedList()</b><br>
   This data-structure, that describes current positioning of snake's body, is updated every second, when game-server sends us new info. After it is updated, we pass it as an input to the tracing method.<br>
<hr>

### More detailed comments you may find within the code.
   
   

