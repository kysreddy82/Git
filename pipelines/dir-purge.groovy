pipeline {
  agent {
	    docker {
	        image 'imagename'
            registryUrl 'http://registryurl'
            registryCredentialsId 'credentials'
            args '-u 0 --dns 10.0.10.68 --dns 10.0.10.50 --dns 10.0.10.150'
	    }
  }
  parameters {
    string(name: 'INVENTORY', description: 'List of VMs/IPs (comma separated)')
    string(name: 'SVCACCOUNT', description: 'Enter service account name grafana/cresanprd')
    string(name: 'ANSIBLE_USER', description: 'Enter Username for Ansible')
    password(name: 'ANSIBLE_PASSWORD', description: 'Enter password for Ansible user account')
    string(name: 'PORT', defaultValue: '22', description: 'SSH port number')
    string(name: 'SERVICE', defaultValue: 'my_service', description: 'Name of the service to start/stop/status')
  }
  environment {
        ANSIBLE_HOST_KEY_CHECKING = 'false'
  }
  stages {
    stage('Git Checkout') {
      steps {
          checkout scm: [
                  $class: 'GitSCM',
                  branches: [[name: '*/main']],
                  userRemoteConfigs: [[
                      credentialsId: 'github-checkout-pat',
                      url: 'https://github.com/GIT-Global/cre-techops-pensieve.git'
                  ]]
          ]
      }
    }
    stage('RunPlayBook') {
            steps {
                sh """
                    ansible-playbook \
                        -e 'ANSIBLE_PORT=${params.PORT} SERVICE=${params.SERVICE} SVCACCOUNT=${params.SVCACCOUNT}' \
                        -i '${params.INVENTORY},' \
                        'non-production/pipelines/pensieve-ops/dir-purge.yml'
                """
            }
        }
 
    }
  }
