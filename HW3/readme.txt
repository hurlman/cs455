Mike Hurley
CS455 HW3 PC
April 2nd, 2018

All questions were answered with a single MapReduce operations, including question 7.
Custom Key and Value types were created to accomplish this goal, and special logic was put
in place to categorize and handle the different types of data in each record.

Question 7  - custom analysis.  Which individual plane travelled the most miles?  Which model of plane
made the most flights?  Which model of plane travelled the most miles?

File list:

results\Answers.md
    Answers to all 7 questions.
results\HubByYear-Graph.pdf
    Additional resource to answer Question 3.  Visualizing the data over time was the best way to
    view it in my opinion.
results\part-r-0000
    Raw MapReduce output.  The file contents are friendly enough to be read by a human.  Data is sorted
    and easy to read.

src\cs455\hadoop\airline\AirlineJob.java
    Main MapReduce job class.
src\cs455\hadoop\airline\AirlineMapper.java
    Map class.
src\cs455\hadoop\airline\AirlineCombiner.java
    Combiner class.
src\cs455\hadoop\airline\AirlineReducer.java
    Reduce class.

src\cs455\hadoop\types\KeyType.java
    Key type for all MapReduce operations.  Contains a data category and value.
src\cs455\hadoop\types\FieldType.java
    Data categorization enum.  Provides the category for the KeyType.
src\cs455\hadoop\types\IntPair.java
    Intermediate value type for MapReduce operations.  Allows for averaging using a combiner.
src\cs455\hadoop\types\RecordData.java
    Class representing each line of data in the data set.  Parses the line and provides the mapper
    with public methods to reason about the data

src\cs455\hadoop\util\Airport.java
    Class representing the airport data from the supplementary data set.
src\cs455\hadoop\util\Constants.java
    Constant strings and ints used throughout the package.  Would be better if these were in
    a config file, but it is done this way for simplicity for me.
src\cs455\hadoop\util\Dictionary.java
    Class that reads in from the supplementary data set, builds data structures of that data,
    and provides public methods to retrieve the data for given keys.
src\cs455\hadoop\util\Plane.java
    Class representing the plane data from the supplementary data set.

build.xml
    ant build file