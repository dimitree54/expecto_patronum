# Expecto patronum project

# Ideas for advertising:
1. It is like Uber, but for free
2. It is like Avito, but international
3. It is like Tinder, but you help each other

# Setting up backend
1. Create an account
2. Create a project
3. Create a cluster (free)
4. Create the database "telegram" (Browse Collections -> Add my own data) with 3 collections: users, wishes, tags
5. Fill `mongodb.host` in application.properties with values from the MongoDB page (the Connect button on your cluster). 
   It will request to specify security options (ip whitelist and user/password).
6. Create environment variables `MONGODB_USER` and `MONGODB_PASSWORD` from 5.