
BASE_DIR="/Users/navid/StudioProjects/NoidAccessibility/TransDroid/apps"
data = {'Geek': ("a3-shopping/a31-Geek Smarter Shopping_v2.3.7_apkpure.com.apk", "com.contextlogic.geek"),
	'Demo': ("demo.apk", "dev.navids.demoapp"),
	'ToDoList': ('todolist.apk', 'com.splendapps.splendo'),
	'Budget': ('budgettracker.apk', 'com.colpit.diamondcoming.isavemoney'),
	'Calorie': ('caloriecounter.apk', 'com.fatsecret.android'),
	'School': ('schoolplanner.apk', 'daldev.android.gradehelper')
	}

import sys
cmd = sys.argv[1]
path = sys.argv[2]
key = path.split('/')[-1].split('_')[0]
result = data.get(key, ('WRONG','WRONG'))
if cmd == 'apk':
    print(BASE_DIR+'/'+result[0])
else:
    print(result[1])


