#!/usr/bin/python

import MySQLdb
import os
config = ''

try:
    os.environ['bamboo_IFS_MYSQL_USER_NAME']
    config = {
        'user': os.environ['bamboo_IFS_MYSQL_USER_NAME'],
        'passwd': os.environ['bamboo_IFS_MYSQL_PASSWORD'],
        'host': os.environ['bamboo_IFS_MYSQL_HOSTNAME'],
        'db': os.environ['bamboo_IFS_MYSQL_DB_NAME'],
        'port': 3306,
    }
except KeyError:
    print("Run Mysql Locally")
    config = {
        'user': 'root',
        'passwd': 'password',
        'host': 'ifs-database',
        'db': 'ifs',
        'port': 3306,
    }
print config

_open_competition_application_name =  'A novel solution to an old problem'
_open_competition_application_2_name = 'Providing sustainable childcare'
_open_competition_application_3_title = 'Mobile Phone Data for Logistics Analytics'
_open_competition_application_4_name =  'Shared infrastructures for economical production lines'
_open_competition_application_5_name =  'A new innovative solution'
_funders_panel_application_1_title =  'Sensing & Control network using the lighting infrastructure'
_funders_panel_application_2_title =    'Matter - Planning for Web'
_in_assessment_application_1_title =    '3D-printed buildings'
_in_assessment_application_3_title =    'Intelligent Building'
_in_assessment_application_4_title =    'Park living'
_in_assessment_application_5_title =    'Products and Services Personalised'

_application_list = { _open_competition_application_name, _open_competition_application_2_name, _open_competition_application_3_title,
                      _open_competition_application_4_name, _open_competition_application_5_name, _funders_panel_application_1_title,
                      _funders_panel_application_2_title, _in_assessment_application_1_title, _in_assessment_application_3_title,
                      _in_assessment_application_4_title, _in_assessment_application_5_title}
_formatted_app_list = ','.join(['%s'] * len(_application_list))

# Open database connection
db = MySQLdb.connect(**config)

# prepare a cursor object using cursor() method
cursor = db.cursor()

# execute SQL query using execute() method, to fetch the Competitions
cursor.execute("SELECT `id`,`name` FROM competition")

# Fetch all competition records
competition_ids = {}
for comp in cursor.fetchall():
    competition_ids[comp[1]] = int(comp[0])

# execute SQL query using execute() method, to fetch the Applications
cursor.execute("SELECT `id`,`name` FROM application WHERE name IN (%s)" % _formatted_app_list,
               tuple(_application_list))

# Fetch only required application records
application_ids = {}
for app in cursor.fetchall():
    application_ids[app[1]] = int(app[0])
    print app[1], app[0]

# disconnect from server
db.close()