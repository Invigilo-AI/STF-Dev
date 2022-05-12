# STF-Dev
This README file is in development for the communication between the m5stickc device and the app, as well as connecting the app to the aws cloud to populate a dashboard for the user

Steps to Follow:
-> App
    1) The UI/UX of the app is not the main priority, the main priority is the functionality
    2) The app must be able to pair with the m5stick-c upon discovery
    3) Once paired, the app must be recieve data from the m5stick-c
    4) Upon an event (defined as a slip, a trip or a fall through the stickc itself), the app must use the phone that it is running on to get location (GPS) and Height (Barometer)
    5) Once this entire data has been collected, the app must send the data to the cloud ec2 instance running

-> Website
    1) The website is to be built using python-streamlit
    2) A simple dashboard, no need for user authentication and tokenisation as of now at the very least
    3) Upon recieving the data, the instance must convert the data into a csv file
    4) This data can be then used to automatically populate the dashboard with values
        -> What will the dashboard show? Any graphs? Any tables? [@Sylvester]
