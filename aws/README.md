Khallware on AWS
=================
Provision AWS CloudFormation Stack
---------------
### Utilizing http://aws.amazon.com/ Website
```shell
chromium-browser https://github.com/harkwell/khallware/blob/github/aws/AWS-cloudformation.json
# right click on "Raw", "Save link as..."
chromium-browser http://aws.amazon.com/  # login to console
# box -> CloudFormation -> "Create New Stack" -> "Choose..." -> "Upload..." -> Choose Filename (from save-as above)
```

Jenkins
---------------
### Access
```shell
chromium-browser http://aws.amazon.com/  # login to console
# box -> EC2 -> "Running Instances"
chromium-browser http://<ec2-instance>.compute.amazonaws.com/jenkins/
```

### Credentials
username=jenkins password=khallware

### Install Git Plugin
```
Jenkins -> Manage Jenkins -> Manage Plugins -> Available -> "Git plugin"
```

### Build/Deploy
```
Jenkins -> "khallware" -> Build Now
# use AWS-SES smtp credentials with a validated from and to email
```

### Devlopment Instance
```shell
chromium-browser http://<ec2-instance>.compute.amazonaws.com/khallware-dev/
echo guest/guest
```

Flyspray
---------------
```shell
chromium-browser http://<ec2-instance>.compute.amazonaws.com/flyspray/
# click through to install
```

Fitnesse
---------------
### Access
```shell
chromium-browser http://<ec2-instance>.compute.amazonaws.com/IntegrationTests
```


Miscellaneous
---------------
### Startup/Shutdown
```shell
ssh -i *.pem ec2-user@10.0.24.100
sudo bash -o vi
service jenkins start
service jenkins stop
tail -f /var/log/jenkins/jenkins.log
```

Tear Down AWS CloudFormation Stack
---------------
aws cloudformation delete-stack --stack-name khallware
