# Homework 3 Answers
Mike Hurley
CS455
April 2nd, 2018
## Question 1
What is the best time-of-day/day-of-week/month-of-year to fly to minimize delays?
:   Time: 5 or 6 am - 1 minute average delay.
    Day: Tuesday or Saturday - 6 minute average delay.
    Month: September or October - 5 minute average delay.
## Question 2
What is the worst time-of-day/day-of-week/month-of-year to fly to minimize delays?
:   Time: 7 or 8 pm - 13 minute average delay.
    Day: Thursday or Friday - 9 minute average delay.
    Month: December - 11 minute average delay.
## Question 3
What are the major hubs (busiest airports) in the US?  List to 10.  How has this changed over the 21 year period of the data set?
:   See included file - HubByYear-Graph.pdf
    5 airports appear in the top 10 every year: 
    - Chicago O'Hare
    - William B Hartsfield-Atlanta
    - Dallas-Fort Worth
    - Phoenix Sky Harbor
    - Los Angeles International
Chicago, Atlanta, and Dallas are always the top 3.  In 2003, Atlanta passed Chicago O'Hare to become the busiest airport, increasing over 60% year-over-year.
The list was stable between 1987 and 2002, after which there were many changes in the top 10.
Denver was the 5th busiest airport for the first couple years of data, and fell out of the top 10 by the late 1990's.  It made it back in the top 10 in 2002, and steadily rose to become the 4th busiest airport by 2008.
See results file for complete list of top 10 airports for each year.
## Question 4
Which carriers have the most delays?  Report on total number of delayed flights, total number of minutes lost to delays.  Carrier with highest average delay.
:   A flight was considered delayed if the departure delay was greater than 0.
    Mesa Airlines Inc. had the highest average delay at 48 minutes.
    Delta had the most delays, with 8,064,705 delayed flights.
    Southwest had the most total minutes lost to delay, at 147,011,568.
## Question 5
Do older planes cause more delays?  Contrast on-time performance with newer planes.  Planes more than 20 years old are considered old.
:   Arrival delay was looked at to make this determination.
    New planes had many more delayed flights, but there are more new planes in circulation than old planes.  So instead I looked at average delay.
    It did not seem that old planes caused more delays than new planes.  Both had an average arrivale delay of 21 minutes.
## Question 6
Which cities experience the most weather related delays?  Please list the top 10.
:   1 Dallas-Fort Worth-TX	74898
    2 Chicago-IL	65429
    3 Atlanta-GA	54745
    4 Houston-TX	43485
    5 New York-NY	32858
    6 Covington-KY	18856
    7 Detroit-MI	18411
    8 Denver-CO	16623
    9 Newark-NJ	16564
    10 Minneapolis-MN	14846
## Question 7 
Original Analysis.  I investigated which individual planes travelled the most miles in the dataset.  Also, which model of plane travelled the most total miles, and which model made the most flights.
:   The plane with tail number N551UA gets the award for most travelled plane.  It is a Boeing 757-222, comissioned in 1992.  It travelled 24,073,793 total miles.
    The model with the most total miles is the Boeing 757-222, followed closely by the 757-232.  Both over 1.8 Billion total miles.
    The model with the third most miles is also the model with the most total flights, the Canadair CL-600-2B19.