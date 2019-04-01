# FixChat [![Project Status: Inactive – The project has reached a stable, usable state but is no longer being actively developed; support/maintenance will be provided as time allows.](https://www.repostatus.org/badges/latest/inactive.svg)](https://www.repostatus.org/#inactive) [![Build Status](https://travis-ci.com/KovuTheHusky/FixChat.svg?branch=master)](https://travis-ci.com/KovuTheHusky/FixChat)

Simple plugin for Bukkit that makes chatting with your friends in game, and on Dynmap much easier.

## Features

* Adds **aliases for the tell command** so players can use /tell, /t, /whisper, and /w to send private messages to others.
* Adds **reply functionality** so players can easily reply using the added /reply and /r commands.
* Adds **away from keyboard** detection and announcements so players know when others are idle or away (unless they are vanished).
* Adds **Dynmap integration** to show achievement unlocks, player deaths, /me messages, and /say messages.

## Commands

This plugin adds aliases for the tell command so you can use any of the following to send a whisper:

* `/tell <player> <message>`
* `/t <player> <message>`
* `/whisper <player> <message>`
* `/w <player> <message>`

You can also easily reply to the last player you received a whisper from by using the reply command:

* `/reply <message>`
* `/r <message>`

Operators or users with the appropriate permission can set the message of the day:

* `/motd <message> OR /motd clear`

## Permissions

**fixchat.motd**

    Allows the user to set the message of the day.

## Configuration

**afk**

    If true, keeps track of whether players are idle and sets them to away after five minutes.
     
**motd**

    Saves the message of the day so it is not lost between server restarts.

## Links

* Website: <https://kovuthehusky.com/projects#fixchat>
* Issues: <https://github.com/KovuTheHusky/FixChat/issues>
* Source: <https://github.com/KovuTheHusky/FixChat>
* Builds: <https://travis-ci.org/KovuTheHusky/FixChat>
* Bukkit: <https://dev.bukkit.org/projects/fixchat>
* Spigot: <https://www.spigotmc.org/resources/fixchat.39662>
* Metrics: <https://bstats.org/plugin/bukkit/FixChat>
