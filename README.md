# Expecto patronum project

"Expecto patronum" is an open source application of telegram bot. 
The purpose of the application is to connect people with some tasks (wishes) with people who can do that task for free.
That application is inspired by following services:

1. [Stack Overflow](https://stackoverflow.com) and other QA-sites and forums where people help each other for free.
2. [Uber](https://uber.com) and similar services matching people with need and people who can help them.
3. [Tinder](https://tinder.com) and other applications with Tinder-like interface making surfing across large databases easy and fun using modern recommendation algorithms.
4. [Telegram](https://telegram.org) as an easy to create user interface for database applications.

Inspired by that services, I thought that desire to make good things can be moved from online QA-sites and forums to real-life.

On the platform you can publish your wishes of any kind, from asking to walk with your dog to buying you a new car. 
Keep in mind though, that simple wishes are more likely to be fulfilled.

Now telegram bot is available in two languages: Russian and English.

## Using Expecto Patronum application

To start using Expecto Patronum application you should send `/start` command to [Expecto Patronum bot](https://t.me/ExpectoPatronumByYID_bot).

### Guide
1. Read and accept terms of use.
2. After accepting you will be redirected to menu. There you will see summary of your account: 
   - number of created, active and finished wishes;
   - wish you are currently fulfilling for other user;
   - your score. You can increase your score by fulfilling wishes of other users. Wishes of users with higher score will be suggested to other users in priority.
3. From menu, you can choose following activities:
   1. Create new wish. Each wish created reduces your reputation balance.
   2. Do good by fulfilling wishes of other users. Each fulfilled wish increases your reputation balance.
   3. Cancel wish you are currently fulfilling. Note, that you can fulfill only one wish at a time, so options "Do good" and "Cancel fulfillment" are mutually exclusive.
   4. Manage your wishes.
   5. Refresh menu. It may be required to update your statistics and menu options after wish you are currently fulfilling were marked as completed by its author.
4. When pressing "Do good" after specifying search criteria (local wishes or worldwide wishes), you will be redirected to search results.
5. Search results suggested to you one by one, so your can skip wishes you do not want to fulfill and accept wish you want.
6. After accepting the wish, you will be invited to chat with author of the wish, where you can discuss details. 
7. Besides wish author, there will be special moderator-bot in that chat, waiting for special commands: 
   - `/finish` (can be done only by wish author) - send it, when your wish is completed.
   - `/cancel` (can be done by both participants) - send it to refuse fulfillment of the wish (wish will be returned to the database).
   - `/report` (can be done by both participants) - send it to report inappropriate behavior of the other user. Wish will be cancelled and you both will be added to the stop list of each other.

## Setting up your own Expecto-Patronum-like bot
1. Set up your own MongoDB database (free version would be enough):
   1. Create an account on https://www.mongodb.com
   2. Create a project
   3. Create a cluster (Shared, M0 Sandbox would be enough)
   4. Create the database "telegram" (Browse Collections -> + Create Database) with 4 collections: users, wishes, rooms, reports. One of these collections should be created together with database creation, 3 other can be added right after by pressing "+" near your database name.
   5. Set up security options for your database ("Database Access" and "Network Access" tabs). You should add your user and IP address there.
   6. After cloning code of that repository, set up connection to MongoDB serve:
      1. Copy your MongoDB connection string from "Database -> Connect -> Connect your application -> Java 4.3 or later" in format "mongodb+srv://<user>:<password>@host" format (without suffixes after host)
      2. Create environment variable "MONGODB_CONNECTION" with that string (do not forget to replace <password> with your password)
2. Register your telegram main bot.
   1. Create a bot using https://t.me/BotFather
   2. Create environment variable "TELEGRAM_EP_MAIN_BOT_TOKEN" with that token
3. Register your telegram moderator bot
   1. Create a bot using https://t.me/BotFather
   2. Create environment variable "TELEGRAM_EP_OBSERVER_BOT_TOKEN" with that token
   3. Run Moderator bot (run_moderator_bot.kt) and copy Telegram bot id from terminal to environment variable "TELEGRAM_EP_USER_ID"
4. Creating group chats and inviting author and patron there is functionality not available for Telegram bots. 
    To do that we use special Telegram account (not bot) that on request create such chats (using TDLib), 
    invites required users there (including moderator bot) and leaves that chat. 
    Note, that if users will report that account, some of its functionality will be restricted, 
    so probably you want to use separate account for that.
   1. Login on https://my.telegram.org/auth
   2. Go to "API Development tools" and create new application there.
   3. Create environment variables "TELEGRAM_API_ID" and "TELEGRAM_API_HASH" values created with application.
   4. Expecto Patronum uses [TDLight-Java](https://github.com/tdlight-team/tdlight-java) to run that Telegram-Hotel. 
   Since I run Expecto Patronum on the macOS M1, there were problems with running TDLight-Java, so I had to compile it myself. 
   Build steps described [here](https://github.com/dimitree54/tdlight-java). 
   For other platforms you can use TDLight-Java directly.
   To do that modify build.gradle.kts file by setting correct tdlight version.

## Contribution

Here is contribution options you can use:
1. Good deeds contribution:
   1. You can simply do good using Expecto Patronum application.
2. Code contribution:
   1. Choose un-assigned issue you want to work on
   2. Write a comment to it
   3. The issue will be assigned to you. If some extra information required, it will be provided in issue comments.
   4. Fork repository
   5. Solve issue
   6. Create pull request
3. Translation contribution: 
   1. If you want to make ExpectoPatronum on your language, create issue "Translate ExpectoPatronum to ..." and write a comment to it.
   2. That issue will be assigned to you.
   3. Then follow instructions for code contribution.
   4. If you are not very familiar with open-source contribution pipeline, do not worry, you can write me in Telegram and I will help you with that.
4. Attention contribution:
   1. You can share information about Expecto Patronum in your social media to let more people know about it.
5. Financial contribution:
   1. For now, it is not supported. But if you interested in donating some money to encourage me to spend more time on Expecto Patronum project, you can write me to Telegram. If I receive several requests for that, I will set up convenient method for that.

## Contacts

Author's telegram: [@dimitree54](https://t.me/dimitree54)

![](readme_images/t_me-dimitree54.jpg)