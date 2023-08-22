# Developer Info

All copyright reserved @Nimesh Vasani 

[GitHub Profile](https://github.com/NimeshVasani)

[Linked-In](https://www.linkedin.com/in/nimesh-vasani-99b642154/)

[Stack Overflow](https://stackoverflow.com/users/16579306/nimesh-vasani)

# About The App

Save and Share Your beautiful memories with your friends and family. 
The family Time app also provides the live locations of various families you have joined. 
You can also have group conversations.
Users can create families add members and many more.

To Update the [google map Api](https://console.cloud.google.com) key in a project to make it work follow the step: goto `"manifest" and update "metadata for google map API"`

```xml
 <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_API_KEY" />
```

# Architecture & Work-Flow

This App uses [Firebase](https://firebase.google.com) for storing and retrieving data. 
### [Realtime Database](https://firebase.google.com/docs/database)
    Saving Chats and live location.
### [Firestore](https://firebase.google.com/docs/firestore) 
    Storing Users, Posts, and Families/Groups' information.
### [Firebase Authentication](https://firebase.google.com/docs/auth) 
    Authenticate the users with email, and/or phone.
### [Cloud Storage](https://firebase.google.com/docs/storage) 
    Storing the images, videos, and files shared by users.
    
    
    
