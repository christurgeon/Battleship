Large test cases were not included for the purpose of memory conserving and triviality.

I could test my board visually when the board size was small. (e.g. 50 x 50 or less)
I ensured that 1, 2, 4, 8, 16, ... threads all produced the same output for a Game of Life
grid for smaller files. I then made them large enough so that they were bigger then the 
pixel dimensions of my machine. I looked at the blocks that each Thread was allocated to compute
and I utilized diff comparisons between grids produced by different thread numbers to ensure 
that they were identical. 

 ---------------------------------------------------------------------------------------

STRATEGY FOR GROUPING...

If the dimensions of the grid are less than a machines pixel dimensions, then I compute the size of the 
pixel rectangles and know that each of those represents one Cell... so NO packing for this case...

If the dimensions of the grid are larger than the pixel dimensions of the current machine, then I get 
the minimum number of pixels that each pixel must hold in the vertical and horizontal direction. 
I then get the (mod) remaining number of pixels and distribute those to some cells to ensure that 
the entire board is represented in the pixels.


