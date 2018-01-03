# Anthony
patron saint of lost machines

anthony is a daemon that keeps track of a machine's public IP,
updates some DNS service when the machine's IP changes and can
send out notifications when connectivity/address changes occur.

currently supports only [GoDaddy](https://www.godaddy.com/) 
as a DNS service and [Mailgun](https://www.mailgun.com/) for
sending out notifications.

## usage
* build or download `anthony-<latest>-fat.jar`
* get yourself a [GoDaddy API key](https://developer.godaddy.com/getstarted).
* (optional) Get yourself a [Mailgun API key](https://documentation.mailgun.com/en/latest/quickstart.html).
  you will also need to configure your domain to use mailgun (see link above).
* create a config file, called `anthony.properties` as follows:
```properties
domain=what.ever
pollIntervalMs=600000

godaddyKey=foo
godaddySecret=bar

#these are optional
notificationDomain=notifications.what.ever
notificationFrom=Tony
notificationTo=someone@some.where
mailgunApiKey=baz
```
* `java -jar anthony-<latest>-fat.jar` 
* when run, Anthony 1st looks for `anthony.properties` in the CWD, and (if not found)
alongside its own jar file. it also creates log files under `CWD/logs`
* add salt to taste

## license
GPLv3