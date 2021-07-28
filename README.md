# Worldbank CLI

This application performs data ingestion
from Worldbank API and executes data queries to
print to the console the results of the top 10 countries by
population growth from 2010 to 2018 and the top 3 countries by GDP growth in the same range.

##### Is written in a pure functional way using [Cats](https://typelevel.org/cats-effect/), [doobie](https://tpolecat.github.io/doobie/), [fs2](https://fs2.io/#/) and [h2](https://www.h2database.com/html/main.html).

### How to use?

- To compile run ```./compile``` in the root folder. If by any mean I fail, run this file with ```-force``` flag
- After compiling please run ```./run --dataload``` and then ```./run --results```

**You should be able to see the output in the console**
