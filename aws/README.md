Khallware on AWS
=================
Jenkins
---------------
### Access
```shell
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
```

### Devlopment Instance
```shell
chromium-browser http://<ec2-instance>.compute.amazonaws.com/khallware-dev/
echo guest/guest
```

### Startup/Shutdown
```shell
ssh -i *.pem ec2-user@10.0.24.100
sudo bash -o vi
service jenkins start
service jenkins stop
tail -f /var/log/jenkins/jenkins.log
```
