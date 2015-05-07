# OptimalSpyglass v1.0#
Copyright 2015 OptimalBI - Licensed under the Apache License, Version 2.0

## The story: ##
At OptimalBI we use many AWS accounts, and from time to time more than one region. This caused an issue when we didn't realise we had AWS resources running that didn't need to be! We looked around and didn't find a free, easy-to-use solution to give us cross-account, cross-region visibility of what's running and an estimated cost per hour. So we built one, and OptimalSpyglass was born!

## What it does: ##
This app is designed to help see what you have running in the AWS cloud, across regions and accounts.
It should also help showing how much this is currently costing you.

## Requirements (All users): ##
* Java 8 Update 25 or later (https://www.java.com/verify/)
* Read-only AWS Access and Secret Key for each account you wish to monitor
* Write permissions to the install directory

### Optional requirements (Advanced users): ###
* Local install of Maven to compile the application from source (http://maven.apache.org/)
* Git installed to copy the repository    

## Java install instructions (Windows/OSX, all users): ##
* Uninstall all versions of Java
* Download Java 8 from https://java.com/en/download/
* Follow the on-screen instructions
* Reboot the computer to make sure the PATH is correct

## Download instructions (Pre-built app users): ##
Download the latest pre-build release from https://github.com/OptimalBI/optimal-spyglass-open-source/releases

## Download and build instructions (Advanced users): ##
* Check that your maven install works correctly (http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
* Create a local copy of the repository using git clone https://github.com/OptimalBI/optimal-spyglass-open-source.git
* Navigate to the directory in a terminal window
* Run "mvn package" to create the .jar in the /target directory
* Copy the OptimalSpyglass-x.x.x.-with-dependencies.jar to your desired run directory.
* Copy the pricing directory to that same directory

## Run instructions (All users): ##
* Run "OptimalSpyglass-x.x.x.jar"
* Input the password you wish to use to access the program. This will be used as the key to encrypt your AWS access keys for this application. Please use best password practices!
* Follow the on-screen prompts to add your AWS Access Keys as required.
* Once the app is running select the desired regions with the button on the left.
* If any bugs are found please create a bug report in the section on GitHub!

## Security: ##
* Security of the credentials file is the responsibility of the user
* The app will automatically encrypt the secret key before it stores it
* The app will then require a password to login each use
* The access key to AWS needs only to have read-only permissions to all services

## Current functionality: ##
* Multiple accounts
* Multiple regions
* List all Ec2 and running Redshift,RDS,DynamoDB,Glacier and S3 from those regions
* Simple summary page of all AWS services
* Securing key is currently the responsibility of the user
* Pricing based on supplied .csv files (See root directory)
    * The app will read the .csv file (Pricing.csv) and match the "Service.Size" to AWS Service types.
    * If it has one then the pricing from "Cost.Per.Hour" will be used to calculate cost.
    * The app will not take OS or Spot/Reserved instances into consideration.
    * The app currently assumes that everything is IOPS optimised
    * It is the end users responsibility to keep the pricing up to date
* Provide standard encryption to the Secret Keys via login password method

## Current known issues: ##
Please see GitHub issue tracking at https://github.com/OptimalBI/optimal-spyglass-open-source/issues

## Patched issues: ##
Please see the change log below.

## Feedback, suggestions, bugs, contributions: ##
Please submit these to GitHub issue tracking at https://github.com/OptimalBI/optimal-spyglass-open-source/issues or join us in developing OptimalSpyglass by forking the project and then making a pull request!

## Find out more: ##
You can get info and find out more from our blog site http://blog.optimalbi.com/category/optimalspyglass/

## Change log: ##
```
v0.0.1
	* Initial Build.
v0.0.2
	* Restricted Redshift to valid regions.
	* Enhanced region menu functionality.
	* Enabled debug mode for alpha builds.
v0.0.3
	* Fixed region display bug caused by RDS databases.
	* Fixed account display with space error.
	* Created autohide event handler to stop javaFX popups hogging screen space.
v0.0.4
	* Fixed bug with white box appearing on the top section of GUI on resize.
	* Fixed RDS being naughty and eating macca's (drawing too wide) when it could.
	* Fixed redraw bug when booting app and the size changes.
	* Added very simple progress bar.
	* Fixed autohide system.
	* Major code clean up and added some documentation to the main GUI class.
v0.0.5
	* Updated to aws-java-sdk v1.9.13.
	* Removed one layer of abstraction from the AWS services section.
	* Fixed some of the terminology to be more consistent.
	* Changed the credentials file to match the AWS naming conventions for keys.
	* Added proper password authentication to the app startup.
	* Added change password system.
	* Remove unneeded debug messages.
v0.0.6
	* Fixed password GUI bugs.
	* Updated branding.
	* Updated licence.
v0.7
	* Updated the dialog drawing system to help it to draw when needed.
	* Tweaked OSX fonts to make it look more similar.
	* Added a version notification system to help people know about new releases.
v0.8
	* Update AWS library to 1.9.19.
	* Changed font system to use Google Fonts so that the app is consistent across all OS's.
	* Added SimpleLog with its proper path.
	* Added Redshift and RDS to the pricing information.
	* Added labels and fixed GUI bugs.
	* Assorted minor bug fixes.
v1.0
    * Update AWS library to 1.9.33.
    * Reformatted GUI to give the more important stuff more room.
    * Removed some annoying sections of the GUI that were not working as intended and replaced them with a new system
    * Added S3, DynamoDB and Glacier.
    * Added a list view so you can sort on important stuff.
```
