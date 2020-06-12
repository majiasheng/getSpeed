# TrackMe
## Goal:
To track user itinerary and provide prediction. 

Description:
When user presses the “Start” button, the app will start to track user’s location trail. It will send periodic (3 seconds interval) location update requests to the GPS, and will connect each location update latitude and longitude pair to form a trail. 

When the user arrives at the destination, s/he will simply press the “Stop” button (the same button as before) to stop the tracking session. The application will then record the trail into history.

Based on the history, each time the user starts at a location, the application will show prediction for the possible trails by displaying them in the map with different colors. The user can click on the destination marker to get prompt for direction from google map’s app.

### Trajectory Prediction:
It is a fact that most people’s daily trajectory (activity route) pattern is the same more or less. Especially for work days, adults go to workplace and students go to school from home, and then back. As for weekends, the general pattern may be broader than that of the work days. But as data accumulates, the prediction will be more accurate.

Based on the given fact, this application predicts user’s trajectory based on time and origin of travel. The description of the prediction algorithm is as follows:
Define the theoretical deviation range/radius of origin, R, to be 860m away from the actual origin.
R is taken primarily based on my living experience in NYC. The place where I park can range from my home parking lot to at most three blocks away (see figure 1). The spec of a standard block is 80m x 274m for manhattan. [1] 

(* For simplicity, the distance between two blocks are omitted for the calculation of the diagonals. The diagonals is rounded to the ceiling of the tenths place.)

```Define the theoretical deviation range of departure time, T, to be 2 hours (7200*1000 milliseconds) away from the actual departure time.
Define database schema:
Origin (LatLng), Destination(LatLng), Trail(List<LatLng>), DepartureTime(long), ArrivalTime(long)
Given a new origin, O, as the center, and departure time, DT:
		For each origin o in database:
If the distance between O and o is within ±R, then compare DT with T:
if DT is in ±1hr range of T, then:
From database: 
Extract list of entries/rows whose departure time is within T±1hr and whose origin’s distance to O is ±R
Return the list as prediction
else, extract list of entries/rows whose origin’s distance to O is ±R
Return the list as prediction
Finally, return the first entry as the prediction, or null if the list is empty```

(* The actual implementation in the code is slightly different because distance between two locations cannot be calculated in the query. Instead, the implementation queries the database for a list of histories that are within the time range. Then each of the results is checked whether it is within the radius. And the first match is returned.)

End note on algorithm: 
This algorithm is fairly trivial, heavy assumptions are placed. That is, the user must be on a strictly regular schedule. 

### Experiments/Proofs (Discussion) of Correctness:
First of all, R and T are theoretical, and they are not empirically backed with large amount of samples. Therefore, R and T provide a rough prediction for a very regular schedule. One scenarios where this algorithm won’t provide close prediction is when the user starts his/her schedule three hours away from the predefined error range. 

Nevertheless, if the user has a regular schedule and itinerary, the app can provide fairly accurate prediction:


### Future Development:
Prediction algorithm can to be improved with machine learning. The current algorithm for retrieving entries of prediction is not efficient and not very accurate: 
The calculation of distance between new origin and origins in the database cannot be calculated directly in the query.
The departure time must be very regular.
Ask for confirmation before clearing history.
Refine allowable errors.
Move predefined constants to a configuration file.
Add an app menu to hold options such as:
Clear history
Delete a particular trail(s)
Show histories and project selected history to map
Etc. 
Note:
The radius of origin can be adjusted for different person based on their living experience.
References:
[1] https://en.wikipedia.org/wiki/City_block

