# Hex Fractal Region Generator
The [Grown Biomes](http://mc-server.xoft.cz/docs/Generator.html#biome.grown) method, but implemented using a hex grid instead.

# Images

![Steps](images/hexsteps.gif?raw=true)

![Bigger Area](images/hexbigger.png?raw=true)

# How it works

1. Create a square array of side length (size * 2^steps)+1. This represents a diagonally-compressed square (rhombus) that maps to the hexagonal lattice.
2. Populate the initial values, spaced out by stride=(2^steps).
    * Care could be taken in this step to ensure that certain region types don't border each other. But in this implementation, they are just assigned randomly.
3. Between each pair of defined values, set a new value that is randomly chosen between the two. These correspond to the midpoints of the triangular edges.
4. Repeat #3 until every cell is assigned a value.
5. Sample the grid using the skew transform from 2D simplex noise.
    * The first implemented sampler just finds the closest hexagon.
    * The second implemented sampler (shown in demo) considers identically-valued cells together, to straighten the edges.

![Steps](images/hexsteps.png?raw=true)
Step order is: Red, Orange, Yellow, Green. Rhombic section is highlighted, with the surrounding area grayed out.

# Extensions

## Infinite Grid
It would be straightforward to extend this to an infinite grid. Divide the grid into compressed-square / rhombic patches, that overlap on just the padding (the +1 in step 1). Maintain a cache of patches. Have the sampler identify the current patch, then load or generate it. Discard old / infrequently used patches from the cache when appropriate, as part of this step. Replace java.util.random with a hash function that always produces the same value for a given coordinate.

## Higher Dimensions
It could be generalized to higher dimensions, using either the A or A* lattice as a generalization of the triangular lattice. See: Simplex or OpenSimplex noise.

## Centers not edges
This approach iteratively populates the midpoints of the edges of triangles. An alternative approach could populate the centers of the triangles instead, chosen by the three corners. This is a more complex case, and would require more padding to make all necessary data available. It may also be more difficult to generalize to higher dimensions. But could be worth exploring the different properties it has, if any. The below image illustrates these steps with the same coloring as the image shown above.

![Midpoint Steps](images/hexstepstri.png?raw=true)

# Is it better?

Is this better than the square grid approach? Hard to say. I do tend to notice hexagonal bias less readily than square bias. Hexagonal grids also have a number of nice properties, such as having more angles of symmetry, and being both the optimal packing and covering of 2D space. I like the result better than the square approach, but I think it could be improved further. Perhaps the "Centers" approach would provide even better results, or perhaps some bias control measures could be employed.

###### Source for hex grid image used as a base for some illustrations: https://svgsilh.com/image/156568.html
