#!/usr/bin/env bash

#if [! -f /vagrant/data/install.txt ];
#then

echo "# Updating package repository"
sudo apt-get -y update

echo "# Installing gnome"
sudo apt-get -y install gdm

echo "# Configuring gnome"
sudo dpkg-reconfigure gdm

echo "# Installing python-wxgtk2.8"
sudo apt-get -y install python-wxgtk2.8

echo "# Installing python-pip"
sudo apt-get -y install python-pip

echo "# Installing robotframework"
sudo pip install robotframework

echo "# Installing robotframework-selenium2library"
sudo pip install robotframework-selenium2library

echo "# Installing robotframework-ride"
sudo pip install robotframework-ride

echo "# Installing terminal"
sudo apt-get -y install gnome-terminal

echo "# Installing firefox"
sudo apt-get -y install firefox

echo "# Starting gnome"
sudo service gdm start

echo "# Finished setting up RIDE virtual machine"

#fi

