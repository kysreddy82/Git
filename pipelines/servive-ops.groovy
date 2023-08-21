pipeline {
  agent {
	    docker {
	        
            args '-u 0 --dns 10.0.10.68 --dns 10.0.10.50 --dns 10.0.10.150'
	    }
  }
  parameters {
    choice(name: 'APP', choices: ['CRE', 'IIB'], description: 'APP Name - Mandatory for ConfigUpdate action')
    string(name: 'INVENTORY', description: 'List of VMs/IPs (comma separated)')
    string(name: 'SVCACCOUNT', description: 'Enter service account name grafana/cresanprd')
    string(name: 'ANSIBLE_USER', description: 'Enter Username for Ansible')
    password(name: 'ANSIBLE_PASSWORD', description: 'Enter password for Ansible user account')
    string(name: 'PORT', defaultValue: '22', description: 'SSH port number')
    string(name: 'SERVICE', defaultValue: 'my_service', description: 'Name of the service to start/stop/status')
    choice(name: 'ACTION', description: 'Action to perform', choices: ['restart', 'stop', 'status', 'list', 'logrotate','ConfigUpdate', 'ServiceUpdate' ])
  }
  environment {
        ANSIBLE_HOST_KEY_CHECKING = 'false'
  }
  stages {
    stage('Git Checkout') {
      steps {
          checkout scm: [
                  $class: 'GitSCM',
                  branches: [[name: '*/-configs']],
                  userRemoteConfigs: [[
                      credentialsId: 'github-checkout-pat',
                      url: 'https://githubt'
                  ]]
          ]
      }
    }
    stage('RunPlayBook') {
            steps {
                sh """
                    ansible-playbook \
                        -e 'ANSIBLE_PORT=${params.PORT} app=${params.APP} SERVICE=${params.SERVICE} ANSIBLE_SVCACCOUNT=${params.SVCACCOUNT}' \
                        -i '${params.INVENTORY},' \
                        '/pipelines/t-${params.ACTION}.yml'
                """
            }
        }
 
    }
  }
