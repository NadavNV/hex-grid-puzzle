# hex-grid-puzzle
A program that solves the following riddle:

![Image of unsolved riddle](http://i68.tinypic.com/24osy91.jpg)

The goal is to fill the grid in such a way that subsequent numbers are adjecnt, e.g. 4 needs to be adjacent to
both 3 and 5.

Currently this program can solve from a specific initial state as shown in the image above using two algorithms. One attempts to
place the smallest number that still isn't on the board next to its predecessor, using a simple DFS search.

Since filling the board in a way that fulfils the puzzle's condition is equivalent to finding a path that covers the board and
does not repeat itself, the other algorithm attempts to place the smallest number that still isn't on the board based on shortest
path to the next highest number that is on the board, thus essentially finding a series of partial paths. Through the limited
testing I've done so far, this method seems more efficient than DFS in every situation.

Features that I plan to add in the future are the ability to generate riddles of this kind, and to let the user input a starting
board state for the program to attempt to solve.

Currently the program also creates a log file called "log.txt" that displays every step the algorithm took before finding the solution.
