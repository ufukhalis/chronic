# Chronic
Chronic is a basic task scheduler application for your cron tasks.

## Getting Started
To use `Chronic`, firstly you need to run this application in your local, remote or VM machine.

After that you can use its API to create or delete for some scheduled tasks.

### API Usage

You can create a task like below example.

    POST http://{YOUR_HOST_IP}:8080/task/
    {
    	"name": "task-name", # Name of the task
    	"command" : "java -version", # Command which you would like to run
    	"startDate": "2019-08-23 15:28:00", # Start date of your task
    	"period" : 1 # Period of your task
    }
    
##### Some Notes
Period accepts seconds.

You can run multiple command with using `;` character.


You can delete your task like below example.

    DELETE http://{YOUR_HOST_IP}:8080/task/{YOUR_TASK_NAME}
    
You can request your task logs like below example.

    GET http://{YOUR_HOST_IP}:8080/log/{YOUR_TASK_NAME}

Note
---

This project is still under development.

For more information about vavr.io, check the site http://vavr-io.github.io

License
---
All code in this repository is licensed under the Apache License, Version 2.0. See [LICENCE](./LICENSE).