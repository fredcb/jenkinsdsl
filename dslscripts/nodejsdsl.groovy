job('nodejs-dsl-checkout') {
    label ('appServer')
    scm {
        github('tetradev01/nodejsapp', 'master')
    }
  
   publishers {
        downstream 'nodejs-dsl-install', 'SUCCESS'
    }
}

job('nodejs-dsl-install') {
	label ('appServer')
	customWorkspace('/jenkins/workspace/nodejs-dsl-checkout')

	steps{
		shell 'uname -a'
		shell 'id -a'
		shell 'npm install'
	}

	publishers{
		downstream 'nodejs-dsl-test', 'SUCCESS'
	}
}

job('nodejs-dsl-test'){
	label ('appServer')
	customWorkspace('/jenkins/workspace/nodejs-dsl-checkout')

	steps{
		shell 'npm start &'
		shell 'sleep 10'
		shell 'curl -s http://127.0.0.1:1337/| grep 'Welcome to TetraNoodle''
		shell 'pkill npm'
		shell 'true'
	}

	publishers{
		downstream 'nodejs-dsl-archive', 'SUCCESS'
	}
}


job('nodejs-dsl-archive'){
	label ('appServer')
	configure { project ->
        project / buildWrappers / 'org.jvnet.hudson.plugins.SSHBuildWrapper' {
            siteName 'release@192.168.1.62:22'
            postScript """        
            	tar -zcvf /var/archive/app.tar.gz /var/myapp/           
	      """
            }
	}

	publishers{
		downstream 'nodejs-dsl-deploy', 'SUCCESS'
	}
}

job('nodejs-dsl-deploy'){
	label ('appServer')
	configure { project ->
        project / buildWrappers / 'org.jvnet.hudson.plugins.SSHBuildWrapper' {
            siteName 'release@192.168.1.62:22'
            postScript """        
            	cd /var/myapp
            	git pull origin master
				"""
            }
	}
    }
