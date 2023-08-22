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

Built with  : [MVVM](https://developer.android.com/topic/libraries/architecture/viewmodel?gclid=CjwKCAjw5dqgBhBNEiwA7PryaEGGNXBuF_269i5vAml9SedixRgYXYfktdB8NOZm__qJWmdN6hpUahoC2IQQAvD_BwE&gclsrc=aw.ds#kotlin_1),[Firebase](https://www.googleadservices.com/pagead/aclk?sa=L&ai=DChcSEwjs792eoPGAAxUCyuMHHTFICUUYABAAGgJ5bQ&gclid=Cj0KCQjwuZGnBhD1ARIsACxbAVifgQbodRIrGKmnYRV5z2H7BCu_BXw827Mi6aKyG5EtHXLPD_BfbyQaAj7BEALw_wcB&ohost=www.google.com&cid=CAESbOD2Ut8Gm6K5BgGftp-5gsV4VHfQXaWHFzYLMlT_gBLgfgPD0lGquSYM4462U84K50A4xUaN16_lkPVVIiMdOxrGDb7069PCsffqXI6HRs558AwEYQSe-dlhhhPn0TKyEV5EVZgXgJZo8rtSsw&sig=AOD64_3SaxRglRR1CiXsTs1DZsPZV-kqyA&q&adurl&ved=2ahUKEwjK5dWeoPGAAxXJj4kEHeD-COUQ0Qx6BAgNEAE&nis=8),[Glide](https://github.com/bumptech/glide) and [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android) with Proper [Navigation UI](https://developer.android.com/guide/navigation/navigation-getting-started).

![alt text](https://github.com/NimeshVasani/Family-Time/blob/890ce652ceaa2f2a87a0b8b5ea9d8f9b4be18a06/snaposhots/mvvm_firebase.png)

This App uses [Firebase](https://firebase.google.com) for storing and retrieving data. 
### [Realtime Database](https://firebase.google.com/docs/database)
    Saving Chats and live location.
### [Firestore](https://firebase.google.com/docs/firestore) 
    Storing Users, Posts, and Families/Groups' information.
### [Firebase Authentication](https://firebase.google.com/docs/auth) 
    Authenticate the users with email, and/or phone.
### [Cloud Storage](https://firebase.google.com/docs/storage) 
    Storing the images, videos, and files shared by users.
    
    
    
