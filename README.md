# cod
It's all about Cost of Delay. Currently this application can help you visualizing the effects on cost of delay for different sequencing of projects.

##Requirements
* Java 8 (or higher)
* Maven (if you would like to build it)

##Build it
* mvn clean install

##Run it
### From you maven build
* java -jar target/cod-0.1.1-SNAPSHOT.jar

### From binary download
* java - jar cod-0.1.RELEASE.jar


##A small documentation
You can load some sample data via File - Load Input Sample.

Calculate sequence - calculates the best sequence to develop the given features. The current algorithm assumes you run all the 
project linearly and not in parallel and all features have to be done.

The resulting sequence is shown in the sequence field. It displays the feature names, separted by colon 
(therefore it's currently best to have short project names ;-) ). Below the sequence the calculated total cost of delay is shown. 
Total cost of delay is the accumulated cost of delay for all weeks and all features.

Via -show chart- you can have a look on the cost of delay distribution:
* the x-axis shows the weeks and for every week the feature being worked on
* the y-axis displays the feature cost of delay, stacked with their delay value

If you play with the sequences (via changing the sequence in the sequence field manually) you can display different 
cost of delay outcomes.

For every calculated sequence you can check all combination results in the file display in the footer. 
Currenlty the calculation runs all possible combinations (kind of a brute force). As this means (number of features)! I would 
not recommend going above 10 features - as this already means 3.628.800	combinations ;-) 
(I ran the program with 12 features and it took around 30' to calculate in my machine). 
Better algorithms will be implemented in future ... for now - take care ;-)
.

Via File -- Save/Load you can store/load your input data. 

You can edit your input data directly in the features table too.

Finally clear input erases your input data.

###Feature Input
For every feature you need to provide:
* name (best a very short one)
* cost of delay/week
* the feature development duration (how long it takes to implement that feature)

In case the cost of delay urgency profile shows a specific cost of delay starting date or period you can specify that using 
either a start/end week combination or start date/end date combination (format dd.MM.yyyy).

Via Add Feature your input gets added to the feature table.

